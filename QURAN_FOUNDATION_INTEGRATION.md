# Quran Foundation entegrasyonu

Uygulama tarafında `QuranFoundationApi` hazırlandı ve `AppContainer` içine bağlandı. Chapters, verses, ayet kıraatçıları, sure kıraatçıları, translations, tafsirs, sure ses dosyaları ve Quran Foundation Search API ayrı doğru yollarıyla sunulur. Sure kıraatçı kataloğu Reciters ekranına eklenir; önizleme URL'si sure/kıraatçı kimliğine göre QF uç noktasından çözülür. Eski AlQuranCloud kataloğu yedek olarak korunur.

## Backend proxy

Firebase Functions v2 için `/api/qf/**` yönlendirmesi ve `functions/` içindeki read-only proxy iskeleti eklendi. Proxy yalnızca izin verilen GET yollarını ve sorgu parametrelerini geçirir; Content ve Search için ayrı OAuth scope/token önbelleği tutar, token süresi yaklaşınca yeniler, üst servise sınırlı süre tanır ve kimlik bilgilerini/üst servis hata gövdelerini istemciye döndürmez. Başarılı içerik için Hosting önbelleği 5 dakika ile sınırlıdır. Uygulama URL'si `https://hudaquran.web.app/api/qf` olarak ayarlanmıştır.

Proxy canlı değildir; bu depoda henüz API hesabı/üretim erişimi, Firebase secret değerleri veya deploy edilmiş bir function doğrulanmadı. Quran Foundation prelive ortamı yalnızca Al-Fatiha ve Al-Baqarah içerir; tam katalog için üretim erişimi gerekir.

## Güvenlik ve lisans

- OAuth `client_secret` Android APK'sına veya tarayıcı bundle'ına konulmaz.
- Uygulama, Huda Quran'a ait bir backend/proxy URL'sine bağlanır; OAuth ve erişim kapsamları backend'de tutulur.
- `content`, `content.quran.read`, `content.translations.read`, `content.tafsirs.read` ve `content.audio.read` gibi yalnızca ihtiyaç duyulan kapsamlar istenir.
- Yanıtlar cache'lenmeden ve yeniden dağıtılmadan önce Quran Foundation/Quran.com şartları, kaynak gösterimi ve ticari kullanım izni kontrol edilir.

## Yayına alma sırası

1. Quran Foundation geliştirici hesabı ve uygulama kaydı açılıp gerekli Content/Search kapsamları alınır.
2. Firebase Functions için Blaze planı etkinleştirilir; `firebase functions:secrets:set QF_CLIENT_ID` ve `firebase functions:secrets:set QF_CLIENT_SECRET` ile değerler Secret Manager'a eklenir. Bu komutlar çalıştırılmadı.
3. Quran Foundation dokümanına göre yeni backend hesabı granular izinler alıyorsa varsayılan `QF_CONTENT_SCOPE_MODE=granular` bırakılır; eski `content` izni kullanan hesapta `QF_CONTENT_SCOPE_MODE=legacy` seçilir. Search için ayrıca `search` kapsamı gerekir.
4. Gerekirse `QF_ENVIRONMENT` parametresi `prelive` olarak bırakılır; production için erişim onayı alındıktan sonra `production` yapılır.
5. Functions ve Hosting deploy edilir. Öncesinde yerel Emulator Suite ile proxy kontrol edilir.
6. `/api/qf/content/api/v4/chapters?language=tr`, `/api/qf/content/api/v4/resources/chapter_reciters?language=tr` ve `/api/qf/search/api/v1/search?mode=quick&query=fatiha` canlı erişimle doğrulanır.
7. Hata/boş yanıt mevcut AlQuranCloud ve yerel içerik yedeklerini etkilemez.

Bu dosya resmi API'nin kullanım rehberine göre hazırlanmıştır; Quran.com arayüzünün veya içeriğinin izinsiz toplu kopyası değildir.
