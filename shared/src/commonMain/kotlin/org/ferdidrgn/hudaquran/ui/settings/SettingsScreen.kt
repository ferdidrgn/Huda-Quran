package org.ferdidrgn.hudaquran.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.ferdidrgn.hudaquran.billing.BillingManager
import org.ferdidrgn.hudaquran.billing.BillingProduct
import org.ferdidrgn.hudaquran.billing.PurchaseOutcome
import org.ferdidrgn.hudaquran.data.local.AppLanguage
import org.ferdidrgn.hudaquran.data.local.TextSizeOption
import org.ferdidrgn.hudaquran.data.local.ThemeMode
import org.ferdidrgn.hudaquran.data.local.appVersionName
import org.ferdidrgn.hudaquran.di.AppContainer
import org.ferdidrgn.hudaquran.domain.model.PrayerLocations
import org.ferdidrgn.hudaquran.notifications.ReminderPlanner
import org.ferdidrgn.hudaquran.platform.Platform
import org.ferdidrgn.hudaquran.platform.currentPlatform
import org.ferdidrgn.hudaquran.ui.components.AdBannerCard
import org.ferdidrgn.hudaquran.ui.components.FilterPill
import org.ferdidrgn.hudaquran.ui.components.GlassSurface
import org.ferdidrgn.hudaquran.ui.components.OrnamentRule
import org.ferdidrgn.hudaquran.ui.components.PageHeader
import org.ferdidrgn.hudaquran.ui.components.SiteFooter
import org.ferdidrgn.hudaquran.ui.components.screenBackground
import org.ferdidrgn.hudaquran.ui.localization.LocalStrings
import org.ferdidrgn.hudaquran.util.shareText

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onOpenReciterPicker: () -> Unit,
    onOpenTranslationPicker: () -> Unit,
    onOpenTafsirPicker: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onOpenZakatCalculator: () -> Unit,
    onOpenDuaList: () -> Unit,
    onOpenIslamicCalendar: () -> Unit,
) {
    val preferences = AppContainer.preferences
    val repository = AppContainer.repository
    val prayerRepository = AppContainer.prayerRepository
    val selectedTheme by preferences.themeMode.collectAsState()
    val selectedTextSize by preferences.textSize.collectAsState()
    val notificationsEnabled by preferences.prayerNotificationsEnabled.collectAsState()
    val appLanguage by preferences.appLanguage.collectAsState()
    val strings = LocalStrings.current
    val adFree by preferences.adFree.collectAsState()
    val uriHandler = LocalUriHandler.current
    fun openLink(url: String) {
        runCatching { uriHandler.openUri(url) }
    }
    val billingScope = rememberCoroutineScope()
    var purchaseBusy by remember { mutableStateOf(false) }
    var purchaseMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { BillingManager.refresh() }
    fun buy(product: BillingProduct) {
        if (purchaseBusy) return
        purchaseBusy = true
        billingScope.launch {
            purchaseMessage = when (BillingManager.purchase(product)) {
                PurchaseOutcome.SUCCESS -> strings.purchaseSuccessMessage
                PurchaseOutcome.CANCELLED -> strings.purchaseCancelledMessage
                PurchaseOutcome.UNAVAILABLE -> strings.purchaseUnavailableMessage
                PurchaseOutcome.NOT_ACTIVE, PurchaseOutcome.ERROR -> strings.purchaseErrorMessage
            }
            purchaseBusy = false
        }
    }

    var reciterName by remember { mutableStateOf(preferences.selectedReciter) }
    var translationName by remember { mutableStateOf(preferences.selectedTranslation) }
    var tafsirName by remember { mutableStateOf(preferences.selectedTafsir) }
    val city = preferences.prayerCity
    val country = preferences.prayerCountry
    val locationDisplayName = PrayerLocations.all.firstOrNull { it.city == city && it.country == country }
        ?.displayName ?: "$city, $country"
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val reciter = repository.getReciters().firstOrNull { it.identifier == preferences.selectedReciter }
        if (reciter != null) reciterName = reciter.displayName
        val translation = repository.getTranslations().firstOrNull { it.identifier == preferences.selectedTranslation }
        if (translation != null) translationName = "${translation.displayName} (${translation.language})"
        val tafsirs = repository.getTafsirs()
        val tafsir = tafsirs.firstOrNull { it.identifier == preferences.selectedTafsir }
        tafsirName = when {
            tafsir != null -> "${tafsir.displayName} (${tafsir.language})"
            preferences.selectedTafsir.isBlank() -> strings.tafsirNotSelected
            else -> preferences.selectedTafsir
        }
    }

    val reminderLead by preferences.prayerReminderLeadMinutes.collectAsState()
    val reminderAtTime by preferences.prayerAtTimeEnabled.collectAsState()
    val occasionReminders by preferences.occasionRemindersEnabled.collectAsState()

    fun rescheduleNotifications() {
        scope.launch { ReminderPlanner.reschedule() }
    }

    // Capped at readable-line-width and centered so a wide desktop browser window reads like a
    // page, not the same phone column stretched full-bleed across the screen. A no-op on mobile,
    // where the available width is always under the cap.
    Box(
        modifier = modifier.fillMaxSize().screenBackground(),
        contentAlignment = Alignment.TopCenter,
    ) {
    org.ferdidrgn.hudaquran.ui.components.IslamicMotifBackground(
        modifier = Modifier.matchParentSize(),
        tint = MaterialTheme.colorScheme.primary,
        alpha = 0.035f,
    )
    Column(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState()),
    ) {
        PageHeader(title = strings.settingsTitle)

        SectionTitle(strings.language)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(
                title = "${appLanguage.flag} ${appLanguage.nativeName}",
                value = strings.languagePickerSubtitleTemplate.replace("{n}", AppLanguage.entries.size.toString()),
                onClick = onOpenLanguagePicker,
            )
        }

        SectionTitle(strings.appearance)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            ThemeSegmentedControl(selected = selectedTheme, onSelect = { preferences.setThemeMode(it) })
        }

        SectionTitle(strings.textSizeLabel)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Text(
                strings.textSizeHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp),
            )
            TextSizeSegmentedControl(selected = selectedTextSize, onSelect = { preferences.setTextSize(it) })
        }

        SectionTitle(strings.recitationAndTranslation)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(title = strings.reciterLabel, value = reciterName, onClick = onOpenReciterPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = strings.translationLabel, value = translationName, onClick = onOpenTranslationPicker)
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(title = strings.tafsirLabel, value = tafsirName, onClick = onOpenTafsirPicker)
            Text(
                strings.tafsirVsMealHint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (!preferences.isAdFree()) {
            AdBannerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        SectionTitle(strings.prayerNotifications)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.notificationsToggleTitle, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        strings.notificationsToggleSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { enabled ->
                        preferences.setPrayerNotificationsEnabled(enabled)
                        rescheduleNotifications()
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            NavigationRow(title = strings.locationLabel, value = locationDisplayName, onClick = onOpenLocationPicker)

            if (notificationsEnabled) {
                OrnamentRule(modifier = Modifier.padding(vertical = 10.dp))
                Text(strings.prayerReminderLeadLabel, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0, 5, 10, 15, 30).forEach { minutes ->
                        FilterPill(
                            label = if (minutes == 0) strings.reminderOffLabel else strings.minutesShortTemplate.replace("{n}", minutes.toString()),
                            selected = reminderLead == minutes,
                            onClick = {
                                preferences.setPrayerReminderLeadMinutes(minutes)
                                rescheduleNotifications()
                            },
                        )
                    }
                }
                ReminderSwitchRow(strings.prayerAtTimeLabel, reminderAtTime) {
                    preferences.setPrayerAtTimeEnabled(it)
                    rescheduleNotifications()
                }
                ReminderSwitchRow(strings.occasionRemindersLabel, occasionReminders) {
                    preferences.setOccasionRemindersEnabled(it)
                    rescheduleNotifications()
                }
                Text(
                    strings.notificationSoundHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 4.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    strings.locationAutoUpdateNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }

        SectionTitle(strings.supportUsTitle)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), ornament = true) {
            val adFreeUntil = preferences.adsRemovedUntilMillis
            Text(
                when {
                    !adFree -> strings.supportUsMessage
                    adFreeUntil < AD_FREE_DATE_DISPLAY_LIMIT -> strings.adFreeUntilTemplate.replace("{date}", formatDate(adFreeUntil))
                    else -> strings.adFreeActiveMessage
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (adFree) 0.9f else 0.7f),
            )
            // Always useful, with or without billing: rate, share and write to the developer.
            Spacer(modifier = Modifier.height(8.dp))
            if (currentPlatform != Platform.WEB) {
                NavigationRow(
                    title = strings.supportRateApp,
                    value = strings.supportRateAppHint,
                    icon = Icons.Filled.Star,
                    onClick = { openLink(rateAppUrl()) },
                )
            }
            NavigationRow(
                title = strings.supportShareApp,
                value = strings.supportShareAppHint,
                icon = Icons.Outlined.Share,
                onClick = { shareText(strings.shareAppMessage.replace("{url}", shareAppUrl())) },
            )
            if (FEEDBACK_EMAIL.isNotBlank()) {
                NavigationRow(
                    title = strings.supportFeedback,
                    value = strings.supportFeedbackHint,
                    icon = Icons.Outlined.Email,
                    onClick = { openLink(feedbackMailto()) },
                )
            }
            if (BillingManager.isSupported) {
                OrnamentRule(modifier = Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { buy(BillingProduct.DONATION_SMALL) },
                        enabled = !purchaseBusy,
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.smallDonationButton) }
                    OutlinedButton(
                        onClick = { buy(BillingProduct.DONATION_MEDIUM) },
                        enabled = !purchaseBusy,
                        modifier = Modifier.weight(1f),
                    ) { Text(strings.mediumDonationButton) }
                }
                if (!adFree) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { buy(BillingProduct.NO_ADS_6_MONTHS) },
                        enabled = !purchaseBusy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        if (purchaseBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(strings.sixMonthAdFreeButton)
                        }
                    }
                }
                // Required by the App Store, and how a reinstall or a new phone gets the purchase back.
                TextButton(
                    onClick = {
                        if (!purchaseBusy) {
                            purchaseBusy = true
                            billingScope.launch {
                                purchaseMessage = when (BillingManager.restore()) {
                                    PurchaseOutcome.SUCCESS -> strings.purchaseSuccessMessage
                                    PurchaseOutcome.NOT_ACTIVE -> strings.restoreNothingMessage
                                    else -> strings.purchaseErrorMessage
                                }
                                purchaseBusy = false
                            }
                        }
                    },
                    enabled = !purchaseBusy,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) { Text(strings.restorePurchasesButton) }
            }
            purchaseMessage?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        SectionTitle(strings.moreTitle)
        GlassSurface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            NavigationRow(
                title = strings.fitreZakatRowTitle,
                value = strings.fitreZakatRowHint,
                icon = Icons.Outlined.Calculate,
                onClick = onOpenZakatCalculator,
            )
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(
                title = strings.duaListTitle,
                value = "",
                icon = Icons.Outlined.AutoStories,
                onClick = onOpenDuaList,
            )
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(
                title = strings.islamicCalendarTitle,
                value = "",
                icon = Icons.Outlined.Event,
                onClick = onOpenIslamicCalendar,
            )
            Spacer(modifier = Modifier.height(4.dp))
            NavigationRow(
                title = strings.hacKuraLabel,
                value = "hacumre.diyanet.gov.tr",
                icon = Icons.Outlined.Language,
                onClick = { openLink("https://hacumre.diyanet.gov.tr/") },
            )
        }

        if (!preferences.isAdFree()) {
            AdBannerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }

        Text(
            "Huda Qur'an v${appVersionName()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(16.dp),
        )
        SiteFooter()
    }
    }
}

