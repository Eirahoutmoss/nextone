# NexTone
## Batı ve Türk çalgıları için makam bilen akort ve çalışma uygulaması

**Belge türü:** Ürün tanımı + teknik mimari + geliştirme planı  
**Hedef:** Geliştirme ajanına doğrudan verilebilecek proje tanımı  
**Durum:** v1 — K1 kontrol noktası (7 Ekim 2026)  
**Dil:** Türkçe

> **Okuma notu:** Henüz karar verilmemiş konular **[AÇIK KARAR]**, bir müzik öğretmeninin onaylaması gereken değerler **[DOĞRULANACAK]** etiketiyle işaretlidir. Hepsi §16'da listelenir.

---

# 1. Amaç ve kapsam

NexTone, müzik öğretmenlerinin ve öğrencilerinin **farklı çalgıları tek uygulamayla akort etmesini** ve perdelerini hem Batı hem makam sistemine göre okuyabilmesini sağlayan bir Android uygulamasıdır.

Temel ilkeler:

- **Ticari değil.** Reklam, abonelik, hesap, sunucu yok.
- **Dağıtım:** APK dosyası, WhatsApp üzerinden öğretmen arkadaşlara (§12).
- **Tamamen çevrimdışı.** Uygulama **internet izni istemez**; tek izin mikrofondur. Hiçbir veri cihazdan çıkmaz.
- **Taraf tutmaz.** Akort ve perde sistemi tartışmalarında (AEU, 53 koma, 12 eşit ses) doğru tek bir sistem iddiası yoktur; referansı kullanıcı seçer.
- **Profesyonel doğruluk.** Ayar hassasiyeti ve kararlılık, ücretli akort uygulamalarıyla yarışacak düzeyde olmalı (§10).

---

# 2. Kullanıcılar ve kullanım anları

| Kim | Nerede | Ne ister |
|---|---|---|
| Müzik öğretmeni (ortaokul, lise, güzel sanatlar lisesi) | Sınıf, 20–30 öğrenci, karışık çalgı | Herkesi hızla aynı akorda buluşturmak |
| Halk Eğitim Merkezi bağlama / ud hocası | Kurs sınıfı | Öğrencinin evde yanlış akortla çalışmasını önlemek |
| Öğrenci | Ev, tek başına | Hızlı, anlaşılır akort; perdesini doğru basıp basmadığını görmek |
| Topluluk / koro çalgıcısı | Prova, sahne | Gürültülü ortamda kararlı okuma, karanlıkta okunur ekran |

**Sosyolojik not:** Karışık çalgılı bir sınıfta asıl sorun tek tek akort değil, **ortak bir referansta buluşmaktır**. Bugün bu iş, öğretmenin her öğrencinin çalgısını eline alıp kulakla düzeltmesiyle yapılıyor ve dersin ilk 10–15 dakikasını yiyor. NexTone'un sınıf modu (§8) bu süreyi kısaltmayı hedefler. Ayrıca öğrenci evde yalnızken hocanın kulağı yanında değildir; doğru referans, yanlış alışkanlığın baştan yerleşmesini önler.

---

# 3. Çalgı kapsamı

## 3.1 Faz 1 — akort profilleri

**Türk çalgıları**

| Çalgı | Faz 1 | Not |
|---|---|---|
| Bağlama (kısa sap) | ✓ | Düzen seçimi + karar sesi (§5) |
| Bağlama (uzun sap) | ✓ | Aynı düzenler, farklı ses alanı |
| Cura | ✓ | Bağlama ailesi |
| Divan sazı | ✓ | Bağlama ailesi |
| Ud (Türk) | ✓ | Ahenk seçimi (§5) |
| Ud (Arap) | ✓ | |
| Tanbur | ✓ | [DOĞRULANACAK] |
| Klasik kemençe | ✓ | [DOĞRULANACAK] |
| Karadeniz kemençesi | ✓ | [DOĞRULANACAK] |
| Cümbüş | ✓ | Ud ve mandolin tipleri |
| Kanun | Serbest mod | Mandal düzenleri makama göre değişir; Faz 2'de ayrı profil |
| Ney | Serbest mod | Akort edilmez; ahenk referansıyla perde kontrolü |

