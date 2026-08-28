package org.ferdidrgn.hudaquran.ui.localization

import androidx.compose.runtime.staticCompositionLocalOf
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.domain.model.SectionKind

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

    // Common
    val ayahWord: String,
    val ayahWordLower: String,
    val meccan: String,
    val medinan: String,
    val serverUnreachable: String,
    val notFound: String,
    val viewAll: String,

    // Home
    val homeSubtitle: String,
    val continueReading: String,
    val dailyAyahTitle: String,
    val dailyAyahError: String,
    val readingLessonsTitle: String,
    val readingLessonsSubtitleTemplate: String,
    val esmaulHusnaTitle: String,
    val discoverQuranTitle: String,
    val mushafModeLabel: String,
    val toggleTranslationLabel: String,
    val pagesLabel: String,
    val manzilsLabel: String,
    val rukusLabel: String,
    val hizbQuartersLabel: String,
    val sajdaVersesLabel: String,
    val quranStatsTitle: String,
    val statSurah: String,
    val statJuz: String,
    val statPage: String,
    val statRuku: String,
    val statHizbQuarter: String,
    val statManzil: String,
    val statSajda: String,
    val featuredSurahsTitle: String,
    val surahsLoadErrorShort: String,
    val nextPrayerLabel: String,
    val hoursMinutesLeftTemplate: String,
    val minutesLeftTemplate: String,
    val favoriteLabel: String,

    // SurahList
    val surahSearchPlaceholder: String,
    val surahsLoadErrorFull: String,

    // SurahDetail
    val surahFallback: String,
    val surahLoadErrorFull: String,

    // Favorites
    val myFavoritesTitle: String,
    val favoritesEmptyMessage: String,
    val surahFallbackNumberedTemplate: String,

    // Settings
    val settingsTitle: String,
    val languagePickerSubtitleTemplate: String,
    val reciterLabel: String,
    val translationLabel: String,
    val notificationsToggleTitle: String,
    val notificationsToggleSubtitle: String,
    val locationLabel: String,
    val locationAutoUpdateNote: String,
    val supportUsTitle: String,
    val adFreeActiveMessage: String,
    val supportUsMessage: String,
    val smallDonationButton: String,
    val mediumDonationButton: String,
    val sixMonthAdFreeButton: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,

    // EditionPicker
    val editionSearchPlaceholder: String,
    val selectTranslationTitle: String,
    val selectLocationTitle: String,
    val selectLanguageTitle: String,

    // Search
    val searchTitle: String,
    val searchAyahPlaceholder: String,
    val searchIdleHint: String,
    val searchErrorFull: String,
    val searchNoResultsTemplate: String,

    // Reciters
    val recitersCountSubtitleTemplate: String,
    val searchReciterPlaceholder: String,
    val recitersLoadErrorFull: String,
    val selectedReciterLabel: String,
    val tapToSelectLabel: String,

    // Sections
    val juzSingular: String,
    val pageSingular: String,
    val pagePlural: String,
    val manzilSingular: String,
    val manzilPlural: String,
    val rukuSingular: String,
    val rukuPlural: String,
    val hizbQuarterSingular: String,
    val hizbQuarterPlural: String,
    val sectionLoadErrorTemplate: String,

    // Sajda
    val sajdaTitle: String,
    val sajdaSubtitle: String,
    val sajdaLoadErrorFull: String,

    // NowPlaying
    val nowPlayingTitle: String,
    val wholeSurahSuffix: String,
    val playbackSpeedTitle: String,

    // Onboarding
    val onboardingSkip: String,
    val onboardingNext: String,
    val onboardingStart: String,
    val onboardTitle1: String,
    val onboardDesc1: String,
    val onboardTitle2: String,
    val onboardDesc2: String,
    val onboardTitle3: String,
    val onboardDesc3: String,
    val onboardTitle4: String,
    val onboardDesc4: String,

    // Splash
    val appTagline: String,

    // Tajwid lessons
    val lessonsSubtitleTemplate: String,
    val lessonNotFound: String,
    val lessonFallback: String,
    val pronounceContentDescription: String,

    // Mushaf mode extras
    val cancelLabel: String,
    val goLabel: String,
    val dismissLabel: String,
    val mushafJumpToPageTitle: String,
    val mushafJumpToPageHint: String,
    val mushafLandscapeHintText: String,
    val mushafResumeSubtitleTemplate: String,
    val mushafStartSubtitle: String,

    // Khatm (hatim) progress
    val khatmProgressTitle: String,
    val khatmProgressTemplate: String,
    val khatmCompletedCountTemplate: String,

    // Qibla
    val qiblaTitle: String,
    val qiblaBearingTemplate: String,
    val qiblaInstructions: String,
    val qiblaLocationUnavailable: String,

    // Accessibility content descriptions
    val cdBack: String,
    val cdPlay: String,
    val cdPause: String,
    val cdClose: String,
    val cdRepeat: String,
    val cdPrevious: String,
    val cdNext: String,

    // Tafsir
    val tafsirLabel: String,
    val selectTafsirTitle: String,
    val changeTafsirLabel: String,
    val tafsirUnavailable: String,
    val tafsirNotSelected: String,
    val qiblaLiveInstructions: String,
    val tafsirVsMealHint: String,
    val textSizeLabel: String,
    val textSizeHint: String,
    val textSizeNormal: String,
    val textSizeLarge: String,
    val textSizeExtraLarge: String,
)

