package org.ferdidrgn.hudaquran.ui.localization

import org.ferdidrgn.hudaquran.data.local.AppLanguage

data class Strings(
    val navHome: String,
    val navSurahs: String,
    val navFavorites: String,
    val navSettings: String,
    val homeGreeting: String,
    val search: String,
    val juz: String,
    val reciters: String,
    val retry: String,
    val appearance: String,
    val recitationAndTranslation: String,
    val prayerNotifications: String,
    val language: String,
)

private val turkish = Strings(
    navHome = "Ana Sayfa", navSurahs = "Sureler", navFavorites = "Favoriler", navSettings = "Ayarlar",
    homeGreeting = "Esselamü Aleyküm", search = "Ara", juz = "Cüzler", reciters = "Hafızlar",
    retry = "Tekrar Dene", appearance = "Görünüm", recitationAndTranslation = "Sesli Okuyuş ve Meal",
    prayerNotifications = "Namaz Vakti Bildirimleri", language = "Dil",
)

private val english = Strings(
    navHome = "Home", navSurahs = "Surahs", navFavorites = "Favorites", navSettings = "Settings",
    homeGreeting = "Peace be upon you", search = "Search", juz = "Juz", reciters = "Reciters",
    retry = "Retry", appearance = "Appearance", recitationAndTranslation = "Recitation & Translation",
    prayerNotifications = "Prayer Notifications", language = "Language",
)

private val arabic = Strings(
    navHome = "الرئيسية", navSurahs = "السور", navFavorites = "المفضلة", navSettings = "الإعدادات",
    homeGreeting = "السلام عليكم", search = "بحث", juz = "الأجزاء", reciters = "القراء",
    retry = "إعادة المحاولة", appearance = "المظهر", recitationAndTranslation = "التلاوة والترجمة",
    prayerNotifications = "إشعارات الصلاة", language = "اللغة",
)

private val german = Strings(
    navHome = "Start", navSurahs = "Suren", navFavorites = "Favoriten", navSettings = "Einstellungen",
    homeGreeting = "Friede sei mit dir", search = "Suche", juz = "Juz", reciters = "Rezitatoren",
    retry = "Erneut versuchen", appearance = "Erscheinungsbild", recitationAndTranslation = "Rezitation & Übersetzung",
    prayerNotifications = "Gebetsbenachrichtigungen", language = "Sprache",
)

private val french = Strings(
    navHome = "Accueil", navSurahs = "Sourates", navFavorites = "Favoris", navSettings = "Paramètres",
    homeGreeting = "Que la paix soit sur vous", search = "Rechercher", juz = "Juz", reciters = "Récitateurs",
    retry = "Réessayer", appearance = "Apparence", recitationAndTranslation = "Récitation et traduction",
    prayerNotifications = "Notifications de prière", language = "Langue",
)

private val uzbek = Strings(
    navHome = "Bosh sahifa", navSurahs = "Suralar", navFavorites = "Sevimlilar", navSettings = "Sozlamalar",
    homeGreeting = "Assalomu alaykum", search = "Qidiruv", juz = "Juz", reciters = "Qorilar",
    retry = "Qayta urinish", appearance = "Ko'rinish", recitationAndTranslation = "Qiroat va tarjima",
    prayerNotifications = "Namoz bildirishnomalari", language = "Til",
)

private val kyrgyz = Strings(
    navHome = "Башкы бет", navSurahs = "Сүрөлөр", navFavorites = "Тандалмалар", navSettings = "Жөндөөлөр",
    homeGreeting = "Ассалому алайкум", search = "Издөө", juz = "Жуз", reciters = "Кураачылар",
    retry = "Кайра аракет кыл", appearance = "Көрүнүш", recitationAndTranslation = "Окуу жана котормо",
    prayerNotifications = "Намаз эскертүүлөрү", language = "Тил",
)

private val turkmen = Strings(
    navHome = "Baş sahypa", navSurahs = "Suralar", navFavorites = "Halanýanlar", navSettings = "Sazlamalar",
    homeGreeting = "Essalamu aleýkim", search = "Gözleg", juz = "Juz", reciters = "Okyjylar",
    retry = "Gaýtadan synanyş", appearance = "Görnüş", recitationAndTranslation = "Okalyş we terjime",
    prayerNotifications = "Namaz duýduryşlary", language = "Dil",
)

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.TURKISH -> turkish
    AppLanguage.ENGLISH -> english
    AppLanguage.ARABIC -> arabic
    AppLanguage.GERMAN -> german
    AppLanguage.FRENCH -> french
    AppLanguage.UZBEK -> uzbek
    AppLanguage.KYRGYZ -> kyrgyz
    AppLanguage.TURKMEN -> turkmen
}