**Batı çalgıları**

| Çalgı | Faz 1 |
|---|---|
| Gitar (klasik / akustik / elektro) | ✓ |
| Bas gitar (4 ve 5 tel) | ✓ |
| Keman | ✓ |
| Viyola | ✓ |
| Viyolonsel | ✓ |
| Kontrbas | ✓ |
| Mandolin | ✓ |
| Ukulele | ✓ |

**Üflemeliler ve diğerleri (serbest mod + transpozisyon)**

| Çalgı | Transpozisyon |
|---|---|
| Blok flüt, yan flüt, melodika, piyano | Yok (Do) |
| Si♭ klarnet, trompet, soprano ve tenor saksafon | Si♭ |
| Mi♭ alto ve bariton saksafon | Mi♭ |
| Sol klarnet (Türk müziğinde yaygın) | Sol |
| Korno | Fa |

Blok flüt Türkiye'deki okullarda en yaygın çalgı olduğu için serbest modda ayrıca öne çıkarılır.

## 3.2 Serbest mod (kromatik)

Çalgı seçmeden her sesi okur. En yakın perdeyi gösterir:

- Batı adıyla: "La4, +6 cent"
- Makam adıyla: "Dügâh, +1 koma"

Transpozisyon ve ahenk seçimi serbest modda da geçerlidir.

---

# 4. Akort verileri

## 4.1 Batı çalgıları (kesin, La4 = 440 Hz)

Frekanslar 12 eşit ses sistemine göre hesaplanmıştır.

| Çalgı | Teller (pesten tize) |
|---|---|
| Gitar (standart) | Mi2 82,41 · La2 110,00 · Re3 146,83 · Sol3 196,00 · Si3 246,94 · Mi4 329,63 |
| Gitar (Drop D) | Re2 73,42 · La2 · Re3 · Sol3 · Si3 · Mi4 |
| Gitar (yarım ton pes) | Tüm teller −1 yarım ton |
| Bas gitar (4) | Mi1 41,20 · La1 55,00 · Re2 73,42 · Sol2 98,00 |
| Bas gitar (5) | Si0 30,87 · Mi1 · La1 · Re2 · Sol2 |
| Keman / Mandolin | Sol3 196,00 · Re4 293,66 · La4 440,00 · Mi5 659,26 |
| Viyola | Do3 130,81 · Sol3 · Re4 · La4 |
| Viyolonsel | Do2 65,41 · Sol2 98,00 · Re3 146,83 · La3 220,00 |
| Kontrbas | Mi1 · La1 · Re2 · Sol2 (bas gitarla aynı) |
| Ukulele (GCEA, re-entrant) | Sol4 392,00 · Do4 261,63 · Mi4 329,63 · La4 440,00 |

Gitar için ek düzenler (DADGAD, Open G, Open D vb.) profil dosyasında tanımlanır.

## 4.2 Bağlama düzenleri (yazılı notaya göre)

Bağlama **transpoze eden** bir çalgıdır: nota üzerinde La yazılan ses, çalgının boyuna ve icracının tercihine göre farklı bir gerçek seste çalınır. Bu yüzden düzen bilgisi yazılı nota olarak, gerçek ses ise "karar sesi" ayarıyla (§5) tutulur.

| Düzen | Alt teller | Orta teller | Üst teller | Durum |
|---|---|---|---|---|
| Bozuk düzen | La | Re | Sol | Kaynaklarda tutarlı |
| Bağlama düzeni (Aşık Veysel düzeni) | Re | Sol | La | Kaynaklarda tutarlı |
| Misket düzeni | — | — | — | [DOĞRULANACAK] |
| Kara düzen | — | — | — | [DOĞRULANACAK] |
| Müstezat düzeni | — | — | — | [DOĞRULANACAK] |
| Abdal düzeni | — | — | — | [DOĞRULANACAK] |
| Diğer yöresel düzenler | — | — | — | Öğretmenlerden toplanacak |

