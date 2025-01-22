package com.pixelrakete.lovecal.data.services

import com.google.ai.client.generativeai.GenerativeModel
import com.pixelrakete.lovecal.data.repository.CoupleRepository
import com.pixelrakete.lovecal.data.repository.DateWishRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor(
    private val generativeModel: GenerativeModel,
    private val dateWishRepository: DateWishRepository,
    private val coupleRepository: CoupleRepository
) {
    suspend fun generateLoveQuote(): String {
        val prompt = "Generate a romantic love quote that is sweet and meaningful. Keep it short and concise."
        val response = generativeModel.generateContent(prompt)
        return response.text ?: "Love is a beautiful journey shared by two hearts."
    }

    suspend fun generateDateSuggestions(partner1Name: String, partner2Name: String, userId: String): List<String> {
        val wishes = dateWishRepository.getDateWishes(userId)
        val randomWish = wishes.randomOrNull()
        val couple = coupleRepository.getCurrentCouple()
        val city = couple?.city ?: "your city"
        
        val prompt = """
            Generiere genau 5 einzigartige und kreative Date-Ideen für ein Paar in $city.
            ${if (randomWish != null) "Eine der Vorschläge MUSS auf diesem Date-Wunsch basieren: ${randomWish.title} - ${randomWish.description}" else ""}
            
            Anforderungen:
            - Schreibe GENAU 5 Vorschläge
            - Die meisten Vorschläge sollten in $city oder der näheren Umgebung sein
            - Ein Vorschlag kann auch ein Wochenendtrip oder Kurzurlaub sein
            - Jeder Vorschlag MUSS exakt diesem Format folgen:
              TITLE: [kurzer, prägnanter Titel für das Date]
              LOCATION: [konkrete Location für das Date]
              BUDGET: [geschätztes Budget in €]
              DESCRIPTION: [Beschreibung in "Wir"-Form]
            - Die DESCRIPTION soll:
              - "Wir"-Form verwenden (z.B. "Wir werden...", "Lass uns...", "Gemeinsam...")
              - 1-2 Vorschläge sollen leidenschaftlich und intim sein mit klaren romantischen Untertönen
              - Spezifische gemeinsame Aktivitäten enthalten
              - Nur zur Jahreszeit passende Aktivitäten vorschlagen
              - 2-3 Sätze lang sein
              - Romantische und sinnliche Elemente enthalten
              - Kreativ und spezifisch sein, keine generischen Vorschläge
              - Ein Vorschlag darf etwas verrückter sein
              - Nicht zu blumig formuliert sein
            
            Gib GENAU 5 Vorschläge zurück, getrennt durch '|||'.
            Jeder Vorschlag MUSS alle 4 Felder enthalten (TITLE, LOCATION, BUDGET, DESCRIPTION).

            Beispielformat:
            TITLE: Romantischer Kochabend
            LOCATION: Zuhause in der Küche
            BUDGET: 50€
            DESCRIPTION: Lass uns einen romantischen Kochabend veranstalten. Wir werden gemeinsam unsere Lieblingsgerichte zubereiten und dabei Wein genießen. Bei Kerzenschein können wir uns dann gegenseitig mit unseren kulinarischen Kreationen verwöhnen. |||
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt).text?.trim() ?: ""
            val suggestions = response.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
            if (suggestions.size < 5) {
                // If we don't get enough suggestions, add some fallback ones
                suggestions + listOf(
                    """
                    TITLE: Romantisches Candle-Light-Dinner
                    LOCATION: Zuhause
                    BUDGET: 40€
                    DESCRIPTION: Lass uns ein romantisches Candle-Light-Dinner zu Hause machen, bei dem wir gemeinsam kochen und uns gegenseitig verwöhnen.
                    """.trimIndent(),
                    """
                    TITLE: Picknick im Sonnenuntergang
                    LOCATION: Stadtpark
                    BUDGET: 30€
                    DESCRIPTION: Wir machen ein Picknick im Park bei Sonnenuntergang mit unserem Lieblingswein und Käse.
                    """.trimIndent(),
                    """
                    TITLE: Sinnliche Massage
                    LOCATION: Schlafzimmer
                    BUDGET: 25€
                    DESCRIPTION: Heute verwöhnen wir uns gegenseitig mit einer sinnlichen Massage bei Kerzenschein und ätherischen Ölen.
                    """.trimIndent(),
                    """
                    TITLE: Champagner & Schaumbad
                    LOCATION: Badezimmer
                    BUDGET: 45€
                    DESCRIPTION: Lass uns ein entspannendes Bad bei Kerzenschein nehmen und dabei Champagner genießen.
                    """.trimIndent(),
                    """
                    TITLE: Tanzstunde & Dinner
                    LOCATION: Tanzschule & Restaurant
                    BUDGET: 120€
                    DESCRIPTION: Wir nehmen eine private Tanzstunde und lassen den Abend mit einem intimen Dinner ausklingen.
                    """.trimIndent()
                ).take(5 - suggestions.size)
            } else {
                suggestions.take(5)
            }
        } catch (e: Exception) {
            listOf(
                """
                TITLE: Romantisches Candle-Light-Dinner
                LOCATION: Zuhause
                BUDGET: 40€
                DESCRIPTION: Lass uns ein romantisches Candle-Light-Dinner zu Hause machen, bei dem wir gemeinsam kochen und uns gegenseitig verwöhnen.
                """.trimIndent(),
                """
                TITLE: Picknick im Sonnenuntergang
                LOCATION: Stadtpark
                BUDGET: 30€
                DESCRIPTION: Wir machen ein Picknick im Park bei Sonnenuntergang mit unserem Lieblingswein und Käse.
                """.trimIndent(),
                """
                TITLE: Sinnliche Massage
                LOCATION: Schlafzimmer
                BUDGET: 25€
                DESCRIPTION: Heute verwöhnen wir uns gegenseitig mit einer sinnlichen Massage bei Kerzenschein und ätherischen Ölen.
                """.trimIndent(),
                """
                TITLE: Champagner & Schaumbad
                LOCATION: Badezimmer
                BUDGET: 45€
                DESCRIPTION: Lass uns ein entspannendes Bad bei Kerzenschein nehmen und dabei Champagner genießen.
                """.trimIndent(),
                """
                TITLE: Tanzstunde & Dinner
                LOCATION: Tanzschule & Restaurant
                BUDGET: 120€
                DESCRIPTION: Wir nehmen eine private Tanzstunde und lassen den Abend mit einem intimen Dinner ausklingen.
                """.trimIndent()
            )
        }
    }
} 