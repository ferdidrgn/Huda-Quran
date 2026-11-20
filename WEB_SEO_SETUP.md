# Huda Quran web, Search Console ve AdSense kurulumu

Bu proje web tarafında iki katmanı birlikte yayımlar:

- SEO için taranabilir HTML içerik sayfaları: `/`, `/mushaf`, `/hadith`, `/tafsir`, `/dua`, `/prayer`, `/reciters`
- Etkileşimli KMP/Compose uygulaması: `/?app=1` ve `?app=1&screen=...`

## Search Console’da sitemap gönderme

CSV yükleme seçeneği yok; sitemap için CSV gerekmez. En kolay yöntem:

1. [Google Search Console](https://search.google.com/search-console) içinde `https://hudaquran.web.app/` mülkünü ekle.
2. Mülk türü olarak **URL öneki** seçersen doğrulaması kolaydır.
3. HTML dosyası doğrulamasında Google’ın verdiği `google...html` dosyasını `webApp/src/webMain/resources/` içine koy. Bu projede mevcut doğrulama dosyası korunur.
4. Firebase Hosting deploy’undan sonra `https://hudaquran.web.app/google5afbd46fec1b3ba7.html` adresini kontrol et.
5. Search Console sol menüsünde **Sitemaps** bölümünü açıp yalnızca `sitemap.xml` yaz ve **Gönder** düğmesine bas. Tam URL otomatik olarak `https://hudaquran.web.app/sitemap.xml` olur.
6. Sonuç durumunun “Başarılı” olmasını bekle. Yeni sayfalar için tekrar CSV yüklemek gerekmez; sitemap URL’si aynı kalır.

Üretimde şu iki URL de doğrudan açılmalıdır:

- `https://hudaquran.web.app/robots.txt`
- `https://hudaquran.web.app/sitemap.xml`

## AdSense hazırlığı

1. AdSense’te **Siteler** bölümünden `hudaquran.web.app` alan adını ekle.
2. AdSense script’i ve `google-adsense-account` meta etiketi ana HTML’de ve içerik sayfalarında korunuyor.
3. `https://hudaquran.web.app/ads.txt` adresini doğrula.
4. `/privacy.html` ve `/terms.html` sayfalarını incele.
5. Hesapta onay/CMP gerekiyorsa AdSense panelinden tamamla. Kod tarafı siteyi incelemeye hazırlar; “Ready” ve reklam gösterim kararı Google hesabından verilir.
6. Site onaylandıktan sonra reklam birimi oluştur. Reklam birimi kimliği (`ca-pub-...` değil, `data-ad-slot` değeri) gelmeden sahte slot ID eklenmemelidir.

## Firebase deploy kontrolü

Production dağıtım klasörü: `webApp/build/dist/wasmJs/productionExecutable`

```powershell
./gradlew.bat :webApp:wasmJsBrowserDistribution
firebase deploy --only hosting:hudaquran
```

Deploy sonrası `/`, `/mushaf`, `/hadith`, `/tafsir`, `/dua`, `/prayer`, `/reciters`, `/robots.txt`, `/sitemap.xml`, `/ads.txt`, `/app-ads.txt`, `/google5afbd46fec1b3ba7.html`, `/.well-known/assetlinks.json` ve `/.well-known/apple-app-site-association` adreslerini kontrol et.

## Android App Links

`assetlinks.json`, `org.ferdidrgn.hudaquran` paketini ve debug/release/Play imza parmak izlerini içerir. Yeni Play App Signing sertifikası veya release keystore kullanılırsa Google Play Console’daki SHA-256 parmak izi bu dosyaya eklenmelidir.

```powershell
adb shell pm verify-app-links --re-verify org.ferdidrgn.hudaquran
adb shell pm get-app-links org.ferdidrgn.hudaquran
```

iOS Universal Links için gerçek Apple Team ID gerekir. Projedeki `TEAM_ID` boş olduğu için AASA dosyası sahte `TEAMID` kullanmadan boş detay listesiyle yayımlanır. Team ID edinildiğinde `details` içine şu kayıt eklenmelidir:

```json
{
  "appID": "GERCEK_TEAM_ID.org.ferdidrgn.hudaquran",
  "paths": ["/*"]
}
```

Ardından Xcode’da Associated Domains capability’si ve `applinks:hudaquran.web.app` entitlement’ı etkinleştirilmelidir.
