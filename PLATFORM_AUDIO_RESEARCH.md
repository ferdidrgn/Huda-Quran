# Huda Quran - Çoklu Platform Ses Araştırması

Tarih: 2026-11-21

## Amaç ve kanıt sınırı

Kur'an dinleme deneyimini Android Auto, Wear OS, Android TV ve Chromecast'e taşırken benzer uygulamalardaki kullanışlı akışları incelemek; yalnızca doğrulanmış davranışları Huda'nın iş listesine dönüştürmek.

Bu rapor iki kanıt türünü ayırır:

- **Kaynak/mağaza kanıtı:** Uygulama açıklaması, resmî geliştirici belgesi, açık kaynak deposu ve Huda kaynak kodu.
- **REA APK kanıtı:** Yerel APK dosyası üzerinde manifest, DEX, kaynak, endpoint adayı ve bileşen incelemesi. Bu oturumda henüz örnek APK dosyası REA'ya açılamadı. Aşağıdaki uygulama özellikleri APK'dan doğrulanmış sayılmaz.

REA'nın APK araçları yerel mutlak dosya yolu istiyor. Resmî GitHub APK linki çalışma alanından indirilmeye çalışıldığında DNS çözümlenmedi; REA ADB envanterinde de bağlı cihaz yok. Kaynak/mağaza araştırması tamamlanabilir, ancak APK'nın gerçek izinleri, sınıfları, endpoint'leri, auth modeli ve ağ trafiği konusunda henüz bulgu yok. Play açıklamasında yazmayan davranışlar varsayılmamalı.

## Platform başına referans uygulama

