package org.ferdidrgn.hudaquran.ui.localization

import androidx.compose.runtime.staticCompositionLocalOf
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.domain.model.SectionKind

/**
 * Localization bundles use a fixed, type-safe value table instead of one constructor parameter
 * per string. This keeps the public property API intact while staying below the JVM method
 * descriptor limit on Android host tests.
 */
private enum class StringId {
    navHome,
    navSurahs,
    navFavorites,
    navSettings,
    homeGreeting,
    search,
    juz,
    reciters,
    retry,
    appearance,
    recitationAndTranslation,
    prayerNotifications,
    language,
    ayahWord,
    ayahWordLower,
    meccan,
    medinan,
    serverUnreachable,
    notFound,
    viewAll,
    homeSubtitle,
    continueReading,
    dailyAyahTitle,
    dailyAyahError,
    readingLessonsTitle,
    readingLessonsSubtitleTemplate,
    quickActionsTitle,
    esmaulHusnaTitle,
    discoverQuranTitle,
    mushafModeLabel,
    mushafViewLabel,
    readingViewLabel,
    decreaseTextSizeLabel,
    increaseTextSizeLabel,
    toggleTranslationLabel,
    pagesLabel,
    manzilsLabel,
    rukusLabel,
    hizbQuartersLabel,
    sajdaVersesLabel,
    quranStatsTitle,
    statSurah,
    statJuz,
    statPage,
    statRuku,
    statHizbQuarter,
    statManzil,
    statSajda,
    featuredSurahsTitle,
    surahsLoadErrorShort,
    nextPrayerLabel,
    hoursMinutesLeftTemplate,
    minutesLeftTemplate,
    favoriteLabel,
    todayTitle,
    todayHadithTitle,
    todayDuaTitle,
    todayOpenHadith,
    todayOpenDua,
    todayNoContent,
    surahSearchPlaceholder,
    surahsLoadErrorFull,
    surahFallback,
    surahLoadErrorFull,
    myFavoritesTitle,
    favoritesEmptyMessage,
    surahFallbackNumberedTemplate,
    settingsTitle,
    languagePickerSubtitleTemplate,
    reciterLabel,
    translationLabel,
    notificationsToggleTitle,
    notificationsToggleSubtitle,
    locationLabel,
    locationAutoUpdateNote,
    supportUsTitle,
    adFreeActiveMessage,
    supportUsMessage,
    smallDonationButton,
    mediumDonationButton,
    sixMonthAdFreeButton,
    themeSystem,
    themeLight,
    themeDark,
    themeSakura,
    editionSearchPlaceholder,
    selectTranslationTitle,
    selectLocationTitle,
    selectLanguageTitle,
    searchTitle,
    searchAyahPlaceholder,
    searchIdleHint,
    searchErrorFull,
    searchNoResultsTemplate,
    recitersCountSubtitleTemplate,
    searchReciterPlaceholder,
    recitersLoadErrorFull,
    selectedReciterLabel,
    tapToSelectLabel,
    juzSingular,
    pageSingular,
    pagePlural,
    manzilSingular,
    manzilPlural,
    rukuSingular,
    rukuPlural,
    hizbQuarterSingular,
    hizbQuarterPlural,
    sectionLoadErrorTemplate,
    sajdaTitle,
    sajdaSubtitle,
    sajdaLoadErrorFull,
    nowPlayingTitle,
    wholeSurahSuffix,
    playbackSpeedTitle,
    onboardingSkip,
    onboardingNext,
    onboardingStart,
    onboardTitle1,
    onboardDesc1,
    onboardTitle2,
    onboardDesc2,
    onboardTitle3,
    onboardDesc3,
    onboardTitle4,
    onboardDesc4,
    appTagline,
    lessonsSubtitleTemplate,
    lessonNotFound,
    lessonFallback,
    pronounceContentDescription,
    cancelLabel,
    goLabel,
    mushafJumpToPageTitle,
    mushafJumpToPageHint,
    mushafResumeSubtitleTemplate,
    mushafStartSubtitle,
    khatmProgressTitle,
    khatmProgressTemplate,
    khatmCompletedCountTemplate,
    qiblaTitle,
    qiblaBearingTemplate,
    qiblaInstructions,
    qiblaLocationUnavailable,
    cdBack,
    cdPlay,
    cdPause,
    cdClose,
    cdRepeat,
    cdPrevious,
    cdNext,
    tafsirLabel,
    selectTafsirTitle,
    changeTafsirLabel,
    tafsirUnavailable,
    tafsirNotSelected,
    qiblaLiveInstructions,
    tafsirVsMealHint,
    textSizeLabel,
    textSizeHint,
    textSizeNormal,
    textSizeLarge,
    textSizeExtraLarge,
    elderlyProfileTitle,
    elderlyProfileSubtitle,
    highContrastTitle,
    highContrastSubtitle,
    largeTouchTargetsTitle,
    largeTouchTargetsSubtitle,
    reducedMotionTitle,
    reducedMotionSubtitle,
    zakatCalculatorTitle,
    zakatWealthLabel,
    zakatCalculateButton,
    zakatResultTemplate,
    zakatNisabInfo,
    duaListTitle,
    postPrayerDhikrTitle,
    postPrayerDhikrSubtitle,
    postPrayerDhikrReset,
    postPrayerDhikrStepTemplate,
    postPrayerDhikrCountTemplate,
    postPrayerDhikrComplete,
    postPrayerDhikrNext,
    islamicCalendarTitle,
    islamicDaysRemainingTemplate,
    streakDaysTemplate,
    cdShare,
    hacKuraLabel,
    moreTitle,
    filterAll,
    zakatLiveValuesTitle,
    zakatGoldPriceLabel,
    zakatNisabTemplate,
    zakatPriceDateTemplate,
    zakatLivePriceUnavailable,
    zakatGoldPriceInputLabel,
    zakatCashLabel,
    zakatGoldGramsLabel,
    zakatOtherAssetsLabel,
    zakatTradeGoodsLabel,
    zakatDebtsLabel,
    zakatNetWealthTemplate,
    zakatBelowNisab,
    zakatCurrencyLabel,
    fitreTitle,
    fitreAmountTemplate,
    fitreSourceNote,
    fitrePersonsLabel,
    fitreTotalTemplate,
    fidyeDaysLabel,
    fidyeTotalTemplate,
    fitreOtherCurrencyNote,
    restorePurchasesButton,
    purchaseSuccessMessage,
    purchaseCancelledMessage,
    purchaseUnavailableMessage,
    purchaseErrorMessage,
    restoreNothingMessage,
    adFreeUntilTemplate,
    prayerSoonTitleTemplate,
    prayerSoonBody,
    prayerNowTitleTemplate,
    prayerNowBody,
    occasionTwoDaysTemplate,
    occasionTomorrowTemplate,
    occasionTomorrowNightTemplate,
    occasionBody,
    prayerReminderLeadLabel,
    reminderOffLabel,
    minutesShortTemplate,
    prayerAtTimeLabel,
    occasionRemindersLabel,
    notificationSoundHint,
    themePickerTitle,
    themePickerSubtitle,
    themeChangeLaterNote,
    themeIntroTitle,
    themeIntroBody,
    supportRateApp,
    supportRateAppHint,
    supportShareApp,
    supportShareAppHint,
    shareAppMessage,
    supportFeedback,
    supportFeedbackHint,
    supportPrivacyPolicy,
    supportPrivacyPolicyHint,
    supportTermsOfService,
    supportTermsOfServiceHint,
    fitreZakatRowTitle,
    fitreZakatRowHint,
    mushafNewSurahTemplate,
    mushafStopAction,
    mushafSafetyStopTemplate,
    mushafContinueAction,
    mushafPageLoadFailed,
    mushafGestureHint,
    mushafGoToPlayingPage,
    mushafKeyboardHint,
    mushafCopiedMessage,
    mushafFavoriteAdded,
    mushafFavoriteRemoved,
    mushafUndoAction,
    mushafAyahActionsTitleTemplate,
    mushafPlayFromHere,
    mushafCopyAction,
    mushafAddFavorite,
    mushafRemoveFavorite,
    shareCardTitle,
    shareImageAction,
    shareTextAction,
    shareCopyLink,
    shareLinkCopied,
    shareImageFailed,
    qiblaSensorNoSensor,
    qiblaSensorLowAccuracy,
    qiblaSensorReady,
    qiblaCalibrationAction,
    qiblaCalibrationHideAction,
    qiblaCalibrationInstructions,
    offlineContentTitle,
    offlineContentSubtitle,
    offlineDownloadAction,
    offlineResumeAction,
    offlinePauseAction,
    offlineRetryAction,
    offlineDeleteAction,
    offlineDownloadedLabel,
    offlineDownloadingLabel,
    offlinePausedLabel,
    offlinePartialLabel,
    offlineCompleteLabel,
    offlineIdleLabel,
    offlineFailedItemsTemplate,
    offlineProgressTemplate,
    offlineCurrentItemTemplate,
    offlineSelectionTemplate,
    offlineAudioCatalogued,
    offlineErrorHint,
}

/**
 * Immutable localization bundle with a small JVM constructor.
 *
 * The public property API remains unchanged (for example, strings.navHome).
 * Values are stored in a fixed, type-safe table so the JVM never has to emit a
 * 296-argument constructor or synthetic method signature.
 */
class Strings internal constructor(private val values: Array<String>) {
    val navHome: String get() = values[StringId.navHome.ordinal]
    val navSurahs: String get() = values[StringId.navSurahs.ordinal]
    val navFavorites: String get() = values[StringId.navFavorites.ordinal]
    val navSettings: String get() = values[StringId.navSettings.ordinal]
    val homeGreeting: String get() = values[StringId.homeGreeting.ordinal]
    val search: String get() = values[StringId.search.ordinal]
    val juz: String get() = values[StringId.juz.ordinal]
    val reciters: String get() = values[StringId.reciters.ordinal]
    val retry: String get() = values[StringId.retry.ordinal]
    val appearance: String get() = values[StringId.appearance.ordinal]
    val recitationAndTranslation: String get() = values[StringId.recitationAndTranslation.ordinal]
    val prayerNotifications: String get() = values[StringId.prayerNotifications.ordinal]
    val language: String get() = values[StringId.language.ordinal]
    val ayahWord: String get() = values[StringId.ayahWord.ordinal]
    val ayahWordLower: String get() = values[StringId.ayahWordLower.ordinal]
    val meccan: String get() = values[StringId.meccan.ordinal]
    val medinan: String get() = values[StringId.medinan.ordinal]
    val serverUnreachable: String get() = values[StringId.serverUnreachable.ordinal]
    val notFound: String get() = values[StringId.notFound.ordinal]
    val viewAll: String get() = values[StringId.viewAll.ordinal]
    val homeSubtitle: String get() = values[StringId.homeSubtitle.ordinal]
    val continueReading: String get() = values[StringId.continueReading.ordinal]
    val dailyAyahTitle: String get() = values[StringId.dailyAyahTitle.ordinal]
    val dailyAyahError: String get() = values[StringId.dailyAyahError.ordinal]
    val readingLessonsTitle: String get() = values[StringId.readingLessonsTitle.ordinal]
    val readingLessonsSubtitleTemplate: String get() = values[StringId.readingLessonsSubtitleTemplate.ordinal]
    val quickActionsTitle: String get() = values[StringId.quickActionsTitle.ordinal]
    val esmaulHusnaTitle: String get() = values[StringId.esmaulHusnaTitle.ordinal]
    val discoverQuranTitle: String get() = values[StringId.discoverQuranTitle.ordinal]
    val mushafModeLabel: String get() = values[StringId.mushafModeLabel.ordinal]
    val mushafViewLabel: String get() = values[StringId.mushafViewLabel.ordinal]
    val readingViewLabel: String get() = values[StringId.readingViewLabel.ordinal]
    val decreaseTextSizeLabel: String get() = values[StringId.decreaseTextSizeLabel.ordinal]
    val increaseTextSizeLabel: String get() = values[StringId.increaseTextSizeLabel.ordinal]
    val toggleTranslationLabel: String get() = values[StringId.toggleTranslationLabel.ordinal]
    val pagesLabel: String get() = values[StringId.pagesLabel.ordinal]
    val manzilsLabel: String get() = values[StringId.manzilsLabel.ordinal]
    val rukusLabel: String get() = values[StringId.rukusLabel.ordinal]
    val hizbQuartersLabel: String get() = values[StringId.hizbQuartersLabel.ordinal]
    val sajdaVersesLabel: String get() = values[StringId.sajdaVersesLabel.ordinal]
    val quranStatsTitle: String get() = values[StringId.quranStatsTitle.ordinal]
    val statSurah: String get() = values[StringId.statSurah.ordinal]
    val statJuz: String get() = values[StringId.statJuz.ordinal]
    val statPage: String get() = values[StringId.statPage.ordinal]
    val statRuku: String get() = values[StringId.statRuku.ordinal]
    val statHizbQuarter: String get() = values[StringId.statHizbQuarter.ordinal]
    val statManzil: String get() = values[StringId.statManzil.ordinal]
    val statSajda: String get() = values[StringId.statSajda.ordinal]
    val featuredSurahsTitle: String get() = values[StringId.featuredSurahsTitle.ordinal]
    val surahsLoadErrorShort: String get() = values[StringId.surahsLoadErrorShort.ordinal]
    val nextPrayerLabel: String get() = values[StringId.nextPrayerLabel.ordinal]
    val hoursMinutesLeftTemplate: String get() = values[StringId.hoursMinutesLeftTemplate.ordinal]
    val minutesLeftTemplate: String get() = values[StringId.minutesLeftTemplate.ordinal]
    val favoriteLabel: String get() = values[StringId.favoriteLabel.ordinal]
    val todayTitle: String get() = values[StringId.todayTitle.ordinal]
    val todayHadithTitle: String get() = values[StringId.todayHadithTitle.ordinal]
    val todayDuaTitle: String get() = values[StringId.todayDuaTitle.ordinal]
    val todayOpenHadith: String get() = values[StringId.todayOpenHadith.ordinal]
    val todayOpenDua: String get() = values[StringId.todayOpenDua.ordinal]
    val todayNoContent: String get() = values[StringId.todayNoContent.ordinal]
    val surahSearchPlaceholder: String get() = values[StringId.surahSearchPlaceholder.ordinal]
    val surahsLoadErrorFull: String get() = values[StringId.surahsLoadErrorFull.ordinal]
    val surahFallback: String get() = values[StringId.surahFallback.ordinal]
    val surahLoadErrorFull: String get() = values[StringId.surahLoadErrorFull.ordinal]
    val myFavoritesTitle: String get() = values[StringId.myFavoritesTitle.ordinal]
    val favoritesEmptyMessage: String get() = values[StringId.favoritesEmptyMessage.ordinal]
    val surahFallbackNumberedTemplate: String get() = values[StringId.surahFallbackNumberedTemplate.ordinal]
    val settingsTitle: String get() = values[StringId.settingsTitle.ordinal]
    val languagePickerSubtitleTemplate: String get() = values[StringId.languagePickerSubtitleTemplate.ordinal]
    val reciterLabel: String get() = values[StringId.reciterLabel.ordinal]
    val translationLabel: String get() = values[StringId.translationLabel.ordinal]
    val notificationsToggleTitle: String get() = values[StringId.notificationsToggleTitle.ordinal]
    val notificationsToggleSubtitle: String get() = values[StringId.notificationsToggleSubtitle.ordinal]
    val locationLabel: String get() = values[StringId.locationLabel.ordinal]
    val locationAutoUpdateNote: String get() = values[StringId.locationAutoUpdateNote.ordinal]
    val supportUsTitle: String get() = values[StringId.supportUsTitle.ordinal]
    val adFreeActiveMessage: String get() = values[StringId.adFreeActiveMessage.ordinal]
    val supportUsMessage: String get() = values[StringId.supportUsMessage.ordinal]
    val smallDonationButton: String get() = values[StringId.smallDonationButton.ordinal]
    val mediumDonationButton: String get() = values[StringId.mediumDonationButton.ordinal]
    val sixMonthAdFreeButton: String get() = values[StringId.sixMonthAdFreeButton.ordinal]
    val themeSystem: String get() = values[StringId.themeSystem.ordinal]
    val themeLight: String get() = values[StringId.themeLight.ordinal]
    val themeDark: String get() = values[StringId.themeDark.ordinal]
    val themeSakura: String get() = values[StringId.themeSakura.ordinal]
    val editionSearchPlaceholder: String get() = values[StringId.editionSearchPlaceholder.ordinal]
    val selectTranslationTitle: String get() = values[StringId.selectTranslationTitle.ordinal]
    val selectLocationTitle: String get() = values[StringId.selectLocationTitle.ordinal]
    val selectLanguageTitle: String get() = values[StringId.selectLanguageTitle.ordinal]
    val searchTitle: String get() = values[StringId.searchTitle.ordinal]
    val searchAyahPlaceholder: String get() = values[StringId.searchAyahPlaceholder.ordinal]
    val searchIdleHint: String get() = values[StringId.searchIdleHint.ordinal]
    val searchErrorFull: String get() = values[StringId.searchErrorFull.ordinal]
    val searchNoResultsTemplate: String get() = values[StringId.searchNoResultsTemplate.ordinal]
    val recitersCountSubtitleTemplate: String get() = values[StringId.recitersCountSubtitleTemplate.ordinal]
    val searchReciterPlaceholder: String get() = values[StringId.searchReciterPlaceholder.ordinal]
    val recitersLoadErrorFull: String get() = values[StringId.recitersLoadErrorFull.ordinal]
    val selectedReciterLabel: String get() = values[StringId.selectedReciterLabel.ordinal]
    val tapToSelectLabel: String get() = values[StringId.tapToSelectLabel.ordinal]
    val juzSingular: String get() = values[StringId.juzSingular.ordinal]
    val pageSingular: String get() = values[StringId.pageSingular.ordinal]
    val pagePlural: String get() = values[StringId.pagePlural.ordinal]
    val manzilSingular: String get() = values[StringId.manzilSingular.ordinal]
    val manzilPlural: String get() = values[StringId.manzilPlural.ordinal]
    val rukuSingular: String get() = values[StringId.rukuSingular.ordinal]
    val rukuPlural: String get() = values[StringId.rukuPlural.ordinal]
    val hizbQuarterSingular: String get() = values[StringId.hizbQuarterSingular.ordinal]
    val hizbQuarterPlural: String get() = values[StringId.hizbQuarterPlural.ordinal]
    val sectionLoadErrorTemplate: String get() = values[StringId.sectionLoadErrorTemplate.ordinal]
    val sajdaTitle: String get() = values[StringId.sajdaTitle.ordinal]
    val sajdaSubtitle: String get() = values[StringId.sajdaSubtitle.ordinal]
    val sajdaLoadErrorFull: String get() = values[StringId.sajdaLoadErrorFull.ordinal]
    val nowPlayingTitle: String get() = values[StringId.nowPlayingTitle.ordinal]
    val wholeSurahSuffix: String get() = values[StringId.wholeSurahSuffix.ordinal]
    val playbackSpeedTitle: String get() = values[StringId.playbackSpeedTitle.ordinal]
    val onboardingSkip: String get() = values[StringId.onboardingSkip.ordinal]
    val onboardingNext: String get() = values[StringId.onboardingNext.ordinal]
    val onboardingStart: String get() = values[StringId.onboardingStart.ordinal]
    val onboardTitle1: String get() = values[StringId.onboardTitle1.ordinal]
    val onboardDesc1: String get() = values[StringId.onboardDesc1.ordinal]
    val onboardTitle2: String get() = values[StringId.onboardTitle2.ordinal]
    val onboardDesc2: String get() = values[StringId.onboardDesc2.ordinal]
    val onboardTitle3: String get() = values[StringId.onboardTitle3.ordinal]
    val onboardDesc3: String get() = values[StringId.onboardDesc3.ordinal]
    val onboardTitle4: String get() = values[StringId.onboardTitle4.ordinal]
    val onboardDesc4: String get() = values[StringId.onboardDesc4.ordinal]
    val appTagline: String get() = values[StringId.appTagline.ordinal]
    val lessonsSubtitleTemplate: String get() = values[StringId.lessonsSubtitleTemplate.ordinal]
    val lessonNotFound: String get() = values[StringId.lessonNotFound.ordinal]
    val lessonFallback: String get() = values[StringId.lessonFallback.ordinal]
    val pronounceContentDescription: String get() = values[StringId.pronounceContentDescription.ordinal]
    val cancelLabel: String get() = values[StringId.cancelLabel.ordinal]
    val goLabel: String get() = values[StringId.goLabel.ordinal]
    val mushafJumpToPageTitle: String get() = values[StringId.mushafJumpToPageTitle.ordinal]
    val mushafJumpToPageHint: String get() = values[StringId.mushafJumpToPageHint.ordinal]
    val mushafResumeSubtitleTemplate: String get() = values[StringId.mushafResumeSubtitleTemplate.ordinal]
    val mushafStartSubtitle: String get() = values[StringId.mushafStartSubtitle.ordinal]
    val khatmProgressTitle: String get() = values[StringId.khatmProgressTitle.ordinal]
    val khatmProgressTemplate: String get() = values[StringId.khatmProgressTemplate.ordinal]
    val khatmCompletedCountTemplate: String get() = values[StringId.khatmCompletedCountTemplate.ordinal]
    val qiblaTitle: String get() = values[StringId.qiblaTitle.ordinal]
    val qiblaBearingTemplate: String get() = values[StringId.qiblaBearingTemplate.ordinal]
    val qiblaInstructions: String get() = values[StringId.qiblaInstructions.ordinal]
    val qiblaLocationUnavailable: String get() = values[StringId.qiblaLocationUnavailable.ordinal]
    val cdBack: String get() = values[StringId.cdBack.ordinal]
    val cdPlay: String get() = values[StringId.cdPlay.ordinal]
    val cdPause: String get() = values[StringId.cdPause.ordinal]
    val cdClose: String get() = values[StringId.cdClose.ordinal]
    val cdRepeat: String get() = values[StringId.cdRepeat.ordinal]
    val cdPrevious: String get() = values[StringId.cdPrevious.ordinal]
    val cdNext: String get() = values[StringId.cdNext.ordinal]
    val tafsirLabel: String get() = values[StringId.tafsirLabel.ordinal]
    val selectTafsirTitle: String get() = values[StringId.selectTafsirTitle.ordinal]
    val changeTafsirLabel: String get() = values[StringId.changeTafsirLabel.ordinal]
    val tafsirUnavailable: String get() = values[StringId.tafsirUnavailable.ordinal]
    val tafsirNotSelected: String get() = values[StringId.tafsirNotSelected.ordinal]
    val qiblaLiveInstructions: String get() = values[StringId.qiblaLiveInstructions.ordinal]
    val tafsirVsMealHint: String get() = values[StringId.tafsirVsMealHint.ordinal]
    val textSizeLabel: String get() = values[StringId.textSizeLabel.ordinal]
    val textSizeHint: String get() = values[StringId.textSizeHint.ordinal]
    val textSizeNormal: String get() = values[StringId.textSizeNormal.ordinal]
    val textSizeLarge: String get() = values[StringId.textSizeLarge.ordinal]
    val textSizeExtraLarge: String get() = values[StringId.textSizeExtraLarge.ordinal]
    val elderlyProfileTitle: String get() = values[StringId.elderlyProfileTitle.ordinal]
    val elderlyProfileSubtitle: String get() = values[StringId.elderlyProfileSubtitle.ordinal]
    val highContrastTitle: String get() = values[StringId.highContrastTitle.ordinal]
    val highContrastSubtitle: String get() = values[StringId.highContrastSubtitle.ordinal]
    val largeTouchTargetsTitle: String get() = values[StringId.largeTouchTargetsTitle.ordinal]
    val largeTouchTargetsSubtitle: String get() = values[StringId.largeTouchTargetsSubtitle.ordinal]
    val reducedMotionTitle: String get() = values[StringId.reducedMotionTitle.ordinal]
    val reducedMotionSubtitle: String get() = values[StringId.reducedMotionSubtitle.ordinal]
    val zakatCalculatorTitle: String get() = values[StringId.zakatCalculatorTitle.ordinal]
    val zakatWealthLabel: String get() = values[StringId.zakatWealthLabel.ordinal]
    val zakatCalculateButton: String get() = values[StringId.zakatCalculateButton.ordinal]
    val zakatResultTemplate: String get() = values[StringId.zakatResultTemplate.ordinal]
    val zakatNisabInfo: String get() = values[StringId.zakatNisabInfo.ordinal]
    val duaListTitle: String get() = values[StringId.duaListTitle.ordinal]
    val postPrayerDhikrTitle: String get() = values[StringId.postPrayerDhikrTitle.ordinal]
    val postPrayerDhikrSubtitle: String get() = values[StringId.postPrayerDhikrSubtitle.ordinal]
    val postPrayerDhikrReset: String get() = values[StringId.postPrayerDhikrReset.ordinal]
    val postPrayerDhikrStepTemplate: String get() = values[StringId.postPrayerDhikrStepTemplate.ordinal]
    val postPrayerDhikrCountTemplate: String get() = values[StringId.postPrayerDhikrCountTemplate.ordinal]
    val postPrayerDhikrComplete: String get() = values[StringId.postPrayerDhikrComplete.ordinal]
    val postPrayerDhikrNext: String get() = values[StringId.postPrayerDhikrNext.ordinal]
    val islamicCalendarTitle: String get() = values[StringId.islamicCalendarTitle.ordinal]
    val islamicDaysRemainingTemplate: String get() = values[StringId.islamicDaysRemainingTemplate.ordinal]
    val streakDaysTemplate: String get() = values[StringId.streakDaysTemplate.ordinal]
    val cdShare: String get() = values[StringId.cdShare.ordinal]
    val hacKuraLabel: String get() = values[StringId.hacKuraLabel.ordinal]
    val moreTitle: String get() = values[StringId.moreTitle.ordinal]
    val filterAll: String get() = values[StringId.filterAll.ordinal]
    val zakatLiveValuesTitle: String get() = values[StringId.zakatLiveValuesTitle.ordinal]
    val zakatGoldPriceLabel: String get() = values[StringId.zakatGoldPriceLabel.ordinal]
    val zakatNisabTemplate: String get() = values[StringId.zakatNisabTemplate.ordinal]
    val zakatPriceDateTemplate: String get() = values[StringId.zakatPriceDateTemplate.ordinal]
    val zakatLivePriceUnavailable: String get() = values[StringId.zakatLivePriceUnavailable.ordinal]
    val zakatGoldPriceInputLabel: String get() = values[StringId.zakatGoldPriceInputLabel.ordinal]
    val zakatCashLabel: String get() = values[StringId.zakatCashLabel.ordinal]
    val zakatGoldGramsLabel: String get() = values[StringId.zakatGoldGramsLabel.ordinal]
    val zakatOtherAssetsLabel: String get() = values[StringId.zakatOtherAssetsLabel.ordinal]
    val zakatTradeGoodsLabel: String get() = values[StringId.zakatTradeGoodsLabel.ordinal]
    val zakatDebtsLabel: String get() = values[StringId.zakatDebtsLabel.ordinal]
    val zakatNetWealthTemplate: String get() = values[StringId.zakatNetWealthTemplate.ordinal]
    val zakatBelowNisab: String get() = values[StringId.zakatBelowNisab.ordinal]
    val zakatCurrencyLabel: String get() = values[StringId.zakatCurrencyLabel.ordinal]
    val fitreTitle: String get() = values[StringId.fitreTitle.ordinal]
    val fitreAmountTemplate: String get() = values[StringId.fitreAmountTemplate.ordinal]
    val fitreSourceNote: String get() = values[StringId.fitreSourceNote.ordinal]
    val fitrePersonsLabel: String get() = values[StringId.fitrePersonsLabel.ordinal]
    val fitreTotalTemplate: String get() = values[StringId.fitreTotalTemplate.ordinal]
    val fidyeDaysLabel: String get() = values[StringId.fidyeDaysLabel.ordinal]
    val fidyeTotalTemplate: String get() = values[StringId.fidyeTotalTemplate.ordinal]
    val fitreOtherCurrencyNote: String get() = values[StringId.fitreOtherCurrencyNote.ordinal]
    val restorePurchasesButton: String get() = values[StringId.restorePurchasesButton.ordinal]
    val purchaseSuccessMessage: String get() = values[StringId.purchaseSuccessMessage.ordinal]
    val purchaseCancelledMessage: String get() = values[StringId.purchaseCancelledMessage.ordinal]
    val purchaseUnavailableMessage: String get() = values[StringId.purchaseUnavailableMessage.ordinal]
    val purchaseErrorMessage: String get() = values[StringId.purchaseErrorMessage.ordinal]
    val restoreNothingMessage: String get() = values[StringId.restoreNothingMessage.ordinal]
    val adFreeUntilTemplate: String get() = values[StringId.adFreeUntilTemplate.ordinal]
    val prayerSoonTitleTemplate: String get() = values[StringId.prayerSoonTitleTemplate.ordinal]
    val prayerSoonBody: String get() = values[StringId.prayerSoonBody.ordinal]
    val prayerNowTitleTemplate: String get() = values[StringId.prayerNowTitleTemplate.ordinal]
    val prayerNowBody: String get() = values[StringId.prayerNowBody.ordinal]
    val occasionTwoDaysTemplate: String get() = values[StringId.occasionTwoDaysTemplate.ordinal]
    val occasionTomorrowTemplate: String get() = values[StringId.occasionTomorrowTemplate.ordinal]
    val occasionTomorrowNightTemplate: String get() = values[StringId.occasionTomorrowNightTemplate.ordinal]
    val occasionBody: String get() = values[StringId.occasionBody.ordinal]
    val prayerReminderLeadLabel: String get() = values[StringId.prayerReminderLeadLabel.ordinal]
    val reminderOffLabel: String get() = values[StringId.reminderOffLabel.ordinal]
    val minutesShortTemplate: String get() = values[StringId.minutesShortTemplate.ordinal]
    val prayerAtTimeLabel: String get() = values[StringId.prayerAtTimeLabel.ordinal]
    val occasionRemindersLabel: String get() = values[StringId.occasionRemindersLabel.ordinal]
    val notificationSoundHint: String get() = values[StringId.notificationSoundHint.ordinal]
    val themePickerTitle: String get() = values[StringId.themePickerTitle.ordinal]
    val themePickerSubtitle: String get() = values[StringId.themePickerSubtitle.ordinal]
    val themeChangeLaterNote: String get() = values[StringId.themeChangeLaterNote.ordinal]
    val themeIntroTitle: String get() = values[StringId.themeIntroTitle.ordinal]
    val themeIntroBody: String get() = values[StringId.themeIntroBody.ordinal]
    val supportRateApp: String get() = values[StringId.supportRateApp.ordinal]
    val supportRateAppHint: String get() = values[StringId.supportRateAppHint.ordinal]
    val supportShareApp: String get() = values[StringId.supportShareApp.ordinal]
    val supportShareAppHint: String get() = values[StringId.supportShareAppHint.ordinal]
    val shareAppMessage: String get() = values[StringId.shareAppMessage.ordinal]
    val supportFeedback: String get() = values[StringId.supportFeedback.ordinal]
    val supportFeedbackHint: String get() = values[StringId.supportFeedbackHint.ordinal]
    val supportPrivacyPolicy: String get() = values[StringId.supportPrivacyPolicy.ordinal]
    val supportPrivacyPolicyHint: String get() = values[StringId.supportPrivacyPolicyHint.ordinal]
    val supportTermsOfService: String get() = values[StringId.supportTermsOfService.ordinal]
    val supportTermsOfServiceHint: String get() = values[StringId.supportTermsOfServiceHint.ordinal]
    val fitreZakatRowTitle: String get() = values[StringId.fitreZakatRowTitle.ordinal]
    val fitreZakatRowHint: String get() = values[StringId.fitreZakatRowHint.ordinal]
    val mushafNewSurahTemplate: String get() = values[StringId.mushafNewSurahTemplate.ordinal]
    val mushafStopAction: String get() = values[StringId.mushafStopAction.ordinal]
    val mushafSafetyStopTemplate: String get() = values[StringId.mushafSafetyStopTemplate.ordinal]
    val mushafContinueAction: String get() = values[StringId.mushafContinueAction.ordinal]
    val mushafPageLoadFailed: String get() = values[StringId.mushafPageLoadFailed.ordinal]
    val mushafGestureHint: String get() = values[StringId.mushafGestureHint.ordinal]
    val mushafGoToPlayingPage: String get() = values[StringId.mushafGoToPlayingPage.ordinal]
    val mushafKeyboardHint: String get() = values[StringId.mushafKeyboardHint.ordinal]
    val mushafCopiedMessage: String get() = values[StringId.mushafCopiedMessage.ordinal]
    val mushafFavoriteAdded: String get() = values[StringId.mushafFavoriteAdded.ordinal]
    val mushafFavoriteRemoved: String get() = values[StringId.mushafFavoriteRemoved.ordinal]
    val mushafUndoAction: String get() = values[StringId.mushafUndoAction.ordinal]
    val mushafAyahActionsTitleTemplate: String get() = values[StringId.mushafAyahActionsTitleTemplate.ordinal]
    val mushafPlayFromHere: String get() = values[StringId.mushafPlayFromHere.ordinal]
    val mushafCopyAction: String get() = values[StringId.mushafCopyAction.ordinal]
    val mushafAddFavorite: String get() = values[StringId.mushafAddFavorite.ordinal]
    val mushafRemoveFavorite: String get() = values[StringId.mushafRemoveFavorite.ordinal]
    val shareCardTitle: String get() = values[StringId.shareCardTitle.ordinal]
    val shareImageAction: String get() = values[StringId.shareImageAction.ordinal]
    val shareTextAction: String get() = values[StringId.shareTextAction.ordinal]
    val shareCopyLink: String get() = values[StringId.shareCopyLink.ordinal]
    val shareLinkCopied: String get() = values[StringId.shareLinkCopied.ordinal]
    val shareImageFailed: String get() = values[StringId.shareImageFailed.ordinal]
    val qiblaSensorNoSensor: String get() = values[StringId.qiblaSensorNoSensor.ordinal]
    val qiblaSensorLowAccuracy: String get() = values[StringId.qiblaSensorLowAccuracy.ordinal]
    val qiblaSensorReady: String get() = values[StringId.qiblaSensorReady.ordinal]
    val qiblaCalibrationAction: String get() = values[StringId.qiblaCalibrationAction.ordinal]
    val qiblaCalibrationHideAction: String get() = values[StringId.qiblaCalibrationHideAction.ordinal]
    val qiblaCalibrationInstructions: String get() = values[StringId.qiblaCalibrationInstructions.ordinal]
    val offlineContentTitle: String get() = values[StringId.offlineContentTitle.ordinal]
    val offlineContentSubtitle: String get() = values[StringId.offlineContentSubtitle.ordinal]
    val offlineDownloadAction: String get() = values[StringId.offlineDownloadAction.ordinal]
    val offlineResumeAction: String get() = values[StringId.offlineResumeAction.ordinal]
    val offlinePauseAction: String get() = values[StringId.offlinePauseAction.ordinal]
    val offlineRetryAction: String get() = values[StringId.offlineRetryAction.ordinal]
    val offlineDeleteAction: String get() = values[StringId.offlineDeleteAction.ordinal]
    val offlineDownloadedLabel: String get() = values[StringId.offlineDownloadedLabel.ordinal]
    val offlineDownloadingLabel: String get() = values[StringId.offlineDownloadingLabel.ordinal]
    val offlinePausedLabel: String get() = values[StringId.offlinePausedLabel.ordinal]
    val offlinePartialLabel: String get() = values[StringId.offlinePartialLabel.ordinal]
    val offlineCompleteLabel: String get() = values[StringId.offlineCompleteLabel.ordinal]
    val offlineIdleLabel: String get() = values[StringId.offlineIdleLabel.ordinal]
    val offlineFailedItemsTemplate: String get() = values[StringId.offlineFailedItemsTemplate.ordinal]
    val offlineProgressTemplate: String get() = values[StringId.offlineProgressTemplate.ordinal]
    val offlineCurrentItemTemplate: String get() = values[StringId.offlineCurrentItemTemplate.ordinal]
    val offlineSelectionTemplate: String get() = values[StringId.offlineSelectionTemplate.ordinal]
    val offlineAudioCatalogued: String get() = values[StringId.offlineAudioCatalogued.ordinal]
    val offlineErrorHint: String get() = values[StringId.offlineErrorHint.ordinal]
}