private val turkish = Strings(
    navHome = "Ana Sayfa", navSurahs = "Sureler", navFavorites = "Favoriler", navSettings = "Ayarlar",
    homeGreeting = "Esselamü Aleyküm", search = "Ara", juz = "Cüzler", reciters = "Hafızlar",
    retry = "Tekrar Dene", appearance = "Görünüm", recitationAndTranslation = "Sesli Okuyuş ve Meal",
    prayerNotifications = "Namaz Vakti Bildirimleri", language = "Dil",

    ayahWord = "Ayet", ayahWordLower = "ayet", meccan = "Mekki", medinan = "Medeni",
    serverUnreachable = "Sunucuya ulaşılamadı, lütfen tekrar deneyin.", notFound = "Sonuç bulunamadı",
    viewAll = "Tümünü Gör",

    homeSubtitle = "Bugün Kur'an ile başlayın", continueReading = "Kaldığınız yerden devam edin",
    dailyAyahTitle = "Günün Ayeti", dailyAyahError = "Ayet yüklenemedi, tekrar deneyin.",
    readingLessonsTitle = "Kur'an Okuma Dersleri",
    readingLessonsSubtitleTemplate = "Elif-Ba'dan tecvide, sesli telaffuzlu {n} sıralı ders",
    esmaulHusnaTitle = "Esma-ül Hüsna", discoverQuranTitle = "Kur'an'ı Keşfet",
    mushafModeLabel = "Kitap Okuma Modu", toggleTranslationLabel = "Meali Göster/Gizle",
    pagesLabel = "Sayfalar", manzilsLabel = "Menziller", rukusLabel = "Rükûlar",
    hizbQuartersLabel = "Hizb Çeyrekleri", sajdaVersesLabel = "Secde Ayetleri",
    quranStatsTitle = "Kur'an-ı Kerim İstatistikleri", statSurah = "Sure", statJuz = "Cüz",
    statPage = "Sayfa", statRuku = "Rükû", statHizbQuarter = "Hizb Çeyreği", statManzil = "Menzil",
    statSajda = "Secde", featuredSurahsTitle = "Öne Çıkan Sureler", surahsLoadErrorShort = "Sureler yüklenemedi.",
    nextPrayerLabel = "Sonraki Namaz", hoursMinutesLeftTemplate = "{h} sa {m} dk kaldı",
    minutesLeftTemplate = "{m} dk kaldı", favoriteLabel = "Favori",

    surahSearchPlaceholder = "Sure ara (isim veya numara)",
    surahsLoadErrorFull = "Sureler yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",

    surahFallback = "Sure",
    surahLoadErrorFull = "Sure yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",

    myFavoritesTitle = "Favorilerim",
    favoritesEmptyMessage = "Henüz favori ayetiniz yok. Sureleri okurken ⭐ ikonuna dokunarak buraya ekleyebilirsiniz.",
    surahFallbackNumberedTemplate = "Sure {n}",

    settingsTitle = "Ayarlar", languagePickerSubtitleTemplate = "{n} dil arasından seçin",
    reciterLabel = "Hafız", translationLabel = "Meal / Çeviri",
    notificationsToggleTitle = "Bildirimleri Aç", notificationsToggleSubtitle = "Vakit girdiğinde bildirim gönder",
    locationLabel = "Konum", locationAutoUpdateNote = "Konum kaydedildiğinde bildirimler otomatik güncellenir.",
    supportUsTitle = "Destek Ol", adFreeActiveMessage = "Reklamsız üyeliğiniz aktif, teşekkürler! 💚",
    supportUsMessage = "Uygulamayı geliştirmemize destek olun", smallDonationButton = "☕ Küçük Bağış",
    mediumDonationButton = "🎁 Orta Bağış", sixMonthAdFreeButton = "✨ 6 Aylık Reklamsız Üyelik",
    themeSystem = "🖥️ Sistem", themeLight = "☀️ Açık", themeDark = "🌙 Koyu",

    editionSearchPlaceholder = "Ara",
    selectTranslationTitle = "Meal Seçin", selectLocationTitle = "Konum Seçin", selectLanguageTitle = "Dil Seçin",

    searchTitle = "Kur'an'da Ara", searchAyahPlaceholder = "Ayet içinde kelime ara...",
    searchIdleHint = "Kur'an-ı Kerim mealinde arama yapmak için yazmaya başlayın.",
    searchErrorFull = "Arama yapılamadı. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",
    searchNoResultsTemplate = "\"{query}\" için sonuç bulunamadı.",

    recitersCountSubtitleTemplate = "{n} hafız • dinleyip seçebilirsiniz",
    searchReciterPlaceholder = "Hafız ara...",
    recitersLoadErrorFull = "Hafız listesi yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",
    selectedReciterLabel = "Seçili hafız", tapToSelectLabel = "Seçmek için dokunun",

    juzSingular = "Cüz", pageSingular = "Sayfa", pagePlural = "Sayfalar",
    manzilSingular = "Menzil", manzilPlural = "Menziller", rukuSingular = "Rükû", rukuPlural = "Rükûlar",
    hizbQuarterSingular = "Hizb Çeyreği", hizbQuarterPlural = "Hizb Çeyrekleri",
    sectionLoadErrorTemplate = "{title} yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",

    sajdaTitle = "Secde Ayetleri", sajdaSubtitle = "Kur'an'da tilavet secdesi gerektiren ayetler",
    sajdaLoadErrorFull = "Secde ayetleri yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin.",

    nowPlayingTitle = "Şimdi Çalıyor", wholeSurahSuffix = "Tüm sure", playbackSpeedTitle = "Oynatma Hızı",

    onboardingSkip = "Geç", onboardingNext = "İleri", onboardingStart = "Başla",
    onboardTitle1 = "Huda Qur'an'a Hoş Geldiniz",
    onboardDesc1 = "Kur'an-ı Kerim'i her zaman, her yerde, cebinizde taşıyın.",
    onboardTitle2 = "Sesli Dinleme",
    onboardDesc2 = "Ayet ayet veya sure sure, seçtiğiniz hafızlardan akıcı bir dille dinleyin.",
    onboardTitle3 = "Mealler ve Çeviriler",
    onboardDesc3 = "Türkçe ve diğer dillerdeki meallerle ayetleri anlayarak okuyun.",
    onboardTitle4 = "Favorileriniz, Kaldığınız Yer",
    onboardDesc4 = "Sevdiğiniz ayetleri kaydedin, okumaya kaldığınız yerden devam edin.",

    appTagline = "Kur'an-ı Kerim, her an yanınızda",

    lessonsSubtitleTemplate = "Elif-Ba'dan tecvide, sıralı {n} ders", lessonNotFound = "Ders bulunamadı",
    lessonFallback = "Ders", pronounceContentDescription = "Telaffuzu dinle",

    cancelLabel = "İptal", goLabel = "Git", dismissLabel = "Kapat",
    mushafJumpToPageTitle = "Sayfaya Git", mushafJumpToPageHint = "Sayfa numarası (1-604)",
    mushafLandscapeHintText = "Telefonunuzu yan çevirin, gerçek bir kitap gibi çift sayfa okuyun",
    mushafResumeSubtitleTemplate = "Sayfa {n}'de kaldınız, devam edin",
    mushafStartSubtitle = "Kitap gibi okuyun, sayfa sayfa çevirin",

    khatmProgressTitle = "Hatim İlerlemeniz",
    khatmProgressTemplate = "Sayfa {page} / {total} (%{percent})",
    khatmCompletedCountTemplate = "Tamamlanan hatim: {n}",

    qiblaTitle = "Kıble Pusulası",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Telefonunuzun pusulasını açıp bu açıyı kuzeye göre hizalayın.",
    qiblaLocationUnavailable = "Konum bilgisi alınamadı. Ayarlar'dan şehir/ülke seçtiğinizden emin olun.",
    cdBack = "Geri", cdPlay = "Oynat", cdPause = "Duraklat", cdClose = "Kapat", cdRepeat = "Tekrarla", cdPrevious = "Önceki", cdNext = "Sonraki",
    tafsirLabel = "Tefsir", selectTafsirTitle = "Tefsir Seç", changeTafsirLabel = "Tefsiri Değiştir", tafsirUnavailable = "Bu ayet için tefsir metni alınamadı.", tafsirNotSelected = "Tefsir seçilmedi",
    qiblaLiveInstructions = "Telefonu düz tutun ve ok, dairenin üstündeki üçgene gelene kadar yavaşça çevirin.",
    tafsirVsMealHint = "Meal, ayetin çevirisidir. Tefsir ise ayetin bağlamını ve anlamını ayrıntılı açıklayan yorumdur.",
    textSizeLabel = "Yazı Boyutu", textSizeHint = "Görmekte zorlanıyorsanız yazıları büyütün.", textSizeNormal = "Normal", textSizeLarge = "Büyük", textSizeExtraLarge = "Çok Büyük",
)

private val english = Strings(
    navHome = "Home", navSurahs = "Surahs", navFavorites = "Favorites", navSettings = "Settings",
    homeGreeting = "Peace be upon you", search = "Search", juz = "Juz", reciters = "Reciters",
    retry = "Retry", appearance = "Appearance", recitationAndTranslation = "Recitation & Translation",
    prayerNotifications = "Prayer Notifications", language = "Language",

    ayahWord = "Ayah", ayahWordLower = "ayahs", meccan = "Meccan", medinan = "Medinan",
    serverUnreachable = "Could not reach the server, please try again.", notFound = "No results found",
    viewAll = "View All",

    homeSubtitle = "Start your day with the Quran", continueReading = "Continue where you left off",
    dailyAyahTitle = "Verse of the Day", dailyAyahError = "Couldn't load the verse, try again.",
    readingLessonsTitle = "Quran Reading Lessons",
    readingLessonsSubtitleTemplate = "From Elif-Ba to Tajwid, {n} guided lessons with audio",
    esmaulHusnaTitle = "The 99 Names of Allah", discoverQuranTitle = "Explore the Quran",
    mushafModeLabel = "Mushaf Mode", toggleTranslationLabel = "Show/Hide Translation",
    pagesLabel = "Pages", manzilsLabel = "Manzils", rukusLabel = "Rukus",
    hizbQuartersLabel = "Hizb Quarters", sajdaVersesLabel = "Sajda Verses",
    quranStatsTitle = "Quran Statistics", statSurah = "Surah", statJuz = "Juz",
    statPage = "Page", statRuku = "Ruku", statHizbQuarter = "Hizb Quarter", statManzil = "Manzil",
    statSajda = "Sajda", featuredSurahsTitle = "Featured Surahs", surahsLoadErrorShort = "Couldn't load surahs.",
    nextPrayerLabel = "Next Prayer", hoursMinutesLeftTemplate = "{h}h {m}m left",
    minutesLeftTemplate = "{m}m left", favoriteLabel = "Favorites",

    surahSearchPlaceholder = "Search surah (name or number)",
    surahsLoadErrorFull = "Couldn't load surahs. Could not reach the server, please try again.",

    surahFallback = "Surah",
    surahLoadErrorFull = "Couldn't load surah. Could not reach the server, please try again.",

    myFavoritesTitle = "My Favorites",
    favoritesEmptyMessage = "You don't have any favorite verses yet. Tap the ⭐ icon while reading to add one here.",
    surahFallbackNumberedTemplate = "Surah {n}",

    settingsTitle = "Settings", languagePickerSubtitleTemplate = "Choose from {n} languages",
    reciterLabel = "Reciter", translationLabel = "Translation",
    notificationsToggleTitle = "Enable Notifications", notificationsToggleSubtitle = "Send a notification when a prayer time begins",
    locationLabel = "Location", locationAutoUpdateNote = "Notifications update automatically once your location is saved.",
    supportUsTitle = "Support Us", adFreeActiveMessage = "Your ad-free membership is active, thank you! 💚",
    supportUsMessage = "Help us keep improving the app", smallDonationButton = "☕ Small Donation",
    mediumDonationButton = "🎁 Medium Donation", sixMonthAdFreeButton = "✨ 6-Month Ad-Free Membership",
    themeSystem = "🖥️ System", themeLight = "☀️ Light", themeDark = "🌙 Dark",

    editionSearchPlaceholder = "Search",
    selectTranslationTitle = "Select Translation", selectLocationTitle = "Select Location", selectLanguageTitle = "Select Language",

    searchTitle = "Search the Quran", searchAyahPlaceholder = "Search a word within verses...",
    searchIdleHint = "Start typing to search the Quran translation.",
    searchErrorFull = "Search failed. Could not reach the server, please try again.",
    searchNoResultsTemplate = "No results found for \"{query}\".",

    recitersCountSubtitleTemplate = "{n} reciters • listen and choose one",
    searchReciterPlaceholder = "Search reciters...",
    recitersLoadErrorFull = "Couldn't load reciters. Could not reach the server, please try again.",
    selectedReciterLabel = "Selected reciter", tapToSelectLabel = "Tap to select",

    juzSingular = "Juz", pageSingular = "Page", pagePlural = "Pages",
    manzilSingular = "Manzil", manzilPlural = "Manzils", rukuSingular = "Ruku", rukuPlural = "Rukus",
    hizbQuarterSingular = "Hizb Quarter", hizbQuarterPlural = "Hizb Quarters",
    sectionLoadErrorTemplate = "Couldn't load {title}. Could not reach the server, please try again.",

    sajdaTitle = "Sajda Verses", sajdaSubtitle = "Verses in the Quran that require prostration",
    sajdaLoadErrorFull = "Couldn't load sajda verses. Could not reach the server, please try again.",

    nowPlayingTitle = "Now Playing", wholeSurahSuffix = "Whole surah", playbackSpeedTitle = "Playback Speed",

    onboardingSkip = "Skip", onboardingNext = "Next", onboardingStart = "Get Started",
    onboardTitle1 = "Welcome to Huda Qur'an",
    onboardDesc1 = "Carry the Holy Quran with you, anytime, anywhere.",
    onboardTitle2 = "Audio Recitation",
    onboardDesc2 = "Listen verse by verse or surah by surah, from the reciter you choose.",
    onboardTitle3 = "Translations",
    onboardDesc3 = "Read and understand verses with translations in Turkish and other languages.",
    onboardTitle4 = "Favorites & Continue Reading",
    onboardDesc4 = "Save the verses you love and pick up reading right where you left off.",

    appTagline = "The Holy Quran, always with you",

    lessonsSubtitleTemplate = "From Elif-Ba to Tajwid, {n} guided lessons", lessonNotFound = "Lesson not found",
    lessonFallback = "Lesson", pronounceContentDescription = "Listen to pronunciation",

    cancelLabel = "Cancel", goLabel = "Go", dismissLabel = "Dismiss",
    mushafJumpToPageTitle = "Go to Page", mushafJumpToPageHint = "Page number (1-604)",
    mushafLandscapeHintText = "Rotate your phone sideways to read two pages at once, just like a real book",
    mushafResumeSubtitleTemplate = "You left off on page {n} — continue reading",
    mushafStartSubtitle = "Read like a real book, page by page",

    khatmProgressTitle = "Your Khatm Progress",
    khatmProgressTemplate = "Page {page} / {total} (%{percent})",
    khatmCompletedCountTemplate = "Completed khatms: {n}",

    qiblaTitle = "Qibla Compass",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Open your phone's compass and align this angle with north.",
    qiblaLocationUnavailable = "Couldn't get your location. Make sure you've set a city/country in Settings.",
    cdBack = "Back", cdPlay = "Play", cdPause = "Pause", cdClose = "Close", cdRepeat = "Repeat", cdPrevious = "Previous", cdNext = "Next",
    tafsirLabel = "Tafsir", selectTafsirTitle = "Select Tafsir", changeTafsirLabel = "Change Tafsir", tafsirUnavailable = "Couldn't load the tafsir for this ayah.", tafsirNotSelected = "No tafsir selected",
    qiblaLiveInstructions = "Hold your phone flat and slowly turn until the arrow lines up with the triangle above the circle.",
    tafsirVsMealHint = "Translation renders the ayah's meaning in your language. Tafsir is a longer scholarly explanation of its context and meaning.",
    textSizeLabel = "Text Size", textSizeHint = "Make text bigger if you have trouble reading it.", textSizeNormal = "Normal", textSizeLarge = "Large", textSizeExtraLarge = "Extra Large",
)

