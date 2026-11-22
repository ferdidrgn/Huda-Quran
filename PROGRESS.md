# Huda Quran — Ürün İlerleme Panosu

Bu dosya, Vaktelya APK/REA incelemesinden çıkan geliştirmeleri Jira benzeri şekilde takip eder.

Durumlar: `TODO` → `IN PROGRESS` → `REVIEW` → `DONE` / `BLOCKED`

## Genel kurallar

- Kullanıcı deneyimi: yaşlı kullanıcılar için büyük yazı, yüksek kontrast, açık geri bildirim ve sade hiyerarşi.
- İçerik: kaynak, dil, yazar ve lisans atfı görünür olmalı; doğrulanamayan API/veri sessizce kullanılmamalı.
- Platform: ortak mantık KMP’de, Android’e özel davranış Android katmanında, web uyumu korunarak.
- Her `DONE` görevi için en az bir derleme/test veya cihaz doğrulaması yazılmalı.
- Çakışan dosyalara aynı anda iki agent yazmamalı.

## Özet

| Alan | Durum | Son not |
|---|---|---|
| Proje panosu | IN PROGRESS | Bu dosya oluşturuldu |
| Çoklu platform sesli Kur’an | IN PROGRESS | PLATFORM_AUDIO_RESEARCH.md: dört platform referansları, Huda mimarisi ve bekleyen REA APK kapısı belgelendi; HUDA-538 ilk kapı |
| Cihaz paketleri ve boyut | IN PROGRESS | Telefon/Android Auto, TV, Automotive OS ve Wear çıktıları ayrıldı; dört Android debug varyantı derlendi, imzalı release/Play cihaz doğrulaması bekliyor |
| Kitap modu ve yaşlı modu | IN PROGRESS | A−/A+ ve ölçekleme eklendi, kapsam genişletilecek |
| Paylaşım sistemi | REVIEW | Ayet/hadis/Esma ortak kart formatı, ölçüm, tema ve çıktı seçenekleri eklendi; temiz derleme kapısı bekliyor |
| Widget ailesi | REVIEW | Android widget’ları beş palet ve örnek başına ayarla yenilendi; ayet/dua/hadis ve Kur’an odağı eklendi; kaynak derlemesi başarılı, doğru upload imzasıyla AAB bekliyor |
| Hadis/dua/Esma içerikleri | REVIEW | Favoriler ayrıştırıldı, hadis akışı sınırlandı; cihaz/ağ ekran testi bekliyor |
| Ses ve hafızlar | REVIEW | Ağ belirsizliğinde katalog korunuyor; cihaz doğrulaması bekliyor |
| Bildirim sesleri | REVIEW | Eski Android sürümlerinde kanal dışı ses/titreşim eklendi; test bildirimi izin/kanal kapalıysa sistem ayarına yönlendiriyor; cihazda ses doğrulaması bekliyor |
| Tefsir/mealler | REVIEW | Gerçek API dilleri, provider/yazar/lisans metadata’sı ve kaynak ayrıntısı gösteriliyor; ekran doğrulaması bekliyor |
| İbadet takibi | IN PROGRESS | Bugün alanı ve yaşlı profili eklendi; takip modülleri sırada |

## Yeni Öncelikli Yol Haritası - Sesli Kur’an ve Çoklu Platform (2026-11-21)

Amaç: Türkçe kullanımı, doğrulanmış kıraatçı seçkisi ve kaldığı yerden devam deneyimini Android Auto, Wear OS, Android TV ve Google Cast’e taşımak. KMP ortak katmanda katalog, playback durumu, devam noktası ve indirme politikaları; yüzeylere özel UI/medya entegrasyonları platform katmanlarında tutulacak. Mevcut uygulama/web akışı korunacak.

| Sıra | Alan | Hedef |
|---|---|---|
| P0 | Ses altyapısı ve kaldığım yer | Tek playback oturumu, arka plan sesi, kalıcı sure/ayet/kıraatçı/pozisyon, medya tuşları ve çevrimdışı indirme |
| P0 | Android Auto | Sürüşe uygun medya kataloğu ve sade oynatma kontrolleri |
| P1 | Google Cast / Chromecast | Telefonda kumanda, alıcıda akış ve oturum durumu |
| P1 | Wear OS | Saatten telefondaki sesi kontrol etme; sonra isteğe bağlı saatten bağımsız offline oynatma |
| P2 | Android TV / Google TV | Kumandayla gezinilen büyük ekran Kur’an dinleme/okuma deneyimi |
| P2 | Cihazlar arası deneyim | Ortak oynatma/okuma konumu ve kullanıcı kontrollü eşleme; hesap/senkron kararı ayrı iş |

### Cihazlara göre küçük Android paketleri (2026-11-22)

Hedef: ortak Kuran/medya mantığını paylaş, her Android form faktörüne yalnızca gerekli giriş noktası, manifest ve servisleri dağıt. Telefon ve Android Auto aynı telefon paketinde kalır; TV, Automotive OS ve Wear ayrı varyant/modül olarak çıkar. Android App Bundle ayrıca cihazın ABI, yoğunluk ve dil bölünmelerini Play üzerinden sunar.

- [x] HUDA-543 — Telefon ve Android Auto paketini ortak telefon varyantında tut
  - Durum: DONE
  - Sonuç: `phone` varyantı Android Auto medya servis/meta verisini, telefon UI'sini ve telefon widget'larını içeriyor; Leanback özelliği ve TV launcher filtresi bu varyanttan çıkarıldı. Aynı MediaSession/PlaybackManager kullanılıyor; ikinci player eklenmedi.
  - Doğrulama: `:androidApp:assemblePhoneDebug` başarılı; birleştirilmiş telefon manifestinde Android Auto metadata ve MediaBrowserService var, TV Leanback girdisi yok.
- [x] HUDA-544 — Android TV için yalnız TV yüzeyini paketle
  - Durum: DONE
  - Sonuç: Ayrı `tv` varyantı derleme sabitine göre yalnız `TvExperience()` girişini açıyor; Ads, Firebase, Wear Data Layer, widget/hatırlatma alıcıları ve Android Auto metadata'sı bu varyanttan çıkarılıyor. Cast UI/provider metadata'sı kaldırıldı ancak ortak `AudioPlayer` doğrudan Cast denetleyicisine bağlı olduğu için Cast playback SDK'sı tutuluyor. TV Leanback launcher zorunlu ve TV banner'ı kullanılıyor.
  - Doğrulama: `:androidApp:assembleTvDebug` başarılı; birleşik TV manifestinde Leanback launcher var, Android Auto ve AD_ID bildirimi yok.
- [x] HUDA-545 — Android Automotive OS için medya odaklı ayrı modül ekle
  - Durum: DONE
  - Sonuç: `automotiveApp` ayrı, activity/launcher içermeyen medya uygulaması olarak eklendi. `android.hardware.type.automotive`, `android:appCategory="audio"`, `com.android.automotive` medya tanımı ve ortak MediaBrowserService kullanılıyor. Reklam, Wear Data Layer, Firebase servis SDK'ları ve RevenueCat hariç; Cast UI/provider metadata'sı yok, fakat ortak `AudioPlayer` nedeniyle Cast playback SDK'sı ve yalnızca onun CCT transport bağımlılığının ihtiyaç duyduğu üç küçük encoder/protobuf yardımcı kütüphanesi tutuluyor.
  - Doğrulama: `:automotiveApp:assembleDebug` başarılı; birleşik manifest Automotive OS medya kategorisini gösteriyor ve Android Auto `com.google.android.gms.car.application` metadata'sını içermiyor.
- [x] HUDA-546 — Wear uygulamasında release küçültmesini etkinleştir
  - Durum: DONE
  - Sonuç: Wear release için R8 ve kaynak küçültme açıldı; Wear modülü telefondan ayrı kalıyor.
  - Doğrulama: `:wearApp:assembleDebug` ve `:wearApp:minifyReleaseWithR8` başarılı. Telefon ve TV release R8 görevleri ile `:automotiveApp:minifyReleaseWithR8` da başarılı. R8 Kotlin metadata uyumsuzluğu uyarıları verdi; detay HUDA-550. Kaynak küçültme ve gerçek AAB indirme boyutu CI bundle adımında ölçülecek.
- [ ] HUDA-547 — İmzalı dört cihaz AAB'sini CI'da üret ve ayırt et
  - Durum: IN PROGRESS
  - Sonuç: GitHub Actions release job'ı dört cihaz AAB'sini ve PEM sertifikasını üretecek şekilde çalışıyor; imzalı bundle doğrulaması configuration-cache engeline takılınca Android release görevinde cache kapatıldı. İlk başarılı CI denemesi AAB'leri `95:03:…:F1:7F` Flutter sertifikasıyla üretti; bu parmak izi Play'de kayıtlı `3E:93:…:41:BE` ile eşleşmediğinden Play yüklemesi atlandı. Kullanıcı talebiyle `ANDROID_KEYSTORE_BASE64`, `_mykey/key_store.jks` içeriğiyle güncellendi.
  - Açık kapı: Yeni JKS için mevcut Actions parola/alias değerleri doğrulanmalı; bir sonraki CI imzayı ve doğru PEM'i üretecek. Play upload-key reset isteği kullanıcı tarafından onaylandı, ancak Play Console'daki reset formu henüz gönderilmedi. Reset sonrası beklenen parmak izi güncellenip phone AAB'si Internal test'e yüklenmeli.
- [ ] HUDA-548 — Cihaz testleri ve Play form faktörü yayın kapıları
  - Durum: TODO
  - Kapsam: Telefon/DHU, Android TV, Android Automotive OS ve eşleşmiş Wear emülatör/cihazında ana akışlar; Play Console'da doğru form faktörü kanalları ve eski reddedilmiş sürümler 1/25'in kapsam dışına alınması.
  - Ruflo: Yedi uzman ajan Ruflo swarm'ına kaydedildi; `agent_execute` sağlayıcı API anahtarı/yetkisi yapılandırılmadığı için görevleri çalıştıramadı. Bu turda uygulama ve build düzenlemeleri doğrudan yapıldı.
- [ ] HUDA-549 — Cast runtime'ını telefon yüzeyine ayırarak TV/AAOS paketini küçült
  - Durum: TODO
  - Gerekçe: Ortak Android `AudioPlayer` doğrudan `CastPlaybackController`'a bağlı olduğu için TV ve AAOS'ta Cast UI kapalı olsa da playback SDK'sı taşınıyor. AAOS ayrıca Cast CCT transport'unun istediği üç encoder/protobuf yardımcı kütüphanesini tutuyor.
  - Kabul: Telefonda Cast sender aynı oturum/metadata davranışını korurken TV ve AAOS minified runtime dependency graph'larında Cast Framework, CCT transport ve encoder yardımcıları bulunmuyor; tüm R8 release hedefleri başarılı.
