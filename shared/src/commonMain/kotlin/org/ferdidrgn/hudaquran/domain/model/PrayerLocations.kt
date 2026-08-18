package org.ferdidrgn.hudaquran.domain.model

/**
 * [city] is the value actually sent to the AlAdhan API, kept ASCII so the geocoder resolves it
 * reliably; [displayCity] is what the user sees and may use native Turkish characters.
 */
data class PrayerLocation(val city: String, val country: String, val displayCity: String = city) {
    val countryDisplayName: String get() = countryDisplayNames[country] ?: country
    val displayName: String get() = "$displayCity, $countryDisplayName"
}

private fun String.toAsciiCityName(): String = this
    .replace("İ", "I").replace("ı", "i")
    .replace("Ğ", "G").replace("ğ", "g")
    .replace("Ü", "U").replace("ü", "u")
    .replace("Ş", "S").replace("ş", "s")
    .replace("Ö", "O").replace("ö", "o")
    .replace("Ç", "C").replace("ç", "c")

private val countryDisplayNames = mapOf(
    "Turkey" to "Türkiye",
    "Saudi Arabia" to "Suudi Arabistan",
    "Germany" to "Almanya",
    "France" to "Fransa",
    "Uzbekistan" to "Özbekistan",
    "Kyrgyzstan" to "Kırgızistan",
    "Turkmenistan" to "Türkmenistan",
    "United Kingdom" to "Birleşik Krallık",
    "United States" to "ABD",
    "Egypt" to "Mısır",
)

private val turkishProvinces = listOf(
    "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin",
    "Aydın", "Balıkesir", "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa",
    "Çanakkale", "Çankırı", "Çorum", "Denizli", "Diyarbakır", "Edirne", "Elazığ", "Erzincan",
    "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari", "Hatay", "Isparta",
    "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir",
    "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla",
    "Muş", "Nevşehir", "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt",
    "Sinop", "Sivas", "Tekirdağ", "Tokat", "Trabzon", "Tunceli", "Şanlıurfa", "Uşak",
    "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman", "Kırıkkale", "Batman",
    "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye", "Düzce",
)

object PrayerLocations {
    val all: List<PrayerLocation> = buildList {
        turkishProvinces.forEach { add(PrayerLocation(city = it.toAsciiCityName(), country = "Turkey", displayCity = it)) }
        add(PrayerLocation("Mecca", "Saudi Arabia"))
        add(PrayerLocation("Medina", "Saudi Arabia"))
        add(PrayerLocation("Riyadh", "Saudi Arabia"))
        add(PrayerLocation("Berlin", "Germany"))
        add(PrayerLocation("Cologne", "Germany"))
        add(PrayerLocation("Munich", "Germany"))
        add(PrayerLocation("Frankfurt", "Germany"))
        add(PrayerLocation("Paris", "France"))
        add(PrayerLocation("Lyon", "France"))
        add(PrayerLocation("Tashkent", "Uzbekistan"))
        add(PrayerLocation("Samarkand", "Uzbekistan"))
        add(PrayerLocation("Bishkek", "Kyrgyzstan"))
        add(PrayerLocation("Osh", "Kyrgyzstan"))
        add(PrayerLocation("Ashgabat", "Turkmenistan"))
        add(PrayerLocation("London", "United Kingdom"))
        add(PrayerLocation("New York", "United States"))
        add(PrayerLocation("Cairo", "Egypt"))
    }

    val default = PrayerLocation(city = "Istanbul", country = "Turkey", displayCity = "İstanbul")
}
