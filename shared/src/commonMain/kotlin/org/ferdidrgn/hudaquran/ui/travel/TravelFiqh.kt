package org.ferdidrgn.hudaquran.ui.travel

/** Educational profiles only; they are deliberately not a fatwa engine. */
enum class TravelMadhhab(val label: String) {
    HANAFI("Hanefî"),
    SHAFII("Şafiî"),
    MALIKI("Mâlikî"),
    HANBALI("Hanbelî"),
    LOCAL_SCHOLAR("Yerel ilmî rehber")
}

data class TravelPrayerGuidance(
    val title: String,
    val summary: String,
    val details: List<String>,
    val sourceUrl: String,
)

fun travelPrayerGuidance(madhhab: TravelMadhhab): TravelPrayerGuidance = when (madhhab) {
    TravelMadhhab.HANAFI -> TravelPrayerGuidance(
        title = "Hanefî için temel yolculuk notu",
        summary = "Seferîlikte dört rekâtlı farzlar iki rekât kılınır; sabah iki, akşam üç rekâttır.",
        details = listOf(
            "Seferî kişi mukim imama uyarsa namazı imamla birlikte tamamlar.",
            "Hanefî uygulamada Arafat ve Müzdelife dışındaki gerçek cem konusu genel bir kestirme değildir; uygulamayı vakitleri içinde planlayın.",
            "Sünnet ve nâfileler için uygulama kişisel ilmî rehberliğe göre değişebilir; uygulama otomatik olarak ‘terk edilebilir’ hükmü vermez.",
        ),
        sourceUrl = "https://webdosyasp.diyanet.gov.tr/muftuluk/UserFiles/sanliurfa/Ilceler/birecik/UserFiles/Files/Dinim%20%C4%B0slam_a1972226-b25f-4d82-87c6-bb2d85e8e230.pdf",
    )
    TravelMadhhab.SHAFII -> TravelPrayerGuidance(
        title = "Şafiî için temel yolculuk notu",
        summary = "Kasr ruhsattır; cem ve kasrın şartları mesafe, niyet, süre ve yolculuk hâline göre değerlendirilir.",
        details = listOf(
            "Uygulama yalnızca durak sayısına göre belirlenmez.",
            "Cem düşünülüyorsa mezhebin şartları ve vakit niyeti birlikte kontrol edilmelidir.",
            "Kesin uygulama için güvenilen bir Şafiî ilmî kaynağa danışın.",
        ),
        sourceUrl = "https://dijital.diyanet.gov.tr/File/Download?id=4130&path=4130_1.pdf",
    )
    else -> TravelPrayerGuidance(
        title = "Mezhebe göre değişen yolculuk notu",
        summary = "Kasr, cem, mesafe ve konaklama süresi mezhebe ve şartlara göre değişebilir.",
        details = listOf(
            "Uygulama tek bir durak, araç veya konum bilgisine indirgenemez.",
            "Bu ekran ibadet planlama ve eğitim içindir; kişisel hüküm/fetva üretmez.",
            "Kararsız kaldığınızda mezhebinizin güvenilir ilmî kaynağına danışın.",
        ),
        sourceUrl = "https://kurul.diyanet.gov.tr/tr/video/yolculuk-halinde-ibadetlerin-hukumleri-ilmihal-27bolum/019a6d99-7be9-7a3b-8360-b071f7568569",
    )
}