private val arabic = Strings(
    navHome = "الرئيسية", navSurahs = "السور", navFavorites = "المفضلة", navSettings = "الإعدادات",
    homeGreeting = "السلام عليكم", search = "بحث", juz = "الأجزاء", reciters = "القراء",
    retry = "إعادة المحاولة", appearance = "المظهر", recitationAndTranslation = "التلاوة والترجمة",
    prayerNotifications = "إشعارات الصلاة", language = "اللغة",

    ayahWord = "آية", ayahWordLower = "آية", meccan = "مكية", medinan = "مدنية",
    serverUnreachable = "تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.", notFound = "لا توجد نتائج",
    viewAll = "عرض الكل",

    homeSubtitle = "ابدأ يومك مع القرآن", continueReading = "تابع من حيث توقفت",
    dailyAyahTitle = "آية اليوم", dailyAyahError = "تعذر تحميل الآية، حاول مرة أخرى.",
    readingLessonsTitle = "دروس تعلم القراءة",
    readingLessonsSubtitleTemplate = "من الألف باء إلى التجويد، {n} دروس مرتبة بالصوت",
    esmaulHusnaTitle = "أسماء الله الحسنى", discoverQuranTitle = "استكشف القرآن",
    mushafModeLabel = "وضع المصحف", toggleTranslationLabel = "إظهار/إخفاء الترجمة",
    pagesLabel = "الصفحات", manzilsLabel = "المنازل", rukusLabel = "الركوعات",
    hizbQuartersLabel = "أرباع الأحزاب", sajdaVersesLabel = "آيات السجدة",
    quranStatsTitle = "إحصائيات القرآن الكريم", statSurah = "سورة", statJuz = "جزء",
    statPage = "صفحة", statRuku = "ركوع", statHizbQuarter = "ربع حزب", statManzil = "منزل",
    statSajda = "سجدة", featuredSurahsTitle = "سور مميزة", surahsLoadErrorShort = "تعذر تحميل السور.",
    nextPrayerLabel = "الصلاة القادمة", hoursMinutesLeftTemplate = "متبقي {h} س {m} د",
    minutesLeftTemplate = "متبقي {m} د", favoriteLabel = "المفضلة",

    surahSearchPlaceholder = "ابحث عن سورة (الاسم أو الرقم)",
    surahsLoadErrorFull = "تعذر تحميل السور. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",

    surahFallback = "سورة",
    surahLoadErrorFull = "تعذر تحميل السورة. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",

    myFavoritesTitle = "مفضلتي",
    favoritesEmptyMessage = "ليس لديك أي آيات مفضلة بعد. اضغط على أيقونة ⭐ أثناء القراءة لإضافتها هنا.",
    surahFallbackNumberedTemplate = "سورة {n}",

    settingsTitle = "الإعدادات", languagePickerSubtitleTemplate = "اختر من بين {n} لغة",
    reciterLabel = "القارئ", translationLabel = "الترجمة",
    notificationsToggleTitle = "تفعيل الإشعارات", notificationsToggleSubtitle = "إرسال إشعار عند دخول وقت الصلاة",
    locationLabel = "الموقع", locationAutoUpdateNote = "يتم تحديث الإشعارات تلقائيًا عند حفظ الموقع.",
    supportUsTitle = "ادعمنا", adFreeActiveMessage = "عضويتك بدون إعلانات مفعّلة، شكرًا لك! 💚",
    supportUsMessage = "ساعدنا في تطوير التطبيق", smallDonationButton = "☕ تبرع صغير",
    mediumDonationButton = "🎁 تبرع متوسط", sixMonthAdFreeButton = "✨ عضوية بدون إعلانات لمدة 6 أشهر",
    themeSystem = "🖥️ النظام", themeLight = "☀️ فاتح", themeDark = "🌙 داكن",

    editionSearchPlaceholder = "بحث",
    selectTranslationTitle = "اختر الترجمة", selectLocationTitle = "اختر الموقع", selectLanguageTitle = "اختر اللغة",

    searchTitle = "البحث في القرآن", searchAyahPlaceholder = "ابحث عن كلمة داخل الآيات...",
    searchIdleHint = "ابدأ الكتابة للبحث في ترجمة معاني القرآن الكريم.",
    searchErrorFull = "فشل البحث. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",
    searchNoResultsTemplate = "لا توجد نتائج لـ \"{query}\".",

    recitersCountSubtitleTemplate = "{n} قارئ • استمع واختر",
    searchReciterPlaceholder = "ابحث عن قارئ...",
    recitersLoadErrorFull = "تعذر تحميل قائمة القراء. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",
    selectedReciterLabel = "القارئ المختار", tapToSelectLabel = "اضغط للاختيار",

    juzSingular = "جزء", pageSingular = "صفحة", pagePlural = "الصفحات",
    manzilSingular = "منزل", manzilPlural = "المنازل", rukuSingular = "ركوع", rukuPlural = "الركوعات",
    hizbQuarterSingular = "ربع حزب", hizbQuarterPlural = "أرباع الأحزاب",
    sectionLoadErrorTemplate = "تعذر تحميل {title}. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",

    sajdaTitle = "آيات السجدة", sajdaSubtitle = "الآيات في القرآن التي تستوجب سجدة التلاوة",
    sajdaLoadErrorFull = "تعذر تحميل آيات السجدة. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى.",

    nowPlayingTitle = "قيد التشغيل الآن", wholeSurahSuffix = "السورة كاملة", playbackSpeedTitle = "سرعة التشغيل",

    onboardingSkip = "تخطي", onboardingNext = "التالي", onboardingStart = "ابدأ",
    onboardTitle1 = "مرحبًا بك في Huda Qur'an",
    onboardDesc1 = "احمل القرآن الكريم معك دائمًا وفي كل مكان.",
    onboardTitle2 = "الاستماع الصوتي",
    onboardDesc2 = "استمع آية آية أو سورة سورة، من القارئ الذي تختاره.",
    onboardTitle3 = "المعاني والترجمات",
    onboardDesc3 = "افهم الآيات من خلال الترجمات بالتركية ولغات أخرى.",
    onboardTitle4 = "مفضلتك ونقطة توقفك",
    onboardDesc4 = "احفظ الآيات التي تحبها وتابع القراءة من حيث توقفت.",

    appTagline = "القرآن الكريم، معك في كل لحظة",

    lessonsSubtitleTemplate = "من الألف باء إلى التجويد، {n} دروس مرتبة", lessonNotFound = "الدرس غير موجود",
    lessonFallback = "درس", pronounceContentDescription = "استمع إلى النطق",

    cancelLabel = "إلغاء", goLabel = "اذهب", dismissLabel = "إغلاق",
    mushafJumpToPageTitle = "الانتقال إلى صفحة", mushafJumpToPageHint = "رقم الصفحة (1-604)",
    mushafLandscapeHintText = "أدر هاتفك أفقيًا لقراءة صفحتين في آنٍ واحد، كما في المصحف الحقيقي",
    mushafResumeSubtitleTemplate = "توقفت عند الصفحة {n}، تابع القراءة",
    mushafStartSubtitle = "اقرأ كأنه كتاب حقيقي، صفحة بصفحة",

    khatmProgressTitle = "تقدمك في الختمة",
    khatmProgressTemplate = "الصفحة {page} / {total} (%{percent})",
    khatmCompletedCountTemplate = "الختمات المكتملة: {n}",

    qiblaTitle = "بوصلة القبلة",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "افتح بوصلة هاتفك وقم بمحاذاة هذه الزاوية مع الشمال.",
    qiblaLocationUnavailable = "تعذر الحصول على موقعك. تأكد من تحديد المدينة/الدولة في الإعدادات.",
    cdBack = "رجوع", cdPlay = "تشغيل", cdPause = "إيقاف مؤقت", cdClose = "إغلاق", cdRepeat = "تكرار", cdPrevious = "السابق", cdNext = "التالي",
    tafsirLabel = "تفسير", selectTafsirTitle = "اختر التفسير", changeTafsirLabel = "تغيير التفسير", tafsirUnavailable = "تعذر تحميل تفسير هذه الآية.", tafsirNotSelected = "لم يتم اختيار تفسير",
    qiblaLiveInstructions = "أمسك هاتفك أفقيًا وأدره ببطء حتى يتوافق السهم مع المثلث أعلى الدائرة.",
    tafsirVsMealHint = "الترجمة (المعنى) هي نقل معنى الآية. أما التفسير فهو شرح تفصيلي لسياق الآية ومعناها.",
    textSizeLabel = "حجم النص", textSizeHint = "كبّر النص إذا كنت تواجه صعوبة في القراءة.", textSizeNormal = "عادي", textSizeLarge = "كبير", textSizeExtraLarge = "كبير جدًا",
)