private class StringsBuilder {
    var navHome: String? = null
    var navSurahs: String? = null
    var navFavorites: String? = null
    var navSettings: String? = null
    var homeGreeting: String? = null
    var search: String? = null
    var juz: String? = null
    var reciters: String? = null
    var retry: String? = null
    var appearance: String? = null
    var recitationAndTranslation: String? = null
    var prayerNotifications: String? = null
    var language: String? = null
    var ayahWord: String? = null
    var ayahWordLower: String? = null
    var meccan: String? = null
    var medinan: String? = null
    var serverUnreachable: String? = null
    var notFound: String? = null
    var viewAll: String? = null
    var homeSubtitle: String? = null
    var continueReading: String? = null
    var dailyAyahTitle: String? = null
    var dailyAyahError: String? = null
    var readingLessonsTitle: String? = null
    var readingLessonsSubtitleTemplate: String? = null
    var quickActionsTitle: String? = null
    var esmaulHusnaTitle: String? = null
    var discoverQuranTitle: String? = null
    var mushafModeLabel: String? = null
    var mushafViewLabel: String? = null
    var readingViewLabel: String? = null
    var decreaseTextSizeLabel: String? = null
    var increaseTextSizeLabel: String? = null
    var toggleTranslationLabel: String? = null
    var pagesLabel: String? = null
    var manzilsLabel: String? = null
    var rukusLabel: String? = null
    var hizbQuartersLabel: String? = null
    var sajdaVersesLabel: String? = null
    var quranStatsTitle: String? = null
    var statSurah: String? = null
    var statJuz: String? = null
    var statPage: String? = null
    var statRuku: String? = null
    var statHizbQuarter: String? = null
    var statManzil: String? = null
    var statSajda: String? = null
    var featuredSurahsTitle: String? = null
    var surahsLoadErrorShort: String? = null
    var nextPrayerLabel: String? = null
    var hoursMinutesLeftTemplate: String? = null
    var minutesLeftTemplate: String? = null
    var favoriteLabel: String? = null
    var todayTitle: String? = "Bugün"
    var todayHadithTitle: String? = "Günün hadisi"
    var todayDuaTitle: String? = "Günün duası"
    var todayOpenHadith: String? = "Hadisleri aç"
    var todayOpenDua: String? = "Duaları aç"
    var todayNoContent: String? = "İçerik şu an yüklenemedi"
    var surahSearchPlaceholder: String? = null
    var surahsLoadErrorFull: String? = null
    var surahFallback: String? = null
    var surahLoadErrorFull: String? = null
    var myFavoritesTitle: String? = null
    var favoritesEmptyMessage: String? = null
    var surahFallbackNumberedTemplate: String? = null
    var settingsTitle: String? = null
    var languagePickerSubtitleTemplate: String? = null
    var reciterLabel: String? = null
    var translationLabel: String? = null
    var notificationsToggleTitle: String? = null
    var notificationsToggleSubtitle: String? = null
    var locationLabel: String? = null
    var locationAutoUpdateNote: String? = null
    var supportUsTitle: String? = null
    var adFreeActiveMessage: String? = null
    var supportUsMessage: String? = null
    var smallDonationButton: String? = null
    var mediumDonationButton: String? = null
    var sixMonthAdFreeButton: String? = null
    var themeSystem: String? = null
    var themeLight: String? = null
    var themeDark: String? = null
    var themeSakura: String? = null
    var editionSearchPlaceholder: String? = null
    var selectTranslationTitle: String? = null
    var selectLocationTitle: String? = null
    var selectLanguageTitle: String? = null
    var searchTitle: String? = null
    var searchAyahPlaceholder: String? = null
    var searchIdleHint: String? = null
    var searchErrorFull: String? = null
    var searchNoResultsTemplate: String? = null
    var recitersCountSubtitleTemplate: String? = null
    var searchReciterPlaceholder: String? = null
    var recitersLoadErrorFull: String? = null
    var selectedReciterLabel: String? = null
    var tapToSelectLabel: String? = null
    var juzSingular: String? = null
    var pageSingular: String? = null
    var pagePlural: String? = null
    var manzilSingular: String? = null
    var manzilPlural: String? = null
    var rukuSingular: String? = null
    var rukuPlural: String? = null
    var hizbQuarterSingular: String? = null
    var hizbQuarterPlural: String? = null
    var sectionLoadErrorTemplate: String? = null
    var sajdaTitle: String? = null
    var sajdaSubtitle: String? = null
    var sajdaLoadErrorFull: String? = null
    var nowPlayingTitle: String? = null
    var wholeSurahSuffix: String? = null
    var playbackSpeedTitle: String? = null
    var onboardingSkip: String? = null
    var onboardingNext: String? = null
    var onboardingStart: String? = null
    var onboardTitle1: String? = null
    var onboardDesc1: String? = null
    var onboardTitle2: String? = null
    var onboardDesc2: String? = null
    var onboardTitle3: String? = null
    var onboardDesc3: String? = null
    var onboardTitle4: String? = null
    var onboardDesc4: String? = null
    var appTagline: String? = null
    var lessonsSubtitleTemplate: String? = null
    var lessonNotFound: String? = null
    var lessonFallback: String? = null
    var pronounceContentDescription: String? = null
    var cancelLabel: String? = null
    var goLabel: String? = null
    var mushafJumpToPageTitle: String? = null
    var mushafJumpToPageHint: String? = null
    var mushafResumeSubtitleTemplate: String? = null
    var mushafStartSubtitle: String? = null
    var khatmProgressTitle: String? = null
    var khatmProgressTemplate: String? = null
    var khatmCompletedCountTemplate: String? = null
    var qiblaTitle: String? = null
    var qiblaBearingTemplate: String? = null
    var qiblaInstructions: String? = null
    var qiblaLocationUnavailable: String? = null
    var cdBack: String? = null
    var cdPlay: String? = null
    var cdPause: String? = null
    var cdClose: String? = null
    var cdRepeat: String? = null
    var cdPrevious: String? = null
    var cdNext: String? = null
    var tafsirLabel: String? = null
    var selectTafsirTitle: String? = null
    var changeTafsirLabel: String? = null
    var tafsirUnavailable: String? = null
    var tafsirNotSelected: String? = null
    var qiblaLiveInstructions: String? = null
    var tafsirVsMealHint: String? = null
    var textSizeLabel: String? = null
    var textSizeHint: String? = null
    var textSizeNormal: String? = null
    var textSizeLarge: String? = null
    var textSizeExtraLarge: String? = null
    var elderlyProfileTitle: String? = null
    var elderlyProfileSubtitle: String? = null
    var highContrastTitle: String? = null
    var highContrastSubtitle: String? = null
    var largeTouchTargetsTitle: String? = null
    var largeTouchTargetsSubtitle: String? = null
    var reducedMotionTitle: String? = null
    var reducedMotionSubtitle: String? = null
    var zakatCalculatorTitle: String? = null
    var zakatWealthLabel: String? = null
    var zakatCalculateButton: String? = null
    var zakatResultTemplate: String? = null
    var zakatNisabInfo: String? = null
    var duaListTitle: String? = null
    var postPrayerDhikrTitle: String? = null
    var postPrayerDhikrSubtitle: String? = null
    var postPrayerDhikrReset: String? = null
    var postPrayerDhikrStepTemplate: String? = null
    var postPrayerDhikrCountTemplate: String? = null
    var postPrayerDhikrComplete: String? = null
    var postPrayerDhikrNext: String? = null
    var islamicCalendarTitle: String? = null
    var islamicDaysRemainingTemplate: String? = null
    var streakDaysTemplate: String? = null
    var cdShare: String? = null
    var hacKuraLabel: String? = null
    var moreTitle: String? = null
    var filterAll: String? = null
    var zakatLiveValuesTitle: String? = null
    var zakatGoldPriceLabel: String? = null
    var zakatNisabTemplate: String? = null
    var zakatPriceDateTemplate: String? = null
    var zakatLivePriceUnavailable: String? = null
    var zakatGoldPriceInputLabel: String? = null
    var zakatCashLabel: String? = null
    var zakatGoldGramsLabel: String? = null
    var zakatOtherAssetsLabel: String? = null
    var zakatTradeGoodsLabel: String? = null
    var zakatDebtsLabel: String? = null
    var zakatNetWealthTemplate: String? = null
    var zakatBelowNisab: String? = null
    var zakatCurrencyLabel: String? = null
    var fitreTitle: String? = null
    var fitreAmountTemplate: String? = null
    var fitreSourceNote: String? = null
    var fitrePersonsLabel: String? = null
    var fitreTotalTemplate: String? = null
    var fidyeDaysLabel: String? = null
    var fidyeTotalTemplate: String? = null
    var fitreOtherCurrencyNote: String? = null
    var restorePurchasesButton: String? = null
    var purchaseSuccessMessage: String? = null
    var purchaseCancelledMessage: String? = null
    var purchaseUnavailableMessage: String? = null
    var purchaseErrorMessage: String? = null
    var restoreNothingMessage: String? = null
    var adFreeUntilTemplate: String? = null
    var prayerSoonTitleTemplate: String? = null
    var prayerSoonBody: String? = null
    var prayerNowTitleTemplate: String? = null
    var prayerNowBody: String? = null
    var occasionTwoDaysTemplate: String? = null
    var occasionTomorrowTemplate: String? = null
    var occasionTomorrowNightTemplate: String? = null
    var occasionBody: String? = null
    var prayerReminderLeadLabel: String? = null
    var reminderOffLabel: String? = null
    var minutesShortTemplate: String? = null
    var prayerAtTimeLabel: String? = null
    var occasionRemindersLabel: String? = null
    var notificationSoundHint: String? = null
    var themePickerTitle: String? = null
    var themePickerSubtitle: String? = null
    var themeChangeLaterNote: String? = null
    var themeIntroTitle: String? = null
    var themeIntroBody: String? = null
    var supportRateApp: String? = null
    var supportRateAppHint: String? = null
    var supportShareApp: String? = null
    var supportShareAppHint: String? = null
    var shareAppMessage: String? = null
    var supportFeedback: String? = null
    var supportFeedbackHint: String? = null
    var supportPrivacyPolicy: String? = null
    var supportPrivacyPolicyHint: String? = null
    var supportTermsOfService: String? = null
    var supportTermsOfServiceHint: String? = null
    var fitreZakatRowTitle: String? = null
    var fitreZakatRowHint: String? = null
    var mushafNewSurahTemplate: String? = null
    var mushafStopAction: String? = null
    var mushafSafetyStopTemplate: String? = null
    var mushafContinueAction: String? = null
    var mushafPageLoadFailed: String? = null
    var mushafGestureHint: String? = null
    var mushafGoToPlayingPage: String? = null
    var mushafKeyboardHint: String? = null
    var mushafCopiedMessage: String? = null
    var mushafFavoriteAdded: String? = null
    var mushafFavoriteRemoved: String? = null
    var mushafUndoAction: String? = null
    var mushafAyahActionsTitleTemplate: String? = null
    var mushafPlayFromHere: String? = null
    var mushafCopyAction: String? = null
    var mushafAddFavorite: String? = null
    var mushafRemoveFavorite: String? = null
    var shareCardTitle: String? = null
    var shareImageAction: String? = null
    var shareTextAction: String? = null
    var shareCopyLink: String? = null
    var shareLinkCopied: String? = null
    var shareImageFailed: String? = null
    var qiblaSensorNoSensor: String? = "Pusula sensörü bulunamadı"
    var qiblaSensorLowAccuracy: String? = "Pusula doğruluğu düşük"
    var qiblaSensorReady: String? = "Pusula doğruluğu normal"
    var qiblaCalibrationAction: String? = "Kalibrasyon rehberini göster"
    var qiblaCalibrationHideAction: String? = "Kalibrasyon rehberini gizle"
    var qiblaCalibrationInstructions: String? = "Telefonu düz tutun ve havada yavaşça 8 çizer gibi hareket ettirin. Ardından telefonu yeniden düz tutup birkaç saniye bekleyin."
    var offlineContentTitle: String? = "Çevrim dışı içerik"
    var offlineContentSubtitle: String? = "Kur'an metni ve seçtiğiniz tefsiri internet olmadan okuyun."
    var offlineDownloadAction: String? = "İçeriği indir"
    var offlineResumeAction: String? = "Devam et"
    var offlinePauseAction: String? = "Duraklat"
    var offlineRetryAction: String? = "Yeniden dene"
    var offlineDeleteAction: String? = "İndirilenleri sil"
    var offlineDownloadedLabel: String? = "İndirildi"
    var offlineDownloadingLabel: String? = "İndiriliyor"
    var offlinePausedLabel: String? = "Duraklatıldı"
    var offlinePartialLabel: String? = "Kısmen hazır"
    var offlineCompleteLabel: String? = "Çevrim dışı kullanıma hazır"
    var offlineIdleLabel: String? = "Henüz indirme yapılmadı"
    var offlineFailedItemsTemplate: String? = "{n} içerik indirilemedi"
    var offlineProgressTemplate: String? = "{done} / {total} içerik"
    var offlineCurrentItemTemplate: String? = "Şu an: {item}"
    var offlineSelectionTemplate: String? = "Meal: {translation} • Tefsir: {tafsir}"
    var offlineAudioCatalogued: String? = "Ses bağlantıları kataloglandı; ses dosyaları cihazda byte olarak saklanmıyor."
    var offlineErrorHint: String? = "Bağlantıyı kontrol edip yeniden deneyin. Başarılı içerikler korunur."