Bağlama düzeninde karar sesi için en yaygın tercihin **Do** olduğu, yani gerçek seslerin pesten tize **Fa · Si♭ · Do** çıktığı belirtilir. Re ve Mi kararlı kullanımlar da vardır.

Kaynaklar: [Flüt Notaları – Bağlama](https://flutnotalari.com/baglama/), [Ekşi Sözlük – bağlama düzeni](https://eksisozluk.com/baglama-duzeni--104155), [Erturgut Sanat Merkezi](https://erturgutsanatmerkezi.com/baglamada-duzen-cesitleri/), [İTÜ TMDK – Bağlamada Bozuk Düzeni](https://Tmdk.itu.edu.tr/docs/librariesprovider4/kitap-icerik/baglamada-bozuk-duzeni.pdf).

## 4.3 Ud

| Ud | Teller (pesten tize, gerçek ses) | Durum |
|---|---|---|
| Türk udu, bolahenk | Do#2 · Fa#2 · Si2 · Mi3 · La3 · Re4 (en pes iki tel ¼ ton tiz) | [DOĞRULANACAK] |
| Arap udu (eski düzen) | Re2 · Sol2 · La2 · Re3 · Sol3 · Do4 | Kaynakta |
| Arap udu (modern) | Fa2 · La2 · Re3 · Sol3 · Do4 · Fa4 | Kaynakta |

Kaynak: [Wikipedia – Oud](https://en.wikipedia.org/wiki/Oud). Türk ud akordunun yazılı perde adlarıyla (yegâh, hüseyni aşiran, dügâh, neva, gerdaniye) karşılığı ve kursta en çok kullanılan ahenk öğretmen tarafından onaylanmalıdır.

## 4.4 Tanbur, kemençe, cümbüş, cura, divan

Bu çalgıların değerleri [DOĞRULANACAK]. Faz 1 iskeleti profil dosyasında boş şablon olarak hazırlanır, değerler öğretmen onayıyla doldurulur. Değerler gelene kadar bu çalgılar serbest modda kullanılabilir.

---

# 5. Karar sesi ve ahenk modeli

Türk müziğinde "kâğıtta yazan" ile "kulağa gelen" farklıdır. NexTone bu farkı tek bir kavramla yönetir: **transpozisyon (yarım ton cinsinden kayma)**.

```text
gerçek ses = yazılı nota + transpozisyon
```

Kullanıcıya bu sayı gösterilmez; çalgıya uygun bir dille sunulur:

| Çalgı ailesi | Kullanıcının gördüğü seçim |
|---|---|
| Bağlama ailesi | "Karar sesi: Do / Re / Mi / La …" |
| Ud, tanbur, ney, kemençe | "Ahenk: Bolahenk / Süpürde / Kız / Mansur / Şah …" |
| Üflemeliler | "Si♭ / Mi♭ / Sol / Fa" |

**Ahenk listesi:** Ney ahenkleri yarım ton aralıklarla dizilir: Bolahenk, Davud, Şah, Mansur, Kız, Yıldız, Müstahsen, Süpürde ve aralarındaki "mabeyn" ahenkler ([Wikipedia – Turkish ney](https://en.wikipedia.org/wiki/Turkish_ney)).

**[DOĞRULANACAK] Bolahenk'in mutlak yeri:** Kaynaklar, bolahenkte yazılı Rast perdesinin hangi gerçek sese denk geldiği konusunda farklı değerler veriyor. Ney tablosu ile ud akort tablosu birbirini tutmuyor. Bu tek değer kesinleşince bütün ahenkler ondan türetilir. Değer profil dosyasında tek bir satırdır; yanlışsa kod değişmeden düzeltilir.

---

# 6. Perde sistemleri

| Sistem | Tanım | Kullanım |
|---|---|---|
| **12 eşit ses** | Oktav 12 eşit yarım ton (100 cent) | Batı çalgıları, varsayılan |
| **Arel-Ezgi-Uzdilek (AEU)** | Pisagor oranlarından türeyen 24 perde | Konservatuvar nazariyatı |
| **53 koma (Holder)** | Oktav 53 eşit koma, 1 koma ≈ 22,64 cent | Türk müziği eğitiminde yaygın pratik hesap |
| **Kullanıcı tanımlı** | Perde başına cent değeri | Hocanın kendi kulağı / yöresel perdeler (Faz 3'te kayıttan üretilebilir) |

AEU ve 53 koma çoğu perdede birbirine çok yakın ama aynı değildir. AEU'da koma Pisagor koması (≈ 23,46 cent) temellidir; 53 koma sistemi bunun eşit bölmeli bir yaklaşımıdır. İkisi ayrı sistem olarak sunulur.

53 koma sistemindeki temel aralıklar: 1 koma 22,64 · 4 koma 90,57 · 5 koma 113,21 · 8 koma 181,13 · 9 koma (tanini) 203,77 cent.

**Gösterim kuralı:** Okuma her zaman "en yakın perde + sapma" şeklindedir. Sapma Batı modunda cent, makam modunda koma ve cent olarak gösterilir.

---

# 7. Ekranlar ve kullanım

## 7.1 Ana ekran: akort

```text
┌─────────────────────────────┐
│  Bağlama · Bozuk · Karar Do │  ← dokununca çalgı/düzen seçimi
│                             │
│           DÜGÂH             │  ← büyük perde adı
│        La · +1 koma         │  ← ikincil okuma
│                             │
│   ◄────────●────────►       │  ← ibre, ±50 cent
│        ▲ yeşil bölge        │
│                             │
│  [Alt La] [Orta Re] [Üst Sol]│ ← teller; otomatik algılanan parlar
│                             │
│   ♪ Referans sesi çal       │
└─────────────────────────────┘
```

- **Yeşil bölge:** ±3 cent (ayarlanabilir). Tel bu bölgede 1 saniye kalınca "tamam" işareti ve hafif titreşim.
- **Otomatik tel tanıma** varsayılan; tele dokununca elle seçime geçer.
- **Referans sesi:** Seçili telin sesini çalar, kulakla akort için.
- **Büyük yazı, tek el kullanım.** Telefon nota sehpasında, kol mesafesindedir.
- **Açık ve koyu tema.** Sınıfta güneşli pencere kenarında açık tema, sahnede koyu tema.
- **Ekran kapanmaz** (akort sırasında ekran açık kalır).

## 7.2 Çalgı seçimi

İki seviye: çalgı → düzen/ahenk. Son kullanılan 3 çalgı en üstte. "Favori çalgılarım" listesi.

## 7.3 Ayarlar

- Referans frekansı: La4 = 415–466 Hz (varsayılan 440)
- Okuma dili: Batı / Makam / İkisi birden
- Perde sistemi (§6)
- Yeşil bölge genişliği
- Arayüz dili: Türkçe (Faz 1), İngilizce (sonra)

---

# 8. Sınıf modu

Öğretmen dersin başında ortak referansı belirler:

```text
Sınıf modu
  Bu derste:  Karar sesi Re  ·  La4 = 440 Hz
  ↓
NexTone her çalgı için tel hedeflerini hesaplar:

  Bağlama (bozuk düzen)   →  alt Re3 · orta Sol3 · üst Do4   (örnek, karar sesine göre)
  Gitar                   →  standart akort, değişiklik yok
  Keman                   →  standart akort, değişiklik yok
  Ud                      →  seçilen ahenge göre
```

**Faz 1:** Öğretmen bu ayarı kendi telefonunda yapar ve bir **sınıf kodu** (ör. `RE-440`) oluşturur. Öğrenciler kodu kendi uygulamalarına girer; uygulama aynı ayara geçer. İnternet ve bağlantı gerekmez; kod tahtaya yazılabilir ya da sesli söylenebilir. Kod, ayarın kısaltılmış metinsel karşılığıdır.

**Faz 2 (araştırma):** Öğretmen telefonunun bir referans ton çalıp öğrenci telefonlarının bunu dinleyerek ayara kendiliğinden geçmesi. Ağ veya izin gerektirmez.

---

# 9. Teknik mimari

## 9.1 Teknik tercih ve gerekçesi

**Kotlin + Jetpack Compose, native Android.**

| Seçenek | Karar | Gerekçe |
|---|---|---|
| **Kotlin + Compose (native)** | **Seçildi** | Gerçek zamanlı ses için Android'de en kararlı ve düşük gecikmeli yol; APK küçük (hedef < 8 MB); eski telefonlarda da akıcı |
| Flutter | Elendi | Ses girişi yine native köprü ister; APK büyük |
| Web/PWA | Elendi | Mikrofon gecikmesi ve tarayıcı farklılıkları; "APK atıp kurma" hedefiyle uyumsuz |
| Go (Nex ailesinin masaüstü yığını) | Elendi | Android'de ses ve arayüz için uygun değil |

**Bilinen sınır:** iPhone kullanan öğretmenler APK kuramaz. [AÇIK KARAR] iOS ileride gerekirse SwiftUI ile ayrı bir uygulama olarak ele alınır; DSP çekirdeği ve profil dosyaları ortak tasarlandığı için taşıma maliyeti düşük tutulur.

## 9.2 Platform hedefi

- **minSdk 24 (Android 7.0):** Okullarda ve öğrencilerde eski telefon yaygın.
- **targetSdk:** güncel kararlı sürüm.
- **İzinler:** yalnızca `RECORD_AUDIO`. `INTERNET` izni **bilerek yok**.

## 9.3 Ses işleme (DSP) çekirdeği

```text
Mikrofon (AudioRecord, 48 kHz, mono, float)
  ↓
Gürültü kapısı (sessizlikte okuma yok, ibre donar)
  ↓
Perde tespiti: McLeod Pitch Method (MPM) — birincil
               YIN — ikincil / doğrulama
  ↓
Oktav hatası düzeltme (harmonik denetimi)
  ↓
Yumuşatma (medyan + üstel ortalama; ibre titremez)
  ↓
Perde eşleme (seçili sisteme göre en yakın perde + sapma)
  ↓
Arayüz (60 fps ibre)
```

- **Pencere boyu çalgıya göre değişir:** En pes hedef 5 telli basın Si0'ı (30,87 Hz). İki periyot için 48 kHz'de ~3.110 örnek gerekir; bu yüzden pes çalgılarda 4.096, tiz çalgılarda 2.048 örnek pencere kullanılır. Gecikme ile pes ses doğruluğu arasındaki denge profil başına ayarlanır.
- **Neden MPM:** Akort uygulamalarında yaygın kullanılan, tellilerde ve sürtme çalgılarda kararlı, hesaplama açısından hafif bir yöntem. YIN ikinci görüş olarak oktav hatalarını yakalamaya yardım eder.
- **Kütüphane kullanılmaz:** Yaygın Java ses kütüphanesi TarsosDSP GPL lisanslıdır; lisans karmaşası yaşamamak ve kodu tam denetlemek için MPM ve YIN sıfırdan yazılır. İkisi de iyi belgelenmiş, kısa algoritmalardır.
- **Otomatik tel tanıma:** Algılanan frekans, seçili düzendeki açık tel hedeflerinin her birine cent olarak uzaklığıyla karşılaştırılır; en yakın tel seçilir. Kararsız bölgede (iki tele eşit uzaklık) son seçili tel korunur (histerezis).
- **Referans sesi üretimi:** Sinüs + birkaç harmonik (sadece sinüs pes seslerde telefon hoparlöründen duyulmaz).

## 9.4 Profil dosyası

Tüm çalgı, düzen, ahenk ve perde sistemi verisi kodun dışında, `assets/profiles/*.json` dosyalarında durur.

```json
{
  "id": "baglama-kisa-sap",
  "ad": "Bağlama (kısa sap)",
  "aile": "baglama",
  "transpozisyonAdi": "karar",
  "varsayilanKarar": "Do",
  "pencere": 4096,
  "duzenler": [
    {
      "id": "bozuk",
      "ad": "Bozuk düzen",
      "teller": [
        { "grup": "Alt",  "yazili": "La4" },
        { "grup": "Orta", "yazili": "Re5" },
        { "grup": "Üst",  "yazili": "Sol4" }
      ],
      "kaynak": "flutnotalari.com/baglama",
      "dogrulandi": false
    }
  ]
}
```

> **[DOĞRULANACAK]** Yukarıdaki örnekte yazılı notaların **oktav numaraları** temsilidir; bağlama yazımında hangi oktavın kullanıldığı ve gerçek sese nasıl çevrildiği öğretmenle kesinleştirilecektir.

- `dogrulandi: false` olan düzenler arayüzde küçük bir "doğrulanmadı" işaretiyle gösterilir.
- Yeni çalgı eklemek **kod değişikliği gerektirmez**; yalnızca JSON dosyası eklenir.
- Profil şeması `docs/profil-semasi.md` ile belgelenir; her derlemede şemaya uymayan profil derlemeyi durdurur.

## 9.5 Proje yapısı

```text
nextone/
├── app/                      # Android uygulaması (Compose arayüzü)
│   └── src/main/assets/profiles/   # çalgı profilleri (JSON)
├── dsp/                      # saf Kotlin/JVM modülü: MPM, YIN, yumuşatma, perde eşleme
│                             # Android'den bağımsız; bilgisayarda test edilebilir
├── tuning/                   # perde sistemleri, transpozisyon, sınıf kodu
├── tests/
│   ├── synthetic/            # üretilmiş sinyallerle doğruluk testleri
│   └── recordings/           # gerçek çalgı kayıtları (öğretmenlerden)
├── docs/
│   ├── profil-semasi.md
│   └── kararlar/             # kapanan her açık karar için kısa not
└── .github/workflows/        # otomatik derleme (§12)
```

`dsp` ve `tuning` modüllerinin Android'den bağımsız olması bilinçli bir tercih: hem test edilebilirlik sağlar hem de ileride iOS veya masaüstü sürümünde aynı mantığın yeniden kullanılmasını kolaylaştırır.

---

# 10. Doğruluk hedefleri ve test

| Ölçüt | Hedef |
|---|---|
| Saf sinüste doğruluk | ±0,5 cent |
| Gerçek tel kaydında okuma kararlılığı | ±2 cent titreme |
| İlk okuma süresi (tel çalındıktan sonra) | < 150 ms |
| En pes desteklenen ses | Si0 (30,87 Hz) |
| En tiz desteklenen ses | ~Do7 (2.093 Hz) |
| Düşük cihazda (2 GB RAM) ibre akıcılığı | 60 fps |

**Test katmanları:**

1. **Sentetik testler (her derlemede otomatik):** Bilinen frekansta sinüs, harmonikli testere/kare dalga, gürültü eklenmiş sinyaller. Her biri için algılanan değer ±0,5 cent içinde olmalı. Oktav hatası testleri: güçlü 2. harmoniği olan sinyalde temel frekans bulunmalı.
2. **Gerçek kayıt testleri:** Öğretmen arkadaşlardan her çalgının açık tellerinin kısa kayıtları (telefonla, WhatsApp sesli mesaj kalitesi yeterli). Bu kayıtlar test setine eklenir.
3. **Cihaz testi:** En az bir eski (Android 7–9) ve bir yeni telefonda elle deneme.

---

# 11. Tasarım dili

- Nex ailesinin koyu, sade yaklaşımı; ancak NexTone **açık temayı da birinci sınıf** destekler (sınıf ortamı).
- İbrenin yeşil bölgesi tek parlak öğedir; geri kalan arayüz sakindir.
- Perde adları Türkçe karakterlerle doğru yazılır: Dügâh, Çârgâh, Hüseynî Aşîrân. Uzatma işaretli yazım ayarla kapatılabilir.
- Renk tek başına bilgi taşımaz: "tamam" durumu renk + simge + titreşimle verilir (renk körlüğü).
- Simge: Nex ailesine uygun, akort ibresi ya da ses dalgası temalı; [AÇIK KARAR] ayrı bir tasarım turunda.

---

# 12. Derleme ve dağıtım

## 12.1 Derleme: GitHub Actions

Geliştirme ortamında Android SDK indirilemediği için APK, GitHub'ın kendi sunucularında derlenir:

```text
Kod → GitHub deposu (eirahoutmoss/nextone, özel)
  ↓
GitHub Actions: testler + imzalı release APK
  ↓
GitHub Release'e "NexTone-v0.1.0.apk" olarak eklenir
  ↓
APK indirilir → WhatsApp ile paylaşılır
```

- Her kontrol noktasında otomatik olarak yeni bir APK oluşur.
- **İmza anahtarı (keystore)** bir kez üretilir ve GitHub'da şifreli "secret" olarak saklanır. **Aynı anahtar kalıcı olarak kullanılmalıdır;** anahtar değişirse kullanıcılar yeni sürümü eskisinin üstüne kuramaz, önce kaldırmaları gerekir. Anahtarın bir yedeği ayrıca güvenli bir yerde (ör. kişisel şifre yöneticisi) tutulur.
- Alternatif: Proje Android Studio ile bilgisayarda da derlenebilir; depo buna uygun standart bir Gradle projesidir.

## 12.2 Dağıtım: WhatsApp

- APK dosyası WhatsApp'tan gönderilir; alıcı "bilinmeyen kaynaklardan yükleme" iznini bir kez verir.
- Uygulamanın internet izni olmadığı için **otomatik güncelleme yoktur.** Yeni sürüm yine WhatsApp'tan gönderilir; aynı imzayla üstüne kurulur, ayarlar korunur.
- Uygulama içinde "Hakkında" ekranında sürüm numarası ve tarih görünür; kimin hangi sürümde olduğu kolayca anlaşılır.

## 12.3 Google geliştirici doğrulaması (2027)

Google, dışarıdan yüklenen uygulamalar için geliştirici doğrulaması getiriyor ([Android Authority](https://www.androidauthority.com/android-sideloading-changes-timeline-3679204/)):

- 30 Eylül 2026'dan itibaren Brezilya, Endonezya, Singapur ve Tayland'da yürürlükte.
- Türkiye dahil diğer ülkelerde **2027'de** başlıyor.
- **Ücretsiz hobi/öğrenci hesabı:** kimlik belgesi ve ücret olmadan **20 cihaza kadar** dağıtım. Öğretmen çevresi için uygun.
- Doğrulanmamış uygulamalar yine kurulabilir; ancak 24 saatlik bekleme süresi ve ek adımlarla.

**Plan:** 2027 öncesinde hobi hesabı açılır ve NexTone'un paket adı (`com.eirahoutmoss.nextone` veya benzeri) bu hesaba kaydedilir. 20 cihaz sınırı aşılırsa yeniden değerlendirilir.

---

# 13. Geri bildirim

- Anket veya form yok. Öğretmenler WhatsApp'tan **sesli not ya da kısa mesajla** dönüş yapar: "Kemençede üst tel hep bir oktav aşağı okuyor", "Misket düzeni bizde şöyle".
- Her sürüm notu kısa ve konuşma dilinde: "Bu sürümde: tanbur eklendi, bas gitarda ibre artık titremiyor."
- Değer düzeltmeleri doğrudan profil dosyasına işlenir ve `dogrulandi: true` yapılır; onaylayan kişinin adı (izin verirse) "Teşekkürler" ekranına eklenir.

---

# 14. Yol haritası

## Faz 1 — Akort (ilk APK)

- Batı çalgıları (§4.1) tam
- Bağlama: bozuk düzen + bağlama düzeni, karar sesi seçimi
- Ud (Türk/Arap), ahenk seçimi
- Doğrulanan diğer Türk çalgıları (öğretmen dönüşüne göre)
- Serbest mod + transpozisyon (üflemeliler, blok flüt)
- Batı / makam okuma, 12 eşit ses + 53 koma + AEU
- Referans sesi
- Sınıf kodu (§8)
- Açık/koyu tema, Türkçe arayüz

## Faz 2 — Perde aynası

- Çalarken sesin makam merdiveninde akması (canlı perde izi)
- Seyir haritası (çalınan bölümün zaman çizelgesi)
- Kanun profili (makama göre mandal hedefleri)
- Ney: ahenge göre perde kontrolü
- Sınıf modunda ton ile otomatik ayar (araştırma)
- İngilizce arayüz

## Faz 3 — Meşk

- Hoca kaydından "kullanıcı tanımlı perde sistemi" üretme
- Ödev: hoca bir cümle kaydeder, öğrenci kendi kaydını karşılaştırır (dosya alışverişi WhatsApp üzerinden; sunucu yok)
- Perde bağı hesaplayıcı (bağlama/tanbur için tel boyuna göre perde yerleri, mm)
- İcraya dayalı kulak eğitimi görevleri

---

# 15. Kontrol noktaları

Geliştirme aşağıdaki noktalarda durur ve onay bekler:

| # | Kontrol noktası | Çıktı |
|---|---|---|
| **K1** | Bu plan + açık kararların kapanması | Onaylı plan |
| **K2** | DSP çekirdeği (MPM, YIN, perde eşleme) + sentetik testler | Test raporu: hangi frekansta ne kadar sapma |
| **K3** | İlk çalışan APK: tek ekran akort, gitar + keman + bağlama (bozuk) | `NexTone-v0.1.0.apk` (önce sizin telefonunuzda) |
| **K4** | Tüm Faz 1 çalgıları + serbest mod + makam okuma + referans sesi | `v0.2.0`, öğretmenlere ilk gönderim |
| **K5** | Öğretmen geri bildirimleri işlenmiş, sınıf kodu, temalar | `v1.0.0` |

Her kontrol noktasında: derleme durumu, test sonuçları, kısa değişiklik listesi ve "telefonda denemeniz gerekenler".

---

# 16. Açık kararlar ve doğrulanacak değerler

## 16.1 Açık kararlar (K1'de kapanmalı)

| # | Karar | Öneri |
|---|---|---|
| 1 | GitHub deposu açılsın mı? (`eirahoutmoss/nextone`, özel) | Evet; APK derlemesinin en pratik yolu |
| 2 | Paket adı | `com.eirahoutmoss.nextone` |
| 3 | iOS ileride düşünülecek mi? | Şimdilik hayır; çekirdek taşınabilir tasarlanır |
| 4 | Simge tasarımı | Ayrı bir tasarım turunda, K3 öncesi |
| 5 | Faz 1'e kesin girecek Türk çalgıları | Bağlama ailesi + ud kesin; tanbur ve kemençeler doğrulama gelirse |

## 16.2 Öğretmenle doğrulanacak değerler

| # | Değer | Bölüm |
|---|---|---|
| 1 | Misket, kara düzen, müstezat, abdal ve diğer bağlama düzenlerinin teller | §4.2 |
| 2 | Bağlama yazımında oktav numaraları ve karar sesine çevrim | §4.2, §9.4 |
| 3 | Türk udu akordu (yazılı perde adları + gerçek ses) | §4.3 |
| 4 | Bolahenk'in mutlak yeri (bütün ahenkler buna bağlı) | §5 |
| 5 | Tanbur, klasik kemençe, Karadeniz kemençesi, cümbüş, cura, divan sazı akortları | §4.4 |
| 6 | Kursta ve okulda en sık kullanılan karar sesleri ve ahenkler (varsayılanlar için) | §5 |

Bu liste öğretmen arkadaşlara gönderilecek kısa bir WhatsApp mesajına dönüştürülebilir.