private val german = Strings(
    navHome = "Start", navSurahs = "Suren", navFavorites = "Favoriten", navSettings = "Einstellungen",
    homeGreeting = "Friede sei mit dir", search = "Suche", juz = "Juz", reciters = "Rezitatoren",
    retry = "Erneut versuchen", appearance = "Erscheinungsbild", recitationAndTranslation = "Rezitation & Übersetzung",
    prayerNotifications = "Gebetsbenachrichtigungen", language = "Sprache",

    ayahWord = "Vers", ayahWordLower = "Verse", meccan = "Mekkanisch", medinan = "Medinensisch",
    serverUnreachable = "Server nicht erreichbar, bitte versuchen Sie es erneut.", notFound = "Keine Ergebnisse gefunden",
    viewAll = "Alle anzeigen",

    homeSubtitle = "Starten Sie Ihren Tag mit dem Koran", continueReading = "Weiterlesen, wo Sie aufgehört haben",
    dailyAyahTitle = "Vers des Tages", dailyAyahError = "Vers konnte nicht geladen werden, erneut versuchen.",
    readingLessonsTitle = "Koran-Leselektionen",
    readingLessonsSubtitleTemplate = "Von Elif-Ba bis Tadschwid, {n} geführte Lektionen mit Audio",
    esmaulHusnaTitle = "Die 99 Namen Allahs", discoverQuranTitle = "Koran entdecken",
    mushafModeLabel = "Mushaf-Modus", toggleTranslationLabel = "Übersetzung ein-/ausblenden",
    pagesLabel = "Seiten", manzilsLabel = "Manzil", rukusLabel = "Ruku",
    hizbQuartersLabel = "Hizb-Viertel", sajdaVersesLabel = "Sadschda-Verse",
    quranStatsTitle = "Koran-Statistiken", statSurah = "Sure", statJuz = "Juz",
    statPage = "Seite", statRuku = "Ruku", statHizbQuarter = "Hizb-Viertel", statManzil = "Manzil",
    statSajda = "Sadschda", featuredSurahsTitle = "Ausgewählte Suren", surahsLoadErrorShort = "Suren konnten nicht geladen werden.",
    nextPrayerLabel = "Nächstes Gebet", hoursMinutesLeftTemplate = "{h} Std {m} Min verbleibend",
    minutesLeftTemplate = "{m} Min verbleibend", favoriteLabel = "Favoriten",

    surahSearchPlaceholder = "Sure suchen (Name oder Nummer)",
    surahsLoadErrorFull = "Suren konnten nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen.",

    surahFallback = "Sure",
    surahLoadErrorFull = "Sure konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen.",

    myFavoritesTitle = "Meine Favoriten",
    favoritesEmptyMessage = "Sie haben noch keine favorisierten Verse. Tippen Sie beim Lesen auf das ⭐-Symbol, um einen hinzuzufügen.",
    surahFallbackNumberedTemplate = "Sure {n}",

    settingsTitle = "Einstellungen", languagePickerSubtitleTemplate = "Aus {n} Sprachen wählen",
    reciterLabel = "Rezitator", translationLabel = "Übersetzung",
    notificationsToggleTitle = "Benachrichtigungen aktivieren", notificationsToggleSubtitle = "Benachrichtigung senden, wenn eine Gebetszeit beginnt",
    locationLabel = "Standort", locationAutoUpdateNote = "Benachrichtigungen werden automatisch aktualisiert, sobald Ihr Standort gespeichert ist.",
    supportUsTitle = "Unterstützen Sie uns", adFreeActiveMessage = "Ihre werbefreie Mitgliedschaft ist aktiv, vielen Dank! 💚",
    supportUsMessage = "Helfen Sie uns, die App weiter zu verbessern", smallDonationButton = "☕ Kleine Spende",
    mediumDonationButton = "🎁 Mittlere Spende", sixMonthAdFreeButton = "✨ 6 Monate werbefreie Mitgliedschaft",
    themeSystem = "🖥️ System", themeLight = "☀️ Hell", themeDark = "🌙 Dunkel",

    editionSearchPlaceholder = "Suchen",
    selectTranslationTitle = "Übersetzung wählen", selectLocationTitle = "Standort wählen", selectLanguageTitle = "Sprache wählen",

    searchTitle = "Im Koran suchen", searchAyahPlaceholder = "Wort in Versen suchen...",
    searchIdleHint = "Beginnen Sie zu tippen, um in der Koranübersetzung zu suchen.",
    searchErrorFull = "Suche fehlgeschlagen. Server nicht erreichbar, bitte erneut versuchen.",
    searchNoResultsTemplate = "Keine Ergebnisse für \"{query}\".",

    recitersCountSubtitleTemplate = "{n} Rezitatoren • anhören und auswählen",
    searchReciterPlaceholder = "Rezitator suchen...",
    recitersLoadErrorFull = "Rezitatorenliste konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen.",
    selectedReciterLabel = "Ausgewählter Rezitator", tapToSelectLabel = "Zum Auswählen tippen",

    juzSingular = "Juz", pageSingular = "Seite", pagePlural = "Seiten",
    manzilSingular = "Manzil", manzilPlural = "Manzil", rukuSingular = "Ruku", rukuPlural = "Ruku",
    hizbQuarterSingular = "Hizb-Viertel", hizbQuarterPlural = "Hizb-Viertel",
    sectionLoadErrorTemplate = "{title} konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen.",

    sajdaTitle = "Sadschda-Verse", sajdaSubtitle = "Verse im Koran, die eine Rezitationsniederwerfung erfordern",
    sajdaLoadErrorFull = "Sadschda-Verse konnten nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen.",

    nowPlayingTitle = "Wird gerade abgespielt", wholeSurahSuffix = "Ganze Sure", playbackSpeedTitle = "Wiedergabegeschwindigkeit",

    onboardingSkip = "Überspringen", onboardingNext = "Weiter", onboardingStart = "Loslegen",
    onboardTitle1 = "Willkommen bei Huda Qur'an",
    onboardDesc1 = "Tragen Sie den Heiligen Koran jederzeit und überall bei sich.",
    onboardTitle2 = "Audio-Rezitation",
    onboardDesc2 = "Hören Sie Vers für Vers oder Sure für Sure, vom Rezitator Ihrer Wahl.",
    onboardTitle3 = "Übersetzungen",
    onboardDesc3 = "Verstehen Sie Verse mit Übersetzungen auf Türkisch und in anderen Sprachen.",
    onboardTitle4 = "Favoriten & Weiterlesen",
    onboardDesc4 = "Speichern Sie Ihre Lieblingsverse und lesen Sie genau dort weiter, wo Sie aufgehört haben.",

    appTagline = "Der Heilige Koran, immer bei Ihnen",

    lessonsSubtitleTemplate = "Von Elif-Ba bis Tadschwid, {n} geführte Lektionen", lessonNotFound = "Lektion nicht gefunden",
    lessonFallback = "Lektion", pronounceContentDescription = "Aussprache anhören",

    cancelLabel = "Abbrechen", goLabel = "Los", dismissLabel = "Schließen",
    mushafJumpToPageTitle = "Zu Seite springen", mushafJumpToPageHint = "Seitenzahl (1-604)",
    mushafLandscapeHintText = "Drehen Sie Ihr Telefon quer, um wie in einem echten Buch zwei Seiten gleichzeitig zu lesen",
    mushafResumeSubtitleTemplate = "Sie waren auf Seite {n} — weiterlesen",
    mushafStartSubtitle = "Lesen Sie wie in einem echten Buch, Seite für Seite",

    khatmProgressTitle = "Ihr Khatm-Fortschritt",
    khatmProgressTemplate = "Seite {page} / {total} (%{percent})",
    khatmCompletedCountTemplate = "Abgeschlossene Khatms: {n}",

    qiblaTitle = "Qibla-Kompass",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Öffnen Sie den Kompass Ihres Telefons und richten Sie diesen Winkel nach Norden aus.",
    qiblaLocationUnavailable = "Standort konnte nicht ermittelt werden. Stellen Sie sicher, dass Stadt/Land in den Einstellungen festgelegt sind.",
    cdBack = "Zurück", cdPlay = "Abspielen", cdPause = "Pause", cdClose = "Schließen", cdRepeat = "Wiederholen", cdPrevious = "Vorherige", cdNext = "Weiter",
    tafsirLabel = "Tafsir", selectTafsirTitle = "Tafsir auswählen", changeTafsirLabel = "Tafsir ändern", tafsirUnavailable = "Der Tafsir für diesen Vers konnte nicht geladen werden.", tafsirNotSelected = "Kein Tafsir ausgewählt",
    qiblaLiveInstructions = "Halte dein Telefon flach und drehe dich langsam, bis der Pfeil mit dem Dreieck über dem Kreis übereinstimmt.",
    tafsirVsMealHint = "Die Übersetzung überträgt die Bedeutung des Verses. Der Tafsir ist eine ausführliche Erklärung seines Kontexts und seiner Bedeutung.",
    textSizeLabel = "Textgröße", textSizeHint = "Vergrößern Sie den Text, wenn Sie Schwierigkeiten beim Lesen haben.", textSizeNormal = "Normal", textSizeLarge = "Groß", textSizeExtraLarge = "Sehr groß",
)