    fun build(): Strings = Strings(
        arrayOf(
            navHome ?: error("Missing localization string: navHome"),
            navSurahs ?: error("Missing localization string: navSurahs"),
            navFavorites ?: error("Missing localization string: navFavorites"),
            navSettings ?: error("Missing localization string: navSettings"),
            homeGreeting ?: error("Missing localization string: homeGreeting"),
            search ?: error("Missing localization string: search"),
            juz ?: error("Missing localization string: juz"),
            reciters ?: error("Missing localization string: reciters"),
            retry ?: error("Missing localization string: retry"),
            appearance ?: error("Missing localization string: appearance"),
            recitationAndTranslation ?: error("Missing localization string: recitationAndTranslation"),
            prayerNotifications ?: error("Missing localization string: prayerNotifications"),
            language ?: error("Missing localization string: language"),
            ayahWord ?: error("Missing localization string: ayahWord"),
            ayahWordLower ?: error("Missing localization string: ayahWordLower"),
            meccan ?: error("Missing localization string: meccan"),
            medinan ?: error("Missing localization string: medinan"),
            serverUnreachable ?: error("Missing localization string: serverUnreachable"),
            notFound ?: error("Missing localization string: notFound"),
            viewAll ?: error("Missing localization string: viewAll"),
            homeSubtitle ?: error("Missing localization string: homeSubtitle"),
            continueReading ?: error("Missing localization string: continueReading"),
            dailyAyahTitle ?: error("Missing localization string: dailyAyahTitle"),
            dailyAyahError ?: error("Missing localization string: dailyAyahError"),
            readingLessonsTitle ?: error("Missing localization string: readingLessonsTitle"),
            readingLessonsSubtitleTemplate ?: error("Missing localization string: readingLessonsSubtitleTemplate"),
            quickActionsTitle ?: error("Missing localization string: quickActionsTitle"),
            esmaulHusnaTitle ?: error("Missing localization string: esmaulHusnaTitle"),
            discoverQuranTitle ?: error("Missing localization string: discoverQuranTitle"),
            mushafModeLabel ?: error("Missing localization string: mushafModeLabel"),
            mushafViewLabel ?: mushafModeLabel ?: error("Missing localization string: mushafViewLabel"),
            readingViewLabel ?: readingLessonsTitle ?: error("Missing localization string: readingViewLabel"),
            decreaseTextSizeLabel ?: textSizeLabel?.let { "$it −" } ?: error("Missing localization string: decreaseTextSizeLabel"),
            increaseTextSizeLabel ?: textSizeLabel?.let { "$it +" } ?: error("Missing localization string: increaseTextSizeLabel"),
            toggleTranslationLabel ?: error("Missing localization string: toggleTranslationLabel"),
            pagesLabel ?: error("Missing localization string: pagesLabel"),
            manzilsLabel ?: error("Missing localization string: manzilsLabel"),
            rukusLabel ?: error("Missing localization string: rukusLabel"),
            hizbQuartersLabel ?: error("Missing localization string: hizbQuartersLabel"),
            sajdaVersesLabel ?: error("Missing localization string: sajdaVersesLabel"),
            quranStatsTitle ?: error("Missing localization string: quranStatsTitle"),
            statSurah ?: error("Missing localization string: statSurah"),
            statJuz ?: error("Missing localization string: statJuz"),
            statPage ?: error("Missing localization string: statPage"),
            statRuku ?: error("Missing localization string: statRuku"),
            statHizbQuarter ?: error("Missing localization string: statHizbQuarter"),
            statManzil ?: error("Missing localization string: statManzil"),
            statSajda ?: error("Missing localization string: statSajda"),
            featuredSurahsTitle ?: error("Missing localization string: featuredSurahsTitle"),
            surahsLoadErrorShort ?: error("Missing localization string: surahsLoadErrorShort"),
            nextPrayerLabel ?: error("Missing localization string: nextPrayerLabel"),
            hoursMinutesLeftTemplate ?: error("Missing localization string: hoursMinutesLeftTemplate"),
            minutesLeftTemplate ?: error("Missing localization string: minutesLeftTemplate"),
            favoriteLabel ?: error("Missing localization string: favoriteLabel"),
            todayTitle ?: error("Missing localization string: todayTitle"),
            todayHadithTitle ?: error("Missing localization string: todayHadithTitle"),
            todayDuaTitle ?: error("Missing localization string: todayDuaTitle"),
            todayOpenHadith ?: error("Missing localization string: todayOpenHadith"),
            todayOpenDua ?: error("Missing localization string: todayOpenDua"),
            todayNoContent ?: error("Missing localization string: todayNoContent"),
            surahSearchPlaceholder ?: error("Missing localization string: surahSearchPlaceholder"),
            surahsLoadErrorFull ?: error("Missing localization string: surahsLoadErrorFull"),
            surahFallback ?: error("Missing localization string: surahFallback"),
            surahLoadErrorFull ?: error("Missing localization string: surahLoadErrorFull"),
            myFavoritesTitle ?: error("Missing localization string: myFavoritesTitle"),
            favoritesEmptyMessage ?: error("Missing localization string: favoritesEmptyMessage"),
            surahFallbackNumberedTemplate ?: error("Missing localization string: surahFallbackNumberedTemplate"),
            settingsTitle ?: error("Missing localization string: settingsTitle"),
            languagePickerSubtitleTemplate ?: error("Missing localization string: languagePickerSubtitleTemplate"),
            reciterLabel ?: error("Missing localization string: reciterLabel"),
            translationLabel ?: error("Missing localization string: translationLabel"),
            notificationsToggleTitle ?: error("Missing localization string: notificationsToggleTitle"),
            notificationsToggleSubtitle ?: error("Missing localization string: notificationsToggleSubtitle"),
            locationLabel ?: error("Missing localization string: locationLabel"),
            locationAutoUpdateNote ?: error("Missing localization string: locationAutoUpdateNote"),
            supportUsTitle ?: error("Missing localization string: supportUsTitle"),
            adFreeActiveMessage ?: error("Missing localization string: adFreeActiveMessage"),
            supportUsMessage ?: error("Missing localization string: supportUsMessage"),
            smallDonationButton ?: error("Missing localization string: smallDonationButton"),
            mediumDonationButton ?: error("Missing localization string: mediumDonationButton"),
            sixMonthAdFreeButton ?: error("Missing localization string: sixMonthAdFreeButton"),
            themeSystem ?: error("Missing localization string: themeSystem"),
            themeLight ?: error("Missing localization string: themeLight"),
            themeDark ?: error("Missing localization string: themeDark"),
            themeSakura ?: error("Missing localization string: themeSakura"),
            editionSearchPlaceholder ?: error("Missing localization string: editionSearchPlaceholder"),
            selectTranslationTitle ?: error("Missing localization string: selectTranslationTitle"),
            selectLocationTitle ?: error("Missing localization string: selectLocationTitle"),
            selectLanguageTitle ?: error("Missing localization string: selectLanguageTitle"),
            searchTitle ?: error("Missing localization string: searchTitle"),
            searchAyahPlaceholder ?: error("Missing localization string: searchAyahPlaceholder"),
            searchIdleHint ?: error("Missing localization string: searchIdleHint"),
            searchErrorFull ?: error("Missing localization string: searchErrorFull"),
            searchNoResultsTemplate ?: error("Missing localization string: searchNoResultsTemplate"),
            recitersCountSubtitleTemplate ?: error("Missing localization string: recitersCountSubtitleTemplate"),
            searchReciterPlaceholder ?: error("Missing localization string: searchReciterPlaceholder"),
            recitersLoadErrorFull ?: error("Missing localization string: recitersLoadErrorFull"),
            selectedReciterLabel ?: error("Missing localization string: selectedReciterLabel"),
            tapToSelectLabel ?: error("Missing localization string: tapToSelectLabel"),
            juzSingular ?: error("Missing localization string: juzSingular"),
            pageSingular ?: error("Missing localization string: pageSingular"),
            pagePlural ?: error("Missing localization string: pagePlural"),
            manzilSingular ?: error("Missing localization string: manzilSingular"),
            manzilPlural ?: error("Missing localization string: manzilPlural"),
            rukuSingular ?: error("Missing localization string: rukuSingular"),
            rukuPlural ?: error("Missing localization string: rukuPlural"),
            hizbQuarterSingular ?: error("Missing localization string: hizbQuarterSingular"),
            hizbQuarterPlural ?: error("Missing localization string: hizbQuarterPlural"),
            sectionLoadErrorTemplate ?: error("Missing localization string: sectionLoadErrorTemplate"),
            sajdaTitle ?: error("Missing localization string: sajdaTitle"),
            sajdaSubtitle ?: error("Missing localization string: sajdaSubtitle"),
            sajdaLoadErrorFull ?: error("Missing localization string: sajdaLoadErrorFull"),
            nowPlayingTitle ?: error("Missing localization string: nowPlayingTitle"),
            wholeSurahSuffix ?: error("Missing localization string: wholeSurahSuffix"),
            playbackSpeedTitle ?: error("Missing localization string: playbackSpeedTitle"),
            onboardingSkip ?: error("Missing localization string: onboardingSkip"),
            onboardingNext ?: error("Missing localization string: onboardingNext"),
            onboardingStart ?: error("Missing localization string: onboardingStart"),
            onboardTitle1 ?: error("Missing localization string: onboardTitle1"),
            onboardDesc1 ?: error("Missing localization string: onboardDesc1"),
            onboardTitle2 ?: error("Missing localization string: onboardTitle2"),
            onboardDesc2 ?: error("Missing localization string: onboardDesc2"),
            onboardTitle3 ?: error("Missing localization string: onboardTitle3"),
            onboardDesc3 ?: error("Missing localization string: onboardDesc3"),
            onboardTitle4 ?: error("Missing localization string: onboardTitle4"),
            onboardDesc4 ?: error("Missing localization string: onboardDesc4"),
            appTagline ?: error("Missing localization string: appTagline"),
            lessonsSubtitleTemplate ?: error("Missing localization string: lessonsSubtitleTemplate"),
            lessonNotFound ?: error("Missing localization string: lessonNotFound"),
            lessonFallback ?: error("Missing localization string: lessonFallback"),
            pronounceContentDescription ?: error("Missing localization string: pronounceContentDescription"),
            cancelLabel ?: error("Missing localization string: cancelLabel"),
            goLabel ?: error("Missing localization string: goLabel"),
            mushafJumpToPageTitle ?: error("Missing localization string: mushafJumpToPageTitle"),
            mushafJumpToPageHint ?: error("Missing localization string: mushafJumpToPageHint"),
            mushafResumeSubtitleTemplate ?: error("Missing localization string: mushafResumeSubtitleTemplate"),
            mushafStartSubtitle ?: error("Missing localization string: mushafStartSubtitle"),
            khatmProgressTitle ?: error("Missing localization string: khatmProgressTitle"),
            khatmProgressTemplate ?: error("Missing localization string: khatmProgressTemplate"),
            khatmCompletedCountTemplate ?: error("Missing localization string: khatmCompletedCountTemplate"),
            qiblaTitle ?: error("Missing localization string: qiblaTitle"),
            qiblaBearingTemplate ?: error("Missing localization string: qiblaBearingTemplate"),
            qiblaInstructions ?: error("Missing localization string: qiblaInstructions"),
            qiblaLocationUnavailable ?: error("Missing localization string: qiblaLocationUnavailable"),
            cdBack ?: error("Missing localization string: cdBack"),
            cdPlay ?: error("Missing localization string: cdPlay"),
            cdPause ?: error("Missing localization string: cdPause"),
            cdClose ?: error("Missing localization string: cdClose"),
            cdRepeat ?: error("Missing localization string: cdRepeat"),
            cdPrevious ?: error("Missing localization string: cdPrevious"),
            cdNext ?: error("Missing localization string: cdNext"),
            tafsirLabel ?: error("Missing localization string: tafsirLabel"),
            selectTafsirTitle ?: error("Missing localization string: selectTafsirTitle"),
            changeTafsirLabel ?: error("Missing localization string: changeTafsirLabel"),
            tafsirUnavailable ?: error("Missing localization string: tafsirUnavailable"),
            tafsirNotSelected ?: error("Missing localization string: tafsirNotSelected"),
            qiblaLiveInstructions ?: error("Missing localization string: qiblaLiveInstructions"),
            tafsirVsMealHint ?: error("Missing localization string: tafsirVsMealHint"),
            textSizeLabel ?: error("Missing localization string: textSizeLabel"),
            textSizeHint ?: error("Missing localization string: textSizeHint"),
            textSizeNormal ?: error("Missing localization string: textSizeNormal"),
            textSizeLarge ?: error("Missing localization string: textSizeLarge"),
            textSizeExtraLarge ?: error("Missing localization string: textSizeExtraLarge"),
            elderlyProfileTitle ?: error("Missing localization string: elderlyProfileTitle"),
            elderlyProfileSubtitle ?: error("Missing localization string: elderlyProfileSubtitle"),
            highContrastTitle ?: error("Missing localization string: highContrastTitle"),
            highContrastSubtitle ?: error("Missing localization string: highContrastSubtitle"),
            largeTouchTargetsTitle ?: error("Missing localization string: largeTouchTargetsTitle"),
            largeTouchTargetsSubtitle ?: error("Missing localization string: largeTouchTargetsSubtitle"),
            reducedMotionTitle ?: error("Missing localization string: reducedMotionTitle"),
            reducedMotionSubtitle ?: error("Missing localization string: reducedMotionSubtitle"),
            zakatCalculatorTitle ?: error("Missing localization string: zakatCalculatorTitle"),
            zakatWealthLabel ?: error("Missing localization string: zakatWealthLabel"),
            zakatCalculateButton ?: error("Missing localization string: zakatCalculateButton"),
            zakatResultTemplate ?: error("Missing localization string: zakatResultTemplate"),
            zakatNisabInfo ?: error("Missing localization string: zakatNisabInfo"),
            duaListTitle ?: error("Missing localization string: duaListTitle"),
            postPrayerDhikrTitle ?: error("Missing localization string: postPrayerDhikrTitle"),
            postPrayerDhikrSubtitle ?: error("Missing localization string: postPrayerDhikrSubtitle"),
            postPrayerDhikrReset ?: error("Missing localization string: postPrayerDhikrReset"),
            postPrayerDhikrStepTemplate ?: error("Missing localization string: postPrayerDhikrStepTemplate"),
            postPrayerDhikrCountTemplate ?: error("Missing localization string: postPrayerDhikrCountTemplate"),
            postPrayerDhikrComplete ?: error("Missing localization string: postPrayerDhikrComplete"),
            postPrayerDhikrNext ?: error("Missing localization string: postPrayerDhikrNext"),
            islamicCalendarTitle ?: error("Missing localization string: islamicCalendarTitle"),
            islamicDaysRemainingTemplate ?: error("Missing localization string: islamicDaysRemainingTemplate"),
            streakDaysTemplate ?: error("Missing localization string: streakDaysTemplate"),
            cdShare ?: error("Missing localization string: cdShare"),
            hacKuraLabel ?: error("Missing localization string: hacKuraLabel"),
            moreTitle ?: error("Missing localization string: moreTitle"),
            filterAll ?: error("Missing localization string: filterAll"),
            zakatLiveValuesTitle ?: error("Missing localization string: zakatLiveValuesTitle"),
            zakatGoldPriceLabel ?: error("Missing localization string: zakatGoldPriceLabel"),
            zakatNisabTemplate ?: error("Missing localization string: zakatNisabTemplate"),
            zakatPriceDateTemplate ?: error("Missing localization string: zakatPriceDateTemplate"),
            zakatLivePriceUnavailable ?: error("Missing localization string: zakatLivePriceUnavailable"),
            zakatGoldPriceInputLabel ?: error("Missing localization string: zakatGoldPriceInputLabel"),
            zakatCashLabel ?: error("Missing localization string: zakatCashLabel"),
            zakatGoldGramsLabel ?: error("Missing localization string: zakatGoldGramsLabel"),
            zakatOtherAssetsLabel ?: error("Missing localization string: zakatOtherAssetsLabel"),
            zakatTradeGoodsLabel ?: error("Missing localization string: zakatTradeGoodsLabel"),
            zakatDebtsLabel ?: error("Missing localization string: zakatDebtsLabel"),
            zakatNetWealthTemplate ?: error("Missing localization string: zakatNetWealthTemplate"),
            zakatBelowNisab ?: error("Missing localization string: zakatBelowNisab"),
            zakatCurrencyLabel ?: error("Missing localization string: zakatCurrencyLabel"),
            fitreTitle ?: error("Missing localization string: fitreTitle"),
            fitreAmountTemplate ?: error("Missing localization string: fitreAmountTemplate"),
            fitreSourceNote ?: error("Missing localization string: fitreSourceNote"),
            fitrePersonsLabel ?: error("Missing localization string: fitrePersonsLabel"),
            fitreTotalTemplate ?: error("Missing localization string: fitreTotalTemplate"),
            fidyeDaysLabel ?: error("Missing localization string: fidyeDaysLabel"),
            fidyeTotalTemplate ?: error("Missing localization string: fidyeTotalTemplate"),
            fitreOtherCurrencyNote ?: error("Missing localization string: fitreOtherCurrencyNote"),
            restorePurchasesButton ?: error("Missing localization string: restorePurchasesButton"),
            purchaseSuccessMessage ?: error("Missing localization string: purchaseSuccessMessage"),
            purchaseCancelledMessage ?: error("Missing localization string: purchaseCancelledMessage"),
            purchaseUnavailableMessage ?: error("Missing localization string: purchaseUnavailableMessage"),
            purchaseErrorMessage ?: error("Missing localization string: purchaseErrorMessage"),
            restoreNothingMessage ?: error("Missing localization string: restoreNothingMessage"),
            adFreeUntilTemplate ?: error("Missing localization string: adFreeUntilTemplate"),
            prayerSoonTitleTemplate ?: error("Missing localization string: prayerSoonTitleTemplate"),
            prayerSoonBody ?: error("Missing localization string: prayerSoonBody"),
            prayerNowTitleTemplate ?: error("Missing localization string: prayerNowTitleTemplate"),
            prayerNowBody ?: error("Missing localization string: prayerNowBody"),
            occasionTwoDaysTemplate ?: error("Missing localization string: occasionTwoDaysTemplate"),
            occasionTomorrowTemplate ?: error("Missing localization string: occasionTomorrowTemplate"),
            occasionTomorrowNightTemplate ?: error("Missing localization string: occasionTomorrowNightTemplate"),
            occasionBody ?: error("Missing localization string: occasionBody"),
            prayerReminderLeadLabel ?: error("Missing localization string: prayerReminderLeadLabel"),
            reminderOffLabel ?: error("Missing localization string: reminderOffLabel"),
            minutesShortTemplate ?: error("Missing localization string: minutesShortTemplate"),
            prayerAtTimeLabel ?: error("Missing localization string: prayerAtTimeLabel"),
            occasionRemindersLabel ?: error("Missing localization string: occasionRemindersLabel"),
            notificationSoundHint ?: error("Missing localization string: notificationSoundHint"),
            themePickerTitle ?: error("Missing localization string: themePickerTitle"),
            themePickerSubtitle ?: error("Missing localization string: themePickerSubtitle"),
            themeChangeLaterNote ?: error("Missing localization string: themeChangeLaterNote"),
            themeIntroTitle ?: error("Missing localization string: themeIntroTitle"),
            themeIntroBody ?: error("Missing localization string: themeIntroBody"),
            supportRateApp ?: error("Missing localization string: supportRateApp"),
            supportRateAppHint ?: error("Missing localization string: supportRateAppHint"),
            supportShareApp ?: error("Missing localization string: supportShareApp"),
            supportShareAppHint ?: error("Missing localization string: supportShareAppHint"),
            shareAppMessage ?: error("Missing localization string: shareAppMessage"),
            supportFeedback ?: error("Missing localization string: supportFeedback"),
            supportFeedbackHint ?: error("Missing localization string: supportFeedbackHint"),
            supportPrivacyPolicy ?: error("Missing localization string: supportPrivacyPolicy"),
            supportPrivacyPolicyHint ?: error("Missing localization string: supportPrivacyPolicyHint"),
            supportTermsOfService ?: error("Missing localization string: supportTermsOfService"),
            supportTermsOfServiceHint ?: error("Missing localization string: supportTermsOfServiceHint"),
            fitreZakatRowTitle ?: error("Missing localization string: fitreZakatRowTitle"),
            fitreZakatRowHint ?: error("Missing localization string: fitreZakatRowHint"),
            mushafNewSurahTemplate ?: error("Missing localization string: mushafNewSurahTemplate"),
            mushafStopAction ?: error("Missing localization string: mushafStopAction"),
            mushafSafetyStopTemplate ?: error("Missing localization string: mushafSafetyStopTemplate"),
            mushafContinueAction ?: error("Missing localization string: mushafContinueAction"),
            mushafPageLoadFailed ?: error("Missing localization string: mushafPageLoadFailed"),
            mushafGestureHint ?: error("Missing localization string: mushafGestureHint"),
            mushafGoToPlayingPage ?: error("Missing localization string: mushafGoToPlayingPage"),
            mushafKeyboardHint ?: error("Missing localization string: mushafKeyboardHint"),
            mushafCopiedMessage ?: error("Missing localization string: mushafCopiedMessage"),
            mushafFavoriteAdded ?: error("Missing localization string: mushafFavoriteAdded"),
            mushafFavoriteRemoved ?: error("Missing localization string: mushafFavoriteRemoved"),
            mushafUndoAction ?: error("Missing localization string: mushafUndoAction"),
            mushafAyahActionsTitleTemplate ?: error("Missing localization string: mushafAyahActionsTitleTemplate"),
            mushafPlayFromHere ?: error("Missing localization string: mushafPlayFromHere"),
            mushafCopyAction ?: error("Missing localization string: mushafCopyAction"),
            mushafAddFavorite ?: error("Missing localization string: mushafAddFavorite"),
            mushafRemoveFavorite ?: error("Missing localization string: mushafRemoveFavorite"),
            shareCardTitle ?: error("Missing localization string: shareCardTitle"),
            shareImageAction ?: error("Missing localization string: shareImageAction"),
            shareTextAction ?: error("Missing localization string: shareTextAction"),
            shareCopyLink ?: error("Missing localization string: shareCopyLink"),
            shareLinkCopied ?: error("Missing localization string: shareLinkCopied"),
            shareImageFailed ?: error("Missing localization string: shareImageFailed"),
            qiblaSensorNoSensor ?: error("Missing localization string: qiblaSensorNoSensor"),
            qiblaSensorLowAccuracy ?: error("Missing localization string: qiblaSensorLowAccuracy"),
            qiblaSensorReady ?: error("Missing localization string: qiblaSensorReady"),
            qiblaCalibrationAction ?: error("Missing localization string: qiblaCalibrationAction"),
            qiblaCalibrationHideAction ?: error("Missing localization string: qiblaCalibrationHideAction"),
            qiblaCalibrationInstructions ?: error("Missing localization string: qiblaCalibrationInstructions"),
            offlineContentTitle ?: error("Missing localization string: offlineContentTitle"),
            offlineContentSubtitle ?: error("Missing localization string: offlineContentSubtitle"),
            offlineDownloadAction ?: error("Missing localization string: offlineDownloadAction"),
            offlineResumeAction ?: error("Missing localization string: offlineResumeAction"),
            offlinePauseAction ?: error("Missing localization string: offlinePauseAction"),
            offlineRetryAction ?: error("Missing localization string: offlineRetryAction"),
            offlineDeleteAction ?: error("Missing localization string: offlineDeleteAction"),
            offlineDownloadedLabel ?: error("Missing localization string: offlineDownloadedLabel"),
            offlineDownloadingLabel ?: error("Missing localization string: offlineDownloadingLabel"),
            offlinePausedLabel ?: error("Missing localization string: offlinePausedLabel"),
            offlinePartialLabel ?: error("Missing localization string: offlinePartialLabel"),
            offlineCompleteLabel ?: error("Missing localization string: offlineCompleteLabel"),
            offlineIdleLabel ?: error("Missing localization string: offlineIdleLabel"),
            offlineFailedItemsTemplate ?: error("Missing localization string: offlineFailedItemsTemplate"),
            offlineProgressTemplate ?: error("Missing localization string: offlineProgressTemplate"),
            offlineCurrentItemTemplate ?: error("Missing localization string: offlineCurrentItemTemplate"),
            offlineSelectionTemplate ?: error("Missing localization string: offlineSelectionTemplate"),
            offlineAudioCatalogued ?: error("Missing localization string: offlineAudioCatalogued"),
            offlineErrorHint ?: error("Missing localization string: offlineErrorHint")
        ),
    )
}

private fun strings(block: StringsBuilder.() -> Unit): Strings =
    StringsBuilder().apply(block).build()