@Composable
private fun ThemeSegmentedControl(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val strings = LocalStrings.current
    val options = listOf(
        ThemeMode.SAKURA to strings.themeSakura,
        ThemeMode.SYSTEM to strings.themeSystem,
        ThemeMode.LIGHT to strings.themeLight,
        ThemeMode.DARK to strings.themeDark,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                rowOptions.forEach { (mode, label) ->
                    val isSelected = selected == mode
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { onSelect(mode) }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextSizeSegmentedControl(selected: TextSizeOption, onSelect: (TextSizeOption) -> Unit) {
    val strings = LocalStrings.current
    val options = listOf(
        TextSizeOption.NORMAL to strings.textSizeNormal,
        TextSizeOption.LARGE to strings.textSizeLarge,
        TextSizeOption.EXTRA_LARGE to strings.textSizeExtraLarge,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (option, label) ->
            val isSelected = selected == option
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    org.ferdidrgn.hudaquran.ui.components.SectionHeader(
        title = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}

/**
 * A tappable settings row: optional leading icon, title + optional hint, chevron. Presses sink the
 * row slightly with a ripple; on the web it shows the hand cursor and a hover tint.
 */
@Composable
private fun NavigationRow(title: String, value: String, onClick: () -> Unit, icon: ImageVector? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "settingsRowScale",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (value.isNotBlank()) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
        Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=org.ferdidrgn.hudaquran"
private const val WEBSITE_URL = "https://hudaquran.web.app/"

/** Set to the numeric App Store ID (e.g. "6478123456") once the iOS app is published. */
private const val IOS_APP_STORE_ID = ""

/** Developer inbox for the "send feedback" row; the row stays hidden while this is blank. */
private const val FEEDBACK_EMAIL = ""

private fun rateAppUrl(): String = when (currentPlatform) {
    Platform.IOS ->
        if (IOS_APP_STORE_ID.isBlank()) WEBSITE_URL
        else "https://apps.apple.com/app/id$IOS_APP_STORE_ID?action=write-review"
    Platform.ANDROID -> PLAY_STORE_URL
    Platform.WEB -> WEBSITE_URL
}

private fun shareAppUrl(): String = when (currentPlatform) {
    Platform.IOS ->
        if (IOS_APP_STORE_ID.isBlank()) "$WEBSITE_URL?src=app_share" else "https://apps.apple.com/app/id$IOS_APP_STORE_ID"
    // Play's `referrer` carries the attribution through the install (utm_source=app_share).
    Platform.ANDROID -> "$PLAY_STORE_URL&referrer=utm_source%3Dapp_share"
    Platform.WEB -> "$WEBSITE_URL?src=app_share"
}

private fun feedbackMailto(): String =
    "mailto:$FEEDBACK_EMAIL?subject=Huda%20Quran%20feedback%20v${appVersionName()}"

/** Expiries beyond this (a lifetime grant) show the plain "ad-free" message instead of a date. */
private const val AD_FREE_DATE_DISPLAY_LIMIT = 4_102_444_800_000L // 2100-01-01

private fun formatDate(epochMillis: Long): String {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}.${date.year}"
}

@Composable
private fun ReminderSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}
