package com.pixelrakete.lovecal.data.manager

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pixelrakete.lovecal.data.model.AuthState
import com.pixelrakete.lovecal.data.model.Couple
import com.pixelrakete.lovecal.data.model.CoupleSettings
import com.pixelrakete.lovecal.data.model.UserData
import com.pixelrakete.lovecal.util.ValidationUtils
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

@Singleton
class UserManager @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val coupleRepository: CoupleRepository
) {
    private val TAG = "UserManager"
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    private val _userData = MutableStateFlow<UserData?>(null)
    private val _hasCalendarPermission = MutableStateFlow(false)
    private var cachedCoupleId: String? = null
    
    private val _coupleSettings = MutableStateFlow<CoupleSettings?>(null)
    val coupleSettings: StateFlow<CoupleSettings?> = _coupleSettings.asStateFlow()
    
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val userData: StateFlow<UserData?> = _userData.asStateFlow()
    val hasCalendarPermission: StateFlow<Boolean> = _hasCalendarPermission.asStateFlow()
    
    private val usersCollection = firestore.collection("users")
    private val couplesCollection = firestore.collection("couples")
    
    private var currentCoupleListener: ListenerRegistration? = null
    
    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            Log.d(TAG, "Firebase auth state changed. User: ${currentUser?.uid}")
            
            if (currentUser == null) {
                _userData.value = null
                _authState.value = AuthState.LoggedOut
                cachedCoupleId = null
            } else {
                // Load user data when auth state changes
                loadUserData(currentUser.uid)
            }
        }
    }
    
    private fun loadUserData(userId: String) {
        Log.d(TAG, "Starting to load user data for userId: $userId")
        usersCollection.document(userId)
            .addSnapshotListener { userDoc, error ->
                if (error != null) {
                    Log.e(TAG, "Error loading user data", error)
                    _authState.value = AuthState.Error(error.message ?: "Failed to load user data")
                    return@addSnapshotListener
                }

                if (userDoc == null) {
                    Log.e(TAG, "User document is null")
                    _authState.value = AuthState.Error("User document not found")
                    return@addSnapshotListener
                }

                try {
                    Log.d(TAG, "User document exists: ${userDoc.exists()}")
                    if (userDoc.exists()) {
                        val email = userDoc.getString("email")
                        val name = userDoc.getString("name")
                        val newCoupleId = userDoc.getString("coupleId")
                        val isPartner1 = userDoc.getBoolean("isPartner1") ?: true

                        // Only update couple ID if:
                        // 1. We don't have a cached ID, or
                        // 2. The new ID is different and not null (changing couples)
                        if (cachedCoupleId == null || (newCoupleId != null && newCoupleId != cachedCoupleId)) {
                            Log.d(TAG, "Couple ID changed from $cachedCoupleId to $newCoupleId")
                            
                            // Clear old listener and settings
                            currentCoupleListener?.remove()
                            currentCoupleListener = null
                            _coupleSettings.value = null
                            
                            // Update cached ID
                            cachedCoupleId = newCoupleId
                            
                            // Setup new listener if we have a couple
                            if (newCoupleId != null) {
                                setupCoupleListener(newCoupleId)
                            }
                        } else {
                            Log.d(TAG, "Keeping existing couple ID: $cachedCoupleId")
                        }

                        Log.d(TAG, "Loaded user data - email: $email, name: $name, coupleId: $newCoupleId, isPartner1: $isPartner1")

                        _userData.value = UserData(
                            id = userId,
                            email = email,
                            displayName = name,
                            coupleId = newCoupleId,
                            isPartner1 = isPartner1
                        )

                        when {
                            newCoupleId == null -> {
                                Log.d(TAG, "User has no couple ID, setting state to NeedsCoupleDecision")
                                _authState.value = AuthState.NeedsCoupleDecision
                            }
                            !_hasCalendarPermission.value -> {
                                Log.d(TAG, "User needs calendar permission")
                                _authState.value = AuthState.NeedsCalendar
                            }
                            else -> {
                                Log.d(TAG, "User fully authenticated")
                                _authState.value = AuthState.Authenticated
                            }
                        }
                    } else {
                        Log.d(TAG, "Creating new user document")
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            val userData = hashMapOf(
                                "email" to currentUser.email,
                                "name" to currentUser.displayName,
                                "createdAt" to Date(),
                                "updatedAt" to Date()
                            )
                            usersCollection.document(userId).set(userData)
                            
                            _userData.value = UserData(
                                id = userId,
                                email = currentUser.email,
                                displayName = currentUser.displayName,
                                coupleId = null,
                                isPartner1 = true
                            )
                            
                            _authState.value = AuthState.NeedsCoupleDecision
                        } else {
                            Log.e(TAG, "Current user is null while trying to create new user document")
                            _authState.value = AuthState.Error("Failed to create user document")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing user data", e)
                    _authState.value = AuthState.Error("Error processing user data: ${e.message}")
                }
            }
    }
    
    private fun setupCoupleListener(coupleId: String) {
        Log.d(TAG, "Setting up couple listener for coupleId: $coupleId")
        
        // Remove any existing listener
        currentCoupleListener?.remove()
        
        // Clear existing settings
        _coupleSettings.value = null
        
        // Set up new listener
        currentCoupleListener = firestore.collection("couples").document(coupleId)
            .addSnapshotListener { coupleDoc, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to couple document", error)
                    return@addSnapshotListener
                }

                if (coupleDoc == null || !coupleDoc.exists()) {
                    Log.e(TAG, "Couple document is null or doesn't exist")
                    // Clear cached ID since couple doesn't exist
                    cachedCoupleId = null
                    _coupleSettings.value = null
                    return@addSnapshotListener
                }

                try {
                    val isPartner1 = _userData.value?.isPartner1 ?: true
                    
                    // Get partner names and colors
                    val partner1Name = coupleDoc.getString("partner1Name") ?: ""
                    val partner2Name = coupleDoc.getString("partner2Name") ?: ""
                    val partner1Color = coupleDoc.getString("partner1Color")?.takeIf { it.startsWith("#") } ?: "#2196F3"
                    val partner2Color = coupleDoc.getString("partner2Color")?.takeIf { it.startsWith("#") } ?: "#F44336"
                    
                    // Get other settings
                    val monthlyBudget = coupleDoc.getDouble("monthlyBudget") ?: 200.0
                    val dateFrequencyWeeks = coupleDoc.getLong("dateFrequencyWeeks")?.toInt() ?: 2
                    val city = coupleDoc.getString("city") ?: ""
                    
                    // Convert interests list safely
                    val interests = try {
                        (coupleDoc.get("interests") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting interests", e)
                        emptyList()
                    }

                    // Create settings based on partner status
                    val settings = if (isPartner1) {
                        CoupleSettings(
                            partner1Name = partner1Name,
                            partner2Name = partner2Name,
                            partner1Color = partner1Color,
                            partner2Color = partner2Color,
                            monthlyBudget = monthlyBudget,
                            dateFrequencyWeeks = dateFrequencyWeeks,
                            interests = interests,
                            city = city
                        )
                    } else {
                        CoupleSettings(
                            partner1Name = partner2Name,
                            partner2Name = partner1Name,
                            partner1Color = partner2Color,
                            partner2Color = partner1Color,
                            monthlyBudget = monthlyBudget,
                            dateFrequencyWeeks = dateFrequencyWeeks,
                            interests = interests,
                            city = city
                        )
                    }

                    Log.d(TAG, "Updated couple settings: $settings")
                    _coupleSettings.value = settings

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing couple document", e)
                }
            }
    }
    
    suspend fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    suspend fun signInWithGoogle(account: GoogleSignInAccount) {
        try {
            Log.d(TAG, "Starting Google sign in process")
            _authState.value = AuthState.Loading
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d(TAG, "Created Firebase credential from Google ID token")
            
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: throw IllegalStateException("Authentication failed")
            Log.d(TAG, "Firebase auth successful. User ID: ${user.uid}")
            
            // Check if user document exists
            val userDoc = usersCollection.document(user.uid).get().await()
            
            if (!userDoc.exists()) {
                Log.d(TAG, "Creating new user document")
                // Create new user document
                val userData = hashMapOf(
                    "email" to user.email,
                    "name" to account.displayName,
                    "createdAt" to Date(),
                    "updatedAt" to Date()
                )
                usersCollection.document(user.uid).set(userData).await()
                Log.d(TAG, "User document created successfully")
                
                _userData.value = UserData(
                    id = user.uid,
                    email = user.email,
                    displayName = account.displayName,
                    coupleId = null,
                    isPartner1 = true
                )
                
                _authState.value = AuthState.NeedsCoupleDecision
            } else {
                Log.d(TAG, "User document exists, retrieving data")
                // Update user data from existing document
                _userData.value = UserData(
                    id = user.uid,
                    email = user.email,
                    displayName = userDoc.getString("name"),
                    coupleId = userDoc.getString("coupleId"),
                    isPartner1 = userDoc.getBoolean("isPartner1") ?: true
                )
                
                if (_userData.value?.coupleId == null) {
                    Log.d(TAG, "User has no couple, setting state to NeedsCoupleDecision")
                    _authState.value = AuthState.NeedsCoupleDecision
                } else if (!_hasCalendarPermission.value) {
                    Log.d(TAG, "User needs calendar permission")
                    _authState.value = AuthState.NeedsCalendar
                } else {
                    Log.d(TAG, "User fully authenticated")
                    _authState.value = AuthState.Authenticated
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error signing in with Google", e)
            _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            throw e
        }
    }
    
    suspend fun createNewCouple(
        name: String,
        color: String,
        interests: List<String> = emptyList(),
        city: String = "",
        monthlyBudget: Double = 500.0,
        dateFrequencyWeeks: Int = 2
    ) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        // Check if user already has a couple
        val userDoc = usersCollection.document(userId).get().await()
        val existingCoupleId = userDoc.getString("coupleId")
        if (existingCoupleId != null) {
            Log.d(TAG, "User already has a couple ID: $existingCoupleId")
            cachedCoupleId = existingCoupleId
            
            // Load existing couple data
            val coupleDoc = couplesCollection.document(existingCoupleId).get().await()
            if (coupleDoc.exists()) {
                Log.d(TAG, "Found existing couple, not creating a new one")
                if (!hasCalendarPermission.value) {
                    _authState.value = AuthState.NeedsCalendar
                } else {
                    _authState.value = AuthState.Authenticated
                }
                return
            } else {
                Log.w(TAG, "Existing couple document not found, will create new one")
            }
        }
        
        // Generate a unique invitation code
        var invitationCode: String
        var isCodeUnique = false
        do {
            invitationCode = ValidationUtils.generateInvitationCode()
            val existingCouple = couplesCollection
                .whereEqualTo("invitationCode", invitationCode)
                .get()
                .await()
                .documents
                .firstOrNull()
            isCodeUnique = existingCouple == null
        } while (!isCodeUnique)
        
        // Create a new couple document
        val couple = Couple(
            partner1Id = userId,
            partner1Name = name,
            partner1Color = color,
            partner1Interests = interests,
            partner2Name = "",
            partner2Color = "#2196F3",
            partner2Interests = emptyList(),
            monthlyBudget = monthlyBudget,
            dateFrequencyWeeks = dateFrequencyWeeks,
            createdAt = Date(),
            updatedAt = Date(),
            invitationCode = invitationCode,
            city = city
        )
        
        val coupleRef = couplesCollection.add(couple).await()
        
        // Cache the couple ID immediately
        cachedCoupleId = coupleRef.id
        Log.d(TAG, "Created new couple with ID: ${coupleRef.id}")
        
        // Update user document with couple ID
        usersCollection.document(userId)
            .update(mapOf(
                "coupleId" to coupleRef.id,
                "isPartner1" to true,
                "updatedAt" to Date()
            ))
            .await()
            
        // Update local state
        _userData.value = _userData.value?.copy(
            coupleId = coupleRef.id,
            isPartner1 = true
        )
        
        if (!hasCalendarPermission.value) {
            _authState.value = AuthState.NeedsCalendar
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    
    suspend fun getCoupleSettings(): CoupleSettings {
        // Return cached settings if available
        _coupleSettings.value?.let { return it }

        // Otherwise load settings synchronously
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Getting couple settings for user: $userId")
        
        val coupleId = cachedCoupleId ?: run {
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                Log.e(TAG, "User document not found")
                throw IllegalStateException("User document not found")
            }
            
            userDoc.getString("coupleId")?.also {
                cachedCoupleId = it
            } ?: throw IllegalStateException("User not in couple")
        }
        
        Log.d(TAG, "Using couple ID: $coupleId")
        
        val coupleDoc = couplesCollection.document(coupleId).get().await()
        if (!coupleDoc.exists()) {
            Log.e(TAG, "Couple document not found")
            cachedCoupleId = null
            throw IllegalStateException("Couple document not found")
        }
        
        // Safe conversion function for List<String>
        fun getStringList(field: String): List<String> {
            return (coupleDoc.get(field) as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        }

        val isPartner1 = _userData.value?.isPartner1 ?: true
        Log.d(TAG, "User is partner1: $isPartner1")
        
        val settings = if (isPartner1) {
            CoupleSettings(
                partner1Name = coupleDoc.getString("partner1Name") ?: "",
                partner1Color = coupleDoc.getString("partner1Color")?.takeIf { it.startsWith("#") } ?: "#2196F3",
                partner2Name = coupleDoc.getString("partner2Name") ?: "",
                partner2Color = coupleDoc.getString("partner2Color")?.takeIf { it.startsWith("#") } ?: "#F44336",
                monthlyBudget = coupleDoc.getDouble("monthlyBudget") ?: 500.0,
                dateFrequencyWeeks = coupleDoc.getLong("dateFrequencyWeeks")?.toInt() ?: 2,
                interests = getStringList("interests"),
                city = coupleDoc.getString("city") ?: ""
            )
        } else {
            CoupleSettings(
                partner1Name = coupleDoc.getString("partner2Name") ?: "",
                partner1Color = coupleDoc.getString("partner2Color")?.takeIf { it.startsWith("#") } ?: "#2196F3",
                partner2Name = coupleDoc.getString("partner1Name") ?: "",
                partner2Color = coupleDoc.getString("partner1Color")?.takeIf { it.startsWith("#") } ?: "#F44336",
                monthlyBudget = coupleDoc.getDouble("monthlyBudget") ?: 500.0,
                dateFrequencyWeeks = coupleDoc.getLong("dateFrequencyWeeks")?.toInt() ?: 2,
                interests = getStringList("interests"),
                city = coupleDoc.getString("city") ?: ""
            )
        }
        
        Log.d(TAG, "Successfully loaded couple settings: $settings")
        _coupleSettings.value = settings
        return settings
    }
    
    suspend fun updateCoupleSettings(settings: CoupleSettings) {
        val coupleId = getCurrentCoupleId() ?: throw IllegalStateException("User not in a couple")
        val existingCouple = getCouple(coupleId) ?: throw IllegalStateException("Couple not found")
        val isPartner1 = _userData.value?.isPartner1 ?: true
        
        val updatedCouple = if (isPartner1) {
            existingCouple.copy(
                partner1Name = settings.partner1Name,
                partner1Color = settings.partner1Color,
                partner1Interests = settings.interests,  // Update partner1's interests
                monthlyBudget = settings.monthlyBudget,
                dateFrequencyWeeks = settings.dateFrequencyWeeks,
                city = settings.city
            )
        } else {
            existingCouple.copy(
                partner2Name = settings.partner1Name,
                partner2Color = settings.partner1Color,
                partner2Interests = settings.interests,  // Update partner2's interests
                monthlyBudget = settings.monthlyBudget,
                dateFrequencyWeeks = settings.dateFrequencyWeeks,
                city = settings.city
            )
        }
        
        coupleRepository.updateCouple(updatedCouple)
    }
    
    suspend fun signOut() {
        Log.d(TAG, "Signing out user")
        auth.signOut()
        _userData.value = null
        _authState.value = AuthState.LoggedOut
        cachedCoupleId = null
        _hasCalendarPermission.value = false
        _coupleSettings.value = null
        currentCoupleListener?.remove()
        currentCoupleListener = null
        Log.d(TAG, "User signed out, all state cleared")
    }
    
    suspend fun joinCouple(invitationCode: String) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        
        // Find couple by invitation code
        val coupleDoc = couplesCollection
            .whereEqualTo("invitationCode", invitationCode)
            .get()
            .await()
            .documents
            .firstOrNull() ?: throw IllegalStateException("Invalid invitation code")
            
        // Check if couple already has a partner2
        if (coupleDoc.getString("partner2Id") != null) {
            throw IllegalStateException("This couple has already been joined by another user")
        }
            
        // Cache the couple ID immediately
        cachedCoupleId = coupleDoc.id
        Log.d(TAG, "Joining couple with ID: ${coupleDoc.id}")
            
        try {
            // Update couple document
            couplesCollection.document(coupleDoc.id)
                .update(mapOf(
                    "partner2Id" to userId,
                    "updatedAt" to Date()
                ))
                .await()
                
            // Update user document
            usersCollection.document(userId)
                .update(mapOf(
                    "coupleId" to coupleDoc.id,
                    "isPartner1" to false,
                    "updatedAt" to Date()
                ))
                .await()
                
            // Update local state
            _userData.value = _userData.value?.copy(
                coupleId = coupleDoc.id,
                isPartner1 = false
            )
            
            if (!hasCalendarPermission.value) {
                _authState.value = AuthState.NeedsCalendar
            } else {
                _authState.value = AuthState.Authenticated
            }
        } catch (e: Exception) {
            // Reset cached couple ID if joining fails
            cachedCoupleId = null
            throw e
        }
    }
    
    suspend fun completePartner2Setup(name: String, color: Long) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val userDoc = usersCollection.document(userId).get().await()
        val coupleId = userDoc.getString("coupleId") ?: throw IllegalStateException("User not in couple")
        
        couplesCollection.document(coupleId)
            .update(mapOf(
                "partner2Name" to name,
                "partner2Color" to String.format("#%06X", (0xFFFFFF and color.toInt())),
                "updatedAt" to Date()
            ))
            .await()
            
        if (!hasCalendarPermission.value) {
            _authState.value = AuthState.NeedsCalendar
        } else {
            _authState.value = AuthState.Authenticated
        }
    }
    
    suspend fun getCurrentCoupleId(): String? {
        val userId = auth.currentUser?.uid ?: return null
        val userDoc = usersCollection.document(userId).get().await()
        return userDoc.getString("coupleId")
    }
    
    fun setCalendarPermission(granted: Boolean) {
        _hasCalendarPermission.value = granted
        if (granted && _authState.value == AuthState.NeedsCalendar) {
            _authState.value = AuthState.Authenticated
        } else if (!granted) {
            _authState.value = AuthState.NeedsCalendar
        }
    }
    
    suspend fun getCoupleId(): String? {
        return _userData.value?.coupleId
    }
    
    suspend fun getCouple(coupleId: String): Couple? {
        val coupleDoc = couplesCollection.document(coupleId).get().await()
        return if (coupleDoc.exists()) {
            coupleDoc.toObject(Couple::class.java)?.copy(id = coupleDoc.id)
        } else {
            null
        }
    }
    
    suspend fun updatePartner2Settings(
        name: String,
        color: String,
        interests: List<String>
    ) {
        val coupleId = getCurrentCoupleId() ?: throw IllegalStateException("User not in a couple")
        val existingCouple = getCouple(coupleId) ?: throw IllegalStateException("Couple not found")
        
        // Update only partner2's fields, keeping partner1's fields unchanged
        val updatedCouple = existingCouple.copy(
            partner2Name = name,
            partner2Color = color,
            partner2Interests = interests  // Store partner2's interests directly
        )
        
        coupleRepository.updateCouple(updatedCouple)
    }
    
    suspend fun deleteUserData() {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val userDoc = usersCollection.document(userId).get().await()
        val coupleId = userDoc.getString("coupleId")

        try {
            // Delete couple document if user is the owner (partner1)
            if (coupleId != null && userDoc.getBoolean("isPartner1") == true) {
                couplesCollection.document(coupleId).delete().await()
            }

            // Delete user document
            usersCollection.document(userId).delete().await()

            // Sign out and clear local state
            signOut()

            Log.d(TAG, "Successfully deleted all user data")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user data", e)
            throw e
        }
    }
    
    fun getCoupleFlow(coupleId: String): Flow<Couple?> {
        return callbackFlow {
            val listener = firestore.collection("couples")
                .document(coupleId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    
                    val couple = snapshot?.toObject(Couple::class.java)?.copy(id = snapshot.id)
                    trySend(couple)
                }
                
            awaitClose { listener.remove() }
        }
    }
    
    suspend fun isPartner2(): Boolean {
        return !(_userData.value?.isPartner1 ?: true)
    }
} 