private val turkish = strings {
    navHome = "Ana Sayfa"
 navSurahs = "Sureler"
 navFavorites = "Favoriler"
 navSettings = "Ayarlar"

    homeGreeting = "Esselamü Aleyküm"
 search = "Ara"
 juz = "Cüzler"
 reciters = "Hafızlar"

    retry = "Tekrar Dene"
 appearance = "Görünüm"
 recitationAndTranslation = "Sesli Okuyuş ve Meal"

    prayerNotifications = "Namaz Vakti Bildirimleri"
 language = "Dil"


    ayahWord = "Ayet"
 ayahWordLower = "ayet"
 meccan = "Mekki"
 medinan = "Medeni"

    serverUnreachable = "Sunucuya ulaşılamadı, lütfen tekrar deneyin."
 notFound = "Sonuç bulunamadı"

    viewAll = "Tümünü Gör"


    homeSubtitle = "Bugün Kur'an ile başlayın"
 continueReading = "Kaldığınız yerden devam edin"

    dailyAyahTitle = "Günün Ayeti"
 dailyAyahError = "Ayet yüklenemedi, tekrar deneyin."

    readingLessonsTitle = "Kur'an Okuma Dersleri"

    readingLessonsSubtitleTemplate = "Elif-Ba'dan tecvide, sesli telaffuzlu {n} sıralı ders"

    quickActionsTitle = "Hızlı Erişim"

    esmaulHusnaTitle = "Esma-ül Hüsna"
 discoverQuranTitle = "Kur'an'ı Keşfet"

    mushafModeLabel = "Kitap Okuma Modu"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "Okuma"
 toggleTranslationLabel = "Meali Göster/Gizle"

    pagesLabel = "Sayfalar"
 manzilsLabel = "Menziller"
 rukusLabel = "Rükûlar"

    hizbQuartersLabel = "Hizb Çeyrekleri"
 sajdaVersesLabel = "Secde Ayetleri"

    quranStatsTitle = "Kur'an-ı Kerim İstatistikleri"
 statSurah = "Sure"
 statJuz = "Cüz"

    statPage = "Sayfa"
 statRuku = "Rükû"
 statHizbQuarter = "Hizb Çeyreği"
 statManzil = "Menzil"

    statSajda = "Secde"
 featuredSurahsTitle = "Öne Çıkan Sureler"
 surahsLoadErrorShort = "Sureler yüklenemedi."

    nextPrayerLabel = "Sonraki Namaz"
 hoursMinutesLeftTemplate = "{h} sa {m} dk kaldı"

    minutesLeftTemplate = "{m} dk kaldı"
 favoriteLabel = "Favori"

    todayTitle = "Bugün"
 todayHadithTitle = "Günün hadisi"
 todayDuaTitle = "Günün duası"

    todayOpenHadith = "Hadisleri aç"
 todayOpenDua = "Duaları aç"
 todayNoContent = "İçerik şu an yüklenemedi"


    surahSearchPlaceholder = "Sure ara (isim veya numara)"

    surahsLoadErrorFull = "Sureler yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin."


    surahFallback = "Sure"

    surahLoadErrorFull = "Sure yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin."


    myFavoritesTitle = "Favorilerim"

    favoritesEmptyMessage = "Henüz favori ayetiniz yok. Sureleri okurken ⭐ ikonuna dokunarak buraya ekleyebilirsiniz."

    surahFallbackNumberedTemplate = "Sure {n}"


    settingsTitle = "Ayarlar"
 languagePickerSubtitleTemplate = "{n} dil arasından seçin"

    reciterLabel = "Hafız"
 translationLabel = "Meal / Çeviri"

    notificationsToggleTitle = "Bildirimleri Aç"
 notificationsToggleSubtitle = "Vakit girdiğinde bildirim gönder"

    locationLabel = "Konum"
 locationAutoUpdateNote = "Konum kaydedildiğinde bildirimler otomatik güncellenir."

    supportUsTitle = "Destek Ol"
 adFreeActiveMessage = "Reklamsız üyeliğiniz aktif, teşekkürler! 💚"

    supportUsMessage = "Uygulamayı geliştirmemize destek olun"
 smallDonationButton = "☕ Küçük Bağış"

    mediumDonationButton = "🎁 Orta Bağış"
 sixMonthAdFreeButton = "✨ 6 Aylık Reklamsız Üyelik"

    themeSystem = "🖥️ Sistem"
 themeLight = "☀️ Açık"
 themeDark = "🌙 Koyu"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Ara"

    selectTranslationTitle = "Meal Seçin"
 selectLocationTitle = "Konum Seçin"
 selectLanguageTitle = "Dil Seçin"


    searchTitle = "Kur'an'da Ara"
 searchAyahPlaceholder = "Ayet içinde kelime ara..."

    searchIdleHint = "Kur'an-ı Kerim mealinde arama yapmak için yazmaya başlayın."

    searchErrorFull = "Arama yapılamadı. Sunucuya ulaşılamadı, lütfen tekrar deneyin."

    searchNoResultsTemplate = "\"{query}\" için sonuç bulunamadı."


    recitersCountSubtitleTemplate = "{n} hafız • dinleyip seçebilirsiniz"

    searchReciterPlaceholder = "Hafız ara..."

    recitersLoadErrorFull = "Hafız listesi yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin."

    selectedReciterLabel = "Seçili hafız"
 tapToSelectLabel = "Seçmek için dokunun"


    juzSingular = "Cüz"
 pageSingular = "Sayfa"
 pagePlural = "Sayfalar"

    manzilSingular = "Menzil"
 manzilPlural = "Menziller"
 rukuSingular = "Rükû"
 rukuPlural = "Rükûlar"

    hizbQuarterSingular = "Hizb Çeyreği"
 hizbQuarterPlural = "Hizb Çeyrekleri"

    sectionLoadErrorTemplate = "{title} yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin."


    sajdaTitle = "Secde Ayetleri"
 sajdaSubtitle = "Kur'an'da tilavet secdesi gerektiren ayetler"

    sajdaLoadErrorFull = "Secde ayetleri yüklenemedi. Sunucuya ulaşılamadı, lütfen tekrar deneyin."


    nowPlayingTitle = "Şimdi Çalıyor"
 wholeSurahSuffix = "Tüm sure"
 playbackSpeedTitle = "Oynatma Hızı"


    onboardingSkip = "Geç"
 onboardingNext = "İleri"
 onboardingStart = "Başla"

    onboardTitle1 = "Huda Qur'an'a Hoş Geldiniz"

    onboardDesc1 = "Kur'an-ı Kerim'i her zaman, her yerde, cebinizde taşıyın."

    onboardTitle2 = "Sesli Dinleme"

    onboardDesc2 = "Ayet ayet veya sure sure, seçtiğiniz hafızlardan akıcı bir dille dinleyin."

    onboardTitle3 = "Mealler ve Çeviriler"

    onboardDesc3 = "Türkçe ve diğer dillerdeki meallerle ayetleri anlayarak okuyun."

    onboardTitle4 = "Favorileriniz, Kaldığınız Yer"

    onboardDesc4 = "Sevdiğiniz ayetleri kaydedin, okumaya kaldığınız yerden devam edin."


    appTagline = "Kur'an-ı Kerim, her an yanınızda"


    lessonsSubtitleTemplate = "Elif-Ba'dan tecvide, sıralı {n} ders"
 lessonNotFound = "Ders bulunamadı"

    lessonFallback = "Ders"
 pronounceContentDescription = "Telaffuzu dinle"


    cancelLabel = "İptal"
 goLabel = "Git"

    mushafJumpToPageTitle = "Sayfaya Git"
 mushafJumpToPageHint = "Sayfa numarası (1-604)"

    mushafResumeSubtitleTemplate = "Sayfa {n}'de kaldınız, devam edin"

    mushafStartSubtitle = "Kitap gibi okuyun, sayfa sayfa çevirin"


    khatmProgressTitle = "Hatim İlerlemeniz"

    khatmProgressTemplate = "Sayfa {page} / {total} (%{percent})"

    khatmCompletedCountTemplate = "Tamamlanan hatim: {n}"


    qiblaTitle = "Kıble Pusulası"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Telefonunuzun pusulasını açıp bu açıyı kuzeye göre hizalayın."

    qiblaLocationUnavailable = "Konum bilgisi alınamadı. Ayarlar'dan şehir/ülke seçtiğinizden emin olun."

    cdBack = "Geri"
 cdPlay = "Oynat"
 cdPause = "Duraklat"
 cdClose = "Kapat"
 cdRepeat = "Tekrarla"
 cdPrevious = "Önceki"
 cdNext = "Sonraki"

    tafsirLabel = "Tefsir"
 selectTafsirTitle = "Tefsir Seç"
 changeTafsirLabel = "Tefsiri Değiştir"
 tafsirUnavailable = "Bu ayet için tefsir metni alınamadı."
 tafsirNotSelected = "Tefsir seçilmedi"

    qiblaLiveInstructions = "Telefonu düz tutun ve ok, dairenin üstündeki üçgene gelene kadar yavaşça çevirin."

    qiblaSensorNoSensor = "Pusula sensörü bulunamadı"

    qiblaSensorLowAccuracy = "Pusula doğruluğu düşük"

    qiblaSensorReady = "Pusula doğruluğu normal"

    qiblaCalibrationAction = "Kalibrasyon rehberini göster"

    qiblaCalibrationHideAction = "Kalibrasyon rehberini gizle"

    qiblaCalibrationInstructions = "Telefonu düz tutun ve havada yavaşça 8 çizer gibi hareket ettirin. Ardından telefonu yeniden düz tutup birkaç saniye bekleyin."

    tafsirVsMealHint = "Meal, ayetin çevirisidir. Tefsir ise ayetin bağlamını ve anlamını ayrıntılı açıklayan yorumdur."

    textSizeLabel = "Yazı Boyutu"
 textSizeHint = "Görmekte zorlanıyorsanız yazıları büyütün."
 textSizeNormal = "Normal"
 textSizeLarge = "Büyük"
 textSizeExtraLarge = "Çok Büyük"

    elderlyProfileTitle = "Yaşlı kullanıcı profili"
 elderlyProfileSubtitle = "Yazıları büyütür, kontrastı artırır, düğmeleri genişletir ve hareketleri azaltır."

    highContrastTitle = "Yüksek kontrast"
 highContrastSubtitle = "Metin ve sayfa renklerini daha belirgin yapar."

    largeTouchTargetsTitle = "Geniş dokunma alanları"
 largeTouchTargetsSubtitle = "Düğmeleri ve satırları daha kolay dokunulacak şekilde büyütür."

    reducedMotionTitle = "Azaltılmış hareket"
 reducedMotionSubtitle = "Sayfa geçişlerini ve görsel hareketleri sakinleştirir."

    zakatCalculatorTitle = "Zekât Hesaplayıcı"

    zakatWealthLabel = "Toplam Varlığınız (nakit, altın, gümüş vb.)"

    zakatCalculateButton = "Hesapla"

    zakatResultTemplate = "Zekâtınız: {amount}"

    zakatNisabInfo = "Zekât, nisap miktarına (80,18 gr altın değeri) ulaşan ve üzerinden bir yıl geçen varlığın %2,5'idir. Bu hesaplama genel bir rehberdir, dini danışmanlık yerine geçmez."

    duaListTitle = "Dualar"
 postPrayerDhikrTitle = "Namaz Sonrası Zikir"
 postPrayerDhikrSubtitle = "Sakin bir ritimle tesbihatınızı tamamlayın."
 postPrayerDhikrReset = "Baştan başla"
 postPrayerDhikrStepTemplate = "{step}. zikir"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Tesbihat tamamlandı"
 postPrayerDhikrNext = "Sonraki zikre geç"

    islamicCalendarTitle = "İslami Takvim"

    islamicDaysRemainingTemplate = "{n} gün kaldı"

    streakDaysTemplate = "{n} günlük okuma serisi"

    cdShare = "Paylaş"

    hacKuraLabel = "Hac Kur'ası Sonuç Sorgulama"

    moreTitle = "Diğer"

    filterAll = "Tümü"

    zakatLiveValuesTitle = "Güncel Değerler"

    zakatGoldPriceLabel = "Gram altın (24 ayar)"

    zakatNisabTemplate = "Nisap ({grams} gr altın)"

    zakatPriceDateTemplate = "{date} tarihli fiyat"

    zakatLivePriceUnavailable = "Canlı altın fiyatı alınamadı. Gram altın fiyatını aşağıya kendiniz girebilirsiniz."

    zakatGoldPriceInputLabel = "Gram altın fiyatı"

    zakatCashLabel = "Nakit ve banka hesapları"

    zakatGoldGramsLabel = "Altın (gram, 24 ayar karşılığı)"

    zakatOtherAssetsLabel = "Döviz, hisse ve diğer yatırımlar"

    zakatTradeGoodsLabel = "Ticari mallar ve alacaklar"

    zakatDebtsLabel = "Borçlar (düşülür)"

    zakatNetWealthTemplate = "Zekâta tabi net varlık: {amount}"

    zakatBelowNisab = "Net varlığınız nisap miktarının altında; zekât düşmez."

    zakatCurrencyLabel = "Para birimi"

    fitreTitle = "Fitre ve Fidye"

    fitreAmountTemplate = "{period} fitresi (kişi başı)"

    fitreSourceNote = "Diyanet İşleri Başkanlığı Din İşleri Yüksek Kurulu'nun belirlediği asgari tutardır; imkânı olan daha fazlasını verebilir. Oruç tutamayanların günlük fidyesi de aynı tutardır."

    fitrePersonsLabel = "Kişi sayısı"

    fitreTotalTemplate = "Toplam fitre: {amount}"

    fidyeDaysLabel = "Tutulamayan oruç günü"

    fidyeTotalTemplate = "Toplam fidye: {amount}"

    fitreOtherCurrencyNote = "Fitre tutarı ülkeye göre değişir; yaşadığınız yerdeki dini kurumun açıkladığı tutarı esas alın. Aşağıdaki tutar Türkiye (Diyanet) içindir."

    restorePurchasesButton = "Satın alımları geri yükle"

    purchaseSuccessMessage = "Teşekkürler! Satın alımınız tamamlandı."

    purchaseCancelledMessage = "Satın alma iptal edildi."

    purchaseUnavailableMessage = "Bu ürün şu anda mağazada bulunamadı. Lütfen daha sonra tekrar deneyin."

    purchaseErrorMessage = "Satın alma tamamlanamadı. İnternet bağlantınızı kontrol edip tekrar deneyin."

    restoreNothingMessage = "Bu hesapta etkin bir reklamsız abonelik bulunamadı."

    adFreeUntilTemplate = "Reklamsız kullanım: {date} tarihine kadar"

    prayerSoonTitleTemplate = "{prayer} vaktine {n} dakika kaldı"

    prayerSoonBody = "Abdestini alıp huzurla hazırlanabilirsin."

    prayerNowTitleTemplate = "{prayer} vakti girdi"

    prayerNowBody = "Kalbine bir nefeslik sükûnet; namaz seni bekliyor."

    occasionTwoDaysTemplate = "{occasion} yaklaşıyor: 2 gün kaldı"

    occasionTomorrowTemplate = "Yarın {occasion}"

    occasionTomorrowNightTemplate = "Yarın akşam {occasion}"

    occasionBody = "Dualarını, niyetini ve sevdiklerini şimdiden hazırla; bu güzel vakti ihya etmeye ne dersin?"

    prayerReminderLeadLabel = "Vakitten önce hatırlat"

    reminderOffLabel = "Yok"

    minutesShortTemplate = "{n} dk"

    prayerAtTimeLabel = "Vakit girince de bildir"

    occasionRemindersLabel = "Kandil ve mübarek gün hatırlatmaları"

    notificationSoundHint = "Bildirimler yumuşak, kısa bir çan sesiyle gelir; sizi rahatsız etmez."

    themePickerTitle = "Temanızı seçin"

    themePickerSubtitle = "Dokunarak deneyin; seçiminiz hemen uygulanır."

    themeChangeLaterNote = "İstediğiniz zaman Ayarlar'dan değiştirebilirsiniz."

    themeIntroTitle = "Yeni: Temalar"

    themeIntroBody = "Uygulamanın görünümünü dilediğin gibi seç. Dokunarak dene, beğendiğinde öyle kalsın."

    supportRateApp = "Uygulamayı puanla"

    supportRateAppHint = "Mağazada değerlendirerek destek olun"

    supportShareApp = "Uygulamayı paylaş"

    supportShareAppHint = "Yakınlarınıza önerin"

    shareAppMessage = "Huda Kur'an: Kur'an-ı Kerim oku ve dinle, namaz vakitleri, dualar ve Esmâ-ül Hüsnâ. {url}"

    supportFeedback = "Geri bildirim gönder"

    supportFeedbackHint = "Öneri veya sorunlarınızı bize iletin"

    supportPrivacyPolicy = "Gizlilik Politikası"

    supportPrivacyPolicyHint = "Verilerinizin nasıl kullanıldığını öğrenin"

    supportTermsOfService = "Kullanım Şartları"

    supportTermsOfServiceHint = "Hizmet şartlarını okuyun"

    fitreZakatRowTitle = "Fitre / Zekât hesaplama"

    fitreZakatRowHint = "Zekât, fitre ve fidyenizi hesaplayın"

    mushafNewSurahTemplate = "Yeni sure: {surah}"

    mushafStopAction = "Durdur"

    mushafSafetyStopTemplate = "{count} sayfa art arda dinlendi. Dinlemeye devam edilsin mi?"

    mushafContinueAction = "Devam et"

    mushafPageLoadFailed = "Sonraki sayfa yüklenemedi"

    mushafGestureHint = "Dinlemek için ayete dokunun, işlemler için basılı tutun"

    mushafGoToPlayingPage = "Okunan sayfaya dön ({page})"

    mushafKeyboardHint = "Boşluk: oynat/duraklat · ← →: sayfa çevir"

    mushafCopiedMessage = "Ayet kopyalandı"

    mushafFavoriteAdded = "Favorilere eklendi"

    mushafFavoriteRemoved = "Favorilerden çıkarıldı"

    mushafUndoAction = "Geri al"

    mushafAyahActionsTitleTemplate = "{surah}, {ayah}. ayet"

    mushafPlayFromHere = "Buradan dinle"

    mushafCopyAction = "Kopyala"

    mushafAddFavorite = "Favorilere ekle"

    mushafRemoveFavorite = "Favorilerden çıkar"

    shareCardTitle = "Ayeti paylaş"

    shareImageAction = "Görseli paylaş"

    shareTextAction = "Metni paylaş"

    shareCopyLink = "Bağlantıyı kopyala"

    shareLinkCopied = "Bağlantı kopyalandı"

    shareImageFailed = "Görsel hazırlanamadı"

}

private val english = strings {
    navHome = "Home"
 navSurahs = "Surahs"
 navFavorites = "Favorites"
 navSettings = "Settings"

    homeGreeting = "Peace be upon you"
 search = "Search"
 juz = "Juz"
 reciters = "Reciters"

    retry = "Retry"
 appearance = "Appearance"
 recitationAndTranslation = "Recitation & Translation"

    prayerNotifications = "Prayer Notifications"
 language = "Language"


    ayahWord = "Ayah"
 ayahWordLower = "ayahs"
 meccan = "Meccan"
 medinan = "Medinan"

    serverUnreachable = "Could not reach the server, please try again."
 notFound = "No results found"

    viewAll = "View All"


    homeSubtitle = "Start your day with the Quran"
 continueReading = "Continue where you left off"

    dailyAyahTitle = "Verse of the Day"
 dailyAyahError = "Couldn't load the verse, try again."

    readingLessonsTitle = "Quran Reading Lessons"

    readingLessonsSubtitleTemplate = "From Elif-Ba to Tajwid, {n} guided lessons with audio"

    quickActionsTitle = "Quick Actions"

    esmaulHusnaTitle = "The 99 Names of Allah"
 discoverQuranTitle = "Explore the Quran"

    mushafModeLabel = "Mushaf Mode"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "Reading"
 toggleTranslationLabel = "Show/Hide Translation"

    pagesLabel = "Pages"
 manzilsLabel = "Manzils"
 rukusLabel = "Rukus"

    hizbQuartersLabel = "Hizb Quarters"
 sajdaVersesLabel = "Sajda Verses"

    quranStatsTitle = "Quran Statistics"
 statSurah = "Surah"
 statJuz = "Juz"

    statPage = "Page"
 statRuku = "Ruku"
 statHizbQuarter = "Hizb Quarter"
 statManzil = "Manzil"

    statSajda = "Sajda"
 featuredSurahsTitle = "Featured Surahs"
 surahsLoadErrorShort = "Couldn't load surahs."

    nextPrayerLabel = "Next Prayer"
 hoursMinutesLeftTemplate = "{h}h {m}m left"

    minutesLeftTemplate = "{m}m left"
 favoriteLabel = "Favorites"

    todayTitle = "Today"
 todayHadithTitle = "Hadith of the day"
 todayDuaTitle = "Dua of the day"

    todayOpenHadith = "Open hadiths"
 todayOpenDua = "Open duas"
 todayNoContent = "Content is unavailable right now"


    surahSearchPlaceholder = "Search surah (name or number)"

    surahsLoadErrorFull = "Couldn't load surahs. Could not reach the server, please try again."


    surahFallback = "Surah"

    surahLoadErrorFull = "Couldn't load surah. Could not reach the server, please try again."


    myFavoritesTitle = "My Favorites"

    favoritesEmptyMessage = "You don't have any favorite verses yet. Tap the ⭐ icon while reading to add one here."

    surahFallbackNumberedTemplate = "Surah {n}"


    settingsTitle = "Settings"
 languagePickerSubtitleTemplate = "Choose from {n} languages"

    reciterLabel = "Reciter"
 translationLabel = "Translation"

    notificationsToggleTitle = "Enable Notifications"
 notificationsToggleSubtitle = "Send a notification when a prayer time begins"

    locationLabel = "Location"
 locationAutoUpdateNote = "Notifications update automatically once your location is saved."

    supportUsTitle = "Support Us"
 adFreeActiveMessage = "Your ad-free membership is active, thank you! 💚"

    supportUsMessage = "Help us keep improving the app"
 smallDonationButton = "☕ Small Donation"

    mediumDonationButton = "🎁 Medium Donation"
 sixMonthAdFreeButton = "✨ 6-Month Ad-Free Membership"

    themeSystem = "🖥️ System"
 themeLight = "☀️ Light"
 themeDark = "🌙 Dark"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Search"

    selectTranslationTitle = "Select Translation"
 selectLocationTitle = "Select Location"
 selectLanguageTitle = "Select Language"


    searchTitle = "Search the Quran"
 searchAyahPlaceholder = "Search a word within verses..."

    searchIdleHint = "Start typing to search the Quran translation."

    searchErrorFull = "Search failed. Could not reach the server, please try again."

    searchNoResultsTemplate = "No results found for \"{query}\"."


    recitersCountSubtitleTemplate = "{n} reciters • listen and choose one"

    searchReciterPlaceholder = "Search reciters..."

    recitersLoadErrorFull = "Couldn't load reciters. Could not reach the server, please try again."

    selectedReciterLabel = "Selected reciter"
 tapToSelectLabel = "Tap to select"


    juzSingular = "Juz"
 pageSingular = "Page"
 pagePlural = "Pages"

    manzilSingular = "Manzil"
 manzilPlural = "Manzils"
 rukuSingular = "Ruku"
 rukuPlural = "Rukus"

    hizbQuarterSingular = "Hizb Quarter"
 hizbQuarterPlural = "Hizb Quarters"

    sectionLoadErrorTemplate = "Couldn't load {title}. Could not reach the server, please try again."


    sajdaTitle = "Sajda Verses"
 sajdaSubtitle = "Verses in the Quran that require prostration"

    sajdaLoadErrorFull = "Couldn't load sajda verses. Could not reach the server, please try again."


    nowPlayingTitle = "Now Playing"
 wholeSurahSuffix = "Whole surah"
 playbackSpeedTitle = "Playback Speed"


    onboardingSkip = "Skip"
 onboardingNext = "Next"
 onboardingStart = "Get Started"

    onboardTitle1 = "Welcome to Huda Qur'an"

    onboardDesc1 = "Carry the Holy Quran with you, anytime, anywhere."

    onboardTitle2 = "Audio Recitation"

    onboardDesc2 = "Listen verse by verse or surah by surah, from the reciter you choose."

    onboardTitle3 = "Translations"

    onboardDesc3 = "Read and understand verses with translations in Turkish and other languages."

    onboardTitle4 = "Favorites & Continue Reading"

    onboardDesc4 = "Save the verses you love and pick up reading right where you left off."


    appTagline = "The Holy Quran, always with you"


    lessonsSubtitleTemplate = "From Elif-Ba to Tajwid, {n} guided lessons"
 lessonNotFound = "Lesson not found"

    lessonFallback = "Lesson"
 pronounceContentDescription = "Listen to pronunciation"


    cancelLabel = "Cancel"
 goLabel = "Go"

    mushafJumpToPageTitle = "Go to Page"
 mushafJumpToPageHint = "Page number (1-604)"

    mushafResumeSubtitleTemplate = "You left off on page {n} — continue reading"

    mushafStartSubtitle = "Read like a real book, page by page"


    khatmProgressTitle = "Your Khatm Progress"

    khatmProgressTemplate = "Page {page} / {total} (%{percent})"

    khatmCompletedCountTemplate = "Completed khatms: {n}"


    qiblaTitle = "Qibla Compass"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Open your phone's compass and align this angle with north."

    qiblaLocationUnavailable = "Couldn't get your location. Make sure you've set a city/country in Settings."

    cdBack = "Back"
 cdPlay = "Play"
 cdPause = "Pause"
 cdClose = "Close"
 cdRepeat = "Repeat"
 cdPrevious = "Previous"
 cdNext = "Next"

    tafsirLabel = "Tafsir"
 selectTafsirTitle = "Select Tafsir"
 changeTafsirLabel = "Change Tafsir"
 tafsirUnavailable = "Couldn't load the tafsir for this ayah."
 tafsirNotSelected = "No tafsir selected"

    qiblaLiveInstructions = "Hold your phone flat and slowly turn until the arrow lines up with the triangle above the circle."

    qiblaSensorNoSensor = "No compass sensor detected"

    qiblaSensorLowAccuracy = "Compass accuracy is low"

    qiblaSensorReady = "Compass accuracy is normal"

    qiblaCalibrationAction = "Show calibration guide"

    qiblaCalibrationHideAction = "Hide calibration guide"

    qiblaCalibrationInstructions = "Hold the phone flat and slowly move it through a figure-eight in the air. Then hold it flat again and wait a few seconds."

    tafsirVsMealHint = "Translation renders the ayah's meaning in your language. Tafsir is a longer scholarly explanation of its context and meaning."

    textSizeLabel = "Text Size"
 textSizeHint = "Make text bigger if you have trouble reading it."
 textSizeNormal = "Normal"
 textSizeLarge = "Large"
 textSizeExtraLarge = "Extra Large"

    elderlyProfileTitle = "Older user profile"
 elderlyProfileSubtitle = "Makes text larger, increases contrast, enlarges controls, and reduces motion."

    highContrastTitle = "High contrast"
 highContrastSubtitle = "Makes text and page colours easier to distinguish."

    largeTouchTargetsTitle = "Larger touch targets"
 largeTouchTargetsSubtitle = "Makes buttons and rows easier to tap."

    reducedMotionTitle = "Reduced motion"
 reducedMotionSubtitle = "Calms page transitions and visual movement."

    zakatCalculatorTitle = "Zakat Calculator"

    zakatWealthLabel = "Your Total Wealth (cash, gold, silver, etc.)"

    zakatCalculateButton = "Calculate"

    zakatResultTemplate = "Your Zakat: {amount}"

    zakatNisabInfo = "Zakat is 2.5% of wealth that has reached the nisab threshold (the value of 80.18 g of gold) and been held for a full year. This is a general guide, not religious advice."

    duaListTitle = "Supplications"
 postPrayerDhikrTitle = "Post-prayer dhikr"
 postPrayerDhikrSubtitle = "Complete your remembrance at a calm, steady pace."
 postPrayerDhikrReset = "Start over"
 postPrayerDhikrStepTemplate = "Dhikr {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Dhikr complete"
 postPrayerDhikrNext = "Next dhikr"

    islamicCalendarTitle = "Islamic Calendar"

    islamicDaysRemainingTemplate = "{n} days left"

    streakDaysTemplate = "{n}-day reading streak"

    cdShare = "Share"

    hacKuraLabel = "Hajj Lottery Result Lookup"

    moreTitle = "More"

    filterAll = "All"

    zakatLiveValuesTitle = "Current Values"

    zakatGoldPriceLabel = "Gold per gram (24k)"

    zakatNisabTemplate = "Nisab ({grams} g of gold)"

    zakatPriceDateTemplate = "Price as of {date}"

    zakatLivePriceUnavailable = "Couldn't fetch the live gold price. You can enter the price per gram below."

    zakatGoldPriceInputLabel = "Gold price per gram"

    zakatCashLabel = "Cash and bank accounts"

    zakatGoldGramsLabel = "Gold (grams, 24k equivalent)"

    zakatOtherAssetsLabel = "Foreign currency, shares and other investments"

    zakatTradeGoodsLabel = "Trade goods and receivables"

    zakatDebtsLabel = "Debts (deducted)"

    zakatNetWealthTemplate = "Net zakatable wealth: {amount}"

    zakatBelowNisab = "Your net wealth is below the nisab, so no zakat is due."

    zakatCurrencyLabel = "Currency"

    fitreTitle = "Fitra and Fidya"

    fitreAmountTemplate = "{period} fitra (per person)"

    fitreSourceNote = "The minimum amount set by Turkey's Presidency of Religious Affairs (Diyanet); those who can afford it may give more. The daily fidya for missed fasts is the same amount."

    fitrePersonsLabel = "Number of people"

    fitreTotalTemplate = "Total fitra: {amount}"

    fidyeDaysLabel = "Missed fasting days"

    fidyeTotalTemplate = "Total fidya: {amount}"

    fitreOtherCurrencyNote = "The fitra amount differs by country; follow the amount announced by the religious authority where you live. The amount below applies to Turkey (Diyanet)."

    restorePurchasesButton = "Restore purchases"

    purchaseSuccessMessage = "Thank you! Your purchase is complete."

    purchaseCancelledMessage = "Purchase cancelled."

    purchaseUnavailableMessage = "This product isn't available in the store right now. Please try again later."

    purchaseErrorMessage = "The purchase couldn't be completed. Check your connection and try again."

    restoreNothingMessage = "No active ad-free subscription was found for this account."

    adFreeUntilTemplate = "Ad-free until {date}"

    prayerSoonTitleTemplate = "{n} minutes until {prayer}"

    prayerSoonBody = "Take your ablution and get ready in peace."

    prayerNowTitleTemplate = "It's time for {prayer}"

    prayerNowBody = "A breath of stillness for your heart; the prayer is waiting."

    occasionTwoDaysTemplate = "{occasion} is near: 2 days to go"

    occasionTomorrowTemplate = "{occasion} is tomorrow"

    occasionTomorrowNightTemplate = "{occasion} is tomorrow evening"

    occasionBody = "Prepare your prayers, your intention and your loved ones; how about reviving this blessed time?"

    prayerReminderLeadLabel = "Remind me before prayer"

    reminderOffLabel = "Off"

    minutesShortTemplate = "{n} min"

    prayerAtTimeLabel = "Also notify when the time begins"

    occasionRemindersLabel = "Reminders for blessed days and nights"

    notificationSoundHint = "Notifications arrive with a soft, short chime — never a harsh alert."

    themePickerTitle = "Choose your theme"

    themePickerSubtitle = "Tap to try; your choice applies right away."

    themeChangeLaterNote = "You can change it anytime in Settings."

    themeIntroTitle = "New: Themes"

    themeIntroBody = "Pick the look you like. Tap to try — keep the one that feels right."

    supportRateApp = "Rate the app"

    supportRateAppHint = "Support us with a review in the store"

    supportShareApp = "Share the app"

    supportShareAppHint = "Recommend it to your loved ones"

    shareAppMessage = "Huda Qur'an: read and listen to the Qur'an, prayer times, duas and the Names of Allah. {url}"

    supportFeedback = "Send feedback"

    supportFeedbackHint = "Tell us your ideas or problems"

    supportPrivacyPolicy = "Privacy Policy"

    supportPrivacyPolicyHint = "Learn how your data is used"

    supportTermsOfService = "Terms of Service"

    supportTermsOfServiceHint = "Read our terms of service"

    fitreZakatRowTitle = "Fitr / Zakat calculator"

    fitreZakatRowHint = "Calculate your zakat, fitra and fidya"

    mushafNewSurahTemplate = "New surah: {surah}"

    mushafStopAction = "Stop"

    mushafSafetyStopTemplate = "{count} pages played in a row. Keep listening?"

    mushafContinueAction = "Continue"

    mushafPageLoadFailed = "Couldn't load the next page"

    mushafGestureHint = "Tap an ayah to listen, hold it for more actions"

    mushafGoToPlayingPage = "Back to the playing page ({page})"

    mushafKeyboardHint = "Space: play/pause · ← →: turn page"

    mushafCopiedMessage = "Ayah copied"

    mushafFavoriteAdded = "Added to favorites"

    mushafFavoriteRemoved = "Removed from favorites"

    mushafUndoAction = "Undo"

    mushafAyahActionsTitleTemplate = "{surah}, ayah {ayah}"

    mushafPlayFromHere = "Listen from here"

    mushafCopyAction = "Copy"

    mushafAddFavorite = "Add to favorites"

    mushafRemoveFavorite = "Remove from favorites"

    shareCardTitle = "Share ayah"

    shareImageAction = "Share image"

    shareTextAction = "Share text"

    shareCopyLink = "Copy link"

    shareLinkCopied = "Link copied"

    shareImageFailed = "Couldn't prepare the image"

}