private val french = Strings(
    navHome = "Accueil", navSurahs = "Sourates", navFavorites = "Favoris", navSettings = "Paramètres",
    homeGreeting = "Que la paix soit sur vous", search = "Rechercher", juz = "Juz", reciters = "Récitateurs",
    retry = "Réessayer", appearance = "Apparence", recitationAndTranslation = "Récitation et traduction",
    prayerNotifications = "Notifications de prière", language = "Langue",

    ayahWord = "Verset", ayahWordLower = "versets", meccan = "Mecquoise", medinan = "Médinoise",
    serverUnreachable = "Impossible de joindre le serveur, veuillez réessayer.", notFound = "Aucun résultat trouvé",
    viewAll = "Tout voir",

    homeSubtitle = "Commencez votre journée avec le Coran", continueReading = "Reprendre là où vous vous êtes arrêté",
    dailyAyahTitle = "Verset du jour", dailyAyahError = "Impossible de charger le verset, réessayez.",
    readingLessonsTitle = "Leçons de lecture du Coran",
    readingLessonsSubtitleTemplate = "De l'Elif-Ba au tajwid, {n} leçons guidées avec audio",
    esmaulHusnaTitle = "Les 99 noms d'Allah", discoverQuranTitle = "Explorer le Coran",
    mushafModeLabel = "Mode Mushaf", toggleTranslationLabel = "Afficher/Masquer la traduction",
    pagesLabel = "Pages", manzilsLabel = "Manzils", rukusLabel = "Rukus",
    hizbQuartersLabel = "Quarts de Hizb", sajdaVersesLabel = "Versets de la Sajda",
    quranStatsTitle = "Statistiques du Coran", statSurah = "Sourate", statJuz = "Juz",
    statPage = "Page", statRuku = "Ruku", statHizbQuarter = "Quart de Hizb", statManzil = "Manzil",
    statSajda = "Sajda", featuredSurahsTitle = "Sourates en vedette", surahsLoadErrorShort = "Impossible de charger les sourates.",
    nextPrayerLabel = "Prochaine prière", hoursMinutesLeftTemplate = "{h} h {m} min restantes",
    minutesLeftTemplate = "{m} min restantes", favoriteLabel = "Favoris",

    surahSearchPlaceholder = "Rechercher une sourate (nom ou numéro)",
    surahsLoadErrorFull = "Impossible de charger les sourates. Serveur injoignable, veuillez réessayer.",

    surahFallback = "Sourate",
    surahLoadErrorFull = "Impossible de charger la sourate. Serveur injoignable, veuillez réessayer.",

    myFavoritesTitle = "Mes favoris",
    favoritesEmptyMessage = "Vous n'avez pas encore de versets favoris. Touchez l'icône ⭐ pendant la lecture pour en ajouter un ici.",
    surahFallbackNumberedTemplate = "Sourate {n}",

    settingsTitle = "Paramètres", languagePickerSubtitleTemplate = "Choisissez parmi {n} langues",
    reciterLabel = "Récitateur", translationLabel = "Traduction",
    notificationsToggleTitle = "Activer les notifications", notificationsToggleSubtitle = "Envoyer une notification à l'heure de prière",
    locationLabel = "Emplacement", locationAutoUpdateNote = "Les notifications se mettent à jour automatiquement une fois l'emplacement enregistré.",
    supportUsTitle = "Soutenez-nous", adFreeActiveMessage = "Votre abonnement sans publicité est actif, merci ! 💚",
    supportUsMessage = "Aidez-nous à améliorer l'application", smallDonationButton = "☕ Petit don",
    mediumDonationButton = "🎁 Don moyen", sixMonthAdFreeButton = "✨ Abonnement sans pub 6 mois",
    themeSystem = "🖥️ Système", themeLight = "☀️ Clair", themeDark = "🌙 Sombre",

    editionSearchPlaceholder = "Rechercher",
    selectTranslationTitle = "Choisir la traduction", selectLocationTitle = "Choisir l'emplacement", selectLanguageTitle = "Choisir la langue",

    searchTitle = "Rechercher dans le Coran", searchAyahPlaceholder = "Rechercher un mot dans les versets...",
    searchIdleHint = "Commencez à taper pour rechercher dans la traduction du Coran.",
    searchErrorFull = "Échec de la recherche. Serveur injoignable, veuillez réessayer.",
    searchNoResultsTemplate = "Aucun résultat pour \"{query}\".",

    recitersCountSubtitleTemplate = "{n} récitateurs • écoutez et choisissez",
    searchReciterPlaceholder = "Rechercher un récitateur...",
    recitersLoadErrorFull = "Impossible de charger les récitateurs. Serveur injoignable, veuillez réessayer.",
    selectedReciterLabel = "Récitateur sélectionné", tapToSelectLabel = "Touchez pour sélectionner",

    juzSingular = "Juz", pageSingular = "Page", pagePlural = "Pages",
    manzilSingular = "Manzil", manzilPlural = "Manzils", rukuSingular = "Ruku", rukuPlural = "Rukus",
    hizbQuarterSingular = "Quart de Hizb", hizbQuarterPlural = "Quarts de Hizb",
    sectionLoadErrorTemplate = "Impossible de charger {title}. Serveur injoignable, veuillez réessayer.",

    sajdaTitle = "Versets de la Sajda", sajdaSubtitle = "Versets du Coran nécessitant une prosternation de récitation",
    sajdaLoadErrorFull = "Impossible de charger les versets de la Sajda. Serveur injoignable, veuillez réessayer.",

    nowPlayingTitle = "Lecture en cours", wholeSurahSuffix = "Sourate entière", playbackSpeedTitle = "Vitesse de lecture",

    onboardingSkip = "Passer", onboardingNext = "Suivant", onboardingStart = "Commencer",
    onboardTitle1 = "Bienvenue sur Huda Qur'an",
    onboardDesc1 = "Emportez le Saint Coran avec vous, à tout moment, partout.",
    onboardTitle2 = "Écoute audio",
    onboardDesc2 = "Écoutez verset par verset ou sourate par sourate, avec le récitateur de votre choix.",
    onboardTitle3 = "Traductions",
    onboardDesc3 = "Comprenez les versets grâce aux traductions en turc et dans d'autres langues.",
    onboardTitle4 = "Favoris et reprise de lecture",
    onboardDesc4 = "Enregistrez vos versets préférés et reprenez la lecture là où vous vous êtes arrêté.",

    appTagline = "Le Saint Coran, toujours avec vous",

    lessonsSubtitleTemplate = "De l'Elif-Ba au tajwid, {n} leçons guidées", lessonNotFound = "Leçon introuvable",
    lessonFallback = "Leçon", pronounceContentDescription = "Écouter la prononciation",

    cancelLabel = "Annuler", goLabel = "Aller", dismissLabel = "Fermer",
    mushafJumpToPageTitle = "Aller à la page", mushafJumpToPageHint = "Numéro de page (1-604)",
    mushafLandscapeHintText = "Tournez votre téléphone à l'horizontale pour lire deux pages à la fois, comme un vrai livre",
    mushafResumeSubtitleTemplate = "Vous étiez à la page {n} — continuer la lecture",
    mushafStartSubtitle = "Lisez comme un vrai livre, page par page",

    khatmProgressTitle = "Votre progression du khatm",
    khatmProgressTemplate = "Page {page} / {total} (%{percent})",
    khatmCompletedCountTemplate = "Khatms terminés : {n}",

    qiblaTitle = "Boussole Qibla",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Ouvrez la boussole de votre téléphone et alignez cet angle avec le nord.",
    qiblaLocationUnavailable = "Impossible d'obtenir votre position. Vérifiez que la ville/le pays sont définis dans les Paramètres.",
    cdBack = "Retour", cdPlay = "Lecture", cdPause = "Pause", cdClose = "Fermer", cdRepeat = "Répéter", cdPrevious = "Précédent", cdNext = "Suivant",
    tafsirLabel = "Tafsir", selectTafsirTitle = "Choisir le tafsir", changeTafsirLabel = "Changer le tafsir", tafsirUnavailable = "Impossible de charger le tafsir de ce verset.", tafsirNotSelected = "Aucun tafsir sélectionné",
    qiblaLiveInstructions = "Tenez votre téléphone à plat et tournez lentement jusqu'à ce que la flèche s'aligne avec le triangle au-dessus du cercle.",
    tafsirVsMealHint = "La traduction rend le sens du verset. Le tafsir est une explication savante plus détaillée de son contexte et de sa signification.",
    textSizeLabel = "Taille du texte", textSizeHint = "Agrandissez le texte si vous avez du mal à le lire.", textSizeNormal = "Normal", textSizeLarge = "Grand", textSizeExtraLarge = "Très grand",
)

