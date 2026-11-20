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
| Kitap modu ve yaşlı modu | IN PROGRESS | A−/A+ ve ölçekleme eklendi, kapsam genişletilecek |
| Paylaşım sistemi | REVIEW | Ayet/hadis/Esma ortak kart formatı, ölçüm, tema ve çıktı seçenekleri eklendi; temiz derleme kapısı bekliyor |
| Widget ailesi | REVIEW | Sıradaki namaz ve günün ayeti widget’ları eklendi; temiz derleme bekliyor |
| Hadis/dua/Esma içerikleri | REVIEW | Favoriler ayrıştırıldı, hadis akışı sınırlandı; cihaz/ağ ekran testi bekliyor |
| Ses ve hafızlar | REVIEW | Ağ belirsizliğinde katalog korunuyor; cihaz doğrulaması bekliyor |
| Tefsir/mealler | REVIEW | Gerçek API dilleri, provider/yazar/lisans metadata’sı ve kaynak ayrıntısı gösteriliyor; ekran doğrulaması bekliyor |
| İbadet takibi | IN PROGRESS | Bugün alanı ve yaşlı profili eklendi; takip modülleri sırada |

## Görevler

### P0 — Kullanılabilirlik ve güvenilirlik

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
  - Sonuç: Aynı gün için geçerli cache okunuyor; cache yoksa veya boş/bozuksa 15 maddelik gömülü ayet fallback kataloğu kullanılıyor. Tarih ve saat dilimi değişiminde provider kendini yeniliyor; uygulama deep link’i korunuyor. Cihaz/launcher testi bekliyor.
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
  - Sonuç: 6 widget metadata’sında min resize ölçüleri, preview, erişilebilir etiketler ve immutable PendingIntent kontrolleri tamamlandı. Widget receiver’ları `exported=false`; yalnızca MediaButtonReceiver sistem medya komutları için bilinçli olarak `exported=true`. Alarm yenilemesi widget varken kuruluyor, kaldırılınca iptal ediliyor. Gerçek launcher yerleştirme/yeniden boyutlandırma matrisi bekliyor.
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
  - Sonuç: `http://localhost:8080/` ve `/mushaf` tarayıcıda açıldı; ana ekran ve Mushaf görünümü görsel olarak kontrol edildi, A+/A- kontrolleri bulundu. Konsolda hata/uyarı görülmedi. Hadis, paylaşım ve yeni akışların ekran bazlı taraması bekliyor.
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