private val arabic = strings {
    navHome = "الرئيسية"
 navSurahs = "السور"
 navFavorites = "المفضلة"
 navSettings = "الإعدادات"

    homeGreeting = "السلام عليكم"
 search = "بحث"
 juz = "الأجزاء"
 reciters = "القراء"

    retry = "إعادة المحاولة"
 appearance = "المظهر"
 recitationAndTranslation = "التلاوة والترجمة"

    prayerNotifications = "إشعارات الصلاة"
 language = "اللغة"


    ayahWord = "آية"
 ayahWordLower = "آية"
 meccan = "مكية"
 medinan = "مدنية"

    serverUnreachable = "تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."
 notFound = "لا توجد نتائج"

    viewAll = "عرض الكل"


    homeSubtitle = "ابدأ يومك مع القرآن"
 continueReading = "تابع من حيث توقفت"

    dailyAyahTitle = "آية اليوم"
 dailyAyahError = "تعذر تحميل الآية، حاول مرة أخرى."

    readingLessonsTitle = "دروس تعلم القراءة"

    readingLessonsSubtitleTemplate = "من الألف باء إلى التجويد، {n} دروس مرتبة بالصوت"

    quickActionsTitle = "الوصول السريع"

    esmaulHusnaTitle = "أسماء الله الحسنى"
 discoverQuranTitle = "استكشف القرآن"

    mushafModeLabel = "وضع المصحف"
 mushafViewLabel = "المصحف"
 readingViewLabel = "القراءة"
 toggleTranslationLabel = "إظهار/إخفاء الترجمة"

    pagesLabel = "الصفحات"
 manzilsLabel = "المنازل"
 rukusLabel = "الركوعات"

    hizbQuartersLabel = "أرباع الأحزاب"
 sajdaVersesLabel = "آيات السجدة"

    quranStatsTitle = "إحصائيات القرآن الكريم"
 statSurah = "سورة"
 statJuz = "جزء"

    statPage = "صفحة"
 statRuku = "ركوع"
 statHizbQuarter = "ربع حزب"
 statManzil = "منزل"

    statSajda = "سجدة"
 featuredSurahsTitle = "سور مميزة"
 surahsLoadErrorShort = "تعذر تحميل السور."

    nextPrayerLabel = "الصلاة القادمة"
 hoursMinutesLeftTemplate = "متبقي {h} س {m} د"

    minutesLeftTemplate = "متبقي {m} د"
 favoriteLabel = "المفضلة"

    todayTitle = "اليوم"
 todayHadithTitle = "حديث اليوم"
 todayDuaTitle = "دعاء اليوم"

    todayOpenHadith = "فتح الأحاديث"
 todayOpenDua = "فتح الأدعية"
 todayNoContent = "المحتوى غير متاح الآن"


    surahSearchPlaceholder = "ابحث عن سورة (الاسم أو الرقم)"

    surahsLoadErrorFull = "تعذر تحميل السور. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."


    surahFallback = "سورة"

    surahLoadErrorFull = "تعذر تحميل السورة. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."


    myFavoritesTitle = "مفضلتي"

    favoritesEmptyMessage = "ليس لديك أي آيات مفضلة بعد. اضغط على أيقونة ⭐ أثناء القراءة لإضافتها هنا."

    surahFallbackNumberedTemplate = "سورة {n}"


    settingsTitle = "الإعدادات"
 languagePickerSubtitleTemplate = "اختر من بين {n} لغة"

    reciterLabel = "القارئ"
 translationLabel = "الترجمة"

    notificationsToggleTitle = "تفعيل الإشعارات"
 notificationsToggleSubtitle = "إرسال إشعار عند دخول وقت الصلاة"

    locationLabel = "الموقع"
 locationAutoUpdateNote = "يتم تحديث الإشعارات تلقائيًا عند حفظ الموقع."

    supportUsTitle = "ادعمنا"
 adFreeActiveMessage = "عضويتك بدون إعلانات مفعّلة، شكرًا لك! 💚"

    supportUsMessage = "ساعدنا في تطوير التطبيق"
 smallDonationButton = "☕ تبرع صغير"

    mediumDonationButton = "🎁 تبرع متوسط"
 sixMonthAdFreeButton = "✨ عضوية بدون إعلانات لمدة 6 أشهر"

    themeSystem = "🖥️ النظام"
 themeLight = "☀️ فاتح"
 themeDark = "🌙 داكن"
 themeSakura = "🌸 ساكورا"


    editionSearchPlaceholder = "بحث"

    selectTranslationTitle = "اختر الترجمة"
 selectLocationTitle = "اختر الموقع"
 selectLanguageTitle = "اختر اللغة"


    searchTitle = "البحث في القرآن"
 searchAyahPlaceholder = "ابحث عن كلمة داخل الآيات..."

    searchIdleHint = "ابدأ الكتابة للبحث في ترجمة معاني القرآن الكريم."

    searchErrorFull = "فشل البحث. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."

    searchNoResultsTemplate = "لا توجد نتائج لـ \"{query}\"."


    recitersCountSubtitleTemplate = "{n} قارئ • استمع واختر"

    searchReciterPlaceholder = "ابحث عن قارئ..."

    recitersLoadErrorFull = "تعذر تحميل قائمة القراء. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."

    selectedReciterLabel = "القارئ المختار"
 tapToSelectLabel = "اضغط للاختيار"


    juzSingular = "جزء"
 pageSingular = "صفحة"
 pagePlural = "الصفحات"

    manzilSingular = "منزل"
 manzilPlural = "المنازل"
 rukuSingular = "ركوع"
 rukuPlural = "الركوعات"

    hizbQuarterSingular = "ربع حزب"
 hizbQuarterPlural = "أرباع الأحزاب"

    sectionLoadErrorTemplate = "تعذر تحميل {title}. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."


    sajdaTitle = "آيات السجدة"
 sajdaSubtitle = "الآيات في القرآن التي تستوجب سجدة التلاوة"

    sajdaLoadErrorFull = "تعذر تحميل آيات السجدة. تعذر الوصول إلى الخادم، يرجى المحاولة مرة أخرى."


    nowPlayingTitle = "قيد التشغيل الآن"
 wholeSurahSuffix = "السورة كاملة"
 playbackSpeedTitle = "سرعة التشغيل"


    onboardingSkip = "تخطي"
 onboardingNext = "التالي"
 onboardingStart = "ابدأ"

    onboardTitle1 = "مرحبًا بك في Huda Qur'an"

    onboardDesc1 = "احمل القرآن الكريم معك دائمًا وفي كل مكان."

    onboardTitle2 = "الاستماع الصوتي"

    onboardDesc2 = "استمع آية آية أو سورة سورة، من القارئ الذي تختاره."

    onboardTitle3 = "المعاني والترجمات"

    onboardDesc3 = "افهم الآيات من خلال الترجمات بالتركية ولغات أخرى."

    onboardTitle4 = "مفضلتك ونقطة توقفك"

    onboardDesc4 = "احفظ الآيات التي تحبها وتابع القراءة من حيث توقفت."


    appTagline = "القرآن الكريم، معك في كل لحظة"


    lessonsSubtitleTemplate = "من الألف باء إلى التجويد، {n} دروس مرتبة"
 lessonNotFound = "الدرس غير موجود"

    lessonFallback = "درس"
 pronounceContentDescription = "استمع إلى النطق"


    cancelLabel = "إلغاء"
 goLabel = "اذهب"

    mushafJumpToPageTitle = "الانتقال إلى صفحة"
 mushafJumpToPageHint = "رقم الصفحة (1-604)"

    mushafResumeSubtitleTemplate = "توقفت عند الصفحة {n}، تابع القراءة"

    mushafStartSubtitle = "اقرأ كأنه كتاب حقيقي، صفحة بصفحة"


    khatmProgressTitle = "تقدمك في الختمة"

    khatmProgressTemplate = "الصفحة {page} / {total} (%{percent})"

    khatmCompletedCountTemplate = "الختمات المكتملة: {n}"


    qiblaTitle = "بوصلة القبلة"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "افتح بوصلة هاتفك وقم بمحاذاة هذه الزاوية مع الشمال."

    qiblaLocationUnavailable = "تعذر الحصول على موقعك. تأكد من تحديد المدينة/الدولة في الإعدادات."

    cdBack = "رجوع"
 cdPlay = "تشغيل"
 cdPause = "إيقاف مؤقت"
 cdClose = "إغلاق"
 cdRepeat = "تكرار"
 cdPrevious = "السابق"
 cdNext = "التالي"

    tafsirLabel = "تفسير"
 selectTafsirTitle = "اختر التفسير"
 changeTafsirLabel = "تغيير التفسير"
 tafsirUnavailable = "تعذر تحميل تفسير هذه الآية."
 tafsirNotSelected = "لم يتم اختيار تفسير"

    qiblaLiveInstructions = "أمسك هاتفك أفقيًا وأدره ببطء حتى يتوافق السهم مع المثلث أعلى الدائرة."

    qiblaSensorNoSensor = "لم يتم العثور على مستشعر بوصلة"

    qiblaSensorLowAccuracy = "دقة البوصلة منخفضة"

    qiblaSensorReady = "دقة البوصلة طبيعية"

    qiblaCalibrationAction = "عرض إرشادات المعايرة"

    qiblaCalibrationHideAction = "إخفاء إرشادات المعايرة"

    qiblaCalibrationInstructions = "أمسك الهاتف بشكل أفقي وحركه ببطء في الهواء على شكل رقم ثمانية، ثم أمسكه أفقيًا وانتظر بضع ثوانٍ."

    tafsirVsMealHint = "الترجمة (المعنى) هي نقل معنى الآية. أما التفسير فهو شرح تفصيلي لسياق الآية ومعناها."

    textSizeLabel = "حجم النص"
 textSizeHint = "كبّر النص إذا كنت تواجه صعوبة في القراءة."
 textSizeNormal = "عادي"
 textSizeLarge = "كبير"
 textSizeExtraLarge = "كبير جدًا"

    elderlyProfileTitle = "وضع المستخدم الكبير سنًا"
 elderlyProfileSubtitle = "يكبّر النص ويزيد التباين ويوسّع الأزرار ويخفف الحركة."

    highContrastTitle = "تباين عالٍ"
 highContrastSubtitle = "يجعل النص والصفحة أوضح للتمييز."

    largeTouchTargetsTitle = "مناطق لمس أكبر"
 largeTouchTargetsSubtitle = "يجعل الأزرار والصفوف أسهل للمس."

    reducedMotionTitle = "حركة أقل"
 reducedMotionSubtitle = "يهدّئ انتقالات الصفحات والحركة البصرية."

    zakatCalculatorTitle = "حاسبة الزكاة"

    zakatWealthLabel = "إجمالي ثروتك (نقد، ذهب، فضة، إلخ)"

    zakatCalculateButton = "احسب"

    zakatResultTemplate = "زكاتك: {amount}"

    zakatNisabInfo = "الزكاة هي 2.5% من المال الذي بلغ النصاب (قيمة 80.18 غرامًا من الذهب) وحال عليه الحول. هذا دليل عام وليس فتوى دينية."

    duaListTitle = "الأدعية"
 postPrayerDhikrTitle = "أذكار بعد الصلاة"
 postPrayerDhikrSubtitle = "أكمل التسبيح بإيقاع هادئ ومتزن."
 postPrayerDhikrReset = "ابدأ من جديد"
 postPrayerDhikrStepTemplate = "الذكر {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "اكتمل الذكر"
 postPrayerDhikrNext = "الذكر التالي"

    islamicCalendarTitle = "التقويم الإسلامي"

    islamicDaysRemainingTemplate = "باقي {n} يومًا"

    streakDaysTemplate = "سلسلة قراءة لمدة {n} يومًا"

    cdShare = "مشاركة"

    hacKuraLabel = "الاستعلام عن نتيجة قرعة الحج"

    moreTitle = "المزيد"

    filterAll = "الكل"

    zakatLiveValuesTitle = "القيم الحالية"

    zakatGoldPriceLabel = "غرام الذهب (عيار 24)"

    zakatNisabTemplate = "النصاب ({grams} غرام ذهب)"

    zakatPriceDateTemplate = "السعر بتاريخ {date}"

    zakatLivePriceUnavailable = "تعذّر جلب سعر الذهب المباشر. يمكنك إدخال سعر الغرام أدناه."

    zakatGoldPriceInputLabel = "سعر غرام الذهب"

    zakatCashLabel = "النقد والحسابات المصرفية"

    zakatGoldGramsLabel = "الذهب (بالغرام، ما يعادل عيار 24)"

    zakatOtherAssetsLabel = "العملات الأجنبية والأسهم والاستثمارات الأخرى"

    zakatTradeGoodsLabel = "عروض التجارة والديون المستحقة لك"

    zakatDebtsLabel = "الديون (تُخصم)"

    zakatNetWealthTemplate = "صافي المال الخاضع للزكاة: {amount}"

    zakatBelowNisab = "صافي مالك دون النصاب، فلا تجب عليك الزكاة."

    zakatCurrencyLabel = "العملة"

    fitreTitle = "الفطرة والفدية"

    fitreAmountTemplate = "فطرة {period} (للفرد)"

    fitreSourceNote = "هو الحد الأدنى الذي حددته رئاسة الشؤون الدينية التركية؛ ومن استطاع فليزد. وفدية اليوم لمن لا يستطيع الصيام بالمقدار نفسه."

    fitrePersonsLabel = "عدد الأشخاص"

    fitreTotalTemplate = "مجموع الفطرة: {amount}"

    fidyeDaysLabel = "أيام الصيام الفائتة"

    fidyeTotalTemplate = "مجموع الفدية: {amount}"

    fitreOtherCurrencyNote = "يختلف مقدار الفطرة من بلد لآخر؛ اعتمد ما تعلنه الجهة الدينية في بلدك. المبلغ أدناه خاص بتركيا (رئاسة الشؤون الدينية)."

    restorePurchasesButton = "استعادة المشتريات"

    purchaseSuccessMessage = "شكرًا لك! تمت عملية الشراء."

    purchaseCancelledMessage = "تم إلغاء الشراء."

    purchaseUnavailableMessage = "هذا المنتج غير متاح في المتجر حاليًا. حاول لاحقًا."

    purchaseErrorMessage = "تعذّر إتمام الشراء. تحقّق من الاتصال وحاول مجددًا."

    restoreNothingMessage = "لم يُعثر على اشتراك نشط بدون إعلانات لهذا الحساب."

    adFreeUntilTemplate = "بدون إعلانات حتى {date}"

    prayerSoonTitleTemplate = "بقي {n} دقائق على {prayer}"

    prayerSoonBody = "توضّأ واستعدّ بطمأنينة."

    prayerNowTitleTemplate = "حان وقت {prayer}"

    prayerNowBody = "لحظة سكينة لقلبك؛ الصلاة بانتظارك."

    occasionTwoDaysTemplate = "{occasion} تقترب: بقي يومان"

    occasionTomorrowTemplate = "{occasion} غدًا"

    occasionTomorrowNightTemplate = "{occasion} مساء الغد"

    occasionBody = "أعدّ دعاءك ونيتك وتذكّر أحبّتك؛ ما رأيك بإحياء هذا الوقت المبارك؟"

    prayerReminderLeadLabel = "التذكير قبل الصلاة"

    reminderOffLabel = "إيقاف"

    minutesShortTemplate = "{n} د"

    prayerAtTimeLabel = "التنبيه أيضًا عند دخول الوقت"

    occasionRemindersLabel = "تذكيرات الليالي والأيام المباركة"

    notificationSoundHint = "تصل الإشعارات بنغمة ناعمة قصيرة لا تُزعج."

    themePickerTitle = "اختر السمة"

    themePickerSubtitle = "المس للتجربة؛ يُطبَّق اختيارك فورًا."

    themeChangeLaterNote = "يمكنك تغييرها في أي وقت من الإعدادات."

    themeIntroTitle = "جديد: السمات"

    themeIntroBody = "اختر المظهر الذي يعجبك. المس للتجربة واحتفظ بما يريحك."

    supportRateApp = "قيّم التطبيق"

    supportRateAppHint = "ادعمنا بتقييمك في المتجر"

    supportShareApp = "شارك التطبيق"

    supportShareAppHint = "انصح به أحباءك"

    shareAppMessage = "هدى القرآن: اقرأ القرآن واستمع إليه، مواقيت الصلاة، الأدعية وأسماء الله الحسنى. {url}"

    supportFeedback = "أرسل ملاحظاتك"

    supportFeedbackHint = "شاركنا أفكارك أو مشاكلك"

    supportPrivacyPolicy = "سياسة الخصوصية"

    supportPrivacyPolicyHint = "تعرف على كيفية استخدام بياناتك"

    supportTermsOfService = "شروط الاستخدام"

    supportTermsOfServiceHint = "اطلع على شروط الخدمة"

    fitreZakatRowTitle = "حاسبة الفطرة والزكاة"

    fitreZakatRowHint = "احسب زكاتك وفطرتك وفديتك"

    mushafNewSurahTemplate = "سورة جديدة: {surah}"

    mushafStopAction = "إيقاف"

    mushafSafetyStopTemplate = "تم الاستماع إلى {count} صفحات متتالية. هل تريد المتابعة؟"

    mushafContinueAction = "متابعة"

    mushafPageLoadFailed = "تعذّر تحميل الصفحة التالية"

    mushafGestureHint = "المس الآية للاستماع، واضغط مطولاً لمزيد من الخيارات"

    mushafGoToPlayingPage = "العودة إلى الصفحة المقروءة ({page})"

    mushafKeyboardHint = "المسافة: تشغيل/إيقاف مؤقت · ← →: قلب الصفحة"

    mushafCopiedMessage = "تم نسخ الآية"

    mushafFavoriteAdded = "أضيفت إلى المفضلة"

    mushafFavoriteRemoved = "أزيلت من المفضلة"

    mushafUndoAction = "تراجع"

    mushafAyahActionsTitleTemplate = "{surah}، الآية {ayah}"

    mushafPlayFromHere = "استمع من هنا"

    mushafCopyAction = "نسخ"

    mushafAddFavorite = "أضف إلى المفضلة"

    mushafRemoveFavorite = "أزل من المفضلة"

    shareCardTitle = "مشاركة الآية"

    shareImageAction = "مشاركة الصورة"

    shareTextAction = "مشاركة النص"

    shareCopyLink = "نسخ الرابط"

    shareLinkCopied = "تم نسخ الرابط"

    shareImageFailed = "تعذّر إعداد الصورة"

}

private val german = strings {
    navHome = "Start"
 navSurahs = "Suren"
 navFavorites = "Favoriten"
 navSettings = "Einstellungen"

    homeGreeting = "Friede sei mit dir"
 search = "Suche"
 juz = "Juz"
 reciters = "Rezitatoren"

    retry = "Erneut versuchen"
 appearance = "Erscheinungsbild"
 recitationAndTranslation = "Rezitation & Übersetzung"

    prayerNotifications = "Gebetsbenachrichtigungen"
 language = "Sprache"


    ayahWord = "Vers"
 ayahWordLower = "Verse"
 meccan = "Mekkanisch"
 medinan = "Medinensisch"

    serverUnreachable = "Server nicht erreichbar, bitte versuchen Sie es erneut."
 notFound = "Keine Ergebnisse gefunden"

    viewAll = "Alle anzeigen"


    homeSubtitle = "Starten Sie Ihren Tag mit dem Koran"
 continueReading = "Weiterlesen, wo Sie aufgehört haben"

    dailyAyahTitle = "Vers des Tages"
 dailyAyahError = "Vers konnte nicht geladen werden, erneut versuchen."

    readingLessonsTitle = "Koran-Leselektionen"

    readingLessonsSubtitleTemplate = "Von Elif-Ba bis Tadschwid, {n} geführte Lektionen mit Audio"

    quickActionsTitle = "Schnellzugriff"

    esmaulHusnaTitle = "Die 99 Namen Allahs"
 discoverQuranTitle = "Koran entdecken"

    mushafModeLabel = "Mushaf-Modus"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "Lesen"
 toggleTranslationLabel = "Übersetzung ein-/ausblenden"

    pagesLabel = "Seiten"
 manzilsLabel = "Manzil"
 rukusLabel = "Ruku"

    hizbQuartersLabel = "Hizb-Viertel"
 sajdaVersesLabel = "Sadschda-Verse"

    quranStatsTitle = "Koran-Statistiken"
 statSurah = "Sure"
 statJuz = "Juz"

    statPage = "Seite"
 statRuku = "Ruku"
 statHizbQuarter = "Hizb-Viertel"
 statManzil = "Manzil"

    statSajda = "Sadschda"
 featuredSurahsTitle = "Ausgewählte Suren"
 surahsLoadErrorShort = "Suren konnten nicht geladen werden."

    nextPrayerLabel = "Nächstes Gebet"
 hoursMinutesLeftTemplate = "{h} Std {m} Min verbleibend"

    minutesLeftTemplate = "{m} Min verbleibend"
 favoriteLabel = "Favoriten"

    todayTitle = "Heute"
 todayHadithTitle = "Hadith des Tages"
 todayDuaTitle = "Dua des Tages"

    todayOpenHadith = "Hadithe öffnen"
 todayOpenDua = "Duas öffnen"
 todayNoContent = "Inhalt ist gerade nicht verfügbar"


    surahSearchPlaceholder = "Sure suchen (Name oder Nummer)"

    surahsLoadErrorFull = "Suren konnten nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen."


    surahFallback = "Sure"

    surahLoadErrorFull = "Sure konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen."


    myFavoritesTitle = "Meine Favoriten"

    favoritesEmptyMessage = "Sie haben noch keine favorisierten Verse. Tippen Sie beim Lesen auf das ⭐-Symbol, um einen hinzuzufügen."

    surahFallbackNumberedTemplate = "Sure {n}"


    settingsTitle = "Einstellungen"
 languagePickerSubtitleTemplate = "Aus {n} Sprachen wählen"

    reciterLabel = "Rezitator"
 translationLabel = "Übersetzung"

    notificationsToggleTitle = "Benachrichtigungen aktivieren"
 notificationsToggleSubtitle = "Benachrichtigung senden, wenn eine Gebetszeit beginnt"

    locationLabel = "Standort"
 locationAutoUpdateNote = "Benachrichtigungen werden automatisch aktualisiert, sobald Ihr Standort gespeichert ist."

    supportUsTitle = "Unterstützen Sie uns"
 adFreeActiveMessage = "Ihre werbefreie Mitgliedschaft ist aktiv, vielen Dank! 💚"

    supportUsMessage = "Helfen Sie uns, die App weiter zu verbessern"
 smallDonationButton = "☕ Kleine Spende"

    mediumDonationButton = "🎁 Mittlere Spende"
 sixMonthAdFreeButton = "✨ 6 Monate werbefreie Mitgliedschaft"

    themeSystem = "🖥️ System"
 themeLight = "☀️ Hell"
 themeDark = "🌙 Dunkel"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Suchen"

    selectTranslationTitle = "Übersetzung wählen"
 selectLocationTitle = "Standort wählen"
 selectLanguageTitle = "Sprache wählen"


    searchTitle = "Im Koran suchen"
 searchAyahPlaceholder = "Wort in Versen suchen..."

    searchIdleHint = "Beginnen Sie zu tippen, um in der Koranübersetzung zu suchen."

    searchErrorFull = "Suche fehlgeschlagen. Server nicht erreichbar, bitte erneut versuchen."

    searchNoResultsTemplate = "Keine Ergebnisse für \"{query}\"."


    recitersCountSubtitleTemplate = "{n} Rezitatoren • anhören und auswählen"

    searchReciterPlaceholder = "Rezitator suchen..."

    recitersLoadErrorFull = "Rezitatorenliste konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen."

    selectedReciterLabel = "Ausgewählter Rezitator"
 tapToSelectLabel = "Zum Auswählen tippen"


    juzSingular = "Juz"
 pageSingular = "Seite"
 pagePlural = "Seiten"

    manzilSingular = "Manzil"
 manzilPlural = "Manzil"
 rukuSingular = "Ruku"
 rukuPlural = "Ruku"

    hizbQuarterSingular = "Hizb-Viertel"
 hizbQuarterPlural = "Hizb-Viertel"

    sectionLoadErrorTemplate = "{title} konnte nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen."


    sajdaTitle = "Sadschda-Verse"
 sajdaSubtitle = "Verse im Koran, die eine Rezitationsniederwerfung erfordern"

    sajdaLoadErrorFull = "Sadschda-Verse konnten nicht geladen werden. Server nicht erreichbar, bitte erneut versuchen."


    nowPlayingTitle = "Wird gerade abgespielt"
 wholeSurahSuffix = "Ganze Sure"
 playbackSpeedTitle = "Wiedergabegeschwindigkeit"


    onboardingSkip = "Überspringen"
 onboardingNext = "Weiter"
 onboardingStart = "Loslegen"

    onboardTitle1 = "Willkommen bei Huda Qur'an"

    onboardDesc1 = "Tragen Sie den Heiligen Koran jederzeit und überall bei sich."

    onboardTitle2 = "Audio-Rezitation"

    onboardDesc2 = "Hören Sie Vers für Vers oder Sure für Sure, vom Rezitator Ihrer Wahl."

    onboardTitle3 = "Übersetzungen"

    onboardDesc3 = "Verstehen Sie Verse mit Übersetzungen auf Türkisch und in anderen Sprachen."

    onboardTitle4 = "Favoriten & Weiterlesen"

    onboardDesc4 = "Speichern Sie Ihre Lieblingsverse und lesen Sie genau dort weiter, wo Sie aufgehört haben."


    appTagline = "Der Heilige Koran, immer bei Ihnen"


    lessonsSubtitleTemplate = "Von Elif-Ba bis Tadschwid, {n} geführte Lektionen"
 lessonNotFound = "Lektion nicht gefunden"

    lessonFallback = "Lektion"
 pronounceContentDescription = "Aussprache anhören"


    cancelLabel = "Abbrechen"
 goLabel = "Los"

    mushafJumpToPageTitle = "Zu Seite springen"
 mushafJumpToPageHint = "Seitenzahl (1-604)"

    mushafResumeSubtitleTemplate = "Sie waren auf Seite {n} — weiterlesen"

    mushafStartSubtitle = "Lesen Sie wie in einem echten Buch, Seite für Seite"


    khatmProgressTitle = "Ihr Khatm-Fortschritt"

    khatmProgressTemplate = "Seite {page} / {total} (%{percent})"

    khatmCompletedCountTemplate = "Abgeschlossene Khatms: {n}"


    qiblaTitle = "Qibla-Kompass"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Öffnen Sie den Kompass Ihres Telefons und richten Sie diesen Winkel nach Norden aus."

    qiblaLocationUnavailable = "Standort konnte nicht ermittelt werden. Stellen Sie sicher, dass Stadt/Land in den Einstellungen festgelegt sind."

    cdBack = "Zurück"
 cdPlay = "Abspielen"
 cdPause = "Pause"
 cdClose = "Schließen"
 cdRepeat = "Wiederholen"
 cdPrevious = "Vorherige"
 cdNext = "Weiter"

    tafsirLabel = "Tafsir"
 selectTafsirTitle = "Tafsir auswählen"
 changeTafsirLabel = "Tafsir ändern"
 tafsirUnavailable = "Der Tafsir für diesen Vers konnte nicht geladen werden."
 tafsirNotSelected = "Kein Tafsir ausgewählt"

    qiblaLiveInstructions = "Halte dein Telefon flach und drehe dich langsam, bis der Pfeil mit dem Dreieck über dem Kreis übereinstimmt."

    qiblaSensorNoSensor = "Kein Kompasssensor gefunden"

    qiblaSensorLowAccuracy = "Die Kompassgenauigkeit ist niedrig"

    qiblaSensorReady = "Die Kompassgenauigkeit ist normal"

    qiblaCalibrationAction = "Kalibrierungshilfe anzeigen"

    qiblaCalibrationHideAction = "Kalibrierungshilfe ausblenden"

    qiblaCalibrationInstructions = "Halten Sie das Telefon flach und bewegen Sie es langsam in der Luft in Form einer Acht. Halten Sie es danach wieder flach und warten Sie einige Sekunden."

    tafsirVsMealHint = "Die Übersetzung überträgt die Bedeutung des Verses. Der Tafsir ist eine ausführliche Erklärung seines Kontexts und seiner Bedeutung."

    textSizeLabel = "Textgröße"
 textSizeHint = "Vergrößern Sie den Text, wenn Sie Schwierigkeiten beim Lesen haben."
 textSizeNormal = "Normal"
 textSizeLarge = "Groß"
 textSizeExtraLarge = "Sehr groß"

    elderlyProfileTitle = "Profil für ältere Nutzer"
 elderlyProfileSubtitle = "Vergrößert Text, erhöht den Kontrast, vergrößert Bedienelemente und reduziert Bewegung."

    highContrastTitle = "Hoher Kontrast"
 highContrastSubtitle = "Macht Text und Seite leichter unterscheidbar."

    largeTouchTargetsTitle = "Größere Touchflächen"
 largeTouchTargetsSubtitle = "Macht Schaltflächen und Zeilen leichter antippbar."

    reducedMotionTitle = "Weniger Bewegung"
 reducedMotionSubtitle = "Beruhigt Seitenübergänge und visuelle Bewegungen."

    zakatCalculatorTitle = "Zakat-Rechner"

    zakatWealthLabel = "Ihr Gesamtvermögen (Bargeld, Gold, Silber usw.)"

    zakatCalculateButton = "Berechnen"

    zakatResultTemplate = "Ihre Zakat: {amount}"

    zakatNisabInfo = "Die Zakat beträgt 2,5 % des Vermögens, das die Nisab-Grenze (Wert von 80,18 g Gold) erreicht und ein volles Jahr gehalten wurde. Dies ist eine allgemeine Orientierung, keine religiöse Rechtsauskunft."

    duaListTitle = "Bittgebete"
 postPrayerDhikrTitle = "Dhikr nach dem Gebet"
 postPrayerDhikrSubtitle = "Vervollständigen Sie Ihr Gedenken in ruhigem Tempo."
 postPrayerDhikrReset = "Neu beginnen"
 postPrayerDhikrStepTemplate = "Dhikr {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Dhikr abgeschlossen"
 postPrayerDhikrNext = "Nächster Dhikr"

    islamicCalendarTitle = "Islamischer Kalender"

    islamicDaysRemainingTemplate = "Noch {n} Tage"

    streakDaysTemplate = "{n}-Tage-Leseserie"

    cdShare = "Teilen"

    hacKuraLabel = "Haddsch-Losergebnis abfragen"

    moreTitle = "Weiteres"

    filterAll = "Alle"

    zakatLiveValuesTitle = "Aktuelle Werte"

    zakatGoldPriceLabel = "Gold pro Gramm (24 Karat)"

    zakatNisabTemplate = "Nisab ({grams} g Gold)"

    zakatPriceDateTemplate = "Preis vom {date}"

    zakatLivePriceUnavailable = "Der aktuelle Goldpreis konnte nicht geladen werden. Sie können den Grammpreis unten selbst eingeben."

    zakatGoldPriceInputLabel = "Goldpreis pro Gramm"

    zakatCashLabel = "Bargeld und Bankkonten"

    zakatGoldGramsLabel = "Gold (Gramm, 24-Karat-Äquivalent)"

    zakatOtherAssetsLabel = "Devisen, Aktien und andere Anlagen"

    zakatTradeGoodsLabel = "Handelswaren und Forderungen"

    zakatDebtsLabel = "Schulden (werden abgezogen)"

    zakatNetWealthTemplate = "Zakat-pflichtiges Nettovermögen: {amount}"

    zakatBelowNisab = "Ihr Nettovermögen liegt unter dem Nisab; es ist keine Zakat fällig."

    zakatCurrencyLabel = "Währung"

    fitreTitle = "Fitra und Fidya"

    fitreAmountTemplate = "Fitra {period} (pro Person)"

    fitreSourceNote = "Mindestbetrag, festgelegt vom türkischen Präsidium für Religionsangelegenheiten (Diyanet); wer kann, darf mehr geben. Die tägliche Fidya für versäumte Fastentage entspricht diesem Betrag."

    fitrePersonsLabel = "Anzahl der Personen"

    fitreTotalTemplate = "Fitra gesamt: {amount}"

    fidyeDaysLabel = "Versäumte Fastentage"

    fidyeTotalTemplate = "Fidya gesamt: {amount}"

    fitreOtherCurrencyNote = "Der Fitra-Betrag ist von Land zu Land verschieden; richten Sie sich nach der religiösen Instanz Ihres Wohnorts. Der folgende Betrag gilt für die Türkei (Diyanet)."

    restorePurchasesButton = "Käufe wiederherstellen"

    purchaseSuccessMessage = "Vielen Dank! Ihr Kauf ist abgeschlossen."

    purchaseCancelledMessage = "Kauf abgebrochen."

    purchaseUnavailableMessage = "Dieses Produkt ist im Store derzeit nicht verfügbar. Bitte später erneut versuchen."

    purchaseErrorMessage = "Der Kauf konnte nicht abgeschlossen werden. Prüfen Sie Ihre Verbindung und versuchen Sie es erneut."

    restoreNothingMessage = "Für dieses Konto wurde kein aktives werbefreies Abo gefunden."

    adFreeUntilTemplate = "Werbefrei bis {date}"

    prayerSoonTitleTemplate = "Noch {n} Minuten bis {prayer}"

    prayerSoonBody = "Nimm die Waschung und bereite dich in Ruhe vor."

    prayerNowTitleTemplate = "Es ist Zeit für {prayer}"

    prayerNowBody = "Ein Atemzug Ruhe für dein Herz; das Gebet wartet."

    occasionTwoDaysTemplate = "{occasion} naht: noch 2 Tage"

    occasionTomorrowTemplate = "Morgen ist {occasion}"

    occasionTomorrowNightTemplate = "Morgen Abend ist {occasion}"

    occasionBody = "Bereite deine Gebete, deine Absicht und deine Liebsten vor – wie wäre es, diese gesegnete Zeit zu beleben?"

    prayerReminderLeadLabel = "Vor dem Gebet erinnern"

    reminderOffLabel = "Aus"

    minutesShortTemplate = "{n} Min."

    prayerAtTimeLabel = "Auch zu Beginn der Gebetszeit benachrichtigen"

    occasionRemindersLabel = "Erinnerungen an gesegnete Tage und Nächte"

    notificationSoundHint = "Benachrichtigungen kommen mit einem sanften, kurzen Klang – nie mit einem schrillen Alarm."

    themePickerTitle = "Wähle dein Design"

    themePickerSubtitle = "Tippe zum Ausprobieren – die Auswahl gilt sofort."

    themeChangeLaterNote = "Du kannst es jederzeit in den Einstellungen ändern."

    themeIntroTitle = "Neu: Designs"

    themeIntroBody = "Wähle das Aussehen, das dir gefällt. Tippe zum Ausprobieren und behalte, was sich richtig anfühlt."

    supportRateApp = "App bewerten"

    supportRateAppHint = "Unterstützen Sie uns mit einer Bewertung im Store"

    supportShareApp = "App teilen"

    supportShareAppHint = "Empfehlen Sie sie Ihren Liebsten"

    shareAppMessage = "Huda Qur'an: Koran lesen und hören, Gebetszeiten, Duas und die Namen Allahs. {url}"

    supportFeedback = "Feedback senden"

    supportFeedbackHint = "Teilen Sie uns Ideen oder Probleme mit"

    supportPrivacyPolicy = "Datenschutzerklärung"

    supportPrivacyPolicyHint = "Erfahren Sie, wie Ihre Daten verwendet werden"

    supportTermsOfService = "Nutzungsbedingungen"

    supportTermsOfServiceHint = "Lesen Sie unsere Nutzungsbedingungen"

    fitreZakatRowTitle = "Fitra- / Zakat-Rechner"

    fitreZakatRowHint = "Berechnen Sie Zakat, Fitra und Fidya"

    mushafNewSurahTemplate = "Neue Sure: {surah}"

    mushafStopAction = "Stopp"

    mushafSafetyStopTemplate = "{count} Seiten am Stück gehört. Weiter zuhören?"

    mushafContinueAction = "Weiter"

    mushafPageLoadFailed = "Nächste Seite konnte nicht geladen werden"

    mushafGestureHint = "Vers antippen zum Anhören, gedrückt halten für weitere Aktionen"

    mushafGoToPlayingPage = "Zurück zur gelesenen Seite ({page})"

    mushafKeyboardHint = "Leertaste: Wiedergabe/Pause · ← →: umblättern"

    mushafCopiedMessage = "Vers kopiert"

    mushafFavoriteAdded = "Zu Favoriten hinzugefügt"

    mushafFavoriteRemoved = "Aus Favoriten entfernt"

    mushafUndoAction = "Rückgängig"

    mushafAyahActionsTitleTemplate = "{surah}, Vers {ayah}"

    mushafPlayFromHere = "Ab hier anhören"

    mushafCopyAction = "Kopieren"

    mushafAddFavorite = "Zu Favoriten hinzufügen"

    mushafRemoveFavorite = "Aus Favoriten entfernen"

    shareCardTitle = "Vers teilen"

    shareImageAction = "Bild teilen"

    shareTextAction = "Text teilen"

    shareCopyLink = "Link kopieren"

    shareLinkCopied = "Link kopiert"

    shareImageFailed = "Bild konnte nicht erstellt werden"

}