private val uzbek = Strings(
    navHome = "Bosh sahifa", navSurahs = "Suralar", navFavorites = "Sevimlilar", navSettings = "Sozlamalar",
    homeGreeting = "Assalomu alaykum", search = "Qidiruv", juz = "Juz", reciters = "Qorilar",
    retry = "Qayta urinish", appearance = "Ko'rinish", recitationAndTranslation = "Qiroat va tarjima",
    prayerNotifications = "Namoz bildirishnomalari", language = "Til",

    ayahWord = "Oyat", ayahWordLower = "oyat", meccan = "Makkiy", medinan = "Madaniy",
    serverUnreachable = "Serverga ulanib bo'lmadi, qayta urinib ko'ring.", notFound = "Natija topilmadi",
    viewAll = "Barchasini ko'rish",

    homeSubtitle = "Kuningizni Qur'on bilan boshlang", continueReading = "To'xtagan joyingizdan davom eting",
    dailyAyahTitle = "Kunning oyati", dailyAyahError = "Oyat yuklanmadi, qayta urinib ko'ring.",
    readingLessonsTitle = "Qur'on o'qish darslari",
    readingLessonsSubtitleTemplate = "Elif-Bodan tajvidgacha, ovozli {n} ketma-ket dars",
    esmaulHusnaTitle = "Allohning 99 go'zal ismi", discoverQuranTitle = "Qur'onni kashf eting",
    mushafModeLabel = "Mushaf Rejimi", toggleTranslationLabel = "Tarjimani Ko'rsatish/Yashirish",
    pagesLabel = "Sahifalar", manzilsLabel = "Manzillar", rukusLabel = "Ruku'lar",
    hizbQuartersLabel = "Hizb choraklari", sajdaVersesLabel = "Sajda oyatlari",
    quranStatsTitle = "Qur'oni Karim statistikasi", statSurah = "Sura", statJuz = "Juz",
    statPage = "Sahifa", statRuku = "Ruku'", statHizbQuarter = "Hizb chorak", statManzil = "Manzil",
    statSajda = "Sajda", featuredSurahsTitle = "Mashhur suralar", surahsLoadErrorShort = "Suralar yuklanmadi.",
    nextPrayerLabel = "Keyingi namoz", hoursMinutesLeftTemplate = "{h} soat {m} daqiqa qoldi",
    minutesLeftTemplate = "{m} daqiqa qoldi", favoriteLabel = "Sevimli",

    surahSearchPlaceholder = "Sura qidirish (nomi yoki raqami)",
    surahsLoadErrorFull = "Suralar yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",

    surahFallback = "Sura",
    surahLoadErrorFull = "Sura yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",

    myFavoritesTitle = "Sevimlilarim",
    favoritesEmptyMessage = "Hali sevimli oyatlaringiz yo'q. O'qish paytida ⭐ belgisini bosib, shu yerga qo'shishingiz mumkin.",
    surahFallbackNumberedTemplate = "{n}-sura",

    settingsTitle = "Sozlamalar", languagePickerSubtitleTemplate = "{n} til orasidan tanlang",
    reciterLabel = "Qori", translationLabel = "Tarjima",
    notificationsToggleTitle = "Bildirishnomalarni yoqish", notificationsToggleSubtitle = "Namoz vaqti kirganda bildirishnoma yuborish",
    locationLabel = "Manzil", locationAutoUpdateNote = "Manzil saqlangach, bildirishnomalar avtomatik yangilanadi.",
    supportUsTitle = "Bizni qo'llab-quvvatlang", adFreeActiveMessage = "Reklamasiz a'zoligingiz faol, rahmat! 💚",
    supportUsMessage = "Ilovani rivojlantirishga yordam bering", smallDonationButton = "☕ Kichik hadya",
    mediumDonationButton = "🎁 O'rta hadya", sixMonthAdFreeButton = "✨ 6 oylik reklamasiz a'zolik",
    themeSystem = "🖥️ Tizim", themeLight = "☀️ Yorug'", themeDark = "🌙 Tungi",

    editionSearchPlaceholder = "Qidirish",
    selectTranslationTitle = "Tarjima tanlang", selectLocationTitle = "Manzil tanlang", selectLanguageTitle = "Til tanlang",

    searchTitle = "Qur'ondan qidirish", searchAyahPlaceholder = "Oyatlar ichidan so'z qidiring...",
    searchIdleHint = "Qur'oni Karim tarjimasidan qidirish uchun yozishni boshlang.",
    searchErrorFull = "Qidiruv amalga oshmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",
    searchNoResultsTemplate = "\"{query}\" bo'yicha natija topilmadi.",

    recitersCountSubtitleTemplate = "{n} qori • tinglab tanlang",
    searchReciterPlaceholder = "Qori qidirish...",
    recitersLoadErrorFull = "Qorilar ro'yxati yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",
    selectedReciterLabel = "Tanlangan qori", tapToSelectLabel = "Tanlash uchun bosing",

    juzSingular = "Juz", pageSingular = "Sahifa", pagePlural = "Sahifalar",
    manzilSingular = "Manzil", manzilPlural = "Manzillar", rukuSingular = "Ruku'", rukuPlural = "Ruku'lar",
    hizbQuarterSingular = "Hizb chorak", hizbQuarterPlural = "Hizb choraklari",
    sectionLoadErrorTemplate = "{title} yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",

    sajdaTitle = "Sajda oyatlari", sajdaSubtitle = "Qur'onda tilovat sajdasini talab qiluvchi oyatlar",
    sajdaLoadErrorFull = "Sajda oyatlari yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring.",

    nowPlayingTitle = "Hozir ijro etilmoqda", wholeSurahSuffix = "Butun sura", playbackSpeedTitle = "Ijro tezligi",

    onboardingSkip = "O'tkazib yuborish", onboardingNext = "Keyingi", onboardingStart = "Boshlash",
    onboardTitle1 = "Huda Qur'an'ga xush kelibsiz",
    onboardDesc1 = "Qur'oni Karimni doim, hamma joyda, cho'ntagingizda olib yuring.",
    onboardTitle2 = "Ovozli tinglash",
    onboardDesc2 = "Oyatma-oyat yoki suraga-sura, tanlagan qoringizdan ravon tinglang.",
    onboardTitle3 = "Tarjimalar",
    onboardDesc3 = "O'zbek va boshqa tillardagi tarjimalar bilan oyatlarni tushunib o'qing.",
    onboardTitle4 = "Sevimlilaringiz, to'xtagan joyingiz",
    onboardDesc4 = "Yoqtirgan oyatlaringizni saqlang, o'qishni to'xtagan joyingizdan davom eting.",

    appTagline = "Qur'oni Karim, har lahza yoningizda",

    lessonsSubtitleTemplate = "Elif-Bodan tajvidgacha, ketma-ket {n} dars", lessonNotFound = "Dars topilmadi",
    lessonFallback = "Dars", pronounceContentDescription = "Talaffuzni tinglash",

    cancelLabel = "Bekor qilish", goLabel = "O'tish", dismissLabel = "Yopish",
    mushafJumpToPageTitle = "Sahifaga o'tish", mushafJumpToPageHint = "Sahifa raqami (1-604)",
    mushafLandscapeHintText = "Haqiqiy kitobdek ikki sahifani birga o'qish uchun telefoningizni yonboshlab tuting",
    mushafResumeSubtitleTemplate = "{n}-sahifada to'xtagansiz, davom eting",
    mushafStartSubtitle = "Haqiqiy kitobdek, sahifama-sahifa o'qing",

    khatmProgressTitle = "Xatm jarayoningiz",
    khatmProgressTemplate = "{page}-sahifa / {total} (%{percent})",
    khatmCompletedCountTemplate = "Tugallangan xatmlar: {n}",

    qiblaTitle = "Qibla Kompasi",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Telefoningizning kompasini oching va bu burchakni shimolga qarab tekislang.",
    qiblaLocationUnavailable = "Joylashuvingizni aniqlab bo'lmadi. Sozlamalarda shahar/davlat tanlanganiga ishonch hosil qiling.",
    cdBack = "Orqaga", cdPlay = "Ijro et", cdPause = "Pauza", cdClose = "Yopish", cdRepeat = "Takrorlash", cdPrevious = "Oldingi", cdNext = "Keyingi",
    tafsirLabel = "Tafsir", selectTafsirTitle = "Tafsirni tanlang", changeTafsirLabel = "Tafsirni o'zgartirish", tafsirUnavailable = "Bu oyat uchun tafsir yuklanmadi.", tafsirNotSelected = "Tafsir tanlanmagan",
    qiblaLiveInstructions = "Telefoningizni tekis tuting va o'q doira ustidagi uchburchak bilan tekislanguncha asta aylantiring.",
    tafsirVsMealHint = "Tarjima oyatning ma'nosini beradi. Tafsir esa uning konteksti va ma'nosining batafsil izohidir.",
    textSizeLabel = "Matn o'lchami", textSizeHint = "O'qishda qiynalsangiz, matnni kattalashtiring.", textSizeNormal = "Oddiy", textSizeLarge = "Katta", textSizeExtraLarge = "Juda katta",
)

