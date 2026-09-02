package org.ferdidrgn.hudaquran.domain.model

data class Dua(
    val id: String,
    val category: String,
    val title: String,
    val arabic: String,
    val transliteration: String,
    val meaning: String,
)

// Turkish-only content, matching the existing Tajwid course (see TajwidCourse.kt) — these are
// short, widely-known hadith/Quran-sourced supplications, not a full multi-language reference
// work, so they're kept in the app's primary language rather than translated into all 8 UI
// languages.
val duaList: List<Dua> = listOf(
    Dua(
        id = "wake_up",
        category = "Güne Başlarken ve Biterken",
        title = "Uyanınca Okunacak Dua",
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
        transliteration = "Elhamdü lillâhillezî ahyânâ ba'de mâ emâtenâ ve ileyhin nüşûr.",
        meaning = "Bizi öldürdükten (uyuttuktan) sonra dirilten (uyandıran) Allah'a hamdolsun. Dönüş de yalnız O'nadır.",
    ),
    Dua(
        id = "sleep",
        category = "Güne Başlarken ve Biterken",
        title = "Uyumadan Önce Okunacak Dua",
        arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
        transliteration = "Bismike Allâhümme emûtü ve ahyâ.",
        meaning = "Allah'ım! Senin adınla ölür (uyur), senin adınla dirilirim (uyanırım).",
    ),
    Dua(
        id = "before_meal",
        category = "Yemek",
        title = "Yemekten Önce Okunacak Dua",
        arabic = "اللَّهُمَّ بَارِكْ لَنَا فِيمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ",
        transliteration = "Allâhümme bârik lenâ fîmâ razaktenâ ve kınâ azâbennâr.",
        meaning = "Allah'ım! Bize verdiğin rızkı bereketli kıl ve bizi ateş azabından koru.",
    ),
    Dua(
        id = "after_meal",
        category = "Yemek",
        title = "Yemekten Sonra Okunacak Dua",
        arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ",
        transliteration = "Elhamdü lillâhillezî et'amenâ ve sekânâ ve ce'alenâ müslimîn.",
        meaning = "Bizi yediren, içiren ve müslümanlardan kılan Allah'a hamdolsun.",
    ),
    Dua(
        id = "entering_home",
        category = "Ev ve Yolculuk",
        title = "Eve Girerken Okunacak Dua",
        arabic = "بِسْمِ اللَّهِ وَلَجْنَا وَبِسْمِ اللَّهِ خَرَجْنَا وَعَلَى اللَّهِ رَبِّنَا تَوَكَّلْنَا",
        transliteration = "Bismillâhi velecnâ ve bismillâhi haracnâ ve alallâhi rabbinâ tevekkelnâ.",
        meaning = "Allah'ın adıyla girdik, Allah'ın adıyla çıktık ve Rabbimiz Allah'a güvendik.",
    ),
    Dua(
        id = "travel",
        category = "Ev ve Yolculuk",
        title = "Yolculuğa Çıkarken Okunacak Dua",
        arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَىٰ رَبِّنَا لَمُنقَلِبُونَ",
        transliteration = "Sübhânellezî sehhara lenâ hâzâ ve mâ künnâ lehû mukrinîn. Ve innâ ilâ rabbinâ lemünkalibûn.",
        meaning = "Bunu bizim hizmetimize vereni tesbih ederiz, yoksa biz buna güç yetiremezdik. Şüphesiz biz Rabbimize döneceğiz. (Zuhruf, 13-14)",
    ),
    Dua(
        id = "distress",
        category = "Sıkıntı ve Hastalık",
        title = "Sıkıntı Anında Okunacak Dua",
        arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ",
        transliteration = "Allâhümme innî eûzü bike minel hemmi vel hazen.",
        meaning = "Allah'ım! Kederden ve hüzünden sana sığınırım.",
    ),
    Dua(
        id = "illness",
        category = "Sıkıntı ve Hastalık",
        title = "Hastalıkta Okunacak Şifa Duası",
        arabic = "اللَّهُمَّ رَبَّ النَّاسِ أَذْهِبِ الْبَأْسَ اشْفِ أَنْتَ الشَّافِي لَا شِفَاءَ إِلَّا شِفَاؤُكَ شِفَاءً لَا يُغَادِرُ سَقَمًا",
        transliteration = "Allâhümme rabben nâs, ezhibil be's, işfi entaş şâfî, lâ şifâe illâ şifâuke, şifâen lâ yugâdiru sekamâ.",
        meaning = "Allah'ım! İnsanların Rabbi! Sıkıntıyı gider, şifa ver. Şifa veren sensin. Senin şifandan başka şifa yoktur. Öyle bir şifa ver ki hiç hastalık bırakmasın.",
    ),
    Dua(
        id = "ease",
        category = "Sıkıntı ve Hastalık",
        title = "Kolaylık Dilemek İçin Okunacak Dua",
        arabic = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
        transliteration = "Allâhümme lâ sehle illâ mâ ce'altehû sehlâ ve ente tec'alül hazne izâ şi'te sehlâ.",
        meaning = "Allah'ım! Senin kolay kıldığından başka kolay yoktur. Sen dilersen zoru kolay kılarsın.",
    ),
)