private val french = strings {
    navHome = "Accueil"
 navSurahs = "Sourates"
 navFavorites = "Favoris"
 navSettings = "Paramètres"

    homeGreeting = "Que la paix soit sur vous"
 search = "Rechercher"
 juz = "Juz"
 reciters = "Récitateurs"

    retry = "Réessayer"
 appearance = "Apparence"
 recitationAndTranslation = "Récitation et traduction"

    prayerNotifications = "Notifications de prière"
 language = "Langue"


    ayahWord = "Verset"
 ayahWordLower = "versets"
 meccan = "Mecquoise"
 medinan = "Médinoise"

    serverUnreachable = "Impossible de joindre le serveur, veuillez réessayer."
 notFound = "Aucun résultat trouvé"

    viewAll = "Tout voir"


    homeSubtitle = "Commencez votre journée avec le Coran"
 continueReading = "Reprendre là où vous vous êtes arrêté"

    dailyAyahTitle = "Verset du jour"
 dailyAyahError = "Impossible de charger le verset, réessayez."

    readingLessonsTitle = "Leçons de lecture du Coran"

    readingLessonsSubtitleTemplate = "De l'Elif-Ba au tajwid, {n} leçons guidées avec audio"

    quickActionsTitle = "Accès rapide"

    esmaulHusnaTitle = "Les 99 noms d'Allah"
 discoverQuranTitle = "Explorer le Coran"

    mushafModeLabel = "Mode Mushaf"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "Lecture"
 toggleTranslationLabel = "Afficher/Masquer la traduction"

    pagesLabel = "Pages"
 manzilsLabel = "Manzils"
 rukusLabel = "Rukus"

    hizbQuartersLabel = "Quarts de Hizb"
 sajdaVersesLabel = "Versets de la Sajda"

    quranStatsTitle = "Statistiques du Coran"
 statSurah = "Sourate"
 statJuz = "Juz"

    statPage = "Page"
 statRuku = "Ruku"
 statHizbQuarter = "Quart de Hizb"
 statManzil = "Manzil"

    statSajda = "Sajda"
 featuredSurahsTitle = "Sourates en vedette"
 surahsLoadErrorShort = "Impossible de charger les sourates."

    nextPrayerLabel = "Prochaine prière"
 hoursMinutesLeftTemplate = "{h} h {m} min restantes"

    minutesLeftTemplate = "{m} min restantes"
 favoriteLabel = "Favoris"

    todayTitle = "Aujourd’hui"
 todayHadithTitle = "Hadith du jour"
 todayDuaTitle = "Invocation du jour"

    todayOpenHadith = "Ouvrir les hadiths"
 todayOpenDua = "Ouvrir les invocations"
 todayNoContent = "Contenu indisponible pour le moment"


    surahSearchPlaceholder = "Rechercher une sourate (nom ou numéro)"

    surahsLoadErrorFull = "Impossible de charger les sourates. Serveur injoignable, veuillez réessayer."


    surahFallback = "Sourate"

    surahLoadErrorFull = "Impossible de charger la sourate. Serveur injoignable, veuillez réessayer."


    myFavoritesTitle = "Mes favoris"

    favoritesEmptyMessage = "Vous n'avez pas encore de versets favoris. Touchez l'icône ⭐ pendant la lecture pour en ajouter un ici."

    surahFallbackNumberedTemplate = "Sourate {n}"


    settingsTitle = "Paramètres"
 languagePickerSubtitleTemplate = "Choisissez parmi {n} langues"

    reciterLabel = "Récitateur"
 translationLabel = "Traduction"

    notificationsToggleTitle = "Activer les notifications"
 notificationsToggleSubtitle = "Envoyer une notification à l'heure de prière"

    locationLabel = "Emplacement"
 locationAutoUpdateNote = "Les notifications se mettent à jour automatiquement une fois l'emplacement enregistré."

    supportUsTitle = "Soutenez-nous"
 adFreeActiveMessage = "Votre abonnement sans publicité est actif, merci ! 💚"

    supportUsMessage = "Aidez-nous à améliorer l'application"
 smallDonationButton = "☕ Petit don"

    mediumDonationButton = "🎁 Don moyen"
 sixMonthAdFreeButton = "✨ Abonnement sans pub 6 mois"

    themeSystem = "🖥️ Système"
 themeLight = "☀️ Clair"
 themeDark = "🌙 Sombre"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Rechercher"

    selectTranslationTitle = "Choisir la traduction"
 selectLocationTitle = "Choisir l'emplacement"
 selectLanguageTitle = "Choisir la langue"


    searchTitle = "Rechercher dans le Coran"
 searchAyahPlaceholder = "Rechercher un mot dans les versets..."

    searchIdleHint = "Commencez à taper pour rechercher dans la traduction du Coran."

    searchErrorFull = "Échec de la recherche. Serveur injoignable, veuillez réessayer."

    searchNoResultsTemplate = "Aucun résultat pour \"{query}\"."


    recitersCountSubtitleTemplate = "{n} récitateurs • écoutez et choisissez"

    searchReciterPlaceholder = "Rechercher un récitateur..."

    recitersLoadErrorFull = "Impossible de charger les récitateurs. Serveur injoignable, veuillez réessayer."

    selectedReciterLabel = "Récitateur sélectionné"
 tapToSelectLabel = "Touchez pour sélectionner"


    juzSingular = "Juz"
 pageSingular = "Page"
 pagePlural = "Pages"

    manzilSingular = "Manzil"
 manzilPlural = "Manzils"
 rukuSingular = "Ruku"
 rukuPlural = "Rukus"

    hizbQuarterSingular = "Quart de Hizb"
 hizbQuarterPlural = "Quarts de Hizb"

    sectionLoadErrorTemplate = "Impossible de charger {title}. Serveur injoignable, veuillez réessayer."


    sajdaTitle = "Versets de la Sajda"
 sajdaSubtitle = "Versets du Coran nécessitant une prosternation de récitation"

    sajdaLoadErrorFull = "Impossible de charger les versets de la Sajda. Serveur injoignable, veuillez réessayer."


    nowPlayingTitle = "Lecture en cours"
 wholeSurahSuffix = "Sourate entière"
 playbackSpeedTitle = "Vitesse de lecture"


    onboardingSkip = "Passer"
 onboardingNext = "Suivant"
 onboardingStart = "Commencer"

    onboardTitle1 = "Bienvenue sur Huda Qur'an"

    onboardDesc1 = "Emportez le Saint Coran avec vous, à tout moment, partout."

    onboardTitle2 = "Écoute audio"

    onboardDesc2 = "Écoutez verset par verset ou sourate par sourate, avec le récitateur de votre choix."

    onboardTitle3 = "Traductions"

    onboardDesc3 = "Comprenez les versets grâce aux traductions en turc et dans d'autres langues."

    onboardTitle4 = "Favoris et reprise de lecture"

    onboardDesc4 = "Enregistrez vos versets préférés et reprenez la lecture là où vous vous êtes arrêté."


    appTagline = "Le Saint Coran, toujours avec vous"


    lessonsSubtitleTemplate = "De l'Elif-Ba au tajwid, {n} leçons guidées"
 lessonNotFound = "Leçon introuvable"

    lessonFallback = "Leçon"
 pronounceContentDescription = "Écouter la prononciation"


    cancelLabel = "Annuler"
 goLabel = "Aller"

    mushafJumpToPageTitle = "Aller à la page"
 mushafJumpToPageHint = "Numéro de page (1-604)"

    mushafResumeSubtitleTemplate = "Vous étiez à la page {n} — continuer la lecture"

    mushafStartSubtitle = "Lisez comme un vrai livre, page par page"


    khatmProgressTitle = "Votre progression du khatm"

    khatmProgressTemplate = "Page {page} / {total} (%{percent})"

    khatmCompletedCountTemplate = "Khatms terminés : {n}"


    qiblaTitle = "Boussole Qibla"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Ouvrez la boussole de votre téléphone et alignez cet angle avec le nord."

    qiblaLocationUnavailable = "Impossible d'obtenir votre position. Vérifiez que la ville/le pays sont définis dans les Paramètres."

    cdBack = "Retour"
 cdPlay = "Lecture"
 cdPause = "Pause"
 cdClose = "Fermer"
 cdRepeat = "Répéter"
 cdPrevious = "Précédent"
 cdNext = "Suivant"

    tafsirLabel = "Tafsir"
 selectTafsirTitle = "Choisir le tafsir"
 changeTafsirLabel = "Changer le tafsir"
 tafsirUnavailable = "Impossible de charger le tafsir de ce verset."
 tafsirNotSelected = "Aucun tafsir sélectionné"

    qiblaLiveInstructions = "Tenez votre téléphone à plat et tournez lentement jusqu'à ce que la flèche s'aligne avec le triangle au-dessus du cercle."

    qiblaSensorNoSensor = "Aucun capteur de boussole détecté"

    qiblaSensorLowAccuracy = "La précision de la boussole est faible"

    qiblaSensorReady = "La précision de la boussole est normale"

    qiblaCalibrationAction = "Afficher le guide d'étalonnage"

    qiblaCalibrationHideAction = "Masquer le guide d'étalonnage"

    qiblaCalibrationInstructions = "Tenez le téléphone à plat et déplacez-le lentement dans l'air en formant un huit. Tenez-le ensuite à plat et attendez quelques secondes."

    tafsirVsMealHint = "La traduction rend le sens du verset. Le tafsir est une explication savante plus détaillée de son contexte et de sa signification."

    textSizeLabel = "Taille du texte"
 textSizeHint = "Agrandissez le texte si vous avez du mal à le lire."
 textSizeNormal = "Normal"
 textSizeLarge = "Grand"
 textSizeExtraLarge = "Très grand"

    elderlyProfileTitle = "Profil pour utilisateurs âgés"
 elderlyProfileSubtitle = "Agrandit le texte, augmente le contraste, élargit les commandes et réduit les mouvements."

    highContrastTitle = "Contraste élevé"
 highContrastSubtitle = "Rend le texte et la page plus faciles à distinguer."

    largeTouchTargetsTitle = "Zones tactiles plus grandes"
 largeTouchTargetsSubtitle = "Rend les boutons et les lignes plus faciles à toucher."

    reducedMotionTitle = "Mouvement réduit"
 reducedMotionSubtitle = "Adoucit les transitions et les mouvements visuels."

    zakatCalculatorTitle = "Calculateur de zakat"

    zakatWealthLabel = "Votre patrimoine total (espèces, or, argent, etc.)"

    zakatCalculateButton = "Calculer"

    zakatResultTemplate = "Votre zakat : {amount}"

    zakatNisabInfo = "La zakat est de 2,5 % du patrimoine ayant atteint le seuil du nisab (valeur de 80,18 g d’or) et conservé pendant une année complète. Ceci est un guide général, pas un avis religieux."

    duaListTitle = "Invocations"
 postPrayerDhikrTitle = "Dhikr après la prière"
 postPrayerDhikrSubtitle = "Terminez vos invocations à un rythme calme et régulier."
 postPrayerDhikrReset = "Recommencer"
 postPrayerDhikrStepTemplate = "Dhikr {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Dhikr terminé"
 postPrayerDhikrNext = "Dhikr suivant"

    islamicCalendarTitle = "Calendrier islamique"

    islamicDaysRemainingTemplate = "{n} jours restants"

    streakDaysTemplate = "Série de lecture de {n} jours"

    cdShare = "Partager"

    hacKuraLabel = "Consulter le résultat du tirage au sort du Hajj"

    moreTitle = "Plus"

    filterAll = "Toutes"

    zakatLiveValuesTitle = "Valeurs actuelles"

    zakatGoldPriceLabel = "Or au gramme (24 carats)"

    zakatNisabTemplate = "Nisab ({grams} g d'or)"

    zakatPriceDateTemplate = "Prix du {date}"

    zakatLivePriceUnavailable = "Impossible de récupérer le cours de l'or en direct. Vous pouvez saisir le prix au gramme ci-dessous."

    zakatGoldPriceInputLabel = "Prix de l'or au gramme"

    zakatCashLabel = "Liquidités et comptes bancaires"

    zakatGoldGramsLabel = "Or (grammes, équivalent 24 carats)"

    zakatOtherAssetsLabel = "Devises, actions et autres placements"

    zakatTradeGoodsLabel = "Marchandises et créances"

    zakatDebtsLabel = "Dettes (déduites)"

    zakatNetWealthTemplate = "Patrimoine net soumis à la zakat : {amount}"

    zakatBelowNisab = "Votre patrimoine net est inférieur au nisab : aucune zakat n'est due."

    zakatCurrencyLabel = "Devise"

    fitreTitle = "Fitra et Fidya"

    fitreAmountTemplate = "Fitra {period} (par personne)"

    fitreSourceNote = "Montant minimum fixé par la Présidence des affaires religieuses de Turquie (Diyanet) ; ceux qui le peuvent donnent davantage. La fidya quotidienne pour un jeûne manqué est du même montant."

    fitrePersonsLabel = "Nombre de personnes"

    fitreTotalTemplate = "Total de la fitra : {amount}"

    fidyeDaysLabel = "Jours de jeûne manqués"

    fidyeTotalTemplate = "Total de la fidya : {amount}"

    fitreOtherCurrencyNote = "Le montant de la fitra varie selon les pays ; suivez celui annoncé par l'autorité religieuse de votre lieu de résidence. Le montant ci-dessous concerne la Turquie (Diyanet)."

    restorePurchasesButton = "Restaurer les achats"

    purchaseSuccessMessage = "Merci ! Votre achat est terminé."

    purchaseCancelledMessage = "Achat annulé."

    purchaseUnavailableMessage = "Ce produit n'est pas disponible dans la boutique pour le moment. Réessayez plus tard."

    purchaseErrorMessage = "L'achat n'a pas pu être finalisé. Vérifiez votre connexion et réessayez."

    restoreNothingMessage = "Aucun abonnement sans publicité actif n'a été trouvé pour ce compte."

    adFreeUntilTemplate = "Sans publicité jusqu'au {date}"

    prayerSoonTitleTemplate = "Plus que {n} minutes avant {prayer}"

    prayerSoonBody = "Fais tes ablutions et prépare-toi sereinement."

    prayerNowTitleTemplate = "C'est l'heure de {prayer}"

    prayerNowBody = "Un souffle de sérénité pour ton cœur ; la prière t'attend."

    occasionTwoDaysTemplate = "{occasion} approche : plus que 2 jours"

    occasionTomorrowTemplate = "{occasion}, c'est demain"

    occasionTomorrowNightTemplate = "{occasion}, c'est demain soir"

    occasionBody = "Prépare tes invocations, ton intention et pense à tes proches : et si tu faisais revivre ce temps béni ?"

    prayerReminderLeadLabel = "Rappel avant la prière"

    reminderOffLabel = "Désactivé"

    minutesShortTemplate = "{n} min"

    prayerAtTimeLabel = "Prévenir aussi à l'entrée de l'heure"

    occasionRemindersLabel = "Rappels des jours et nuits bénis"

    notificationSoundHint = "Les notifications arrivent avec un carillon doux et bref, jamais une alarme stridente."

    themePickerTitle = "Choisissez votre thème"

    themePickerSubtitle = "Touchez pour essayer ; votre choix s'applique aussitôt."

    themeChangeLaterNote = "Vous pouvez le changer à tout moment dans les Réglages."

    themeIntroTitle = "Nouveau : les thèmes"

    themeIntroBody = "Choisissez l'apparence qui vous plaît. Touchez pour essayer et gardez celle qui vous convient."

    supportRateApp = "Noter l'application"

    supportRateAppHint = "Soutenez-nous avec un avis sur le store"

    supportShareApp = "Partager l'application"

    supportShareAppHint = "Recommandez-la à vos proches"

    shareAppMessage = "Huda Coran : lisez et écoutez le Coran, horaires de prière, invocations et les Noms d'Allah. {url}"

    supportFeedback = "Envoyer un avis"

    supportFeedbackHint = "Dites-nous vos idées ou vos problèmes"

    supportPrivacyPolicy = "Politique de confidentialité"

    supportPrivacyPolicyHint = "Découvrez comment vos données sont utilisées"

    supportTermsOfService = "Conditions d'utilisation"

    supportTermsOfServiceHint = "Consultez nos conditions d'utilisation"

    fitreZakatRowTitle = "Calcul de la fitra / zakat"

    fitreZakatRowHint = "Calculez votre zakat, fitra et fidya"

    mushafNewSurahTemplate = "Nouvelle sourate : {surah}"

    mushafStopAction = "Arrêter"

    mushafSafetyStopTemplate = "{count} pages écoutées d'affilée. Continuer l'écoute ?"

    mushafContinueAction = "Continuer"

    mushafPageLoadFailed = "Impossible de charger la page suivante"

    mushafGestureHint = "Touchez un verset pour l'écouter, maintenez-le pour plus d'actions"

    mushafGoToPlayingPage = "Revenir à la page en lecture ({page})"

    mushafKeyboardHint = "Espace : lecture/pause · ← → : tourner la page"

    mushafCopiedMessage = "Verset copié"

    mushafFavoriteAdded = "Ajouté aux favoris"

    mushafFavoriteRemoved = "Retiré des favoris"

    mushafUndoAction = "Annuler"

    mushafAyahActionsTitleTemplate = "{surah}, verset {ayah}"

    mushafPlayFromHere = "Écouter à partir d'ici"

    mushafCopyAction = "Copier"

    mushafAddFavorite = "Ajouter aux favoris"

    mushafRemoveFavorite = "Retirer des favoris"

    shareCardTitle = "Partager le verset"

    shareImageAction = "Partager l'image"

    shareTextAction = "Partager le texte"

    shareCopyLink = "Copier le lien"

    shareLinkCopied = "Lien copié"

    shareImageFailed = "Impossible de préparer l'image"

}

