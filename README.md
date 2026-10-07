# NexTone

Batı ve Türk çalgıları için makam bilen akort uygulaması (Android). Ticari değildir; internet izni yoktur, yalnızca mikrofonu kullanır.

Proje planı: `NexTone_Proje_Plani.md` (proje sahibinin bilgisayarında, `D:\Nex Ailesi\NexTone`).

## Modüller

| Modül | İçerik | Bağımlılık |
|---|---|---|
| `dsp` | Perde tespiti: MPM (birincil), YIN (onay), FFT, kare takipçisi | Yalnızca Kotlin standart kütüphanesi |
| `tuning` | Nota adları, perde sistemleri (12 eşit ses, Arel-Ezgi-Uzdilek, 53 koma), transpozisyon, otomatik tel tanıma | Yalnızca Kotlin standart kütüphanesi |
| `app` | Android arayüzü (Jetpack Compose) | K3'te eklenecek |

`dsp` ve `tuning` Android'den bağımsızdır; bilgisayarda test edilir ve ileride başka platformlarda yeniden kullanılabilir.

## Temel kararlar

- **Referans:** La4 = Dügâh = kullanıcı ayarı (varsayılan 440 Hz). Makam perdeleri Dügâh'a sabitlenir.
- **Transpozisyon:** gerçek ses = yazılı nota + transpozisyon (cent). Bağlamanın karar sesi, ud/ney ahengi ve Si♭/Mi♭ üflemeliler bu tek değerle ifade edilir.
- **AEU perdeleri** elle yazılmış cent değerleriyle değil, Pisagor oranlarından hesaplanır.
- **Kütüphane yok:** Ses analizi sıfırdan yazıldı (yaygın Java ses kütüphanesi TarsosDSP GPL lisanslıdır).

## Test

```bash
./gradlew test
```

Doğruluk raporu: `dsp/build/reports/nextone/dsp-dogruluk.md`. GitHub Actions her gönderimde raporu iş özetine yazar.

## Doğrulanacak değerler

Pes ve tiz oktavdaki bazı perde adları (Yegâh, Irak, Gevest, Sünbüle vb.) `tuning/.../PitchSystem.kt` içinde `[DOĞRULANACAK]` olarak işaretlidir; bir müzik öğretmeninin onayını bekler.

## Sürüm çıkarma

1. `SURUM` dosyasındaki numarayı artırın (ör. `0.1.0` → `0.2.0`).
2. İsteğe bağlı: `docs/surum-notlari/v0.2.0.md` dosyasına sürüm notlarını yazın.
3. Ana dala gönderin. GitHub Actions testleri koşturur, APK'yı gerçek anahtarla imzalar ve
   bu numaranın sürümü henüz yoksa etiketi ve GitHub sürümünü kendisi oluşturur.

İmza anahtarı GitHub secret'larında durur (`NEXTONE_KEYSTORE_B64`, `NEXTONE_KEYSTORE_PASSWORD`,
`NEXTONE_KEY_ALIAS`). Anahtar yoksa APK geçici deneme imzasıyla derlenir ve sürüm yayınlanmaz.