private val kyrgyz = Strings(
    navHome = "Башкы бет", navSurahs = "Сүрөлөр", navFavorites = "Тандалмалар", navSettings = "Жөндөөлөр",
    homeGreeting = "Ассалому алайкум", search = "Издөө", juz = "Жуз", reciters = "Кураачылар",
    retry = "Кайра аракет кыл", appearance = "Көрүнүш", recitationAndTranslation = "Окуу жана котормо",
    prayerNotifications = "Намаз эскертүүлөрү", language = "Тил",

    ayahWord = "Аят", ayahWordLower = "аят", meccan = "Меккелик", medinan = "Мадиналык",
    serverUnreachable = "Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.", notFound = "Натыйжа табылган жок",
    viewAll = "Баарын көрүү",

    homeSubtitle = "Күнүңүздү Кураан менен баштаңыз", continueReading = "Токтогон жериңизден улантыңыз",
    dailyAyahTitle = "Күндүн аяты", dailyAyahError = "Аят жүктөлбөдү, кайра аракет кылыңыз.",
    readingLessonsTitle = "Кураан окуу сабактары",
    readingLessonsSubtitleTemplate = "Элип-Байдан тажвидге чейин, үн менен {n} ырааттуу сабак",
    esmaulHusnaTitle = "Аллахтын 99 сулуу ысымы", discoverQuranTitle = "Кураанды изилдеңиз",
    mushafModeLabel = "Мусхаф режими", toggleTranslationLabel = "Котормону көрсөтүү/жашыруу",
    pagesLabel = "Барактар", manzilsLabel = "Манзилдер", rukusLabel = "Рукулар",
    hizbQuartersLabel = "Хизб чейректери", sajdaVersesLabel = "Сажда аяттары",
    quranStatsTitle = "Кураани Каримдин статистикасы", statSurah = "Сүрө", statJuz = "Жуз",
    statPage = "Барак", statRuku = "Руку", statHizbQuarter = "Хизб чейрек", statManzil = "Манзил",
    statSajda = "Сажда", featuredSurahsTitle = "Тандалган сүрөлөр", surahsLoadErrorShort = "Сүрөлөр жүктөлбөдү.",
    nextPrayerLabel = "Кийинки намаз", hoursMinutesLeftTemplate = "{h} саат {m} мүнөт калды",
    minutesLeftTemplate = "{m} мүнөт калды", favoriteLabel = "Тандалма",

    surahSearchPlaceholder = "Сүрө издөө (аты же номери)",
    surahsLoadErrorFull = "Сүрөлөр жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",

    surahFallback = "Сүрө",
    surahLoadErrorFull = "Сүрө жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",

    myFavoritesTitle = "Тандалмаларым",
    favoritesEmptyMessage = "Азырынча тандалма аяттарыңыз жок. Окуу учурунда ⭐ белгисин басып, бул жерге кошо аласыз.",
    surahFallbackNumberedTemplate = "{n}-сүрө",

    settingsTitle = "Жөндөөлөр", languagePickerSubtitleTemplate = "{n} тилдин арасынан тандаңыз",
    reciterLabel = "Кураачы", translationLabel = "Котормо",
    notificationsToggleTitle = "Эскертүүлөрдү күйгүзүү", notificationsToggleSubtitle = "Намаз убактысы киргенде эскертүү жиберүү",
    locationLabel = "Жайгашкан жер", locationAutoUpdateNote = "Жайгашкан жер сакталгандан кийин эскертүүлөр автоматтык жаңыртылат.",
    supportUsTitle = "Бизди колдоңуз", adFreeActiveMessage = "Жарнаксыз мүчөлүгүңүз активдүү, рахмат! 💚",
    supportUsMessage = "Колдонмону өнүктүрүүгө жардам бериңиз", smallDonationButton = "☕ Кичине белек",
    mediumDonationButton = "🎁 Орто белек", sixMonthAdFreeButton = "✨ 6 айлык жарнаксыз мүчөлүк",
    themeSystem = "🖥️ Система", themeLight = "☀️ Жарык", themeDark = "🌙 Караңгы",

    editionSearchPlaceholder = "Издөө",
    selectTranslationTitle = "Котормону тандаңыз", selectLocationTitle = "Жайгашкан жерди тандаңыз", selectLanguageTitle = "Тилди тандаңыз",

    searchTitle = "Кураандан издөө", searchAyahPlaceholder = "Аяттардын ичинен сөз издеңиз...",
    searchIdleHint = "Кураани Каримдин котормосунан издөө үчүн жаза баштаңыз.",
    searchErrorFull = "Издөө ишке ашкан жок. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",
    searchNoResultsTemplate = "\"{query}\" үчүн натыйжа табылган жок.",

    recitersCountSubtitleTemplate = "{n} кураачы • угуп тандаңыз",
    searchReciterPlaceholder = "Кураачы издөө...",
    recitersLoadErrorFull = "Кураачылар тизмеси жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",
    selectedReciterLabel = "Тандалган кураачы", tapToSelectLabel = "Тандоо үчүн басыңыз",

    juzSingular = "Жуз", pageSingular = "Барак", pagePlural = "Барактар",
    manzilSingular = "Манзил", manzilPlural = "Манзилдер", rukuSingular = "Руку", rukuPlural = "Рукулар",
    hizbQuarterSingular = "Хизб чейрек", hizbQuarterPlural = "Хизб чейректери",
    sectionLoadErrorTemplate = "{title} жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",

    sajdaTitle = "Сажда аяттары", sajdaSubtitle = "Кураанда тиловат саждасын талап кылган аяттар",
    sajdaLoadErrorFull = "Сажда аяттары жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз.",

    nowPlayingTitle = "Азыр ойнотулууда", wholeSurahSuffix = "Бүткүл сүрө", playbackSpeedTitle = "Ойнотуу ылдамдыгы",

    onboardingSkip = "Өткөрүп жиберүү", onboardingNext = "Кийинки", onboardingStart = "Баштоо",
    onboardTitle1 = "Huda Qur'an'га кош келиңиз",
    onboardDesc1 = "Кураани Каримди дайыма, каалаган жерде, чөнтөгүңүздө алып жүрүңүз.",
    onboardTitle2 = "Үн менен угуу",
    onboardDesc2 = "Аят-аят же сүрө-сүрө, тандаган кураачыңыздан таза үн менен угуңуз.",
    onboardTitle3 = "Котормолор",
    onboardDesc3 = "Түрк жана башка тилдердеги котормолор менен аяттарды түшүнүп окуңуз.",
    onboardTitle4 = "Тандалмаларыңыз, токтогон жериңиз",
    onboardDesc4 = "Жаккан аяттарыңызды сактаңыз, окууну токтогон жериңизден улантыңыз.",

    appTagline = "Кураани Карим, ар дайым жаныңызда",

    lessonsSubtitleTemplate = "Элип-Байдан тажвидге чейин, ырааттуу {n} сабак", lessonNotFound = "Сабак табылган жок",
    lessonFallback = "Сабак", pronounceContentDescription = "Айтылышын угуу",

    cancelLabel = "Жокко чыгаруу", goLabel = "Өтүү", dismissLabel = "Жабуу",
    mushafJumpToPageTitle = "Баракка өтүү", mushafJumpToPageHint = "Барак номери (1-604)",
    mushafLandscapeHintText = "Чыныгы китептей эки баракты бирге окуу үчүн телефонуңузду жанбаштата буруңуз",
    mushafResumeSubtitleTemplate = "{n}-барактан токтогонсуз, улантыңыз",
    mushafStartSubtitle = "Чыныгы китептей, барактап окуңуз",

    khatmProgressTitle = "Хатм жүрүшүңүз",
    khatmProgressTemplate = "{page}-барак / {total} (%{percent})",
    khatmCompletedCountTemplate = "Аяктаган хатмдар: {n}",

    qiblaTitle = "Кыбыла Компасы",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Телефонуңуздун компасын ачып, бул бурчту түндүккө карай тегиздеңиз.",
    qiblaLocationUnavailable = "Жайгашкан жериңизди аныктоо мүмкүн болбоду. Жөндөөлөрдө шаар/өлкө тандалганына ынаныңыз.",
    cdBack = "Артка", cdPlay = "Ойнотуу", cdPause = "Тыныгуу", cdClose = "Жабуу", cdRepeat = "Кайталоо", cdPrevious = "Мурунку", cdNext = "Кийинки",
    tafsirLabel = "Тафсир", selectTafsirTitle = "Тафсирди тандаңыз", changeTafsirLabel = "Тафсирди өзгөртүү", tafsirUnavailable = "Бул аят үчүн тафсир жүктөлгөн жок.", tafsirNotSelected = "Тафсир тандалган жок",
    qiblaLiveInstructions = "Телефонуңузду түз кармап, жебе тегеректин үстүндөгү үч бурчтук менен дал келгенче жай бурулуңуз.",
    tafsirVsMealHint = "Котормо аяттын маанисин берет. Тафсир болсо анын мазмунун жана маанисин кеңири түшүндүрөт.",
    textSizeLabel = "Тексттин өлчөмү", textSizeHint = "Окууга кыйналсаңыз, текстти чоңойтуңуз.", textSizeNormal = "Кадимки", textSizeLarge = "Чоң", textSizeExtraLarge = "Өтө чоң",
)

