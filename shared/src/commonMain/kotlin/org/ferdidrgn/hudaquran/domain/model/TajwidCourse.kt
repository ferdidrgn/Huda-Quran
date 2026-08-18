package org.ferdidrgn.hudaquran.domain.model

data class TajwidExample(val arabic: String, val transliteration: String, val explanation: String)

data class TajwidLesson(
    val id: String,
    val order: Int,
    val title: String,
    val summary: String,
    val examples: List<TajwidExample>,
)

// Arabic combining marks (harekeler)
private const val FATHA = "َ" // üstün
private const val KASRA = "ِ" // esre
private const val DAMMA = "ُ" // ötre
private const val SUKUN = "ْ" // cezm / sükun
private const val FATHATAN = "ً" // çift üstün (tenvin)
private const val KASRATAN = "ٍ" // çift esre (tenvin)
private const val DAMMATAN = "ٌ" // çift ötre (tenvin)
private const val SHADDA = "ّ" // şedde

private data class ConsonantSample(val letter: String, val name: String, val sound: String)

private val sampleConsonants = listOf(
    ConsonantSample("ب", "Be", "b"),
    ConsonantSample("ت", "Te", "t"),
    ConsonantSample("ج", "Cim", "c"),
    ConsonantSample("د", "Dal", "d"),
    ConsonantSample("ر", "Ra", "r"),
    ConsonantSample("س", "Sin", "s"),
    ConsonantSample("ف", "Fe", "f"),
    ConsonantSample("ك", "Kef", "k"),
    ConsonantSample("ل", "Lam", "l"),
    ConsonantSample("م", "Mim", "m"),
    ConsonantSample("ن", "Nun", "n"),
)

val tajwidCourse: List<TajwidLesson> = listOf(
    TajwidLesson(
        id = "letters",
        order = 1,
        title = "1. Ders: Elif-Ba (Harfler)",
        summary = "Arap alfabesinin 28 harfi ve tek başlarına nasıl okunduğu.",
        examples = arabicAlphabet.map { letter ->
            TajwidExample(letter.letter, letter.name, letter.pronunciation)
        },
    ),
    TajwidLesson(
        id = "harakat",
        order = 2,
        title = "2. Ders: Harekeler (Üstün, Esre, Ötre)",
        summary = "Harfler hareke alınca nasıl okunur: üstün \"a\", esre \"i\", ötre \"u\" sesi verir.",
        examples = sampleConsonants.flatMap { c ->
            listOf(
                TajwidExample("${c.letter}$FATHA", "${c.sound}a", "${c.name} harfi üstün ile: kısa \"${c.sound}a\" sesi"),
                TajwidExample("${c.letter}$KASRA", "${c.sound}i", "${c.name} harfi esre ile: kısa \"${c.sound}i\" sesi"),
                TajwidExample("${c.letter}$DAMMA", "${c.sound}u", "${c.name} harfi ötre ile: kısa \"${c.sound}u\" sesi"),
            )
        },
    ),
    TajwidLesson(
        id = "sukun",
        order = 3,
        title = "3. Ders: Sükun (Cezm)",
        summary = "Sükunlu harf harekesizdir; kendinden önceki harfle birlikte, sesli harf eklenmeden okunur.",
        examples = sampleConsonants.map { c ->
            TajwidExample("${c.letter}$SUKUN", c.sound, "${c.name} harfi sükun ile: harekesiz okunur, ekstra sesli harf yoktur")
        },
    ),
    TajwidLesson(
        id = "tanwin",
        order = 4,
        title = "4. Ders: Tenvin (Çift Hareke)",
        summary = "Kelime sonunda çift hareke, harfi genizden gelen \"n\" sesiyle uzatır: an, in, un.",
        examples = sampleConsonants.flatMap { c ->
            listOf(
                TajwidExample("${c.letter}$FATHATAN", "${c.sound}an", "Çift üstün (fethateyn): \"${c.sound}an\" diye genizden okunur"),
                TajwidExample("${c.letter}$KASRATAN", "${c.sound}in", "Çift esre (kesrateyn): \"${c.sound}in\" diye genizden okunur"),
                TajwidExample("${c.letter}$DAMMATAN", "${c.sound}un", "Çift ötre (dammeteyn): \"${c.sound}un\" diye genizden okunur"),
            )
        },
    ),
    TajwidLesson(
        id = "medd",
        order = 5,
        title = "5. Ders: Med Harfleri (Uzatma)",
        summary = "Elif (ا), vav (و) ve ye (ي) — kendinden önceki harekeyle uyumlu geldiklerinde sesi uzatır.",
        examples = listOf(
            TajwidExample("با", "baa", "Üstünden sonra gelen elif (ا): \"a\" sesi normalden 2 kat uzun okunur"),
            TajwidExample("بو", "buu", "Ötreden sonra gelen vav (و): \"u\" sesi normalden 2 kat uzun okunur"),
            TajwidExample("بي", "bii", "Esreden sonra gelen ye (ي): \"i\" sesi normalden 2 kat uzun okunur"),
            TajwidExample("سا", "saa", "Üstünden sonra elif: uzun \"a\" sesi"),
            TajwidExample("نو", "nuu", "Ötreden sonra vav: uzun \"u\" sesi"),
            TajwidExample("مي", "mii", "Esreden sonra ye: uzun \"i\" sesi"),
        ),
    ),
    TajwidLesson(
        id = "shadda",
        order = 6,
        title = "6. Ders: Şedde (Vurgu/İkileme)",
        summary = "Şeddeli harf iki harf yerine geçer: ilki sakin, ikincisi harekeli olacak şekilde vurgulu okunur.",
        examples = sampleConsonants.map { c ->
            TajwidExample(
                "${c.letter}$SHADDA$FATHA",
                "${c.sound}${c.sound}a",
                "${c.name} harfi şeddeli: harf iki kez vurgulu okunur (${c.sound}-${c.sound}a)",
            )
        },
    ),
)
