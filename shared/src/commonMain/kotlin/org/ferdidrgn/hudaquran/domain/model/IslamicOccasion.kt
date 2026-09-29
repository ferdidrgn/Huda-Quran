package org.ferdidrgn.hudaquran.domain.model

/** A recurring occasion fixed to a specific day within the Hijri calendar (not a Gregorian date). */
data class IslamicOccasion(
    val id: String,
    val name: String,
    val hijriMonth: Int,
    val hijriDay: Int,
)

// Turkish-only content, matching TajwidCourse.kt/Dua.kt. Regaib Kandili is deliberately omitted:
// unlike the others, it isn't a fixed Hijri calendar date (it's observed the first Thursday night
// of Recep) and getting that wrong risks showing an incorrect date for a real religious occasion.
val islamicOccasions: List<IslamicOccasion> = listOf(
    IslamicOccasion("hijri_new_year", "Hicri Yılbaşı", hijriMonth = 1, hijriDay = 1),
    IslamicOccasion("ashura", "Aşure Günü", hijriMonth = 1, hijriDay = 10),
    IslamicOccasion("mevlid", "Mevlid Kandili", hijriMonth = 3, hijriDay = 12),
    IslamicOccasion("mirac", "Miraç Kandili", hijriMonth = 7, hijriDay = 27),
    IslamicOccasion("berat", "Berat Kandili", hijriMonth = 8, hijriDay = 15),
    IslamicOccasion("ramazan_start", "Ramazan Başlangıcı", hijriMonth = 9, hijriDay = 1),
    IslamicOccasion("kadir", "Kadir Gecesi", hijriMonth = 9, hijriDay = 27),
    IslamicOccasion("ramazan_bayrami", "Ramazan Bayramı", hijriMonth = 10, hijriDay = 1),
    IslamicOccasion("kurban_bayrami", "Kurban Bayramı", hijriMonth = 12, hijriDay = 10),
)