private val turkmen = Strings(
    navHome = "Baş sahypa", navSurahs = "Suralar", navFavorites = "Halanýanlar", navSettings = "Sazlamalar",
    homeGreeting = "Essalamu aleýkim", search = "Gözleg", juz = "Juz", reciters = "Okyjylar",
    retry = "Gaýtadan synanyş", appearance = "Görnüş", recitationAndTranslation = "Okalyş we terjime",
    prayerNotifications = "Namaz duýduryşlary", language = "Dil",

    ayahWord = "Aýat", ayahWordLower = "aýat", meccan = "Mekgeli", medinan = "Medineli",
    serverUnreachable = "Serwere baglanyp bolmady, gaýtadan synanyşyň.", notFound = "Netije tapylmady",
    viewAll = "Ählisini gör",

    homeSubtitle = "Gününizi Kuran bilen başlaň", continueReading = "Galan ýeriňizden dowam ediň",
    dailyAyahTitle = "Günüň aýaty", dailyAyahError = "Aýat ýüklenmedi, gaýtadan synanyşyň.",
    readingLessonsTitle = "Kuran okaýyş sapaklary",
    readingLessonsSubtitleTemplate = "Elif-Baýdan tejwide çenli, sesli {n} yzygiderli sapak",
    esmaulHusnaTitle = "Allanyň 99 owadan ady", discoverQuranTitle = "Kurany öwreniň",
    mushafModeLabel = "Mushaf Režimi", toggleTranslationLabel = "Terjimäni Görkez/Gizle",
    pagesLabel = "Sahypalar", manzilsLabel = "Manzillar", rukusLabel = "Rukular",
    hizbQuartersLabel = "Hizb çärýekleri", sajdaVersesLabel = "Sejde aýatlary",
    quranStatsTitle = "Kurany Kerimiň statistikasy", statSurah = "Sura", statJuz = "Juz",
    statPage = "Sahypa", statRuku = "Ruku", statHizbQuarter = "Hizb çärýek", statManzil = "Manzil",
    statSajda = "Sejde", featuredSurahsTitle = "Saýlanan suralar", surahsLoadErrorShort = "Suralar ýüklenmedi.",
    nextPrayerLabel = "Indiki namaz", hoursMinutesLeftTemplate = "{h} sag {m} min galdy",
    minutesLeftTemplate = "{m} min galdy", favoriteLabel = "Halanýan",

    surahSearchPlaceholder = "Sura gözle (ady ýa-da sany)",
    surahsLoadErrorFull = "Suralar ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň.",

    surahFallback = "Sura",
    surahLoadErrorFull = "Sura ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň.",

    myFavoritesTitle = "Halanýanlarym",
    favoritesEmptyMessage = "Entek halanýan aýatyňyz ýok. Okaýan wagtyňyz ⭐ belligine basyp, şu ýere goşup bilersiňiz.",
    surahFallbackNumberedTemplate = "{n}-sura",

    settingsTitle = "Sazlamalar", languagePickerSubtitleTemplate = "{n} diliň arasyndan saýlaň",
    reciterLabel = "Okyjy", translationLabel = "Terjime",
    notificationsToggleTitle = "Duýduryşlary aç", notificationsToggleSubtitle = "Namaz wagty gireninde duýduryş iber",
    locationLabel = "Ýerleşiş", locationAutoUpdateNote = "Ýerleşiş ýatda saklanandan soň duýduryşlar awtomatik täzelenýär.",
    supportUsTitle = "Bize goldaw beriň", adFreeActiveMessage = "Mahabatsyz agzalygyňyz işjeň, sag boluň! 💚",
    supportUsMessage = "Programmany ösdürmäge kömek ediň", smallDonationButton = "☕ Kiçi sowgat",
    mediumDonationButton = "🎁 Orta sowgat", sixMonthAdFreeButton = "✨ 6 aýlyk mahabatsyz agzalyk",
    themeSystem = "🖥️ Ulgam", themeLight = "☀️ Açyk", themeDark = "🌙 Garaňky",

    editionSearchPlaceholder = "Gözle",
    selectTranslationTitle = "Terjimäni saýlaň", selectLocationTitle = "Ýerleşişi saýlaň", selectLanguageTitle = "Dili saýlaň",

    searchTitle = "Kurandan gözle", searchAyahPlaceholder = "Aýatlaryň içinden söz gözläň...",
    searchIdleHint = "Kurany Kerimiň terjimesinden gözlemek üçin ýazyp başlaň.",
    searchErrorFull = "Gözleg başa barmady. Serwere baglanyp bolmady, gaýtadan synanyşyň.",
    searchNoResultsTemplate = "\"{query}\" üçin netije tapylmady.",

    recitersCountSubtitleTemplate = "{n} okyjy • diňläp saýlaň",
    searchReciterPlaceholder = "Okyjy gözle...",
    recitersLoadErrorFull = "Okyjylar sanawy ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň.",
    selectedReciterLabel = "Saýlanan okyjy", tapToSelectLabel = "Saýlamak üçin basyň",

    juzSingular = "Juz", pageSingular = "Sahypa", pagePlural = "Sahypalar",
    manzilSingular = "Manzil", manzilPlural = "Manzillar", rukuSingular = "Ruku", rukuPlural = "Rukular",
    hizbQuarterSingular = "Hizb çärýek", hizbQuarterPlural = "Hizb çärýekleri",
    sectionLoadErrorTemplate = "{title} ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň.",

    sajdaTitle = "Sejde aýatlary", sajdaSubtitle = "Kuranda tilawat sejdesini talap edýän aýatlar",
    sajdaLoadErrorFull = "Sejde aýatlary ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň.",

    nowPlayingTitle = "Häzir oýnadylýar", wholeSurahSuffix = "Bütin sura", playbackSpeedTitle = "Oýnatma tizligi",

    onboardingSkip = "Geç", onboardingNext = "Indiki", onboardingStart = "Başla",
    onboardTitle1 = "Huda Qur'an'a hoş geldiňiz",
    onboardDesc1 = "Kurany Kerimi hemişe, her ýerde, jübiňizde göteriň.",
    onboardTitle2 = "Sesli diňleme",
    onboardDesc2 = "Aýat-aýat ýa-da sura-sura, saýlan okyjyňyzdan akgytly diňläň.",
    onboardTitle3 = "Terjimeler",
    onboardDesc3 = "Türkçe we beýleki dillerdäki terjimeler bilen aýatlary düşünip okaň.",
    onboardTitle4 = "Halanýanlaryňyz, galan ýeriňiz",
    onboardDesc4 = "Halan aýatlaryňyzy ýatda saklaň, okamagy galan ýeriňizden dowam ediň.",

    appTagline = "Kurany Kerim, hemişe ýanyňyzda",

    lessonsSubtitleTemplate = "Elif-Baýdan tejwide çenli, yzygiderli {n} sapak", lessonNotFound = "Sapak tapylmady",
    lessonFallback = "Sapak", pronounceContentDescription = "Aýdylyşyny diňle",

    cancelLabel = "Ýatyr", goLabel = "Git", dismissLabel = "Ýap",
    mushafJumpToPageTitle = "Sahypa git", mushafJumpToPageHint = "Sahypa belgisi (1-604)",
    mushafLandscapeHintText = "Hakyky kitap ýaly iki sahypany bilelikde okamak üçin telefonyňyzy ýan tarap öwrüň",
    mushafResumeSubtitleTemplate = "{n}-sahypada galdyňyz, dowam ediň",
    mushafStartSubtitle = "Hakyky kitap ýaly, sahypama-sahypa okaň",

    khatmProgressTitle = "Hatmiňiziň dowamy",
    khatmProgressTemplate = "{page}-sahypa / {total} (%{percent})",
    khatmCompletedCountTemplate = "Tamamlanan hatmlar: {n}",

    qiblaTitle = "Kybla Kompasy",
    qiblaBearingTemplate = "{degree}° ({cardinal})",
    qiblaInstructions = "Telefonyňyzyň kompasyny açyp, bu burçy demirgazyga görä deňleşdiriň.",
    qiblaLocationUnavailable = "Ýerleşiş maglumaty alynyp bilinmedi. Sazlamalarda şäher/ýurt saýlanandygyna göz ýetiriň.",
    cdBack = "Yza", cdPlay = "Ýerine ýetir", cdPause = "Duruzmak", cdClose = "Ýapmak", cdRepeat = "Gaýtalamak", cdPrevious = "Öňki", cdNext = "Indiki",
    tafsirLabel = "Tefsir", selectTafsirTitle = "Tefsir saýlaň", changeTafsirLabel = "Tefsiri üýtget", tafsirUnavailable = "Bu aýat üçin tefsir ýüklenip bilinmedi.", tafsirNotSelected = "Tefsir saýlanmady",
    qiblaLiveInstructions = "Telefonyňyzy tekiz saklaň we ok, tegeleg üstündäki üçburçluk bilen deňeşýänçä ýuwaş öwrüň.",
    tafsirVsMealHint = "Terjime aýatyň manysyny berýär. Tefsir bolsa onuň manysynyň we mazmunynyň jikme-jik düşündirişidir.",
    textSizeLabel = "Ýazy ölçegi", textSizeHint = "Okamakda kynçylyk çekseňiz, ýazyny ulaldyň.", textSizeNormal = "Adaty", textSizeLarge = "Uly", textSizeExtraLarge = "Has uly",
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

val LocalStrings = staticCompositionLocalOf { turkish }

fun Strings.sectionSingular(kind: SectionKind): String = when (kind) {
    SectionKind.JUZ -> juzSingular
    SectionKind.PAGE -> pageSingular
    SectionKind.MANZIL -> manzilSingular
    SectionKind.RUKU -> rukuSingular
    SectionKind.HIZB_QUARTER -> hizbQuarterSingular
}

fun Strings.sectionPlural(kind: SectionKind): String = when (kind) {
    SectionKind.JUZ -> juz
    SectionKind.PAGE -> pagePlural
    SectionKind.MANZIL -> manzilPlural
    SectionKind.RUKU -> rukuPlural
    SectionKind.HIZB_QUARTER -> hizbQuarterPlural
}