- [ ] HUDA-550 — R8/Kotlin metadata uyumluluk uyarılarını gider
  - Durum: IN PROGRESS
  - Bulgu: Dört release R8 görevi başarıyla tamamlandı ancak R8, Kotlin metadata sürümünü parser desteğinden yeni bulduğuna dair uyarılar verdi. Resmî Android uyumluluk tablosunda Kotlin 2.4 için gerekli R8 9.1.29 olarak listeleniyor. AGP 9.5.0 / Gradle 9.6.0 geçişi denendi; Gradle dağıtımı için ağ DNS'i (`UnknownHostException: services.gradle.org`) ve kullanıcı profilindeki Gradle kilidi erişim izni olmadığından yeni sürüm indirilemedi ve KMP projede uyumluluk doğrulanamadı. Doğrulanmamış araç zinciri değişikliği geri alındı; mevcut AGP 9.0.1 / Gradle 9.1.0 korundu.
  - Kabul: Phone/TV/AAOS/Wear R8 çıktılarında metadata parse uyarısı yok; AAB paketleme ve mevcut Play upload sertifikası korunuyor.

### P0 - REA örnek uygulama incelemesi, ortak ses temeli ve Android Auto

- [ ] HUDA-539 - Bildirim seslerini eski Android'de ve izin kapalıyken güvenilir kıl
  - Durum: REVIEW
  - Sonuç: API 26 öncesinde NotificationCompat için seçili ses/titreşim eklendi. Test bildirimi uygulama/kanal bildirimleri kapalıysa sessizce başarısız olmak yerine ilgili Android bildirim ayarını açıyor. Kaynak ses dosyalarının varlığı/container başlıkları doğrulandı; gerçek cihazda işitilebilirlik bekliyor.

- [ ] HUDA-538 - Seçilen dört referans uygulamayı REA ile kanıta dayalı incele
  - Durum: BLOCKED
  - Kapsam: Quran for Android (Android Auto), Quran for Wear OS, Quran Kareem Radio TV ve Azkar (Cast) için kaynak/version/digest, paket/imza, manifest izinleri/servisleri, DEX sınıfları, playback/resume/download/cast akışları, network host/path/header/auth adayları ve progress saklama biçimini kıyasla. Statik URL adaylarını runtime API kanıtı diye etiketleme; gerekiyorsa kontrollü cihazda REA ağ yakalaması yap. Ayrıntılı rapor: PLATFORM_AUDIO_RESEARCH.md.
  - Engelleyici: REA APK araçları yerel dosya ister. Quran for Android 3.6.5 ve Quran Kareem Radio TV v1.4.1 resmî release APK URL'leri ve digest'leri bulundu, ancak çalışma alanında indirme DNS çözümlemesinde başarısız oldu. Wear ve Azkar için geliştirici APK/release'i bulunamadı; ADB'de bağlı Android/TV cihazı yok. Bu uygulamalar için APK bulgusu yapılmış gibi yazılmayacak.
  - Kabul: Erişilebilir her APK için SHA-256 ile REA çıktısı; APK olmayan örnekler için kaynak/mağaza kanıtı ayrı sınıfta; API ve akış bulguları kanıt kimliği ve sınırlamasıyla belgeli.

- [ ] HUDA-530 - KMP ses kataloğu ve playback durum modelini belirle
  - Durum: IN PROGRESS
  - Kapsam: Sure, ayet aralığı, kıraatçı/riwayet, ses kaynağı/lisans/atıf, indirme durumu ve kaldığı yer için platformdan bağımsız modeller/arayüzler. Mevcut KMP PlaybackManager, Android ExoPlayer ve MediaSessionCompat korunup MediaBrowserService/MediaLibraryService ile Cast remote state aynı playback kaynağına bağlanacak; ikinci player oluşturulmayacak. Android audio URL/CDN ve Quran Foundation API ayrı provider olarak belgelenmeli.
  - Kabul: Uygulama, bildirim, kilit ekranı, Bluetooth ve araç tek oturumun durumunu/komutlarını görür; ağ kaybında uygun indirilmiş ses devam eder; web derlemesi Android sınıflarına bağlanmaz.
- [ ] HUDA-531 - Ses kaldığım yer ve offline indirme akışı
  - Durum: TODO
  - Kapsam: Son sure/ayet, kıraatçı, sıra ve zaman konumunu kalıcı saklama; açık indirme/iptal/silme, depolama kullanımı, hata ve lisansı belli içerik gösterimi. Ayar ve katalog dili Türkçe öncelikli olacak.
  - Kabul: Yeniden açılışta seçilen kıraatçı ve konumdan sürer; uçak modunda indirilmiş sureleri listeler ve oynatır; indirme lisans/URL koşulları kayıtlıdır.
- [ ] HUDA-532 - Android Auto için sürüşe uygun Kur’an medya tarayıcısı
  - Durum: REVIEW
  - İlk uygulama: MediaBrowserServiceCompat tabanlı Android Auto medya kataloğu eklendi; Devam Et, son dinlenen, sureler, kıraatçılar ve arama mevcut PlaybackManager oturumunu kullanıyor. Offline indirme kataloğu yok; fiziksel araç/DHU doğrulaması bekliyor.
  - Kapsam: Media3 MediaLibraryService/MediaSession ile Devam Et, Son Dinlenenler, Sureler, Kıraatçılar ve İndirilenler; oynat/duraklat, ileri/geri ve sesli sistem komutları. Araç ekranında metin okuma/serbest gezinme yok; sürüş kurallarına uygun sınırlı medya UI.
  - Kabul: Android Auto DHU/emülatör ve gerçek araçta servis keşfi, kaldığı yer, ses odağı kesintisi, telefon kilidi, bağlantı kopması ve offline akışları geçer; bağlanınca kendiliğinden ses başlatmaz.

### P1 - Google Cast ve Wear OS

- [ ] HUDA-533 - Google Cast ile telefondan TV/hoparlöre ses aktar
  - Durum: REVIEW
  - İlk uygulama: Cast sender/route seçici ve remote playback state eklendi; HTTP(S) içerik gönderilir, Cast kapanınca yerel ExoPlayer'a döner. Offline dosya Cast'e aktarılmaz; cihaz doğrulaması bekliyor.
  - Kapsam: Android sender entegrasyonu, Cast cihaz seçici, alıcının erişebileceği kararlı ses URL’si, metadata, kuyruk ve sender/receiver durum eşlemesi. Yerel telefon dosyalarının Cast alıcısından doğrudan okunamayacağı varsayımı açık test edilecek; offline dosya için desteklenmeyen aktarım vaat edilmeyecek.
  - Kabul: Telefon ve TV duraklat/ara/sonraki/ses ve bağlantı kopmasında tutarlı; kaynak URL erişilebilir ve izinli; Cast sonlandırılınca telefon oynatımına dönüş anlaşılır.
- [ ] HUDA-534 - Wear OS saatini ortak MediaSession için uzaktan kumanda yap
  - Durum: REVIEW
  - İlk uygulama: Ayrı Wear OS Compose uygulaması ve Data Layer köprüsü eklendi; saatte başlık/kıraatçı/konum, oynat/duraklat, önceki/sonraki ve ±15 sn var. Telefonsuz offline oynatma yok; eşleşmiş saat doğrulaması bekliyor.
  - Kapsam: Companion app, saat/telefon arasındaki Wearable Data Layer mesajları ve mevcut PlaybackManager'a yönlenen oynatma kontrolleri. Sistem MediaSession kontrolleri ve fiziksel cihaz eşleşmesi ayrıca doğrulanmalı.
  - Kabul: Telefon sesi saatten kontrol edilir; Bluetooth kopma/ağ değişiminde durum düzelir; uygulama telefonda açık değilken de oturum medya kontrollerine sunulur.
- [ ] HUDA-535 - Wear OS bağımsız offline kıraatı kapsamlandır
  - Durum: TODO
  - Kapsam: Saat depolaması, Bluetooth kulaklık/saat hoparlörü, batarya ve indirme boyutu fizibilitesi; yalnızca değer doğrulanırsa saat uygulamasında indirme/oynatma.
  - Kabul: Telefon olmadan oynatma, indirme sırasında kesinti, ses çıkışı ve batarya etkisi saat/emülatör matrisinde denenir; destek yoksa kapsam dışı olduğu açık yazılır.

### P2 - Android TV ve cihazlar arası devam

- [ ] HUDA-536 - Android TV/Google TV için kumanda odaklı dinleme ekranı
  - Durum: REVIEW
  - İlk uygulama: Leanback giriş noktası ve D-pad ile sure/kıraatçı seçimi, devam etme ve oynatıcı kontrolleri eklendi. TV cihazı ve farklı ekran oranları doğrulaması bekliyor.
  - Kapsam: TV manifest/dağıtımı, D-pad gezinme/focus, büyük yazı, sure/kıraatçı seçimi, ayet takibi tercihleri ve medya kontrolleri. Android TV deneyimi Cast alıcısından ayrı ürün yolu olarak değerlendirilecek.
  - Kabul: TV kumandasıyla tüm ana akışlar kullanılır; touch gerektirmez; ekran oranları ve uyku/arka plan playback davranışı doğrulanır.
- [ ] HUDA-537 - Okuma ve dinleme konumunu kullanıcı kontrollü cihazlar arasında eşle
  - Durum: TODO
  - Kapsam: Yerel durum tek gerçek kaynak olacak; hesapla senkron opsiyonel, açık rıza/mahremiyet ve çakışma davranışı ayrı tasarlanacak. Cihazlar arası oynatım aktarımı ile sadece kaldığım yeri eşleme birbirinden ayrılacak.
  - Kabul: Saat/telefon/TV son konumu ve kıraatçı tercihini seçilebilir biçimde gösterir; hesap açmadan yerel kullanım bozulmaz; aktarım kullanıcı eylemi olmadan sesi başlatmaz.

