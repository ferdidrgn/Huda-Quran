package org.ferdidrgn.hudaquran.domain.model

data class ArabicLetter(val letter: String, val name: String, val pronunciation: String)

val arabicAlphabet = listOf(
    ArabicLetter("ا", "Elif", "a, e, ı sesi"),
    ArabicLetter("ب", "Be", "b sesi"),
    ArabicLetter("ت", "Te", "t sesi"),
    ArabicLetter("ث", "Se", "ince s sesi"),
    ArabicLetter("ج", "Cim", "c sesi"),
    ArabicLetter("ح", "Ha", "boğazdan h sesi"),
    ArabicLetter("خ", "Hı", "hırıltılı h sesi"),
    ArabicLetter("د", "Dal", "d sesi"),
    ArabicLetter("ذ", "Zel", "ince z sesi"),
    ArabicLetter("ر", "Ra", "r sesi"),
    ArabicLetter("ز", "Ze", "z sesi"),
    ArabicLetter("س", "Sin", "s sesi"),
    ArabicLetter("ش", "Şin", "ş sesi"),
    ArabicLetter("ص", "Sad", "kalın s sesi"),
    ArabicLetter("ض", "Dad", "kalın d sesi"),
    ArabicLetter("ط", "Tı", "kalın t sesi"),
    ArabicLetter("ظ", "Zı", "kalın z sesi"),
    ArabicLetter("ع", "Ayn", "boğazdan gelen özel ses"),
    ArabicLetter("غ", "Gayn", "gırtlaktan g sesi"),
    ArabicLetter("ف", "Fe", "f sesi"),
    ArabicLetter("ق", "Kaf", "kalın k sesi"),
    ArabicLetter("ك", "Kef", "k sesi"),
    ArabicLetter("ل", "Lam", "l sesi"),
    ArabicLetter("م", "Mim", "m sesi"),
    ArabicLetter("ن", "Nun", "n sesi"),
    ArabicLetter("و", "Vav", "v, u, o sesi"),
    ArabicLetter("ه", "He", "h sesi"),
    ArabicLetter("ي", "Ye", "y, i sesi"),
)
