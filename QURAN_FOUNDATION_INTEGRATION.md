# Quran Foundation entegrasyonu

Uygulama tarafında `QuranFoundationApi` hazırlandı. Bu adaptör chapters, verses, recitations, translations, tafsirs, audio ve search uçlarını tek bir sağlayıcı sınırında toplar.

## Güvenlik ve lisans

- OAuth `client_secret` Android APK'sına veya tarayıcı bundle'ına konulmaz.
- Uygulama, Huda Quran'a ait bir backend/proxy URL'sine bağlanır; OAuth ve erişim kapsamları backend'de tutulur.
- `content`, `content.quran.read`, `content.translations.read`, `content.tafsirs.read` ve `content.audio.read` gibi yalnızca ihtiyaç duyulan kapsamlar istenir.
- Yanıtlar cache'lenmeden ve yeniden dağıtılmadan önce Quran Foundation/Quran.com şartları, kaynak gösterimi ve ticari kullanım izni kontrol edilir.

## Yayına alma sırası

1. Quran Foundation geliştirici hesabı ve uygulama kaydı açılır.
2. OAuth secret yalnızca Firebase/Cloud Functions, Cloud Run veya benzeri backend secret store'a eklenir.
3. Backend, uygulama için dar kapsamlı read-only proxy uçları sağlar ve rate limit uygular.
4. Web ve Android `QuranFoundationProxyConfig(baseUrl = ...)` ile yalnızca proxy URL'sini kullanır.
5. Hata/boş yanıt cache'e başarı gibi yazılmaz; mevcut alquran.cloud ve yerel içerik yedekleri korunur.

Bu dosya resmi API'nin kullanım rehberine göre hazırlanmıştır; Quran.com arayüzünün veya içeriğinin izinsiz toplu kopyası değildir.