Araştırma başlangıçları: [Quran Auto](https://play.google.com/store/apps/details?id=com.quranapp.auto), [Quran Audio](https://play.google.com/store/apps/details?id=com.quranicaudio.app), [Quran Android açık kaynak kodu](https://github.com/quran/quran_android), [Quran Android 3.6.5 APK](https://github.com/quran/quran_android/releases/download/v3.6.5/quran-3.6.5.apk) (resmî proje release’i; SHA-256: f9180b86d519de39ab643d639a1a061c691feda0cadbcb6bca91ee033a77b61c), [Taqwa KMP açık kaynak kodu](https://github.com/MohamedAbulgasem/Taqwa), [Wear OS Quran](https://play.google.com/store/apps/details?id=com.loftymr.quranforwear), [Quran for Android TV](https://play.google.com/store/apps/details?id=qariapps.prayertimetv), [Azkar Cast](https://azkar.cloud24.tech/en/features/chromecast-quran). APK incelemesinde mümkün olduğunda resmî Play dağıtımı veya proje sahibinin GitHub release kaynağı kullanılacak; kaynak ve imza bilgisi kaydedilecek.
TV için APK'lı açık kaynak audio referansı: [Quran Kareem Radio TV v1.4.1](https://github.com/ramysami/quran-kareem-radio-tv/releases/download/v1.4.1/QuranKareemRadio.apk) (SHA-256: 8b72a7f4611892a315a9d762653628525012a797e3d85ac7673e5fb1df1d1a54). Dört platform örneği ve APK/REA sınırları PLATFORM_AUDIO_RESEARCH.md dosyasında açıklanmıştır.

## Platform uygulama ve mağaza yayına hazırlık durumu (2026-11-22)

Bu bölüm kaynak kodundaki mevcut giriş noktalarını, tamamlanmamış cihaz doğrulamasını ve Play Console'da yapılacak işleri birbirinden ayırır. `REVIEW` uygulama kodunun gözden geçirilmesi gerektiğini belirtir; mağaza yayını, test kanalına yükleme veya gerçek cihazda çalıştığı anlamına gelmez. Platform ekran görüntüleri yalnızca ilgili uygulamanın gerçek çalışan ekranından alınmalı; konsept/mockup görseller mağaza ekran görüntüsü olarak kullanılmamalıdır.

| Platform | Depoda görülen uygulama temeli | Bu turdaki doğrulama | Play Console / cihaz işi |
|---|---|---|---|
| Android telefon + Auto | `phone` varyantı telefon UI/widget'larıyla Android Auto `MediaBrowserServiceCompat` kataloğunu sunuyor. | `:androidApp:assemblePhoneDebug` başarılı; birleştirilmiş manifest telefon/Auto için Auto metadata içeriyor, TV Leanback yok. Debug artefaktı Play'e yüklenmemeli. | CI'da aynı Kotlin upload anahtarıyla phone AAB üret; Internal test ve Android Auto/DHU cihaz doğrulaması yap. |
| Android TV | `tv` varyantı `TvHomeScreen`/`TvPlayerScreen`, zorunlu Leanback özelliği ve TV banner'ı içeriyor; Ads/Cast/Firebase/Wear/widget bileşenleri çıkarılıyor. | `:androidApp:assembleTvDebug` başarılı; TV manifesti ayrı ve Android Auto metadata'sız. TV emülatörü/D-pad doğrulaması bekliyor. | TV AAB'sini CI artefaktından al; TV form faktörü track'ine yükle; ekran görüntüleri ve inceleme politikasını tamamla. |
| Android Automotive OS | Ayrı `automotiveApp` medya-only modülü, `android:appCategory="audio"`, `com.android.automotive` ve ortak MediaBrowserService kullanıyor. | `:automotiveApp:assembleDebug` başarılı; AAOS manifestinde Auto metadata ve launcher activity yok. Araç emülatörü/cihaz testi bekliyor. | CI'daki Automotive AAB'yi AAOS dedicated track'e yükle; reddedilmiş code 1/25 artefaktlarını tüm ilgili track'lerde dışla ve politikayı yeniden incelet. |
| Wear OS | Ayrı `wearApp` modülü, `WearPlaybackActivity`, telefondaki Data Layer kontrol servisi ve hızlı erişim Tile'ı var; release R8/kaynak küçültme açık. | `:wearApp:assembleDebug` başarılı. Eşlenmiş saat/emülatör testi ve küçültülmüş release boyutu bekliyor. | Aynı upload anahtarıyla CI Wear AAB'sini üret; Wear OS track'ine yükle; ekran görüntüsü ve inceleme politikasını tamamla. |
| Google Cast / Chromecast | Android Cast route button ve `CastPlaybackController`; remote metadata/durum/komutlar, bağlantı kesilince yerel oynatmaya dönüş ve HTTP(S) URL kontrolü var. | Kotlin derlemesi başarılı; önceki yerel AAB debug anahtarıyla üretildi ve Play'e yüklenmemeli. Cast cihazıyla uçtan uca test bekliyor. | Doğru upload anahtarıyla yeniden paketle; Cast cihazıyla ses URL'si ve atıf doğrulaması yap. |

### Bu turda tamamlanmış sayılmaması gereken işlemler

- Play Console'a ekran görüntüsü, APK veya AAB yükleme yapılmış olarak kaydedilmedi.
- TV, Wear OS veya Automotive OS dağıtım anahtarlarının açıldığı ya da inceleme politikalarının kabul edildiği doğrulanmadı.
- Cihaz/emülatör üzerinde platform akışlarının başarılı olduğu henüz kanıtlanmadı. Derleme başarısı da tek başına cihaz uyumluluğu kanıtı değildir.
- Bu turda `:androidApp:assemblePhoneDebug`, `:androidApp:assembleTvDebug`, `:automotiveApp:assembleDebug` ve `:wearApp:assembleDebug` başarılı oldu. Debug APK boyutları release/Play indirme boyutlarını temsil etmez; gerçek cihaz listesi boş.
- Mağaza görselleri için Huda Quran'ın gerçek uygulama ekranları alınmalı; görsel taslaklar veya başka projeye ait ekranlar kullanılmamalı.

### Sıradaki tamamlanma kapıları

1. Platform kodunu ilgili Gradle hedeflerinde derle; başarısız hedefleri `BLOCKED` olarak ve hata metniyle kaydet.
2. TV, Wear, Android Auto/Automotive ve Cast için kullanılabilir cihaz/emülatör matrisiyle temel akışları doğrula; her gerçek ekran görüntüsünü kaynak ekran ve cihaz bilgisiyle arşivle.
3. Wear ve Automotive için uygun APK/AAB üretip Play Console test kanallarında yayınla; paket kimliği/sürüm ve test track bağlantısını bu panoya yaz.
4. Her platformun mağaza girişindeki görsel/dağıtım/politika adımlarını Play Console'da tamamla ve yalnızca konsoldan doğrulandıktan sonra görevi `DONE` yap.

## Görevler

### P0 — Kullanılabilirlik ve güvenilirlik

- [x] HUDA-522 — Tefsir ekranında derlemeyi durduran eksik kapatma ayracını düzelt
  - Durum: DONE
  - Sonuç: `TafsirScreen` fonksiyonunun kapanış ayracı eklendi; Android, Kotlin/JS, Kotlin/Wasm derlemeleri ve debug APK başarılı.

- [x] HUDA-001 — Vaktelya APK’sını REA ile statik envanterle
  - Durum: DONE
  - Sonuç: Flutter APK; 99 Esma, 43 dua, Türkçe hadis dosyası, sesler, takvim ve kaynak atıfları tespit edildi.
- [x] HUDA-002 — Hafız doğrulamasında ağ hatası nedeniyle katalog daralmasını düzelt
  - Durum: DONE
  - Sonuç: Belirsiz ağ durumu hafızı gizlemiyor; yalnızca kesin HTTP hatası eleniyor.
- [x] HUDA-003 — Kitap modu A−/A+ ve büyük yazı ölçeklemesi
  - Durum: DONE
  - Sonuç: Normal/Büyük/Çok büyük; ayet, meal ve satır aralığı birlikte ölçekleniyor.
- [ ] HUDA-004 — Kitap modunda iki okuma görünümü
  - Durum: REVIEW
  - Sonuç: Ortak KMP render akışında “Mushaf” (Arapça ağırlıklı) ve “Okuma” (her ayet ile mealini birlikte tutan) görünümleri ayrıldı; mevcut çeviri ayarı bu iki görünümle senkron tutuluyor.
  - Doğrulama: `:shared:compileAndroidMain` ve `:shared:compileKotlinJs` başarılı; gerçek ekran kontrolü bekliyor.
- [ ] HUDA-005 — Kitap modu küçük ekran fit-to-content testi
  - Durum: REVIEW
  - Sonuç: 320dp ve 360dp için ekran genişliğine bağlı yatay/dikey padding, kaydırılabilir içerik, minimum 48dp dokunma alanları ve yaşlı yazı ölçeklemesi uygulandı. Mushaf görünümünde A+/Çok büyük seçimi artık sayfayı sığdırmak için sessizce küçültülmüyor; normal profil fit edilirken büyük profil kaydırılabilir okunabilirlik önceliğiyle korunuyor.
  - Doğrulama: Android/JS derlemesi başarılı; 320/360dp gerçek cihaz ve web render kontrolü bekliyor.
- [ ] HUDA-006 — Hadis “Tümü” akışını sayfalı ve hızlı hale getir
  - Durum: REVIEW
  - Sonuç: “Tümü” artık önce HadeethEnc’in kategori parametresiz sayfalı yanıtını kullanıyor; sağlayıcı bunu boş döndürürse tüm kategoriler süre sınırı içinde paralel yedek olarak taranıyor. Ekran ve yedek akışının aynı kategorileri iki kez istememesi için repository içinde kilitli kategori önbelleği eklendi. Devam sayfalaması, retry/empty/error durumları ve eski isteğin state’i ezmesini engelleyen request generation korunuyor. Ağ/timeout durumunda cache veya yerel içerik yoksa artık boş liste yerine açık yükleme hatası üretiliyor.
  - Doğrulama: Android host testleri, Android/JS/Wasm ortak derlemeleri ve debug APK başarılı; gerçek hadis ekranı ve ağ senaryosu testi bekliyor.

- [ ] HUDA-515 — Web ana sayfasını HTML/CSS/JavaScript okuma masasına dönüştür
  - Durum: REVIEW
  - Sonuç: Kök `/` artık KMP canvas yerine modern, responsive bir HTML/CSS/JS karşılama deneyimi sunuyor: etkileşimli okuma masası, meal görünürlüğü, büyük yazı modu, 114 surelik atlas, kütüphane rotaları, mobil menü ve bağlantı paylaşımı eklendi. `/mushaf`, `/hadith`, `/?app=1` ve diğer uygulama rotaları KMP/Compose olarak korunuyor.
  - Doğrulama: `:webApp:jsBrowserDevelopmentWebpack` başarılı; yerel tarayıcıda ana sayfa DOM ve etkileşimli kontroller doğrulandı. Masaüstü ve gerçek telefon ekranı matrisi bekliyor.
- [ ] HUDA-516 — AdSense için content-first web ve mobil uygulama vitrini
  - Durum: REVIEW
  - Sonuç: Ana HTML’e AdSense site meta/script bağlantısı, kaynak rotaları için JS’siz HTML fallback, gizlilik/şartlar bağlantıları ve Google Play indirme vitrini eklendi. `/mushaf`, `/hadith`, `/tafsir`, `/dua`, `/prayer` ve `/reciters` için bağımsız içerik sayfaları ile Firebase exact rewrites ve sitemap kayıtları eklendi. Compose uygulaması yalnızca interaktif `?app=1` yolunda çalışıyor; `ads.txt` publisher kaydı korunuyor.
  - Doğrulama: `:webApp:jsBrowserDevelopmentWebpack`, `:webApp:wasmJsBrowserProductionWebpack` ve `:webApp:wasmJsBrowserDistribution` başarılı; production deploy klasöründe tüm statik sayfalar, `content.css`, `index.html` ve `ads.txt` bulundu. Canlı Firebase crawler kontrolü, gerçek reklam gösterimi, site “Ready” onayı ve CMP seçimi hesap sahibinin AdSense panelinden yapılmalı.
- [ ] HUDA-517 — Web uygulama bağlantılarında hash deep-link kırılmasını düzelt
  - Durum: REVIEW
  - Sonuç: Eski `/#/ekran` bağlantıları tarayıcıda hash olarak kalıp Compose tarafına ulaşmıyordu. Web giriş noktası artık `?app=1&screen=...` rotasını doğrudan DeepLink ile açıyor; mevcut eski bağlantılar da tek bir JS uyumluluk katmanıyla yeni formata dönüştürülüyor. `duas` → `dua`, `tafsir` → ayarlar ve ekranı olmayan genel bağlantılar güvenli uygulama girişine yönleniyor.
  - Doğrulama: DeepLink testlerine dua/hadis ve eski alias rotaları eklendi; `:shared:testAndroidHostTest`, `:webApp:jsBrowserDevelopmentWebpack`, `:webApp:wasmJsBrowserDistribution` ve `:androidApp:assembleDebug` başarılı. Statik production kaynaklarında rota sayfaları ve `web.js` mevcut; yerel tarayıcıda `/?app=1#/hadith` Compose uygulama kabını açtı.
  - 2026-11-21 canlı kontrol: `/?app=1#/hadith` eski bağlantısı Compose Hadisler ekranına yönlendi; `/?app=1&screen=hadith` doğrudan bağlantısı da aynı ekranı açtı. Hadis API'si erişilemediğinde açık hata ve "Tekrar dene" eylemi gösterildi.
- [x] HUDA-518 — Android emülatör kritik akış smoke testi
  - Durum: DONE
  - Sonuç: Güncel debug APK test emülatörüne kuruldu. Bildirim izni ekranı, onboarding geçişi ve ana ekran açılışı doğrulandı. Hadisler ekranında ağ kapalı senaryosu sonsuz loading yerine açık hata ve “Tekrar dene” aksiyonu gösterdi; çökme görülmedi.
  - Doğrulama: `adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`, UI hierarchy dump ve logcat kontrolü başarılı. Fiziksel telefon, launcher widget boyutları ve sensör testi ayrı HUDA-502/503 kapsamındadır.
- [ ] HUDA-519 — Web sahipliği, sitemap, robots ve App Links yayın kontrolü
  - Durum: REVIEW
  - Sonuç: `sitemap.xml` artık yalnızca taranabilir HTML içerik sayfalarını, gizlilik/şartlar sayfalarını ve ana rotayı içeriyor; dinamik Compose ekranları sitemap’ten çıkarıldı. `robots.txt`, AdSense doğrulama HTML’i, `ads.txt`, `app-ads.txt`, Android `assetlinks.json`, manifest `autoVerify` ve Firebase MIME header’ları production kaynaklarında mevcut. Search Console/AdSense panelinde izlenecek adımlar `WEB_SEO_SETUP.md` içine yazıldı. iOS AASA’daki sahte `TEAMID` kaldırıldı; gerçek Apple Team ID verilene kadar boş detay listesi yayımlanıyor.
  - Doğrulama: `:webApp:wasmJsBrowserDistribution`, `:shared:testAndroidHostTest`, `:androidApp:assembleDebug` ve `:androidApp:signingReport` başarılı. Production dağıtım klasöründe tüm SEO/ownership dosyaları mevcut; sitemap XML olarak parse edildi, 9 taranabilir URL içeriyor ve dinamik Compose rotası içermiyor. Debug SHA-256 parmak izi `assetlinks.json` içinde doğrulandı. Canlı Firebase deploy ve Search Console mülk doğrulaması hesap sahibinin panelinden yapılmalı.

### P1 — İçerik ve offline deneyimi

- [ ] HUDA-101 — HadeethEnc Türkçe hadislerini lisans/atıf kontrollü offline katalog yap
  - Durum: REVIEW
  - Sonuç: HadithRepository artık 8 saniye zaman aşımı, son başarılı liste/detail cache’i ve HadeethEnc attribution rozeti kullanıyor; hadis listesindeki kaynak metni de repository atfından geliyor. 5,7 MB’lık APK snapshot’ı lisans/boyut doğrulaması yapılmadan gömülmedi; tam offline katalog için lisanslı dağıtım kararı bekliyor.
  - Doğrulama: DTO kaynak/atıf ve açıklama ayrımı testleri ile ortak Android host testleri başarılı; tam offline katalog lisans kararı ve gerçek ekran testi bekliyor.
- [ ] HUDA-102 — Dua kataloğunu genişlet, kaynak ve kategori bilgisi ekle
  - Durum: REVIEW
  - Sonuç: DuaContentRepository’nin kategori, arama ve kaynak rozeti artık Dua ekranında kullanılıyor. Kur’an’dan kaynaklı 8 yaygın dua daha eklenerek katalog 21 kayda çıkarıldı; ilim, aile, rızık, sıkıntı, şifa ve af kategorileri genişletildi. Yeni uzak dua kaynağı doğrulanmadan otomatik birleştirilmiyor.
  - Doğrulama: Kategori/arama/kaynak davranışı ve katalog alt sınırı ortak testlerle doğrulandı; Android host testleri ile JS/Wasm derlemeleri başarılı. Gerçek ekran/favori akışı testi bekliyor.
- [x] HUDA-103 — Dua/Esma/Hadis favorilerini tek Favoriler merkezinde düzenle
  - Durum: DONE
  - Sonuç: AppPreferences’te dua, Esma ve hadis favorileri ayrı anahtarlarla tutuluyor; Favoriler ekranı artık Ayet/Hadis/Dua/Esma sekmelerinin dördünde ayrı sayaç, liste ve çıkarma aksiyonu sunuyor. Ortak içerik katmanı bu ayrımı koruyor.
  - Doğrulama: Dört favori namespace’inin birbirine karışmadığını doğrulayan ortak test ve 61 Android host testi başarılı; gerçek ekran testi bekliyor.
- [ ] HUDA-104 — Türkçe öncelikli meal ve tefsir kataloglarını dil filtreleriyle göster
  - Durum: REVIEW
  - Sonuç: Translation/Tafsir modellerine kaynak bilgisi eklendi; QuranRepository Türkçe öncelikli sıralama, `languageCode` filtresi ve katalog cache’i sağlıyor.
- [ ] HUDA-105 — Almanca, Fransızca, İspanyolca ve İngilizce tefsir/meal kaynaklarını doğrula
  - Durum: REVIEW
  - Sonuç: Sahte fallback dilleri kaldırıldı; yalnızca API veya doğrulanmış cache kaynaklı gerçek diller gösteriliyor. Türkçe önceliği, provider/yazar/lisans metadata’sı ve lisans bilinmiyorsa açık uyarı eklendi. Ortak Android/JS derlemesi başarılı; gerçek API ekran kontrolü bekliyor.
- [ ] HUDA-106 — İçerik kaynak/author/licence rozetleri ve kaynak detay ekranı
  - Durum: REVIEW
  - Sonuç: Edition picker ve tefsir ekranında sağlayıcı/yazar/lisans/kaynak ayrıntıları gösteriliyor; kaynakta lisans yoksa uydurma bilgi yerine açıkça belirtiliyor. Ortak Android/JS derlemesi başarılı; gerçek ekran kontrolü bekliyor.
- [ ] HUDA-107 — Offline içerik indirme ve indirme durumu ekranı
  - Durum: REVIEW
  - Sonuç: Android/web ortak offline ekranı, ilerleme, duraklat/devam et, hata/yeniden dene ve tamamlanan içerikleri koruyan devam mantığı eklendi. Sesler byte olarak indirilmiş gösterilmiyor; Android/JS derlemesi başarılı, ekran testi bekliyor.

### P1 — Paylaşım ve görsel kimlik

- [ ] HUDA-201 — Paylaşım kartı şablon sistemi
  - Durum: REVIEW
  - Sonuç: Ortak KMP kart sistemi; kağıt, gece, adaçayı ve yüksek kontrast temaları Ayah/Hadith/Esma akışlarına bağlandı. Hadis kartında Arapça, Türkçe anlam ve açıklama başlıkları görsel olarak ayrı tutuluyor.
- [ ] HUDA-202 — Kart kişiselleştirme paneli
  - Durum: REVIEW
  - Sonuç: Tema ve çıktı biçimi seçimleri korunuyor; metin paylaşımı artık web bağlantısının yanında `hudaquran:/hadith/...` uygulama deep link’ini de açık etiketle taşıyor.
- [ ] HUDA-203 — Hadis paylaşımında açıklamayı ayrı blok/slayt yap
  - Durum: REVIEW
  - Sonuç: Arapça, Türkçe anlam ve açıklama ayrı başlıklı bloklarda; açıklama kelime sınırında ve ölçülmüş metin stiliyle sığdırılıyor. Metin çıktısında da bölümler birbirine yapışmıyor.
- [ ] HUDA-204 — PNG/JPEG render test matrisi
  - Durum: REVIEW
  - Sonuç: 1080×1350 ve 1080×1080 ölçüleri bağlandı; ortak ölçü/kısaltma ve küçük bütçe testleri eklendi. Hadis ölçümünde gerçek Arapça fontu kullanılıyor, kaynak/marka altbilgisi taşmaya karşı iki satır ve ellipsis ile sınırlandı. JPEG encoder ve gerçek bitmap render matrisi hâlâ açık.
- [x] HUDA-205 — Esmaül Hüsna metin/görsel paylaşım ve favori
  - Durum: DONE
  - Sonuç: Ayrı paylaşım sheet’i, PNG ve metin bağlantısı eklendi.

### P1 — Widget ailesi

- [x] HUDA-301 — Kompakt saat/tarih widget’ı
  - Durum: DONE
- [ ] HUDA-302 — Kompakt sıradaki namaz + kalan süre widget’ı
  - Durum: REVIEW
  - Sonuç: Ağ beklemeden yalnızca `PRAYER_WIDGET` türündeki gerçek vakti seçiyor; geri sayım her dakika veya bir sonraki vakit sınırında güvenli AlarmManager yenilemesiyle tazeleniyor. Exact alarm izni yoksa pencere tabanlı fallback kullanılıyor ve widget kaldırılınca alarm iptal ediliyor. Cihaz/launcher testi bekliyor.
- [ ] HUDA-303 — Günün ayeti/hadisi widget’ı
  - Durum: REVIEW
  - Sonuç: Tek widget örneği ayet, dua veya hadis olarak seçilebiliyor; Arapça/çeviri, kaynak atfı ve beş renk paleti örnek bazında ayarlanıyor. Doğrulanmış kaynağı olmayan içerik gösterilmiyor. Cihaz/launcher testi bekliyor.
- [x] HUDA-304 — Kaldığın yerden devam et widget’ı revizyonu
  - Durum: DONE
- [ ] HUDA-305 — Zikir sayacı widget’ı
  - Durum: REVIEW
  - Sonuç: Bağımsız sayaç, +1, sıfırlama ve uygulama deep link’i korunuyor; yalnızca gerçekten yerleştirilmiş widget ID’leri kabul ediliyor ve sayaç taşması 99.999 ile sınırlandırılıyor. Launcher boyut testi bekliyor.
- [ ] HUDA-306 — Hicri takvim/Ramazan widget’ı
  - Durum: REVIEW
  - Sonuç: Hicri gün/ay/yıl ve Ramazan günü/kalan gün görünümü eklendi; Android Kotlin derlemesi ve tam APK başarılı, launcher boyut testi bekliyor.
- [ ] HUDA-307 — MediaSession tabanlı ses oynatıcı widget’ı
  - Durum: REVIEW
  - Sonuç: Gerçek PlaybackManager durumuna bağlı play/pause/stop/next/previous callback’leri, kuyruk güncellemesi ve aktif ayet deep link’i korunuyor. MediaButtonReceiver artık `MEDIA_BUTTON` servis eylemine bağlanıyor; sistemin donanım/kilit ekranı komutları servise ulaşabiliyor. Servis kapanırken MediaSession durdurulup stale lock-screen state temizleniyor; MediaStyle kompakt kontrol listesi üç aksiyonla sınırlı. Gerçek kilit ekranı testi bekliyor.
- [ ] HUDA-308 — Widget boyut/launcher uyumluluk testi
  - Durum: REVIEW
  - Sonuç: 7 widget sağlayıcısında yapılandırma, beş palet, yazı boyutu/yerleşim ayarı ve yeniden boyutlandırma yenilemesi var. Namaz geri sayımı yalnızca desteklenen şehir saat diliminde gösteriliyor. Kotlin kaynak derlemesi başarılı; doğru Kotlin upload anahtarıyla signed AAB ve gerçek launcher matrisi bekliyor.
- [ ] HUDA-520 — Namaz widget’ında gerçek vakit kaynağını ayır
  - Durum: REVIEW
  - Sonuç: Bildirim planındaki önceden hatırlatma kayıtları widget verisinden ayrıldı. Widget yalnızca yaklaşan gerçek namaz kaydını okuyor; widget kayıtları Android/iOS bildirim scheduler’ına alarm olarak kurulmadı. ReminderPlanner testleri widget kaydının `scheduleNotification=false` olduğunu doğruluyor; launcher üzerinde gerçek yerleştirme testi HUDA-308 kapsamında bekliyor.

### P2 — Günlük kullanım ve farklılaşma

- [ ] HUDA-401 — “Bugün” ana ekranı
  - Durum: REVIEW
  - Sonuç: Namaz özeti, günün ayeti/hadisi/duası, kaldığın yerden devam ve hatim hedefi kartları eklendi; dar mobilde tek sütun, geniş web’de iki sütun düzeni var. Android/JS derlemesi başarılı; ekran testi bekliyor.
- [ ] HUDA-402 — Namaz kıldım/kaza/haftalık hedef takibi
  - Durum: REVIEW
  - Sonuç: Kalıcı ibadet takip ekranı, günlük durumlar ve haftalık hedef görünümü eklendi; Android/JS derlemesi başarılı, ekran testi bekliyor.
- [ ] HUDA-403 — Hatim yolculuğu ve okuma serisi
  - Durum: REVIEW
  - Sonuç: Mevcut hatim/okuma ilerlemesiyle bağlı hatim yolculuğu ekranı eklendi; Android/JS derlemesi başarılı, ekran testi bekliyor.
- [ ] HUDA-404 — Aralıklı tekrar ile sure/ayet ezber modu
  - Durum: REVIEW
  - Sonuç: Günlük ezber kartı, tekrar sayısı ve tamamlanma aksiyonu eklendi; Android/JS derlemesi başarılı, ekran testi bekliyor.
- [ ] HUDA-405 — Yaşlı modu profili
  - Durum: REVIEW
  - Sonuç: Açılıp kapanabilen büyük yazı, yüksek kontrast, geniş dokunma alanı ve azaltılmış hareket profili eklendi; mevcut A-/A+ korunuyor. Android/JS derlemesi başarılı, gerçek ekran testi bekliyor.
- [ ] HUDA-406 — Çocuk/öğrenci modu
  - Durum: REVIEW
  - Sonuç: Gerçek repository verilerini kullanan Kevser, İhlâs, Felak ve Nâs kartları; 3 kartlık hedef, yıldız ilerlemesi ve boş durum eklendi. Android/JS derlemesi başarılı, ekran testi bekliyor.
- [ ] HUDA-407 — Seferî konum ve yolculuk modu
  - Durum: REVIEW
  - Sonuç: Kalıcı ve açık/kapatılabilir seferî mod anahtarı ile namaz vakitleri alanında açıklama eklendi; konum/API hesabı değiştirilmedi. Android/JS derlemesi başarılı, ekran testi bekliyor.
- [ ] HUDA-408 — Namaz sonrası zikir/tesbih akışı
  - Durum: REVIEW
  - Sonuç: Üç aşamalı ortak KMP zikir akışı, büyük sayaç, sıfırlama/tamamlanma durumu ve `hudaquran:/dhikr` deep link’i eklendi. Ortak Android/JS derlemesi başarılı; ekran/erişilebilirlik testi bekliyor.
- [ ] HUDA-409 — Kıble doğruluk kalibrasyonu ve güven göstergesi
  - Durum: REVIEW
  - Sonuç: Sensör yokluğu/düşük doğruluk/normal durumları ayrıştırıldı; düşük doğrulukta yön gizleniyor ve 8 çizerek kalibrasyon rehberi gösteriliyor. Android/JS derlemesi başarılı; cihaz sensör testi bekliyor.

## Agent iş dağılımı

| Agent | Görev kümesi | Yazma alanı | Durum |
|---|---|---|---|
| Agent A | Kitap modu + yaşlı modu | `shared/.../ui/mushaf`, ilgili testler | REVIEW |
| Agent B | Paylaşım kartları | `shared/.../ui/share`, Esma/Hadis paylaşım ekranları | REVIEW |
| Agent C | Widget ailesi | `androidApp/.../widget`, widget layout/xml/manifest | REVIEW |
| Agent D | İçerik/offline hadis-dua-meal | `shared/.../data`, içerik modelleri ve repository’ler | REVIEW |

## Aktif agent turu — 2026-11-11

| Agent | Kapsam | Yazma alanı | Kabul kriteri |
|---|---|---|---|
| Feynman | HUDA-006 hadis ilk açılış, pagination, retry/empty/error ve erişilebilirlik | `shared/.../ui/hadith`, ilgili hadis testleri | “Tümü” ilk açılışta takılmayacak; eski istek yeni state’i ezmeyecek; test/derleme geçecek |
| Mencius | HUDA-201–204 paylaşım kartı okunabilirliği ve ölçüm testleri | `shared/.../ui/share`, ilgili paylaşım testleri | Arapça/anlam/açıklama/deep-link blokları taşmayacak; küçük bütçe testleri geçecek |
| Laplace | HUDA-104–106 meal/tefsir seçim ekranı ve kaynak şeffaflığı | `shared/.../ui/settings`, ilgili remote/test alanları | Türkçe önceliği korunacak; ağ hatası, boş durum, dil filtresi ve lisans bilgisi açık olacak |

Tur sonucu: Laplace denetimi tamamlandı ve bulguları HUDA-508’e işlendi. Feynman ve Mencius agent kotası nedeniyle kod üretemeden durdu; hadis ve paylaşım REVIEW işleri açık kalıyor.

Her agent dönüşünden sonra değişiklikler ana derleme kapısında yeniden doğrulanır; yalnızca ilgili test/derleme başarılıysa görev `DONE` veya `REVIEW` olarak güncellenir.

## Değişiklik günlüğü

- 2026-11-10 — Pano oluşturuldu; Vaktelya/REA bulguları görevlere ayrıldı.
- 2026-11-10 — İlk paralel agent görevleri başlatıldı.
- 2026-11-10 — Agent D: Hadith timeout/cache, Türkçe öncelikli meal/tefsir katalog cache’i, dil filtreleri ve kaynak rozetleri eklendi; büyük HadeethEnc dump’ı lisans/boyut riski nedeniyle gömülmedi.
- 2026-11-10 — Agent C, HUDA-302 ve HUDA-303 kodlarını tamamladı; derleme kilidi nedeniyle REVIEW’a alındı.
- 2026-11-10 — Agent B, HUDA-201–204 kapsamında ortak paylaşım kartı seçenekleri, yüksek kontrast temaları, ölçümlü metin yerleşimi ve sosyal medya çıktı boyutlarını ekledi; `git diff --check` temiz. Derleme, Kotlin daemon/OneDrive önbellek kilidi nedeniyle REVIEW’da.
- 2026-11-10 — Temiz Kotlin önbelleği ve daemon sonrası Android + Kotlin/JS derlemesi, debug APK paketlemesi başarılı; UI/device doğrulamaları bekliyor.
- 2026-11-10 — İkinci tur agent’lar başlatıldı: hadis “Tümü” akışı, zikir/Hicri widget’lar ve dil/kaynak doğrulaması.
- 2026-11-10 — İkinci turda HUDA-006, HUDA-105 ve HUDA-106 kodları REVIEW’a alındı; temiz derleme sonrası kapanış yapılacak.
- 2026-11-10 — Zikir ve Hicri/Ramazan widget’ları eklendi; `:androidApp:compileDebugKotlin` başarılı, launcher testi bekliyor.
- 2026-11-10 — İkinci tur değişiklikleriyle ortak Android + Kotlin/JS + debug APK derlemesi tekrar başarılı oldu.
- 2026-11-10 — JS test görevi ChromeHeadless bulunamadığı için çalışmadı; bu makinede browser runtime doğrulaması eksik.
- 2026-11-10 — Üçüncü turda HUDA-401, HUDA-405 ve HUDA-409 kodları REVIEW’a alındı; temiz derleme ve gerçek ekran/sensör testi bekliyor.
- 2026-11-10 — Üçüncü turdaki birleşim hataları düzeltildi (`Dua?`, `Modifier.size`, `largeTouchTargets`); Android + Kotlin/JS + debug APK derlemesi yeniden başarılı oldu.
- 2026-11-10 — HUDA-408 zikir akışı REVIEW’a alındı; kod derlemesi geçti, ekran testi bekliyor.
- 2026-11-10 — HUDA-402/403/404, HUDA-406 ve HUDA-407 kodları REVIEW’a alındı; ortak derleme ve ekran testleri bekliyor.
- 2026-11-11 — DeepLink exhaustive route düzeltildi; yeni ibadet/hatim/ezber rotaları eklendi ve güncel Android + Kotlin/JS + debug APK derlemesi başarılı oldu.
- 2026-11-11 — HUDA-107, HUDA-307 ve HUDA-308 kodları REVIEW’a alındı; tam derleme ve cihaz/launcher doğrulaması bekliyor.
- 2026-11-11 — HUDA-107/307/308 sonrası Android + Kotlin/JS + debug APK derlemesi başarılı oldu; cihaz/launcher doğrulaması eksik.
- 2026-11-11 — HUDA-302–308 Android denetimi: tüm widget/bildirim PendingIntent’leri `FLAG_IMMUTABLE` ile doğrulandı; MediaStyle dört kompakt aksiyon hatası düzeltildi; MediaSession hız/transport metadata davranışı sağlamlaştırıldı; seçili namaz bildirim sesi yoksa `huda_chime_soft`, o da yoksa açık sessiz fallback eklendi. Yeni ses dosyası eklenmedi.
- 2026-11-11 — Hadis/Dua/paylaşım REVIEW turu: “Tümü” ağ hatası artık boş durumla karışmıyor; Dua ekranı repository arama/kategori/kaynak akışını kullanıyor; Favoriler’e ayrı Dua sekmesi eklendi; hadis paylaşım metninde Arapça/anlam/açıklama bölümleri ve web + uygulama bağlantıları ayrıştırıldı; ortak DTO, repository, favori ve kart biçim testleri eklendi. Derleme, kapsam dışı `Strings.kt` sözdizimi hatası nedeniyle tekrar doğrulanamadı.
- 2026-11-11 — Bu turdaki ilk derleme notu, sonradan doğrulanan `Strings` method-signature sınırı ve Hadith DTO test import eksikliği nedeniyle güncellendi.
- 2026-11-11 — Wasm ses Promise `catch` dönüş tipleri düzeltildi ve `:shared:compileKotlinWasmJs` başarılı oldu.
- 2026-11-11 — `Strings` sınırı kalıcı olarak giderildi; Hadith DTO testine eksik `toDomain` import’u eklendi; `ReminderPlanner` testleri global Android context’e bağımlı olmaktan çıkarıldı. Temiz `:shared:testAndroidHostTest --rerun-tasks --no-build-cache` başarılı: 61 test.
- 2026-11-11 — Son ortak doğrulama başarılı: `:shared:compileAndroidMain`, `:shared:compileKotlinJs`, `:shared:compileKotlinWasmJs` ve `:androidApp:assembleDebug`; güncel APK yeniden üretildi. `git diff --check` temiz. ADB daemon çalışıyor ancak cihaz listesi hâlâ boş.
- 2026-11-11 — Laplace denetimi sonrası HUDA-508 ve HUDA-509 yerelde tamamlandı. İlk doğrulamada paylaşım katmanında iki derleme regresyonu ve bir hadis test varsayımı yakalandı; düzeltildi. Son doğrulama: Android/JS/Wasm derlemeleri, debug APK ve Android host testleri başarılı.
- 2026-11-11 — Web geliştirme sunucusu (`http://localhost:8080/`) canlı doğrulandı; ana sayfa ve SEO/route bağlantıları erişilebilir. Hadis/paylaşım ekranlarının derinlemesine taraması ve gerçek Android cihaz doğrulaması hâlâ açık.
- 2026-11-11 — ADB yeniden kontrol edildi; cihaz listesi hâlâ boş. Bu nedenle HUDA-502/503 gerçek cihaz ve launcher doğrulaması olarak açık tutuldu; APK üretimi ve ortak derlemeler tamam.

- 2026-11-11 — HUDA-510 testleri eklendi: provider dil normalization ve Edition picker filtre davranışı. Son doğrulama `BUILD SUCCESSFUL`; Android/JS/Wasm, debug APK ve Android host testleri geçti.
- 2026-11-11 — HUDA-511 tamamlandı: stuck audio watchdog artık hata sinyali veriyor; kıraatçı preview dokunma alanları büyük profil ile uyumlu. Ortak doğrulama `BUILD SUCCESSFUL`.

- 2026-11-11 — HUDA-511 tamamlandı: stuck audio watchdog artık hata sinyali veriyor; kıraatçı preview dokunma alanları büyük profil ile uyumlu. Son doğrulama: host testleri + debug APK `BUILD SUCCESSFUL`.
- 2026-11-11 — HUDA-302/303/305/307/520 güvenlik ve davranış turu: namaz widget’ına dakika sınırında AlarmManager yenilemesi ve zaman değişimi yayınları, günlük içerik cache/fallback kataloğu, zikir widget ID doğrulaması/taşma sınırı, MediaButtonReceiver servis yönlendirmesi ve kapanışta MediaSession temizliği eklendi. Manifest/kaynak doğrulaması başarılı. Android birim/host test ve APK derlemesi, kapsam dışı mevcut `StudentModeScreen.kt`/`PostPrayerDhikrScreen.kt` derleme hataları nedeniyle bu turda tamamlanamadı; bu dosyalara dokunulmadı.

- 2026-11-11 — HUDA-512 tamamlandı: kıble yönü için güvenli koordinat doğrulaması ve malformed koordinat testleri eklendi. Son ortak doğrulama `BUILD SUCCESSFUL`.

- 2026-11-11 — HUDA-513 tamamlandı: offline indirme final durumunda duraklatma isteği korunuyor; durum önceliği testleri geçti. Son ortak doğrulama `BUILD SUCCESSFUL`.
- 2026-11-11 — HUDA-514 bildirim tercihi regresyon testi eklendi ve `:shared:testAndroidHostTest` başarılı.
- 2026-11-11 — HUDA-006 iyileştirildi: hadis “Tümü” akışı önce doğrudan sayfalı endpoint’i deniyor, boş yanıtta tüm kategorileri paralel yedekliyor. Android/JS/Wasm, host testleri ve debug APK başarılı.
- 2026-11-11 — HUDA-006 optimizasyonu: kategori listesi aynı repository içinde kilitli önbellekle tekilleştirildi; ortak derlemeler, host testleri ve debug APK yeniden başarılı.
- 2026-11-11 — HUDA-005 kitap modu erişilebilirlik düzeltmesi: A+/Çok büyük Mushaf yazısı artık fit hesabı tarafından geri küçültülmüyor; kaydırmalı görünüm korunuyor ve üç regresyon testi eklendi. Host testleri başarılı.

## Son tur notu

- 2026-11-21 — Önceki turdan kalan `TafsirScreen.kt` sözdizimi hatası düzeltildi; Android/JS/Wasm derlemeleri, `:androidApp:assembleDebug` ve `:shared:testAndroidHostTest` başarılı. `git diff --check` temiz (yalnızca `.gitignore` satır sonu uyarısı). Vaktelya takvim JSON tarihleri resmi Diyanet 2026/2027 listeleriyle karşılaştırıldı; tarihler eşleşiyor. ADB hizmeti açıldı, ancak bağlı Android cihaz bulunmadı; gerçek cihaz/launcher işleri hâlâ BLOCKED.

- 2026-11-11 — HUDA-515 web dönüşümü: Flutter varsayımı yerine mevcut Kotlin/Wasm Compose mimarisi korunarak kök web deneyimi saf HTML/CSS/JavaScript okuma masasına ayrıldı. True-black-flat yönü, sıcak amber vurgu, Newsreader/Manrope/Amiri tipografisi, 114 surelik atlas, meal/büyük yazı etkileşimi ve reduced-motion desteği eklendi. Webpack derlemesi başarılı; ekran matrisi REVIEW.
- 2026-11-11 — HUDA-102 içerik turu: Kur’an’dan kaynaklı 8 yaygın dua eklendi; katalog 21 kayda ve yeni ilim/rızık/aile/şifa kategorilerine genişletildi. Host testleri ve JS/Wasm derlemeleri başarılı.
- 2026-11-11 — HUDA-516 AdSense hazırlığı: HTML başına publisher meta/script, uygulama rotalarında JS’siz içerik fallback’i, gizlilik/şartlar navigasyonu ve Google Play indirme çağrısı eklendi. AdSense’in resmi kriterleriyle karşılaştırma yapıldı; canlı onay/CMP adımı bekliyor.
- 2026-11-11 — HUDA-516 genişletildi: Mushaf, hadis, tefsir, dua, namaz vakitleri ve kıraat için bağımsız HTML/CSS içerik sayfaları; Firebase exact rewrite ve sitemap kayıtları eklendi. Ana sayfa navigasyonu bu crawl edilebilir rotalara bağlandı. JS development, Wasm production webpack ve Wasm distribution doğrulamaları başarılı.
- 2026-11-11 — HUDA-517: Kök sayfadaki eski `/#/...` uygulama bağlantıları `?app=1&screen=...` akışına taşındı; Compose deep-link yönlendirmesi ve dua/hadis rota testleri eklendi.
- 2026-11-11 — HUDA-518: Güncel APK test emülatörüne kuruldu; bildirim izni, onboarding, ana ekran ve offline hadis hata/yeniden deneme akışı UI hierarchy/logcat ile doğrulandı. Fiziksel cihaz ve launcher matrisi açık bırakıldı.
- 2026-11-11 — HUDA-519: Search Console sitemap gönderim akışı ve AdSense/Firebase yayın kontrol listesi `WEB_SEO_SETUP.md` olarak eklendi. Robots, doğrulama HTML’i, ads.txt/app-ads.txt ve App Links dosyaları denetlendi; gerçek Apple Team ID bulunmadığı için AASA’daki sahte `TEAMID` kaldırıldı.
- 2026-11-11 — HUDA-520: Namaz widget’ı gerçek vakit kayıtlarını bildirim öncesi hatırlatmalardan ayıracak şekilde düzeltildi; Android `compileDebugKotlin` başarılı.
- 2026-11-11 — HUDA-521: Mevcut web sürümü `:webApp:wasmJsBrowserDistribution` ile production derlenip Firebase Hosting `hudaquran.web.app` adresine yayınlandı. Ana sayfa, `/mushaf`, `sitemap.xml` ve `robots.txt` canlıda HTTP 200 ile doğrulandı.
- 2026-11-11 — Paralel kalan işler turu: widget/ses ajanı gerçek vakit seçimi, günlük içerik fallback’i, widget sayaç güvenliği ve stale MediaSession temizliğini tamamladı; kitap/günlük deneyim ajanı Mushaf dar ekran erişilebilirliği, “Bugün”, ibadet takibi, hatim/ezber/seferî/zikir akışlarını güçlendirdi; hadis/paylaşım ajanı akış, metadata ve kart ayrıştırmalarını tamamladı. Android host testleri (98 test) ve debug APK başarılı.
- 2026-11-11 — Paylaşım kartı dosya adı güvenliği düzeltildi: yol içeren içerik kimliği ilk güvenli segmente indirgeniyor. Web production dağıtımı yeniden yapıldı; canlı `/`, `/hadith`, `/mushaf` ve `sitemap.xml` HTTP 200.
- 2026-11-11 — Web app-mode çift scroll düzeltildi: `html/body` dış scroll’u kapatıldı, Compose hedefi `100dvh` ile tek iç scroll yüzeyi olarak sabitlendi; canlı `https://hudaquran.web.app/?app=1` ölçümünde dış `scrollHeight == clientHeight` doğrulandı.
- 2026-11-17 — Paylaşım kartı görsel uyumu: varsayılan kart teması artık aktif uygulama temasını izliyor; kağıt/kontrast/gece/adaçayı paletleri sabitlendi, gradient arka planlar kaldırıldı ve metin kontrastı korunacak düz yüzeyler kullanıldı. Android host testleri, debug APK, JS/Wasm derlemeleri başarılı; web yeniden deploy edildi.
- 2026-11-17 — Paylaşım tema regresyon testi eklendi: kart seçenekleri varsayılan olarak `APP` temasını kullanıyor; `ShareCardStyleTest` başarılı.

- 2026-11-18 — HUDA-407 seferî/yolculuk modu genişletildi: kalıcı yolculuk adı ve varış yeri, hazırlık/aktif/dönüş/tamamlandı fazları, başlangıç-dönüş hazır seçenekleri, çevrimdışı içerik ve ihtiyaç kontrol listeleri, günlük check-in ve kıble/dua/offline kısayolları eklendi. Uygulama vakit/konum hesabını otomatik değiştirmiyor; fıkhî sınır açıklaması korunuyor. `TravelModeLogicTest` ile faz geçişleri doğrulandı.
- 2026-11-18 — Quran Foundation için gizli anahtar içermeyen backend/proxy adaptörü eklendi (`QuranFoundationApi`) ve entegrasyon notu oluşturuldu. Chapters, verses, recitations, translations, tafsirs, audio ve search uçları tek sağlayıcı sınırında toplandı; OAuth secret Android/web içine konmadı. Proxy URL'si ve lisans/attribution onayı bekleniyor.
- 2026-11-18 — Seferî tur doğrulaması: `git diff --check`, `:shared:testAndroidHostTest --tests ...TravelModeLogicTest`, `:shared:compileKotlinJs`, `:shared:compileKotlinWasmJs` ve `:androidApp:assembleDebug` başarılı.
- 2026-11-18 — Yeni Vaktelya APK’sı REA ile yeniden doğrulandı: SHA-256 `d14067881920829f1a0183dbd143937017dd1df7d9572c43fe54bf867638a9cb`; 1.571 içerik düğümü ve 1.708 occurrence bütünlük çelişkisi olmadan incelendi. APK’nın doğrulanmış 43 seçili Mişari el-Afasi ayeti, Esma toplu kaydı, 3 ezan, 3 bildirim sesi, Türkçe içerik/hadis/kalendar/recordings manifestleri ve kaynak bildirimi KMP ortak kaynaklarına aktarıldı. Ortak `VaktelyaCatalog` okuyucu adı, ayet, kaynak URL’si ve yerel resource URI’siyle kayıtları sunuyor. Android kaynak adlandırma hatası düzeltilerek Android/JS/Wasm ve debug APK derlemeleri başarılı oldu.

- 2026-11-11 — Mushaf/yaşlı modu turunda 320/360dp dokunma bölgeleri, A-/A+ semantiği, büyük dokunma alanları, reduced-motion davranışı ve 8 dilde Mushaf/Okuma etiketleri düzeltildi. Paylaşım kartlarında gövde bütçesi, URL satır kırma ve ayrı deep-link başlıkları iyileştirildi; ortak testler eklendi.
- 2026-11-11 — Edition picker ağ hata/yeniden dene durumu ve boş katalog cache davranışı düzeltildi. Son doğrulama: Android/JS/Wasm + debug APK başarılı; `:shared:testAndroidHostTest` başarılı.
- 2026-11-11 — Ayarlar katalog yüklemeleri exception dayanıklı hale getirildi; Türkçe sağlayıcı kullanıcıya üçüncü taraf API olarak etiketlendi. Son ortak derleme, debug APK ve Android host testleri başarılı.

## Doğrulama kapısı

## Son doğrulama görevleri

- [ ] HUDA-501 — Web smoke test ve responsive ekran kontrolü
  - Durum: REVIEW
  - Sonuç: `http://localhost:8080/` ve `/mushaf` daha önce tarayıcıda açılıp görsel olarak kontrol edildi. 2026-11-21 canlı kontrolde ana sayfanın DOM'u ve kütüphane bağlantıları, `/?app=1` uygulama kabı, eski `/#/hadith` uyumluluk bağlantısı ve `/?app=1&screen=hadith` doğrudan deep link'i doğrulandı. Hadis ekranı, ağ yanıt vermediğinde açıklayıcı hata ve yeniden dene eylemi gösterdi. 320/390dp responsive matrisi, paylaşım akışı ve yeni ekranların tam ekran taraması bekliyor.
- [ ] HUDA-502 — Android gerçek cihaz kurulumu ve kritik akış testi
  - Durum: BLOCKED
  - Sonuç: Güncel debug APK üretildi; ADB cihaz listesi boş olduğu için telefona kurulum ve gerçek cihaz testi yapılamadı. Telefon USB hata ayıklama ile tekrar görünür olduğunda kurulup test edilecek.
- [ ] HUDA-503 — Widget/MediaSession/sensör matrisi
  - Durum: BLOCKED
  - Sonuç: Widget metadata’sı ve MediaSession kodu derlendi; launcher, kilit ekranı, ses çıkışı ve kıble sensörü gerçek cihaz olmadan doğrulanamaz.
- [x] HUDA-504 — Wasm ses oynatma derleme uyumluluğu
  - Durum: DONE
  - Sonuç: Wasm ses dosyalarındaki Promise `catch` dönüş tipleri düzeltildi; `:shared:compileKotlinWasmJs` başarılı. Kalan Wasm interop mesajları uyarı seviyesinde.
- [x] HUDA-505 — JVM testlerinde büyük localization sınıfı
  - Durum: DONE
  - Sonuç: 296 parametreli `Strings` constructor’ı tek tablo tabanlı temsil ile değiştirildi; mevcut `strings.navHome` API’si ve 8 dil korunuyor. `ReminderPlanner` test edilebilirliği için global tercih bağımlılığı enjekte edilebilir hale getirildi. `:shared:testAndroidHostTest` başarılı (61 test).
- [x] HUDA-506 — Katalog seçiminde sonsuz loading ve boş cache düzeltmesi
  - Durum: DONE
  - Sonuç: Edition picker ağ hatasında açık hata ve yeniden dene aksiyonu gösteriyor. Çeviri/tefsir katalogları boş sonucu işlem ömrü boyunca memoize etmiyor; bağlantı geri geldiğinde yeniden denenebiliyor. Android/JS/Wasm derlemeleri ve Android host testleri başarılı.
- [x] HUDA-507 — Ayarlar kataloğu hata dayanıklılığı ve sağlayıcı şeffaflığı
  - Durum: DONE
  - Sonuç: Ayarlar ekranındaki hafız/meal/tefsir katalog okumaları artık tek bir ağ hatasıyla ekranı düşürmüyor. Türkçe üçüncü taraf sağlayıcı etiketi açıkça belirtiliyor; lisans bilgisi bilinmiyorsa uydurulmuyor. Android/JS/Wasm, APK ve Android host testleri başarılı.
- [x] HUDA-508 — Meal/tefsir dil metadata’sı ve yaşlı seçim ekranı sertleştirmesi
  - Durum: DONE
  - Sonuç: Quranpedia yanıtında eksik dil bilgisi artık varsayılan olarak Arapça kabul edilmiyor; metadata eksikse kayıt katalogdan güvenli biçimde dışarıda bırakılıyor. Edition picker dil filtresini normalize ediyor, katalog yenilenince geçersiz filtreyi temizliyor, yaşlı/büyük dokunma profillerinde filtre hedeflerini en az 48dp tutuyor ve boş katalogda yeniden deneme sunuyor. Ortak Android/JS/Wasm derlemeleri ve host testleri başarılı.
- [x] HUDA-509 — Paylaşım önizleme KMP derleme regresyonları
  - Durum: DONE
  - Sonuç: Esma kartındaki BoxWithConstraints alıcı kapsamı düzeltildi; her hedefte bulunmayan `drawLayer` çağrısı yerine kayıtlı katman + canlı önizleme ayrımı kullanıldı. Debug APK ve Android host testleri dahil ortak doğrulama başarılı.
- [x] HUDA-510 — Meal/tefsir metadata ve dil filtresi regresyon testleri
  - Durum: DONE
  - Sonuç: Eksik/boş Quranpedia dil kodunun Arapça’ya dönüşmemesi ve Edition picker’ın normalize edilmiş dil filtresi, birleşik arama filtresi ve boş filtre davranışı ortak testlerle doğrulandı. Android/JS/Wasm derlemeleri, debug APK ve Android host testleri başarılı.
- [x] HUDA-511 — Ses yükleme watchdog’u ve kıraatçı preview erişilebilirliği
  - Durum: DONE
  - Sonuç: 20 saniye boyunca açılmayan ses artık sessizce durmak yerine `PlaybackFailure` olarak yayınlanıyor; Reciters ekranı bu kıraatçıyı güvenli biçimde listeden çıkarabiliyor. Preview düğmesi normal profilde 48dp, büyük dokunma profilinde 60dp oldu. Watchdog davranışı ortak testle doğrulandı; host testleri ve APK başarılı.
- [x] HUDA-512 — Kıble koordinat güvenliği
  - Durum: DONE
  - Sonuç: Eksik, NaN veya aralık dışı konum verisi artık sahte bir kıble yönüne dönüştürülmüyor; ekran güvenli biçimde konum kullanılamıyor durumuna geçiyor. İstanbul/New York/Cakarta azimut testlerine malformed koordinat regresyon testi eklendi. Android/JS/Wasm, debug APK ve Android host testleri başarılı.
- [x] HUDA-513 — Offline indirmede son anda duraklatma güvenliği
  - Durum: DONE
  - Sonuç: Son ağ isteği tamamlanırken gelen duraklatma isteği artık final `COMPLETE` durumuyla ezilmiyor; manifest `PAUSED` olarak korunuyor ve devam aksiyonu sunuluyor. Tamamlanma/kısmi/duraklatma önceliği ortak testlerle doğrulandı. Android/JS/Wasm, debug APK ve Android host testleri başarılı.
- [x] HUDA-514 — Bildirimde kapalı namaz vakitlerini dışlama testi
  - Durum: DONE
  - Sonuç: Kullanıcının kapattığı bir namaz vakti için ne ön hatırlatma ne de vakit bildirimi üretiliyor; diğer vakitlerin iki bildirimli akışı korunuyor. Android host testleri başarılı; ortak kaynak kodu önceki Android/JS/Wasm/APK kapısından da geçti.
- [x] HUDA-511 — Ses yükleme watchdog’u ve kıraatçı preview erişilebilirliği
  - Durum: DONE
  - Sonuç: 20 saniye boyunca açılmayan ses artık sessizce durmak yerine `PlaybackFailure` olarak yayınlanıyor; Reciters ekranı bu kıraatçıyı güvenli biçimde listeden çıkarabiliyor. Preview düğmesi normal profilde 48dp, büyük dokunma profilinde 60dp oldu. Android/JS/Wasm derlemeleri, debug APK ve Android host testleri başarılı.

Bir görev `DONE` olmadan önce:

1. `git diff --check`
2. Ortak Android/web derlemesi
3. Uygunsa gerçek Android cihaz testi
4. Kullanıcıya görünen değişiklikte ekran/akış kontrolü
5. Bu dosyada görev kutusu ve sonuç notu güncellemesi

## Önceki agent doğrulama notu

- [ ] HUDA-523 — Quran Foundation API ve kıraatçı sağlayıcısını güvenli biçimde bağla
  - Durum: IN PROGRESS
  - Sonuç: Common KMP adaptörü AppContainer/QuranRepository'ye bağlandı; sure ve ayet kıraatçı kimlikleri ayrıldı, QF sure kıraatçıları Reciters ekranında listelenip sure ses URL'si üzerinden önizlenebiliyor. Firebase Functions v2 read-only proxy, dar endpoint/query allowlist'i, Content/Search ayrı scope token önbelleği, timeout ve rate limit ile eklendi. Hosting rewrite proxy'ye yönlendirildi.
  - Açık kapı: Quran Foundation hesabı ve üretim erişimi, Firebase Secret Manager değerleri, Blaze planı ve deploy gerekli. Proxy deploy edilmediği için gerçek ağ/ekran doğrulaması bekliyor; prelive yalnızca ilk iki sureyi içerir.
  - Sonraki adım: Hesap erişimleri hazır olduğunda Secret Manager'a anahtarları ekleyip Emulator Suite ve üretim erişimi doğrulaması yapmak.

- `git diff --check`: başarılı.
- İlk denemelerde OneDrive/Kotlin cache kilidi oluştu; daemon ve Kotlin cache temizlendikten sonra ortak Android/JS derlemesi ve `:androidApp:assembleDebug` başarılı oldu.

- [x] HUDA-515 — Ayet çalışma akışında meal, tefsir ve nüzûl bağlamını ayırma
  - Durum: DONE
  - Sonuç: Ayet detayında meal ve tefsir artık ayrı bölümler olarak gösteriliyor. Nüzûl sebebi ayrı bir kavram olarak açıklanıyor; doğrulanmış ayrı kaynak yoksa içerik uydurulmuyor. Meal, kullanıcının seçtiği çeviri API’sinden çekiliyor.
- [x] HUDA-516 — Seferî mod için mezhep-duyarlı namaz rehberi
  - Durum: DONE
  - Sonuç: Hanefî ve Şafiî profilleri eklendi. Hanefî profili dört rekâtlı farzların seferîlikte iki rekât oluşunu, mukim imama uyma durumunu ve cem konusunda sınırları açıklar. Uygulama durak sayısından otomatik hüküm üretmiyor; sünnetleri terk etmeyi blanket bir öneri olarak sunmuyor. Diğer mezhepler için güvenli genel bilgilendirme ve ilmî kaynağa yönlendirme bulunuyor.
- [x] HUDA-517 — Meal/tefsir ve seferî rehber Android + web doğrulaması
  - Durum: DONE
  - Sonuç: `:shared:compileAndroidMain`, `:shared:compileKotlinJs`, `:shared:compileKotlinWasmJs` ve `:androidApp:assembleDebug` başarılı. `git diff --check` yalnızca mevcut satır sonu uyarıları verdi; yeni değişiklikte derleme hatası yok.
- [x] HUDA-518 — Vaktelya yerel kayıtlarını gerçek offline akışına bağlama
  - Durum: DONE
  - Sonuç: APK’dan doğrulanan yerel Vaktelya kayıtları artık Offline içerikler ekranında katalog olarak görünür ve ağ olmadan önizlenebilir. Kayıtların tam kıraatçı kataloğu olmadığı kullanıcıya açıkça belirtiliyor; `VaktelyaCatalog` kaynak/atıf bilgisini koruyor. Android/JS/Wasm ve debug APK derlemesi başarılı.
- [x] HUDA-519 — Esma sesli okuma ve detay reklam güvenliği
  - Durum: DONE
  - Sonuç: Esmaü’l-Hüsna detayındaki dinle/duraklat kontrolü yerel Vaktelya Esma kaydına bağlandı. Hadis detayında reklam içerik akışının içine değil, metin bittikten sonra tek ayrılmış alana yerleştirildi. Reklamlar yalnızca Android’de çalışıyor; debug’da Google test birimleri, release’te gerçek birimler kullanılıyor. Android/JS/Wasm ve debug APK derlemesi başarılı.

- [x] HUDA-520 — Vaktelya Türkçe hadis snapshot’ını üretim fallback kaynağına bağla
  - Durum: DONE
  - Sonuç: APK’dan taşınmış `vaktelya_hadith_tr.json` artık `VaktelyaHadithLocalDataSource` üzerinden `HadithRepository` varsayılanına bağlandı. Remote veri önceliği korunuyor; ağ/boş yanıt durumunda HadeethEnc atıflı Türkçe katalog ve detay fallback’i kullanılabiliyor.
  - Dosyalar: `shared/src/commonMain/kotlin/org/ferdidrgn/hudaquran/data/local/HadithLocalDataSource.kt`, `shared/src/commonMain/kotlin/org/ferdidrgn/hudaquran/data/repository/HadithRepository.kt`, `shared/src/commonMain/kotlin/org/ferdidrgn/hudaquran/di/AppContainer.kt`.
  - Doğrulama: `:shared:compileAndroidMain`, `:shared:compileKotlinJs`, `:shared:compileKotlinWasmJs` başarılı. Gerçek hadis ekranı ve uzun katalog performans testi açık.

- [x] HUDA-521 — APK → hedef proje entegrasyon matrisi ve statik kanıt günlüğü
  - Durum: DONE
  - Sonuç: Kaynak APK’nın servis/ayar/bildirim/widget/ses/kıble işlevleri ile hedef dosya ve katman karşılıkları `VAKTELYA_INTEGRATION_MATRIX.md` içinde eşleştirildi. Statik analiz sınırları ve runtime’da doğrulanması gereken maddeler açıkça yazıldı.
  - Doğrulama: REA artifact ve Android package evidence kayıtları belgeye işlendi; hedef ortak derlemeleri başarılı.

### HUDA-524 follow-up
- Status: REVIEW
- The 43 Quran duas in `vaktelya_content_tr.json` are connected to the offline dua list with their source and ayah reference. No transliteration is invented; existing favorites remain intact.
- Android, JS, and Wasm compile tasks passed. Visual dua-screen review remains; the names, additives, and surah-name arrays were subsequently surfaced through the HUDA-525 Settings flows.

- REA re-opened the exact APK SHA-256 recorded earlier. The APK is Flutter debug/JIT (`assets/flutter_assets/kernel_blob.bin`) and has no `lib/<abi>/libapp.so`; the Dart AOT inspector cannot extract the embedded API call graph. ADB reports no connected device, so live endpoint capture is still unavailable.

### HUDA-525 follow-up
- Status: REVIEW
- The 99-name Vaktelya catalog is selectable alongside the Huda Quran Esma list, the 601-item additive guide is searchable and source-linked, and the 114 Turkish surah names are selectable in Settings for the Surah list. No unsupported halal/haram verdict is shown.
- Android, JS, and Wasm shared compilation passed. Device-level visual review remains unavailable because ADB has no connected device.

### Crash ve sure anlamı takip işleri
- [ ] HUDA-540 - Sure listesinde anlamları uygulama diline göre göster
  - Durum: REVIEW
  - Sonuç: Quran Foundation bölüm adları seçili arayüz dilinde isteniyor; Arapça arayüzde İngilizce anlam gösteriliyor. Ağ/boş yanıt durumunda mevcut İngilizce anlam korunuyor. Kaynak çağrısı repository katmanında ve 3 saniye zaman aşımıyla yapılıyor.
- [ ] HUDA-541 - Çevrimdışı içerik ekranının ilk açılış çökmesini önle
  - Durum: REVIEW
  - Sonuç: Manifest okuma/temizleme ve kayıtlı kaynak tercihlerini okuma hata toleranslı hale getirildi. Kullanıcı geri bildirimi: internet açıkken uygulamada çevrimdışı içerikler sayfasına ilk kez girerken çöküyor. Gerçek stack trace/cihaz logu henüz yok; emülatör veya cihazda ilk açılış doğrulaması bekliyor.
- [ ] HUDA-542 - Esmaü'l-Hüsna ekranındaki liste çökmesini önle
  - Durum: REVIEW
  - Sonuç: Esma ve Vaktelya katalog gridleri ekranın kalan yüksekliğine sığacak şekilde sınırlandı. Kök neden gerçek stack trace ile doğrulanmadı; cihazda açılış kontrolü bekliyor.