private val uzbek = strings {
    navHome = "Bosh sahifa"
 navSurahs = "Suralar"
 navFavorites = "Sevimlilar"
 navSettings = "Sozlamalar"

    homeGreeting = "Assalomu alaykum"
 search = "Qidiruv"
 juz = "Juz"
 reciters = "Qorilar"

    retry = "Qayta urinish"
 appearance = "Ko'rinish"
 recitationAndTranslation = "Qiroat va tarjima"

    prayerNotifications = "Namoz bildirishnomalari"
 language = "Til"


    ayahWord = "Oyat"
 ayahWordLower = "oyat"
 meccan = "Makkiy"
 medinan = "Madaniy"

    serverUnreachable = "Serverga ulanib bo'lmadi, qayta urinib ko'ring."
 notFound = "Natija topilmadi"

    viewAll = "Barchasini ko'rish"


    homeSubtitle = "Kuningizni Qur'on bilan boshlang"
 continueReading = "To'xtagan joyingizdan davom eting"

    dailyAyahTitle = "Kunning oyati"
 dailyAyahError = "Oyat yuklanmadi, qayta urinib ko'ring."

    readingLessonsTitle = "Qur'on o'qish darslari"

    readingLessonsSubtitleTemplate = "Elif-Bodan tajvidgacha, ovozli {n} ketma-ket dars"

    quickActionsTitle = "Tezkor havolalar"

    esmaulHusnaTitle = "Allohning 99 go'zal ismi"
 discoverQuranTitle = "Qur'onni kashf eting"

    mushafModeLabel = "Mushaf Rejimi"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "O‘qish"
 toggleTranslationLabel = "Tarjimani Ko'rsatish/Yashirish"

    pagesLabel = "Sahifalar"
 manzilsLabel = "Manzillar"
 rukusLabel = "Ruku'lar"

    hizbQuartersLabel = "Hizb choraklari"
 sajdaVersesLabel = "Sajda oyatlari"

    quranStatsTitle = "Qur'oni Karim statistikasi"
 statSurah = "Sura"
 statJuz = "Juz"

    statPage = "Sahifa"
 statRuku = "Ruku'"
 statHizbQuarter = "Hizb chorak"
 statManzil = "Manzil"

    statSajda = "Sajda"
 featuredSurahsTitle = "Mashhur suralar"
 surahsLoadErrorShort = "Suralar yuklanmadi."

    nextPrayerLabel = "Keyingi namoz"
 hoursMinutesLeftTemplate = "{h} soat {m} daqiqa qoldi"

    minutesLeftTemplate = "{m} daqiqa qoldi"
 favoriteLabel = "Sevimli"

    todayTitle = "Bugun"
 todayHadithTitle = "Bugungi hadis"
 todayDuaTitle = "Bugungi duo"

    todayOpenHadith = "Hadislarni ochish"
 todayOpenDua = "Duolarni ochish"
 todayNoContent = "Kontent hozir mavjud emas"


    surahSearchPlaceholder = "Sura qidirish (nomi yoki raqami)"

    surahsLoadErrorFull = "Suralar yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."


    surahFallback = "Sura"

    surahLoadErrorFull = "Sura yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."


    myFavoritesTitle = "Sevimlilarim"

    favoritesEmptyMessage = "Hali sevimli oyatlaringiz yo'q. O'qish paytida ⭐ belgisini bosib, shu yerga qo'shishingiz mumkin."

    surahFallbackNumberedTemplate = "{n}-sura"


    settingsTitle = "Sozlamalar"
 languagePickerSubtitleTemplate = "{n} til orasidan tanlang"

    reciterLabel = "Qori"
 translationLabel = "Tarjima"

    notificationsToggleTitle = "Bildirishnomalarni yoqish"
 notificationsToggleSubtitle = "Namoz vaqti kirganda bildirishnoma yuborish"

    locationLabel = "Manzil"
 locationAutoUpdateNote = "Manzil saqlangach, bildirishnomalar avtomatik yangilanadi."

    supportUsTitle = "Bizni qo'llab-quvvatlang"
 adFreeActiveMessage = "Reklamasiz a'zoligingiz faol, rahmat! 💚"

    supportUsMessage = "Ilovani rivojlantirishga yordam bering"
 smallDonationButton = "☕ Kichik hadya"

    mediumDonationButton = "🎁 O'rta hadya"
 sixMonthAdFreeButton = "✨ 6 oylik reklamasiz a'zolik"

    themeSystem = "🖥️ Tizim"
 themeLight = "☀️ Yorug'"
 themeDark = "🌙 Tungi"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Qidirish"

    selectTranslationTitle = "Tarjima tanlang"
 selectLocationTitle = "Manzil tanlang"
 selectLanguageTitle = "Til tanlang"


    searchTitle = "Qur'ondan qidirish"
 searchAyahPlaceholder = "Oyatlar ichidan so'z qidiring..."

    searchIdleHint = "Qur'oni Karim tarjimasidan qidirish uchun yozishni boshlang."

    searchErrorFull = "Qidiruv amalga oshmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."

    searchNoResultsTemplate = "\"{query}\" bo'yicha natija topilmadi."


    recitersCountSubtitleTemplate = "{n} qori • tinglab tanlang"

    searchReciterPlaceholder = "Qori qidirish..."

    recitersLoadErrorFull = "Qorilar ro'yxati yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."

    selectedReciterLabel = "Tanlangan qori"
 tapToSelectLabel = "Tanlash uchun bosing"


    juzSingular = "Juz"
 pageSingular = "Sahifa"
 pagePlural = "Sahifalar"

    manzilSingular = "Manzil"
 manzilPlural = "Manzillar"
 rukuSingular = "Ruku'"
 rukuPlural = "Ruku'lar"

    hizbQuarterSingular = "Hizb chorak"
 hizbQuarterPlural = "Hizb choraklari"

    sectionLoadErrorTemplate = "{title} yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."


    sajdaTitle = "Sajda oyatlari"
 sajdaSubtitle = "Qur'onda tilovat sajdasini talab qiluvchi oyatlar"

    sajdaLoadErrorFull = "Sajda oyatlari yuklanmadi. Serverga ulanib bo'lmadi, qayta urinib ko'ring."


    nowPlayingTitle = "Hozir ijro etilmoqda"
 wholeSurahSuffix = "Butun sura"
 playbackSpeedTitle = "Ijro tezligi"


    onboardingSkip = "O'tkazib yuborish"
 onboardingNext = "Keyingi"
 onboardingStart = "Boshlash"

    onboardTitle1 = "Huda Qur'an'ga xush kelibsiz"

    onboardDesc1 = "Qur'oni Karimni doim, hamma joyda, cho'ntagingizda olib yuring."

    onboardTitle2 = "Ovozli tinglash"

    onboardDesc2 = "Oyatma-oyat yoki suraga-sura, tanlagan qoringizdan ravon tinglang."

    onboardTitle3 = "Tarjimalar"

    onboardDesc3 = "O'zbek va boshqa tillardagi tarjimalar bilan oyatlarni tushunib o'qing."

    onboardTitle4 = "Sevimlilaringiz, to'xtagan joyingiz"

    onboardDesc4 = "Yoqtirgan oyatlaringizni saqlang, o'qishni to'xtagan joyingizdan davom eting."


    appTagline = "Qur'oni Karim, har lahza yoningizda"


    lessonsSubtitleTemplate = "Elif-Bodan tajvidgacha, ketma-ket {n} dars"
 lessonNotFound = "Dars topilmadi"

    lessonFallback = "Dars"
 pronounceContentDescription = "Talaffuzni tinglash"


    cancelLabel = "Bekor qilish"
 goLabel = "O'tish"

    mushafJumpToPageTitle = "Sahifaga o'tish"
 mushafJumpToPageHint = "Sahifa raqami (1-604)"

    mushafResumeSubtitleTemplate = "{n}-sahifada to'xtagansiz, davom eting"

    mushafStartSubtitle = "Haqiqiy kitobdek, sahifama-sahifa o'qing"


    khatmProgressTitle = "Xatm jarayoningiz"

    khatmProgressTemplate = "{page}-sahifa / {total} (%{percent})"

    khatmCompletedCountTemplate = "Tugallangan xatmlar: {n}"


    qiblaTitle = "Qibla Kompasi"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Telefoningizning kompasini oching va bu burchakni shimolga qarab tekislang."

    qiblaLocationUnavailable = "Joylashuvingizni aniqlab bo'lmadi. Sozlamalarda shahar/davlat tanlanganiga ishonch hosil qiling."

    cdBack = "Orqaga"
 cdPlay = "Ijro et"
 cdPause = "Pauza"
 cdClose = "Yopish"
 cdRepeat = "Takrorlash"
 cdPrevious = "Oldingi"
 cdNext = "Keyingi"

    tafsirLabel = "Tafsir"
 selectTafsirTitle = "Tafsirni tanlang"
 changeTafsirLabel = "Tafsirni o'zgartirish"
 tafsirUnavailable = "Bu oyat uchun tafsir yuklanmadi."
 tafsirNotSelected = "Tafsir tanlanmagan"

    qiblaLiveInstructions = "Telefoningizni tekis tuting va o'q doira ustidagi uchburchak bilan tekislanguncha asta aylantiring."

    qiblaSensorNoSensor = "Kompas sensori topilmadi"

    qiblaSensorLowAccuracy = "Kompas aniqligi past"

    qiblaSensorReady = "Kompas aniqligi normal"

    qiblaCalibrationAction = "Kalibrlash qo'llanmasini ko'rsatish"

    qiblaCalibrationHideAction = "Kalibrlash qo'llanmasini yashirish"

    qiblaCalibrationInstructions = "Telefonni tekis tuting va havoda asta-sekin sakkizlik shaklida harakatlantiring. Keyin uni yana tekis tutib, bir necha soniya kuting."

    tafsirVsMealHint = "Tarjima oyatning ma'nosini beradi. Tafsir esa uning konteksti va ma'nosining batafsil izohidir."

    textSizeLabel = "Matn o'lchami"
 textSizeHint = "O'qishda qiynalsangiz, matnni kattalashtiring."
 textSizeNormal = "Oddiy"
 textSizeLarge = "Katta"
 textSizeExtraLarge = "Juda katta"

    elderlyProfileTitle = "Katta yoshli foydalanuvchi profili"
 elderlyProfileSubtitle = "Matnni kattalashtiradi, kontrastni oshiradi, boshqaruvlarni kengaytiradi va harakatni kamaytiradi."

    highContrastTitle = "Yuqori kontrast"
 highContrastSubtitle = "Matn va sahifani ajratishni osonlashtiradi."

    largeTouchTargetsTitle = "Kattaroq teginish maydonlari"
 largeTouchTargetsSubtitle = "Tugma va qatorlarni bosishni osonlashtiradi."

    reducedMotionTitle = "Kamaytirilgan harakat"
 reducedMotionSubtitle = "Sahifa o‘tishlari va vizual harakatlarni sokinlashtiradi."

    zakatCalculatorTitle = "Zakot kalkulyatori"

    zakatWealthLabel = "Umumiy boyligingiz (naqd pul, oltin, kumush va h.k.)"

    zakatCalculateButton = "Hisoblash"

    zakatResultTemplate = "Zakotingiz: {amount}"

    zakatNisabInfo = "Zakot — nisob miqdoriga (80,18 gramm oltin qiymati) yetgan va bir yil saqlangan mol-mulkning 2,5 foizi. Bu umumiy qo'llanma, diniy fatvo emas."

    duaListTitle = "Dualar"
 postPrayerDhikrTitle = "Namazdan keyingi zikr"
 postPrayerDhikrSubtitle = "Zikrlaringizni sokin va bir maromda yakunlang."
 postPrayerDhikrReset = "Qaytadan boshlash"
 postPrayerDhikrStepTemplate = "Zikr {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Zikr yakunlandi"
 postPrayerDhikrNext = "Keyingi zikr"

    islamicCalendarTitle = "Islom taqvimi"

    islamicDaysRemainingTemplate = "{n} kun qoldi"

    streakDaysTemplate = "{n} kunlik o'qish seriyasi"

    cdShare = "Ulashish"

    hacKuraLabel = "Haj qur'asi natijasini so'rash"

    moreTitle = "Boshqa"

    filterAll = "Barchasi"

    zakatLiveValuesTitle = "Joriy qiymatlar"

    zakatGoldPriceLabel = "Oltin grammi (24 karat)"

    zakatNisabTemplate = "Nisob ({grams} g oltin)"

    zakatPriceDateTemplate = "{date} sanasidagi narx"

    zakatLivePriceUnavailable = "Oltinning jonli narxini olib bo'lmadi. Gramm narxini quyida o'zingiz kiritishingiz mumkin."

    zakatGoldPriceInputLabel = "Oltin grammi narxi"

    zakatCashLabel = "Naqd pul va bank hisoblari"

    zakatGoldGramsLabel = "Oltin (gramm, 24 karat hisobida)"

    zakatOtherAssetsLabel = "Valyuta, aksiyalar va boshqa sarmoyalar"

    zakatTradeGoodsLabel = "Savdo mollari va qarzdorliklar"

    zakatDebtsLabel = "Qarzlar (ayiriladi)"

    zakatNetWealthTemplate = "Zakot olinadigan sof mulk: {amount}"

    zakatBelowNisab = "Sof mulkingiz nisobdan kam, zakot vojib emas."

    zakatCurrencyLabel = "Valyuta"

    fitreTitle = "Fitr va fidya"

    fitreAmountTemplate = "{period} fitri (bir kishi uchun)"

    fitreSourceNote = "Turkiya Diniy ishlar boshqarmasi (Diyanet) belgilagan eng kam miqdor; imkoni borlar ko'proq berishi mumkin. Ro'za tutolmaganlar uchun kunlik fidya ham shu miqdorda."

    fitrePersonsLabel = "Kishilar soni"

    fitreTotalTemplate = "Jami fitr: {amount}"

    fidyeDaysLabel = "Tutilmagan ro'za kunlari"

    fidyeTotalTemplate = "Jami fidya: {amount}"

    fitreOtherCurrencyNote = "Fitr miqdori mamlakatga qarab farq qiladi; yashash joyingizdagi diniy idora e'lon qilgan miqdorga amal qiling. Quyidagi miqdor Turkiya (Diyanet) uchun."

    restorePurchasesButton = "Xaridlarni tiklash"

    purchaseSuccessMessage = "Rahmat! Xaridingiz yakunlandi."

    purchaseCancelledMessage = "Xarid bekor qilindi."

    purchaseUnavailableMessage = "Bu mahsulot hozircha do'konda mavjud emas. Keyinroq qayta urinib ko'ring."

    purchaseErrorMessage = "Xaridni yakunlab bo'lmadi. Internet aloqasini tekshirib, qayta urinib ko'ring."

    restoreNothingMessage = "Bu hisobda faol reklamasiz obuna topilmadi."

    adFreeUntilTemplate = "{date} gacha reklamasiz"

    prayerSoonTitleTemplate = "{prayer} vaqtiga {n} daqiqa qoldi"

    prayerSoonBody = "Tahorat olib, xotirjam tayyorlaning."

    prayerNowTitleTemplate = "{prayer} vaqti kirdi"

    prayerNowBody = "Qalbingizga bir nafas sukunat; namoz sizni kutmoqda."

    occasionTwoDaysTemplate = "{occasion} yaqinlashmoqda: 2 kun qoldi"

    occasionTomorrowTemplate = "Ertaga {occasion}"

    occasionTomorrowNightTemplate = "Ertaga kechqurun {occasion}"

    occasionBody = "Duolaringizni, niyatingizni va yaqinlaringizni hozirdan tayyorlang; bu muborak vaqtni ehyo qilishga nima deysiz?"

    prayerReminderLeadLabel = "Namozdan oldin eslatish"

    reminderOffLabel = "O'chiq"

    minutesShortTemplate = "{n} daq"

    prayerAtTimeLabel = "Vaqt kirganda ham xabar berish"

    occasionRemindersLabel = "Muborak kun va kechalar eslatmalari"

    notificationSoundHint = "Bildirishnomalar yumshoq, qisqa qo'ng'iroq ovozi bilan keladi, bezovta qilmaydi."

    themePickerTitle = "Mavzuni tanlang"

    themePickerSubtitle = "Sinab ko'rish uchun bosing; tanlovingiz darhol qo'llanadi."

    themeChangeLaterNote = "Istalgan vaqtda Sozlamalardan o'zgartirishingiz mumkin."

    themeIntroTitle = "Yangi: Mavzular"

    themeIntroBody = "Ilova ko'rinishini xohlaganingizcha tanlang. Bosib sinab ko'ring, yoqqanini qoldiring."

    supportRateApp = "Ilovani baholash"

    supportRateAppHint = "Do'konda baho berib qo'llab-quvvatlang"

    supportShareApp = "Ilovani ulashish"

    supportShareAppHint = "Yaqinlaringizga tavsiya qiling"

    shareAppMessage = "Huda Qur'on: Qur'on o'qing va tinglang, namoz vaqtlari, duolar va Asmoul Husna. {url}"

    supportFeedback = "Fikr-mulohaza yuborish"

    supportFeedbackHint = "G'oya yoki muammolaringizni bizga yuboring"

    supportPrivacyPolicy = "Maxfiylik siyosati"

    supportPrivacyPolicyHint = "Ma'lumotlaringiz qanday ishlatilishini bilib oling"

    supportTermsOfService = "Foydalanish shartlari"

    supportTermsOfServiceHint = "Xizmat shartlarimizni o'qing"

    fitreZakatRowTitle = "Fitr / zakot hisoblash"

    fitreZakatRowHint = "Zakot, fitr va fidyangizni hisoblang"

    mushafNewSurahTemplate = "Yangi sura: {surah}"

    mushafStopAction = "To'xtatish"

    mushafSafetyStopTemplate = "Ketma-ket {count} sahifa tinglandi. Tinglashni davom ettirasizmi?"

    mushafContinueAction = "Davom etish"

    mushafPageLoadFailed = "Keyingi sahifani yuklab bo'lmadi"

    mushafGestureHint = "Tinglash uchun oyatga bosing, amallar uchun bosib turing"

    mushafGoToPlayingPage = "O'qilayotgan sahifaga qaytish ({page})"

    mushafKeyboardHint = "Probel: ijro/pauza · ← →: sahifani varaqlash"

    mushafCopiedMessage = "Oyat nusxalandi"

    mushafFavoriteAdded = "Sevimlilarga qo'shildi"

    mushafFavoriteRemoved = "Sevimlilardan olib tashlandi"

    mushafUndoAction = "Bekor qilish"

    mushafAyahActionsTitleTemplate = "{surah}, {ayah}-oyat"

    mushafPlayFromHere = "Shu yerdan tinglash"

    mushafCopyAction = "Nusxalash"

    mushafAddFavorite = "Sevimlilarga qo'shish"

    mushafRemoveFavorite = "Sevimlilardan olib tashlash"

    shareCardTitle = "Oyatni ulashish"

    shareImageAction = "Rasmni ulashish"

    shareTextAction = "Matnni ulashish"

    shareCopyLink = "Havolani nusxalash"

    shareLinkCopied = "Havola nusxalandi"

    shareImageFailed = "Rasmni tayyorlab bo'lmadi"

}

private val kyrgyz = strings {
    navHome = "Башкы бет"
 navSurahs = "Сүрөлөр"
 navFavorites = "Тандалмалар"
 navSettings = "Жөндөөлөр"

    homeGreeting = "Ассалому алайкум"
 search = "Издөө"
 juz = "Жуз"
 reciters = "Кураачылар"

    retry = "Кайра аракет кыл"
 appearance = "Көрүнүш"
 recitationAndTranslation = "Окуу жана котормо"

    prayerNotifications = "Намаз эскертүүлөрү"
 language = "Тил"


    ayahWord = "Аят"
 ayahWordLower = "аят"
 meccan = "Меккелик"
 medinan = "Мадиналык"

    serverUnreachable = "Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."
 notFound = "Натыйжа табылган жок"

    viewAll = "Баарын көрүү"


    homeSubtitle = "Күнүңүздү Кураан менен баштаңыз"
 continueReading = "Токтогон жериңизден улантыңыз"

    dailyAyahTitle = "Күндүн аяты"
 dailyAyahError = "Аят жүктөлбөдү, кайра аракет кылыңыз."

    readingLessonsTitle = "Кураан окуу сабактары"

    readingLessonsSubtitleTemplate = "Элип-Байдан тажвидге чейин, үн менен {n} ырааттуу сабак"

    quickActionsTitle = "Тез кирүү"

    esmaulHusnaTitle = "Аллахтын 99 сулуу ысымы"
 discoverQuranTitle = "Кураанды изилдеңиз"

    mushafModeLabel = "Мусхаф режими"
 mushafViewLabel = "Мусхаф"
 readingViewLabel = "Окуу"
 toggleTranslationLabel = "Котормону көрсөтүү/жашыруу"

    pagesLabel = "Барактар"
 manzilsLabel = "Манзилдер"
 rukusLabel = "Рукулар"

    hizbQuartersLabel = "Хизб чейректери"
 sajdaVersesLabel = "Сажда аяттары"

    quranStatsTitle = "Кураани Каримдин статистикасы"
 statSurah = "Сүрө"
 statJuz = "Жуз"

    statPage = "Барак"
 statRuku = "Руку"
 statHizbQuarter = "Хизб чейрек"
 statManzil = "Манзил"

    statSajda = "Сажда"
 featuredSurahsTitle = "Тандалган сүрөлөр"
 surahsLoadErrorShort = "Сүрөлөр жүктөлбөдү."

    nextPrayerLabel = "Кийинки намаз"
 hoursMinutesLeftTemplate = "{h} саат {m} мүнөт калды"

    minutesLeftTemplate = "{m} мүнөт калды"
 favoriteLabel = "Тандалма"

    todayTitle = "Бүгүн"
 todayHadithTitle = "Бүгүнкү хадис"
 todayDuaTitle = "Бүгүнкү дуба"

    todayOpenHadith = "Хадистерди ачуу"
 todayOpenDua = "Дубаларды ачуу"
 todayNoContent = "Мазмун азыр жеткиликсиз"


    surahSearchPlaceholder = "Сүрө издөө (аты же номери)"

    surahsLoadErrorFull = "Сүрөлөр жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."


    surahFallback = "Сүрө"

    surahLoadErrorFull = "Сүрө жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."


    myFavoritesTitle = "Тандалмаларым"

    favoritesEmptyMessage = "Азырынча тандалма аяттарыңыз жок. Окуу учурунда ⭐ белгисин басып, бул жерге кошо аласыз."

    surahFallbackNumberedTemplate = "{n}-сүрө"


    settingsTitle = "Жөндөөлөр"
 languagePickerSubtitleTemplate = "{n} тилдин арасынан тандаңыз"

    reciterLabel = "Кураачы"
 translationLabel = "Котормо"

    notificationsToggleTitle = "Эскертүүлөрдү күйгүзүү"
 notificationsToggleSubtitle = "Намаз убактысы киргенде эскертүү жиберүү"

    locationLabel = "Жайгашкан жер"
 locationAutoUpdateNote = "Жайгашкан жер сакталгандан кийин эскертүүлөр автоматтык жаңыртылат."

    supportUsTitle = "Бизди колдоңуз"
 adFreeActiveMessage = "Жарнаксыз мүчөлүгүңүз активдүү, рахмат! 💚"

    supportUsMessage = "Колдонмону өнүктүрүүгө жардам бериңиз"
 smallDonationButton = "☕ Кичине белек"

    mediumDonationButton = "🎁 Орто белек"
 sixMonthAdFreeButton = "✨ 6 айлык жарнаксыз мүчөлүк"

    themeSystem = "🖥️ Система"
 themeLight = "☀️ Жарык"
 themeDark = "🌙 Караңгы"
 themeSakura = "🌸 Сакура"


    editionSearchPlaceholder = "Издөө"

    selectTranslationTitle = "Котормону тандаңыз"
 selectLocationTitle = "Жайгашкан жерди тандаңыз"
 selectLanguageTitle = "Тилди тандаңыз"


    searchTitle = "Кураандан издөө"
 searchAyahPlaceholder = "Аяттардын ичинен сөз издеңиз..."

    searchIdleHint = "Кураани Каримдин котормосунан издөө үчүн жаза баштаңыз."

    searchErrorFull = "Издөө ишке ашкан жок. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."

    searchNoResultsTemplate = "\"{query}\" үчүн натыйжа табылган жок."


    recitersCountSubtitleTemplate = "{n} кураачы • угуп тандаңыз"

    searchReciterPlaceholder = "Кураачы издөө..."

    recitersLoadErrorFull = "Кураачылар тизмеси жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."

    selectedReciterLabel = "Тандалган кураачы"
 tapToSelectLabel = "Тандоо үчүн басыңыз"


    juzSingular = "Жуз"
 pageSingular = "Барак"
 pagePlural = "Барактар"

    manzilSingular = "Манзил"
 manzilPlural = "Манзилдер"
 rukuSingular = "Руку"
 rukuPlural = "Рукулар"

    hizbQuarterSingular = "Хизб чейрек"
 hizbQuarterPlural = "Хизб чейректери"

    sectionLoadErrorTemplate = "{title} жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."


    sajdaTitle = "Сажда аяттары"
 sajdaSubtitle = "Кураанда тиловат саждасын талап кылган аяттар"

    sajdaLoadErrorFull = "Сажда аяттары жүктөлбөдү. Серверге туташуу мүмкүн болбоду, кайра аракет кылыңыз."


    nowPlayingTitle = "Азыр ойнотулууда"
 wholeSurahSuffix = "Бүткүл сүрө"
 playbackSpeedTitle = "Ойнотуу ылдамдыгы"


    onboardingSkip = "Өткөрүп жиберүү"
 onboardingNext = "Кийинки"
 onboardingStart = "Баштоо"

    onboardTitle1 = "Huda Qur'an'га кош келиңиз"

    onboardDesc1 = "Кураани Каримди дайыма, каалаган жерде, чөнтөгүңүздө алып жүрүңүз."

    onboardTitle2 = "Үн менен угуу"

    onboardDesc2 = "Аят-аят же сүрө-сүрө, тандаган кураачыңыздан таза үн менен угуңуз."

    onboardTitle3 = "Котормолор"

    onboardDesc3 = "Түрк жана башка тилдердеги котормолор менен аяттарды түшүнүп окуңуз."

    onboardTitle4 = "Тандалмаларыңыз, токтогон жериңиз"

    onboardDesc4 = "Жаккан аяттарыңызды сактаңыз, окууну токтогон жериңизден улантыңыз."


    appTagline = "Кураани Карим, ар дайым жаныңызда"


    lessonsSubtitleTemplate = "Элип-Байдан тажвидге чейин, ырааттуу {n} сабак"
 lessonNotFound = "Сабак табылган жок"

    lessonFallback = "Сабак"
 pronounceContentDescription = "Айтылышын угуу"


    cancelLabel = "Жокко чыгаруу"
 goLabel = "Өтүү"

    mushafJumpToPageTitle = "Баракка өтүү"
 mushafJumpToPageHint = "Барак номери (1-604)"

    mushafResumeSubtitleTemplate = "{n}-барактан токтогонсуз, улантыңыз"

    mushafStartSubtitle = "Чыныгы китептей, барактап окуңуз"


    khatmProgressTitle = "Хатм жүрүшүңүз"

    khatmProgressTemplate = "{page}-барак / {total} (%{percent})"

    khatmCompletedCountTemplate = "Аяктаган хатмдар: {n}"


    qiblaTitle = "Кыбыла Компасы"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Телефонуңуздун компасын ачып, бул бурчту түндүккө карай тегиздеңиз."

    qiblaLocationUnavailable = "Жайгашкан жериңизди аныктоо мүмкүн болбоду. Жөндөөлөрдө шаар/өлкө тандалганына ынаныңыз."

    cdBack = "Артка"
 cdPlay = "Ойнотуу"
 cdPause = "Тыныгуу"
 cdClose = "Жабуу"
 cdRepeat = "Кайталоо"
 cdPrevious = "Мурунку"
 cdNext = "Кийинки"

    tafsirLabel = "Тафсир"
 selectTafsirTitle = "Тафсирди тандаңыз"
 changeTafsirLabel = "Тафсирди өзгөртүү"
 tafsirUnavailable = "Бул аят үчүн тафсир жүктөлгөн жок."
 tafsirNotSelected = "Тафсир тандалган жок"

    qiblaLiveInstructions = "Телефонуңузду түз кармап, жебе тегеректин үстүндөгү үч бурчтук менен дал келгенче жай бурулуңуз."

    qiblaSensorNoSensor = "Компас сенсору табылган жок"

    qiblaSensorLowAccuracy = "Компастын тактыгы төмөн"

    qiblaSensorReady = "Компастын тактыгы нормалдуу"

    qiblaCalibrationAction = "Калибрлөө көрсөтмөсүн ачуу"

    qiblaCalibrationHideAction = "Калибрлөө көрсөтмөсүн жашыруу"

    qiblaCalibrationInstructions = "Телефонуңузду түз кармап, абада сегиз санын тарткандай жай кыймылдатыңыз. Андан соң кайра түз кармап, бир нече секунд күтүңүз."

    tafsirVsMealHint = "Котормо аяттын маанисин берет. Тафсир болсо анын мазмунун жана маанисин кеңири түшүндүрөт."

    textSizeLabel = "Тексттин өлчөмү"
 textSizeHint = "Окууга кыйналсаңыз, текстти чоңойтуңуз."
 textSizeNormal = "Кадимки"
 textSizeLarge = "Чоң"
 textSizeExtraLarge = "Өтө чоң"

    elderlyProfileTitle = "Улуу жаштагы колдонуучу профили"
 elderlyProfileSubtitle = "Текстти чоңойтот, контрастты күчөтөт, баскычтарды кеңейтет жана кыймылды азайтат."

    highContrastTitle = "Жогорку контраст"
 highContrastSubtitle = "Текст менен бетти айырмалоону жеңилдетет."

    largeTouchTargetsTitle = "Чоңураак тийүү аймактары"
 largeTouchTargetsSubtitle = "Баскычтарды жана саптарды басууну жеңилдетет."

    reducedMotionTitle = "Азайтылган кыймыл"
 reducedMotionSubtitle = "Бет өтүүлөрүн жана визуалдык кыймылды жайлатат."

    zakatCalculatorTitle = "Зекет эсептегич"

    zakatWealthLabel = "Жалпы байлыгыңыз (накталай акча, алтын, күмүш ж.б.)"

    zakatCalculateButton = "Эсептөө"

    zakatResultTemplate = "Зекетиңиз: {amount}"

    zakatNisabInfo = "Зекет — нисап өлчөмүнө (80,18 грамм алтын баасына) жеткен жана бир жыл сакталган мал-мүлктүн 2,5 пайызы. Бул жалпы көрсөтмө, диний фетва эмес."

    duaListTitle = "Дубалар"
 postPrayerDhikrTitle = "Намаздан кийинки зикир"
 postPrayerDhikrSubtitle = "Зикирди жай жана туруктуу ыргакта бүтүрүңүз."
 postPrayerDhikrReset = "Кайра баштоо"
 postPrayerDhikrStepTemplate = "Зикир {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Зикир бүттү"
 postPrayerDhikrNext = "Кийинки зикир"

    islamicCalendarTitle = "Ислам календары"

    islamicDaysRemainingTemplate = "{n} күн калды"

    streakDaysTemplate = "{n} күндүк окуу сериясы"

    cdShare = "Бөлүшүү"

    hacKuraLabel = "Хаж кура жыйынтыгын текшерүү"

    moreTitle = "Дагы"

    filterAll = "Баары"

    zakatLiveValuesTitle = "Учурдагы маанилер"

    zakatGoldPriceLabel = "Алтындын грамы (24 карат)"

    zakatNisabTemplate = "Нисап ({grams} г алтын)"

    zakatPriceDateTemplate = "{date} күнкү баа"

    zakatLivePriceUnavailable = "Алтындын жандуу баасын алуу мүмкүн болгон жок. Грамм баасын төмөндө өзүңүз киргизсеңиз болот."

    zakatGoldPriceInputLabel = "Алтындын грамм баасы"

    zakatCashLabel = "Накталай акча жана банк эсептери"

    zakatGoldGramsLabel = "Алтын (грамм, 24 карат эсебинде)"

    zakatOtherAssetsLabel = "Валюта, акциялар жана башка инвестициялар"

    zakatTradeGoodsLabel = "Соода буюмдары жана алына турган карыздар"

    zakatDebtsLabel = "Карыздар (кемитилет)"

    zakatNetWealthTemplate = "Зекетке тийиштүү таза мүлк: {amount}"

    zakatBelowNisab = "Таза мүлкүңүз нисаптан аз, зекет милдеттүү эмес."

    zakatCurrencyLabel = "Валюта"

    fitreTitle = "Пітир жана фидия"

    fitreAmountTemplate = "{period} пітири (бир кишиге)"

    fitreSourceNote = "Түркиянын Дин иштери башкармалыгы (Diyanet) белгилеген эң аз өлчөм; мүмкүнчүлүгү барлар көбүрөөк берсе болот. Орозо кармай албагандардын күндүк фидиясы да ушул өлчөмдө."

    fitrePersonsLabel = "Кишилердин саны"

    fitreTotalTemplate = "Жалпы пітир: {amount}"

    fidyeDaysLabel = "Кармалбаган орозо күндөрү"

    fidyeTotalTemplate = "Жалпы фидия: {amount}"

    fitreOtherCurrencyNote = "Пітирдин өлчөмү өлкөгө жараша айырмаланат; жашаган жериңиздеги диний мекеме жарыялаган өлчөмдү негиз кылыңыз. Төмөнкү сумма Түркия (Diyanet) үчүн."

    restorePurchasesButton = "Сатып алууларды калыбына келтирүү"

    purchaseSuccessMessage = "Рахмат! Сатып алууңуз аяктады."

    purchaseCancelledMessage = "Сатып алуу жокко чыгарылды."

    purchaseUnavailableMessage = "Бул продукт азыр дүкөндө жок. Кийинчерээк кайра аракет кылыңыз."

    purchaseErrorMessage = "Сатып алууну аяктоо мүмкүн болгон жок. Байланышты текшерип, кайра аракет кылыңыз."

    restoreNothingMessage = "Бул аккаунтта активдүү жарнамасыз жазылуу табылган жок."

    adFreeUntilTemplate = "{date} чейин жарнамасыз"

    prayerSoonTitleTemplate = "{prayer} убактысына {n} мүнөт калды"

    prayerSoonBody = "Даарат алып, тынч даярданыңыз."

    prayerNowTitleTemplate = "{prayer} убактысы кирди"

    prayerNowBody = "Жүрөгүңүзгө бир дем тынчтык; намаз сизди күтүп турат."

    occasionTwoDaysTemplate = "{occasion} жакындап калды: 2 күн калды"

    occasionTomorrowTemplate = "Эртең {occasion}"

    occasionTomorrowNightTemplate = "Эртең кечинде {occasion}"

    occasionBody = "Дубаларыңызды, ниетиңизди жана жакындарыңызды азыр эле даярдаңыз; бул ыйык убакытты ибадат менен өткөрөлүбү?"

    prayerReminderLeadLabel = "Намаздан мурун эскертүү"

    reminderOffLabel = "Өчүк"

    minutesShortTemplate = "{n} мүн"

    prayerAtTimeLabel = "Убакыт киргенде да билдирүү"

    occasionRemindersLabel = "Ыйык күндөр жана түндөр боюнча эскертүүлөр"

    notificationSoundHint = "Билдирүүлөр жумшак, кыска коңгуроо үнү менен келет, тынчыңызды албайт."

    themePickerTitle = "Теманы тандаңыз"

    themePickerSubtitle = "Сынап көрүү үчүн басыңыз; тандооңуз дароо колдонулат."

    themeChangeLaterNote = "Каалаган убакта Жөндөөлөрдөн өзгөртө аласыз."

    themeIntroTitle = "Жаңы: Темалар"

    themeIntroBody = "Колдонмонун көрүнүшүн каалаганыңыздай тандаңыз. Басып сынап көрүңүз, жаккан бойдон калсын."

    supportRateApp = "Колдонмону баалоо"

    supportRateAppHint = "Дүкөндө баа берип колдоңуз"

    supportShareApp = "Колдонмону бөлүшүү"

    supportShareAppHint = "Жакындарыңызга сунуштаңыз"

    shareAppMessage = "Худа Куран: Куран окуңуз жана угуңуз, намаз убактылары, дубалар жана Асмаул Хусна. {url}"

    supportFeedback = "Пикир жөнөтүү"

    supportFeedbackHint = "Идеяңызды же көйгөйүңүздү бизге жазыңыз"

    supportPrivacyPolicy = "Купуялык саясаты"

    supportPrivacyPolicyHint = "Маалыматыңыз кантип колдонулаарын билиңиз"

    supportTermsOfService = "Колдонуу шарттары"

    supportTermsOfServiceHint = "Кызмат көрсөтүү шарттарын окуңуз"

    fitreZakatRowTitle = "Питир / зекет эсептөө"

    fitreZakatRowHint = "Зекетиңизди, питириңизди жана фидияңызды эсептеңиз"

    mushafNewSurahTemplate = "Жаңы сүрө: {surah}"

    mushafStopAction = "Токтотуу"

    mushafSafetyStopTemplate = "Катары менен {count} барак угулду. Угууну улантасызбы?"

    mushafContinueAction = "Улантуу"

    mushafPageLoadFailed = "Кийинки баракты жүктөө мүмкүн болгон жок"

    mushafGestureHint = "Угуу үчүн аятты басыңыз, аракеттер үчүн басып туруңуз"

    mushafGoToPlayingPage = "Окулуп жаткан баракка кайтуу ({page})"

    mushafKeyboardHint = "Боштук: ойнотуу/тыным · ← →: барак которуу"

    mushafCopiedMessage = "Аят көчүрүлдү"

    mushafFavoriteAdded = "Тандалмаларга кошулду"

    mushafFavoriteRemoved = "Тандалмалардан алынды"

    mushafUndoAction = "Жокко чыгаруу"

    mushafAyahActionsTitleTemplate = "{surah}, {ayah}-аят"

    mushafPlayFromHere = "Ушул жерден угуу"

    mushafCopyAction = "Көчүрүү"

    mushafAddFavorite = "Тандалмаларга кошуу"

    mushafRemoveFavorite = "Тандалмалардан алып салуу"

    shareCardTitle = "Аятты бөлүшүү"

    shareImageAction = "Сүрөттү бөлүшүү"

    shareTextAction = "Текстти бөлүшүү"

    shareCopyLink = "Шилтемени көчүрүү"

    shareLinkCopied = "Шилтеме көчүрүлдү"

    shareImageFailed = "Сүрөттү даярдоо мүмкүн болбоду"

}

