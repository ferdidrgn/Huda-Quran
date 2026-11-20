package org.ferdidrgn.hudaquran.widget

import android.content.Context

/** A small offline catalog used whenever the remote/cache layer has no usable daily item. */
internal data class DailyWidgetItem(val reference: String, val text: String)

internal object DailyWidgetContent {
    private const val STORE = "huda_daily_widget"
    private const val KEY_DAY = "cached_day"
    private const val KEY_REFERENCE = "cached_reference"
    private const val KEY_TEXT = "cached_text"

    private val FALLBACK = listOf(
        DailyWidgetItem("Bakara 2:286", "Allah hiç kimseye gücünün yettiğinden fazlasını yüklemez."),
        DailyWidgetItem("İnşirah 94:5", "Şüphesiz güçlükle beraber bir kolaylık vardır."),
        DailyWidgetItem("Ra'd 13:28", "Kalpler ancak Allah'ı anmakla huzur bulur."),
        DailyWidgetItem("Zümer 39:53", "Allah'ın rahmetinden ümit kesmeyin."),
        DailyWidgetItem("Ankebut 29:69", "Bizim uğrumuzda gayret edenleri elbette yollarımıza eriştireceğiz."),
        DailyWidgetItem("Talak 65:3", "Kim Allah'a güvenirse O, ona yeter."),
        DailyWidgetItem("Bakara 2:152", "Beni anın ki Ben de sizi anayım; Bana şükredin, nankörlük etmeyin."),
        DailyWidgetItem("Hud 11:6", "Yeryüzünde hiçbir canlı yoktur ki rızkı Allah'a ait olmasın."),
        DailyWidgetItem("Ali İmran 3:139", "Gevşemeyin, üzülmeyin; inanıyorsanız üstün gelecek olan sizsiniz."),
        DailyWidgetItem("Bakara 2:153", "Sabır ve namazla yardım dileyin. Şüphesiz Allah sabredenlerle beraberdir."),
        DailyWidgetItem("Rad 13:11", "Bir toplum kendilerindeki durumu değiştirmedikçe Allah onların durumunu değiştirmez."),
        DailyWidgetItem("Tevbe 9:51", "De ki: Allah'ın bizim için yazdığından başkası bize asla erişmez."),
        DailyWidgetItem("Nahl 16:97", "Kim iman ederek salih amel işlerse ona güzel bir hayat yaşatacağız."),
        DailyWidgetItem("Şura 42:36", "Size verilen şeyler dünya hayatının geçimliğidir; Allah katındaki ise daha hayırlı ve kalıcıdır."),
        DailyWidgetItem("Fecr 89:27-30", "Ey huzura ermiş nefis! Rabbine dön; O senden razı, sen de O'ndan razı."),
    )

    fun forDay(context: Context, dayOfYear: Int): DailyWidgetItem {
        val preferences = context.getSharedPreferences(STORE, Context.MODE_PRIVATE)
        val cachedDay = preferences.getInt(KEY_DAY, Int.MIN_VALUE)
        val cached = DailyWidgetItem(
            preferences.getString(KEY_REFERENCE, null).orEmpty().trim(),
            preferences.getString(KEY_TEXT, null).orEmpty().trim(),
        )
        if (cachedDay == dayOfYear && cached.reference.isNotEmpty() && cached.text.isNotEmpty()) return cached
        return fallbackForDay(dayOfYear)
    }

    /** Lets the app/API layer provide a fresh item without making the widget depend on a network. */
    fun cacheForDay(context: Context, dayOfYear: Int, item: DailyWidgetItem) {
        val reference = item.reference.trim().take(120)
        val text = item.text.trim().take(600)
        if (reference.isEmpty() || text.isEmpty()) return
        context.getSharedPreferences(STORE, Context.MODE_PRIVATE).edit()
            .putInt(KEY_DAY, dayOfYear)
            .putString(KEY_REFERENCE, reference)
            .putString(KEY_TEXT, text)
            .apply()
    }

    internal fun fallbackForDay(dayOfYear: Int): DailyWidgetItem = FALLBACK[Math.floorMod(dayOfYear - 1, FALLBACK.size)]

    internal val fallbackSize: Int get() = FALLBACK.size
}