| Platform | Seçilen referans | Mağaza/kaynakta görülen | Resmî APK durumu |
|---|---|---|---|
| Android Auto | [Quran for Android / Quran.com](https://github.com/quran/quran_android) | Android Auto albüm görseli ve medya oynatma desteği var; proje kaynak kodu açık. [Quran Auto](https://play.google.com/store/apps/details?id=com.quranapp.auto) karşılaştırma için resume, ayet aralığı, reciter seçimi, tekrar, offline download, voice search ve Cast özelliklerini listeliyor. | Quran for Android 3.6.5 resmî GitHub release APK'sı ve SHA-256 aşağıda var; dosya bu oturumda indirilemedi. |
| Wear OS | [Quran for Wear OS](https://play.google.com/store/apps/details?id=com.loftymr.quranforwear) | Telefon+saat Kur'an metni, çevrimdışı Arapça metin, yuvarlak ekran/rotary gezinme, kıraatçı akışı, yer imi ve okuma konumu eşleme, Tile. Bu bilgiler mağaza tanımıdır. | Geliştirici tarafından yayımlanmış APK veya kaynak deposu bulunamadı. Google Play dışı APK kullanmayacağız. |
| Android TV | [Quran Kareem Radio TV](https://github.com/ramysami/quran-kareem-radio-tv) | Android TV'de canlı Kur'an radyosu; geliştirici deposunda tam kumanda desteği ve uyku sayacı belirtiliyor. [Quran for Android TV](https://play.google.com/store/apps/details?id=qariapps.prayertimetv) ayrı karşılaştırma olarak büyük ekranda Uthmani mushaf metni ve görünüm seçenekleri sunuyor. | Quran Kareem Radio TV v1.4.1 resmî GitHub release APK'sı ve SHA-256 aşağıda var; dosya bu oturumda indirilemedi. |
| Chromecast / Google Cast | [Azkar](https://play.google.com/store/apps/details?id=tech.cloud24.allahsnames) | Kur'an kıraatı Cast ile TV/Nest'e gönderiliyor; telefon kumanda ve ayet takibi arayüzü, reciter seçimi ve tekrar mevcut. Google Cast sender/receiver modelinde sender kontrol isteği yollar, receiver kendi ağından medyayı yükler. | Geliştiricinin doğrudan APK/release dosyası bulunamadı; resmî Google Play listing mevcut. |

### APK bağlantıları ve kimlik bilgileri

- Quran for Android v3.6.5, proje sahibi release: [APK indir](https://github.com/quran/quran_android/releases/download/v3.6.5/quran-3.6.5.apk)  
  GitHub release API'sinin yayımladığı SHA-256: f9180b86d519de39ab643d639a1a061c691feda0cadbcb6bca91ee033a77b61c (9,248,360 byte).
- Quran Kareem Radio TV v1.4.1, proje sahibi release: [APK indir](https://github.com/ramysami/quran-kareem-radio-tv/releases/download/v1.4.1/QuranKareemRadio.apk)  
  GitHub release API'sinin yayımladığı SHA-256: 8b72a7f4611892a315a9d762653628525012a797e3d85ac7673e5fb1df1d1a54 (15,777,019 byte).
- Wear ve Cast mağaza uygulamalarında resmî açık APK yok. Üçüncü taraf APK arşivleri güncel Play imzası/sürümüyle eşleştiği kanıtlanmadan örnek APK sayılmayacak.

SHA-256 release metadata'sı kaynağın yayımladığı digest'tir; dosya yerel olarak indirilip digest eşleşmesi yapılmadan dosyanın elde olduğu veya REA'dan geçtiği anlamına gelmez.

## Huda kod tabanındaki mevcut yapı

Kaynak kod incelemesinde:

- KMP common katmanındaki PlaybackManager, ayet kuyruğu/sure modu, sıra indeksi, kıraatçı kimliği, tekrar ve oynatma state'ini yönetiyor.
- Android AudioPlayer Media3 ExoPlayer kullanıyor; medya item URL ile açılıyor. Mevcut implementasyonda ExoPlayer private; kalıcı Media3 DownloadManager/Cache akışı görünmüyor.
- PlaybackNotificationService bir MediaSessionCompat kuruyor. Bildirim/kilit ekranı/sistem medya kontrolü play, pause, stop, seek, next, previous, metadata ve position sağlıyor.
- AppContainer oynatma ilerlemesini AppPreferences.saveLastRead(...) çağrısına bağlıyor. Bu, son ayet bilgisinin saklandığını gösterir; servis yeniden kurulduğunda tam audio queue, reciter ve milisaniye konumunun yeniden kurulması henüz doğrulanmış değildir.
- Android manifestte playback notification service var; Android Auto içerik kataloğu için MediaBrowserServiceCompat/MediaLibraryService, root/children hiyerarşisi ve automotive media descriptor görünmüyor.
- Google Cast sender SDK/receiver akışı, Wear OS modülü ve TV launcher/TV giriş noktası görünmüyor.
- Uygulama kodunda Quran Foundation proxy'si https://hudaquran.web.app/api/qf olarak tanımlı. Kıraat audio'su için ortak player URL üzerinden oynatıyor; bu mevcut kod bulgusudur, referans uygulamaların API'si değildir.

## Kullanıcı akışları ve Huda'ya uyarlama

### Android Auto

1. Araç bağlantısında uygulamanın ses kataloğu görünür.
2. En üstte Devam Et ve Son Dinlenenler bulunur; sonra Sureler, Kıraatçılar ve İndirilenler gelir.
3. Kullanıcı sure/kıraatçı seçer; oynat, duraklat, önceki/sonraki ve desteklenen sesli arama çalışır.
4. Araç ekranı sürüşe uygun medya UI'ıdır; mushaf metninde serbest gezinme veya araç kullanırken metin okuma eklenmez.
5. Bağlantı kurulması kendiliğinden playback başlatmaz. Ses kesintisi, telefon kilidi, resume ve offline durumları doğrulanır.

### Wear OS

1. İlk artımda saat, telefondaki aktif media session için play/pause/seek/next/previous kumandası olur.
2. Küçük ekranda Devam Et, oynatıcı başlığı ve az sayıda büyük kontrol gösterilir.
3. Ayrı Wear OS Kur'an UI'ı ve telefonsuz ses indirme ikinci aşamadır; saat bataryası, kulaklık/ses çıkışı, ağ geçişi ve indirme alanı ölçülür.
4. Phone-Wear Data Layer yalnızca Android eşlemesi için; platformlar arası eşleme ayrı backend kararıdır.

### Android TV / Google TV

1. TV açılışında Devam Et, Son Dinlenenler, Sureler, Cüzler, Kıraatçılar ve İndirilenler.
2. D-pad ile her öğe ve playback kontrolüne odaklanma; büyük metin, görünür focus, geri tuşu.
3. Oynatıcı sure/ayet bilgisini ve mevcut ayet takibini TV mesafesinden okunur biçimde gösterir.
4. Cast alıcısından ayrı TV uygulama yüzeyi; TV manifest/launcher/banner ve D-pad kalite kontrolleri gerekir.

### Chromecast / Google Cast

1. Kullanıcı telefonda sure/kıraatçı seçip Cast cihazına bağlanır.
2. Telefon sender/remote; TV/Nest receiver. Telefon play/pause/seek/next/ses komutlarını yollar ve receiver durumu telefona döner.
3. Receiver'ın erişebildiği HTTPS audio URL, metadata, kuyruk ve session lifecycle desteklenir.
4. Telefonun yalnızca yerel diskinde olan offline sesin Chromecast tarafından doğrudan okunabildiği varsayılmaz; bu davranış ayrı sınanır.
5. Cast oturumu kapandığında oynatma telefona dönüşü ve progress kaydı kullanıcıya açık olur.

## REA APK inceleme matrisi - bekleyen iş

Her APK için şu kanıtlar çıkarılacak ve uygulamalar arasında kıyaslanacak:

1. Dosya özeti: kaynak URL, paket adı, versionName/versionCode, byte size, SHA-256, signer certificate; split/APK seti var mı.
2. Manifest: SDK seviyeleri, permissions, exported components, deep link/intent filters, foreground service türleri, media session, auto/TV/watch metadata.
3. Paket graph: Activity, Service, Receiver, Provider; ekran/feature sınıfları, media session/client, player ve download bileşenleri.
4. API: URL host/path/query adayları, ağ client kütüphaneleri, auth/token/header kullanımı, remote config/analytics/ads, reciter catalog ve audio CDN.
5. Playback: URL üretimi, surah/ayah range queue, reciter/riwayet mapping, resume alanları, repeat/speed/sleep timer, interruption/focus, cache/offline dosya yolu.
6. Veri akışı: bookmark/progress/settings'in local DB/DataStore/Preferences/cloud sync'e yazılması; saklama süresi ve kişisel veri.
7. UI: başlangıç, reciter seçimi, browse tree, player, download manager, cast connect/disconnect, watch phone/standalone, TV D-pad akışları.
8. İzin ve risk: ağ/konum/kimlik/foreground/notification izinlerinin call path ile kullanımı; şüpheli veya gereksiz izinler; içerik/lisans atıf metinleri.
9. Kanıt standardı: statik endpoint ve strings yalnızca aday; gerçek istek ve response için kontrollü çalışma cihazında REA network capture gerekir. APK içinden her şeyi çıkarmak garanti edilemez; şifrelenmiş/pin'li trafik veya sunucu davranışı statik analizle görünmeyebilir.

## Teknik kaynaklar

- [Android for Cars media app](https://developer.android.com/media/implement/surfaces/cars): MediaLibraryService/MediaSession ve sürüşe uygun browser.
- [Android media on Wear OS](https://developer.android.com/media/implement/surfaces/wear-os): downloaded content, spotty network, düşük batarya, Media3 downloads/WorkManager.
- [Android TV media apps](https://developer.android.com/media/implement/surfaces/android-tv): TV'de MediaSession ve uygun medya deneyimi.
- [Google Cast Android Sender](https://developers.google.com/cast/docs/android_sender/integrate): Sender, receiver ve state senkronizasyonu.
- [Google Cast interaction model](https://developers.google.com/cast/docs/ux_guidelines): receiver medya içeriğini Cast cihazının ağ bağlantısından alır.

## Kaynaklar

- [Quran Auto Play](https://play.google.com/store/apps/details?id=com.quranapp.auto)
- [Quran Audio Play](https://play.google.com/store/apps/details?id=com.quranicaudio.app)
- [Quran for Wear OS Play](https://play.google.com/store/apps/details?id=com.loftymr.quranforwear)
- [Azkar Cast feature](https://azkar.cloud24.tech/en/features/chromecast-quran)
- [Quran for Android TV Play](https://play.google.com/store/apps/details?id=qariapps.prayertimetv)
- [Quran Android releases](https://github.com/quran/quran_android/releases)
- [Quran Kareem Radio TV source/releases](https://github.com/ramysami/quran-kareem-radio-tv)
- [Taqwa KMP/Wear OS source](https://github.com/MohamedAbulgasem/Taqwa)
