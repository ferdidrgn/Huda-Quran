# Vaktelya 1.4.1 → Huda Quran entegrasyon matrisi

Bu belge, `C:\Users\ferdi\Downloads\Vaktelya-Pro-1.4.1-debug.apk` üzerinde REA ile yapılan statik inceleme ile hedef KMP projesinin mevcut karşılıklarını birlikte tutar.

## İnceleme kimliği ve sınırlar

- APK SHA-256: `d14067881920829f1a0183dbd143937017dd1df7d9572c43fe54bf867638a9cb`
- Paket: `com.vaktelya.pro`, sürüm `1.4.1` / versionCode `9`
- Android: min SDK 24, target SDK 35
- REA artifact özeti: 1.571 düğüm, 1.708 occurrence, bütünlük çelişkisi yok.
- REA kanıtları: artifact `ev_b243b8c45b5f3e511b203d4943e99f1fb11e9a1bf51697ae634d80ed6dc8ba5f`; Android paket `ev_723a089fdd939f62894e2df6f3073d43a54fec781695c6718625fa7c7e1cd2a9`.
- Bu belge statik kanıttır. APK çalıştırılmadığı için gerçek cihazda gözlemlenmiş davranış iddiası değildir; runtime ve fiziksel cihaz maddeleri ayrıca işaretlenmiştir.

## Kaynak → hedef eşleştirmesi

| Kaynak işlevi / kanıt | Hedef karşılığı | Durum / eksik doğrulama |
|---|---|---|
| Flutter ana uygulama ve ekran yönlendirmesi | `shared/.../App.kt`, `DeepLink.kt`, ortak Compose ekranları | Uyarlanmış; kaynak tasarım kopyalanmadı. Tam Flutter route listesi runtime ile tamamlanmalı. |
| Kur’an metni ve meal; `alquran.cloud` atıfları | `data/remote/QuranApi.kt`, `QuranRepository.kt`, `ui/quran`, `ui/mushaf` | Mevcut API zinciri korunuyor; edition seçiminde kaynak/dil metadata’sı var. |
| Seçili Mişari el-Afasi ayet sesleri | `composeResources/files/vaktelya_quran-*.mp3`, `data/local/VaktelyaCatalog.kt`, `ui/offline/OfflineContentScreen.kt` | Bağlandı; katalog tam kıraatçı listesi değildir. |
| Esmaü’l-Hüsna toplu ses kaydı | `files/vaktelya_asma-ul-husna.ogg`, `ui/esmaulhusna/EsmaulHusnaDetailScreen.kt` | Bağlandı; ekran içi oynat/duraklat mevcut. |
| Türkçe hadis snapshot’ı (HadeethEnc) | `files/vaktelya_hadith_tr.json`, `HadithLocalDataSource.kt`, `HadithRepository.kt` | Bağlandı; remote öncelikli, Türkçe offline/fallback kaynak aktif. |
| Hadis açıklama, derece, referans ve kaynak URL’si | `HadithDtos.kt`, `domain/model/Hadith.kt`, `ui/hadith`, `ui/share/HadithPostcard.kt` | Ayrı alanlar korunuyor; görsel paylaşımda metin blokları ayrıştırılmış. |
| Dini takvim ve Türkçe içerik JSON’ları | `files/vaktelya_religious_calendar.json`, `files/vaktelya_content_tr.json` | Dosyalar taşındı; repository/UI bağlantısı ayrıca tamamlanmalı. |
| `flutter_local_notifications` receiver’ları | `notifications/PrayerNotificationScheduler.*`, `ReminderPlanner.kt`, Android notification channels | Ortak zamanlama ve Android/iOS karşılıkları mevcut; kaynak payload’ı runtime ile karşılaştırılmalı. |
| `AdhanAlarms.schedule/restore/clear` | `ReminderPlanner`, `PrayerNotificationScheduler.android.kt`, `AppPreferences` | İş mantığı karşılığı var. Kaynakta `vaktelya_adhan/alarms` SharedPreferences ve exact alarm kullanılıyor; hedefte mimariye uygun uyarlama var. |
| Bildirim/ezan sesleri | `AppPreferences.PrayerNotificationSound`, `PrayerNotificationScheduler`, Android raw resources | Hedefte 3 ezan, kısa bip ve titreşim seçimi var; gerçek cihaz ses testi açık. |
| Geolocation | `PrayerRepository`, Android location katmanı ve ayarlar | Karşılığı var; izin reddi/konum yok durumu fallback ile işleniyor. |
| Kıble (`flutter_qiblah`) | `sensors/QiblaCompass.*`, `domain/model/QiblaTest.kt` | Hesap güvenliği ve malformed koordinat koruması var; sensör cihaz testi açık. |
| Widget MethodChannel `com.vaktelya.pro/widgets` | Android widget provider’ları ve deep-link rotaları | Hedefte widget ailesi var; kaynak MethodChannel sözleşmesi birebir değil, native widget akışı kullanılıyor. |
| Audio `just_audio` | `audio/AudioPlayer.*`, `PlaybackManager`, `PlaybackNotificationService` | Karşılık mevcut; loading watchdog’u ve MediaSession var. |
| FirebaseMessaging sınıfları | `analytics/PushNotifications.*`, `HudaQuranMessagingService.kt` | Hedefte push kanalı mevcut; kaynak APK’da FirebaseMessaging sınıfı bulunmadı, doğrulanmış kaynak push payload’ı iddia edilmiyor. |
| NFC, foreground media playback, wake lock, exact alarm izinleri | Android manifest ve ilgili platform katmanları | Kaynak izinleri matrise alındı; release izin audit’i açık. |

## Kaynakta doğrulanan sözleşmeler

- `MainActivity` MethodChannel: `com.vaktelya.pro/widgets`.
- `onNewIntent`, `/settings` ve `/compass` rotalarını `openRoute` olarak Flutter tarafına iletiyor.
- `AdhanAlarms.schedule`, gelecekteki `at` (epoch millis), `id` ve `title` alanlarını bekliyor; Android 12+ exact alarm izni yoksa `SecurityException` üretiyor.
- Alarm kayıtları kaynakta `vaktelya_adhan` / `alarms` altında JSON dizi olarak saklanıyor; `restore` hatayı yutuyor, `clear` pending intent’leri iptal edip servisi durduruyor.
- Statik sınıf envanterinde local notification receiver’ları, geolocator, qibla ve just_audio bulundu; FirebaseMessaging sınıfı bulunmadı.

## Açık doğrulama maddeleri

1. `vaktelya_content_tr.json` ve `vaktelya_religious_calendar.json` için hedef repository/UI tüketimi.
2. Kaynak APK’nın tam Flutter route listesi ve ekran callback’leri için runtime/ekran keşfi.
3. Gerçek Android cihazda bildirim sesi, exact alarm, widget tıklaması, audio focus ve kıble sensörü.
4. Vaktelya ile Huda Quran arasında lisans/atıf metinlerinin son release kontrolü.
5. Release reklam ortamının debug/test birimleriyle karışmadığının imzalı build üzerinde doğrulanması.