private val turkmen = strings {
    navHome = "Baş sahypa"
 navSurahs = "Suralar"
 navFavorites = "Halanýanlar"
 navSettings = "Sazlamalar"

    homeGreeting = "Essalamu aleýkim"
 search = "Gözleg"
 juz = "Juz"
 reciters = "Okyjylar"

    retry = "Gaýtadan synanyş"
 appearance = "Görnüş"
 recitationAndTranslation = "Okalyş we terjime"

    prayerNotifications = "Namaz duýduryşlary"
 language = "Dil"


    ayahWord = "Aýat"
 ayahWordLower = "aýat"
 meccan = "Mekgeli"
 medinan = "Medineli"

    serverUnreachable = "Serwere baglanyp bolmady, gaýtadan synanyşyň."
 notFound = "Netije tapylmady"

    viewAll = "Ählisini gör"


    homeSubtitle = "Gününizi Kuran bilen başlaň"
 continueReading = "Galan ýeriňizden dowam ediň"

    dailyAyahTitle = "Günüň aýaty"
 dailyAyahError = "Aýat ýüklenmedi, gaýtadan synanyşyň."

    readingLessonsTitle = "Kuran okaýyş sapaklary"

    readingLessonsSubtitleTemplate = "Elif-Baýdan tejwide çenli, sesli {n} yzygiderli sapak"

    quickActionsTitle = "Çalt giriş"

    esmaulHusnaTitle = "Allanyň 99 owadan ady"
 discoverQuranTitle = "Kurany öwreniň"

    mushafModeLabel = "Mushaf Režimi"
 mushafViewLabel = "Mushaf"
 readingViewLabel = "Okamak"
 toggleTranslationLabel = "Terjimäni Görkez/Gizle"

    pagesLabel = "Sahypalar"
 manzilsLabel = "Manzillar"
 rukusLabel = "Rukular"

    hizbQuartersLabel = "Hizb çärýekleri"
 sajdaVersesLabel = "Sejde aýatlary"

    quranStatsTitle = "Kurany Kerimiň statistikasy"
 statSurah = "Sura"
 statJuz = "Juz"

    statPage = "Sahypa"
 statRuku = "Ruku"
 statHizbQuarter = "Hizb çärýek"
 statManzil = "Manzil"

    statSajda = "Sejde"
 featuredSurahsTitle = "Saýlanan suralar"
 surahsLoadErrorShort = "Suralar ýüklenmedi."

    nextPrayerLabel = "Indiki namaz"
 hoursMinutesLeftTemplate = "{h} sag {m} min galdy"

    minutesLeftTemplate = "{m} min galdy"
 favoriteLabel = "Halanýan"

    todayTitle = "Şu gün"
 todayHadithTitle = "Günüň hadysy"
 todayDuaTitle = "Günüň dogasy"

    todayOpenHadith = "Hadyslary aç"
 todayOpenDua = "Dogalary aç"
 todayNoContent = "Mazmun häzir elýeterli däl"


    surahSearchPlaceholder = "Sura gözle (ady ýa-da sany)"

    surahsLoadErrorFull = "Suralar ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň."


    surahFallback = "Sura"

    surahLoadErrorFull = "Sura ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň."


    myFavoritesTitle = "Halanýanlarym"

    favoritesEmptyMessage = "Entek halanýan aýatyňyz ýok. Okaýan wagtyňyz ⭐ belligine basyp, şu ýere goşup bilersiňiz."

    surahFallbackNumberedTemplate = "{n}-sura"


    settingsTitle = "Sazlamalar"
 languagePickerSubtitleTemplate = "{n} diliň arasyndan saýlaň"

    reciterLabel = "Okyjy"
 translationLabel = "Terjime"

    notificationsToggleTitle = "Duýduryşlary aç"
 notificationsToggleSubtitle = "Namaz wagty gireninde duýduryş iber"

    locationLabel = "Ýerleşiş"
 locationAutoUpdateNote = "Ýerleşiş ýatda saklanandan soň duýduryşlar awtomatik täzelenýär."

    supportUsTitle = "Bize goldaw beriň"
 adFreeActiveMessage = "Mahabatsyz agzalygyňyz işjeň, sag boluň! 💚"

    supportUsMessage = "Programmany ösdürmäge kömek ediň"
 smallDonationButton = "☕ Kiçi sowgat"

    mediumDonationButton = "🎁 Orta sowgat"
 sixMonthAdFreeButton = "✨ 6 aýlyk mahabatsyz agzalyk"

    themeSystem = "🖥️ Ulgam"
 themeLight = "☀️ Açyk"
 themeDark = "🌙 Garaňky"
 themeSakura = "🌸 Sakura"


    editionSearchPlaceholder = "Gözle"

    selectTranslationTitle = "Terjimäni saýlaň"
 selectLocationTitle = "Ýerleşişi saýlaň"
 selectLanguageTitle = "Dili saýlaň"


    searchTitle = "Kurandan gözle"
 searchAyahPlaceholder = "Aýatlaryň içinden söz gözläň..."

    searchIdleHint = "Kurany Kerimiň terjimesinden gözlemek üçin ýazyp başlaň."

    searchErrorFull = "Gözleg başa barmady. Serwere baglanyp bolmady, gaýtadan synanyşyň."

    searchNoResultsTemplate = "\"{query}\" üçin netije tapylmady."


    recitersCountSubtitleTemplate = "{n} okyjy • diňläp saýlaň"

    searchReciterPlaceholder = "Okyjy gözle..."

    recitersLoadErrorFull = "Okyjylar sanawy ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň."

    selectedReciterLabel = "Saýlanan okyjy"
 tapToSelectLabel = "Saýlamak üçin basyň"


    juzSingular = "Juz"
 pageSingular = "Sahypa"
 pagePlural = "Sahypalar"

    manzilSingular = "Manzil"
 manzilPlural = "Manzillar"
 rukuSingular = "Ruku"
 rukuPlural = "Rukular"

    hizbQuarterSingular = "Hizb çärýek"
 hizbQuarterPlural = "Hizb çärýekleri"

    sectionLoadErrorTemplate = "{title} ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň."


    sajdaTitle = "Sejde aýatlary"
 sajdaSubtitle = "Kuranda tilawat sejdesini talap edýän aýatlar"

    sajdaLoadErrorFull = "Sejde aýatlary ýüklenmedi. Serwere baglanyp bolmady, gaýtadan synanyşyň."


    nowPlayingTitle = "Häzir oýnadylýar"
 wholeSurahSuffix = "Bütin sura"
 playbackSpeedTitle = "Oýnatma tizligi"


    onboardingSkip = "Geç"
 onboardingNext = "Indiki"
 onboardingStart = "Başla"

    onboardTitle1 = "Huda Qur'an'a hoş geldiňiz"

    onboardDesc1 = "Kurany Kerimi hemişe, her ýerde, jübiňizde göteriň."

    onboardTitle2 = "Sesli diňleme"

    onboardDesc2 = "Aýat-aýat ýa-da sura-sura, saýlan okyjyňyzdan akgytly diňläň."

    onboardTitle3 = "Terjimeler"

    onboardDesc3 = "Türkçe we beýleki dillerdäki terjimeler bilen aýatlary düşünip okaň."

    onboardTitle4 = "Halanýanlaryňyz, galan ýeriňiz"

    onboardDesc4 = "Halan aýatlaryňyzy ýatda saklaň, okamagy galan ýeriňizden dowam ediň."


    appTagline = "Kurany Kerim, hemişe ýanyňyzda"


    lessonsSubtitleTemplate = "Elif-Baýdan tejwide çenli, yzygiderli {n} sapak"
 lessonNotFound = "Sapak tapylmady"

    lessonFallback = "Sapak"
 pronounceContentDescription = "Aýdylyşyny diňle"


    cancelLabel = "Ýatyr"
 goLabel = "Git"

    mushafJumpToPageTitle = "Sahypa git"
 mushafJumpToPageHint = "Sahypa belgisi (1-604)"

    mushafResumeSubtitleTemplate = "{n}-sahypada galdyňyz, dowam ediň"

    mushafStartSubtitle = "Hakyky kitap ýaly, sahypama-sahypa okaň"


    khatmProgressTitle = "Hatmiňiziň dowamy"

    khatmProgressTemplate = "{page}-sahypa / {total} (%{percent})"

    khatmCompletedCountTemplate = "Tamamlanan hatmlar: {n}"


    qiblaTitle = "Kybla Kompasy"

    qiblaBearingTemplate = "{degree}° ({cardinal})"

    qiblaInstructions = "Telefonyňyzyň kompasyny açyp, bu burçy demirgazyga görä deňleşdiriň."

    qiblaLocationUnavailable = "Ýerleşiş maglumaty alynyp bilinmedi. Sazlamalarda şäher/ýurt saýlanandygyna göz ýetiriň."

    cdBack = "Yza"
 cdPlay = "Ýerine ýetir"
 cdPause = "Duruzmak"
 cdClose = "Ýapmak"
 cdRepeat = "Gaýtalamak"
 cdPrevious = "Öňki"
 cdNext = "Indiki"

    tafsirLabel = "Tefsir"
 selectTafsirTitle = "Tefsir saýlaň"
 changeTafsirLabel = "Tefsiri üýtget"
 tafsirUnavailable = "Bu aýat üçin tefsir ýüklenip bilinmedi."
 tafsirNotSelected = "Tefsir saýlanmady"

    qiblaLiveInstructions = "Telefonyňyzy tekiz saklaň we ok, tegeleg üstündäki üçburçluk bilen deňeşýänçä ýuwaş öwrüň."

    qiblaSensorNoSensor = "Kompas datçigi tapylmady"

    qiblaSensorLowAccuracy = "Kompas takyklygy pes"

    qiblaSensorReady = "Kompas takyklygy kadaly"

    qiblaCalibrationAction = "Kalibrleme gollanmasyny görkez"

    qiblaCalibrationHideAction = "Kalibrleme gollanmasyny gizle"

    qiblaCalibrationInstructions = "Telefony tekiz saklaň we howada ýuwaşlyk bilen sekizlik şekilinde hereketlendiriň. Soňra telefony täzeden tekiz saklap, birnäçe sekunt garaşyň."

    tafsirVsMealHint = "Terjime aýatyň manysyny berýär. Tefsir bolsa onuň manysynyň we mazmunynyň jikme-jik düşündirişidir."

    textSizeLabel = "Ýazy ölçegi"
 textSizeHint = "Okamakda kynçylyk çekseňiz, ýazyny ulaldyň."
 textSizeNormal = "Adaty"
 textSizeLarge = "Uly"
 textSizeExtraLarge = "Has uly"

    elderlyProfileTitle = "Uly ýaşly ulanyjy profili"
 elderlyProfileSubtitle = "Ýazgyny ulaldýar, kontrasty güýçlendirýär, düwmeleri giňeldýär we hereketi azaldýar."

    highContrastTitle = "Ýokary kontrast"
 highContrastSubtitle = "Ýazgyny we sahypany tapawutlandyrmagy ýeňilleşdirýär."

    largeTouchTargetsTitle = "Uly degirme meýdanlary"
 largeTouchTargetsSubtitle = "Düwmeleri we setirleri basmagy aňsatlaşdyrýar."

    reducedMotionTitle = "Azaldylan hereket"
 reducedMotionSubtitle = "Sahypa geçişlerini we wizual hereketleri köşeşdirýär."

    zakatCalculatorTitle = "Zekat hasaplaýjy"

    zakatWealthLabel = "Umumy baýlygyňyz (nagt pul, altyn, kümüş we ş.m.)"

    zakatCalculateButton = "Hasapla"

    zakatResultTemplate = "Zekatyňyz: {amount}"

    zakatNisabInfo = "Zekat — nisap mukdaryna (80,18 gram altyn bahasyna) ýeten we bir ýyl saklanan baýlygyň 2,5 göterimi. Bu umumy gollanma, dini pikir däl."

    duaListTitle = "Dogalar"
 postPrayerDhikrTitle = "Namazdan soňky zikir"
 postPrayerDhikrSubtitle = "Zikiriňizi asuda we birsydyrgyn depginde tamamlaň."
 postPrayerDhikrReset = "Täzeden başla"
 postPrayerDhikrStepTemplate = "Zikir {step}"
 postPrayerDhikrCountTemplate = "{count} / {target}"
 postPrayerDhikrComplete = "Zikir tamamlandy"
 postPrayerDhikrNext = "Indiki zikir"

    islamicCalendarTitle = "Yslam senenamasy"

    islamicDaysRemainingTemplate = "{n} gün galdy"

    streakDaysTemplate = "{n} günlük okaýyş seriýasy"

    cdShare = "Paýlaş"

    hacKuraLabel = "Hac kurasy netijesini soragy"

    moreTitle = "Ýene"

    filterAll = "Hemmesi"

    zakatLiveValuesTitle = "Häzirki bahalar"

    zakatGoldPriceLabel = "Bir gram altyn (24 karat)"

    zakatNisabTemplate = "Nisap ({grams} g altyn)"

    zakatPriceDateTemplate = "{date} senesindäki baha"

    zakatLivePriceUnavailable = "Altynyň göni bahasyny alyp bolmady. Gram bahasyny aşakda özüňiz girizip bilersiňiz."

    zakatGoldPriceInputLabel = "Bir gram altynyň bahasy"

    zakatCashLabel = "Nagt pul we bank hasaplary"

    zakatGoldGramsLabel = "Altyn (gram, 24 karat hasabynda)"

    zakatOtherAssetsLabel = "Walýuta, paýnamalar we beýleki maýa goýumlar"

    zakatTradeGoodsLabel = "Söwda harytlary we alynmaly bergiler"

    zakatDebtsLabel = "Bergiler (aýrylýar)"

    zakatNetWealthTemplate = "Zekada degişli arassa baýlyk: {amount}"

    zakatBelowNisab = "Arassa baýlygyňyz nisapdan az, zekat parz däl."

    zakatCurrencyLabel = "Pul birligi"

    fitreTitle = "Pitir we fidýa"

    fitreAmountTemplate = "{period} pitiri (bir adam üçin)"

    fitreSourceNote = "Türkiýäniň Din işleri müdirligi (Diyanet) tarapyndan kesgitlenen iň az mukdar; mümkinçiligi bolanlar köpräk berip biler. Oraza tutup bilmeýänleriň günlük fidýasy hem şu mukdardyr."

    fitrePersonsLabel = "Adam sany"

    fitreTotalTemplate = "Jemi pitir: {amount}"

    fidyeDaysLabel = "Tutulmadyk oraza günleri"

    fidyeTotalTemplate = "Jemi fidýa: {amount}"

    fitreOtherCurrencyNote = "Pitir mukdary ýurda görä tapawutlanýar; ýaşaýan ýeriňizdäki dini edaranyň yglan eden mukdaryny esas alyň. Aşakdaky mukdar Türkiýe (Diyanet) üçin."

    restorePurchasesButton = "Satyn alyşlary dikelt"

    purchaseSuccessMessage = "Sag boluň! Satyn alşyňyz tamamlandy."

    purchaseCancelledMessage = "Satyn alyş ýatyryldy."

    purchaseUnavailableMessage = "Bu önüm häzir dükanda elýeterli däl. Soňrak gaýtadan synanyşyň."

    purchaseErrorMessage = "Satyn alyşy tamamlap bolmady. Aragatnaşygy barlap, gaýtadan synanyşyň."

    restoreNothingMessage = "Bu hasapda işjeň mahabatsyz abuna tapylmady."

    adFreeUntilTemplate = "{date} senesine çenli mahabatsyz"

    prayerSoonTitleTemplate = "{prayer} wagtyna {n} minut galdy"

    prayerSoonBody = "Täret alyp, rahatlyk bilen taýýarlanyň."

    prayerNowTitleTemplate = "{prayer} wagty girdi"

    prayerNowBody = "Ýüregiňize bir demlik rahatlyk; namaz sizi garaşýar."

    occasionTwoDaysTemplate = "{occasion} ýakynlaşýar: 2 gün galdy"

    occasionTomorrowTemplate = "Ertir {occasion}"

    occasionTomorrowNightTemplate = "Ertir agşam {occasion}"

    occasionBody = "Dogalaryňyzy, niýetiňizi we ýakynlaryňyzy şu wagtdan taýýarlaň; bu mübärek wagty ybadat bilen geçirmäge näme diýýärsiňiz?"

    prayerReminderLeadLabel = "Namazdan öň ýatlat"

    reminderOffLabel = "Öçük"

    minutesShortTemplate = "{n} min"

    prayerAtTimeLabel = "Wagt girende hem habar ber"

    occasionRemindersLabel = "Mübärek gün we gijeler barada ýatlatmalar"

    notificationSoundHint = "Bildirişler ýumşak, gysga jaň sesi bilen gelýär, biynjalyk etmeýär."

    themePickerTitle = "Temaňyzy saýlaň"

    themePickerSubtitle = "Synap görmek üçin basyň; saýlawyňyz derrew ulanylýar."

    themeChangeLaterNote = "Islän wagtyňyz Sazlamalardan üýtgedip bilersiňiz."

    themeIntroTitle = "Täze: Temalar"

    themeIntroBody = "Programmanyň görnüşini isleýşiňiz ýaly saýlaň. Basyp synap görüň, halanyňyzy galdyryň."

    supportRateApp = "Programmany baha ber"

    supportRateAppHint = "Dükanda baha berip goldaw ber"

    supportShareApp = "Programmany paýlaş"

    supportShareAppHint = "Ýakynlaryňyza maslahat ber"

    shareAppMessage = "Huda Gurhan: Gurhany oka we diňle, namaz wagtlary, dogalar we Esmaül Hüsna. {url}"

    supportFeedback = "Pikir iber"

    supportFeedbackHint = "Pikirleriňizi ýa-da meselelerňizi bize ýazyň"

    supportPrivacyPolicy = "Gizlinlik syýasaty"

    supportPrivacyPolicyHint = "Maglumatlaryňyzyň nähili ulanylýandygyny öwreniň"

    supportTermsOfService = "Ulanyş şertleri"

    supportTermsOfServiceHint = "Hyzmat şertlerimizi okaň"

    fitreZakatRowTitle = "Pitir / zekat hasaplamasy"

    fitreZakatRowHint = "Zekatyňyzy, pitiriňizi we fidýaňyzy hasaplaň"

    mushafNewSurahTemplate = "Täze süre: {surah}"

    mushafStopAction = "Saklat"

    mushafSafetyStopTemplate = "Yzygiderli {count} sahypa diňlenildi. Diňlemegi dowam etdirmelimi?"

    mushafContinueAction = "Dowam et"

    mushafPageLoadFailed = "Indiki sahypany ýükläp bolmady"

    mushafGestureHint = "Diňlemek üçin aýata basyň, amallar üçin basyp saklaň"

    mushafGoToPlayingPage = "Okalýan sahypa dolan ({page})"

    mushafKeyboardHint = "Boşluk: oýnat/sakla · ← →: sahypa öwür"

    mushafCopiedMessage = "Aýat göçürildi"

    mushafFavoriteAdded = "Halanlaryma goşuldy"

    mushafFavoriteRemoved = "Halanlarymdan aýryldy"

    mushafUndoAction = "Yzyna al"

    mushafAyahActionsTitleTemplate = "{surah}, {ayah}-nji aýat"

    mushafPlayFromHere = "Şu ýerden diňle"

    mushafCopyAction = "Göçür"

    mushafAddFavorite = "Halanlaryma goş"

    mushafRemoveFavorite = "Halanlarymdan aýyr"

    shareCardTitle = "Aýaty paýlaş"

    shareImageAction = "Suraty paýlaş"

    shareTextAction = "Teksti paýlaş"

    shareCopyLink = "Baglanyşygy göçür"

    shareLinkCopied = "Baglanyşyk göçürildi"

    shareImageFailed = "Suraty taýýarlap bolmady"

}

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
