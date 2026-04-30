<<<<<<< HEAD
import java.io.*;

/**
 * Görev Planlayıcı Sistemi - Dosya Yöneticisi
 *
 * <p>Bekleyen görevleri {@code gorevler.txt} dosyasına kaydeder
 * ve program başladığında yeniden yükler (Veri Kalıcılığı / Bonus).</p>
 *
 * <p>Dosya formatı (CSV benzeri, her satır bir görev):</p>
 * <pre>id,ad,oncelik,eklenmeZamani</pre>
 */
public class DosyaYoneticisi {

    private static final String DOSYA_ADI = "gorevler.txt";

    /**
     * Bağlı listedeki tüm görevleri dosyaya kaydeder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — n: listedeki eleman sayısı</p>
     *
     * @param liste Kaydedilecek görevleri içeren bağlı liste
     */
    public static void kaydet(OzelBagliListe liste) {
        try (BufferedWriter yazar = new BufferedWriter(new FileWriter(DOSYA_ADI))) {
            Gorev[] gorevler = liste.diziOlarakAl();
            for (Gorev g : gorevler) {
                yazar.write(g.getId() + ","
                        + g.getAd().replace(",", "_;_") + ","   // virgülden kaçış
                        + g.getOncelik() + ","
                        + g.getEklenmeZamani());
                yazar.newLine();
            }
            System.out.println("✔ Görevler '" + DOSYA_ADI + "' dosyasına kaydedildi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya kaydı sırasında hata: " + e.getMessage());
        }
    }

    /**
     * Dosyadan görevleri okuyup bağlı listeye yükler.
     * Dosya yoksa sessizce devam eder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @param liste         Görevlerin yükleneceği bağlı liste
     * @param sonIdSayaci   Mevcut en büyük ID; dosyadaki ID'lere göre güncellenir
     * @return Dosyadan okunan son en büyük ID değeri
     */
    public static int yukle(OzelBagliListe liste, int sonIdSayaci) {
        File dosya = new File(DOSYA_ADI);
        if (!dosya.exists()) return sonIdSayaci;

        int maxId = sonIdSayaci;
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosya))) {
            String satir;
            int yuklenen = 0;
            while ((satir = okuyucu.readLine()) != null) {
                satir = satir.trim();
                if (satir.isEmpty()) continue;
                String[] parcalar = satir.split(",", 4);
                if (parcalar.length < 4) continue;
                try {
                    int id             = Integer.parseInt(parcalar[0].trim());
                    String ad          = parcalar[1].trim().replace("_;_", ",");
                    int oncelik        = Integer.parseInt(parcalar[2].trim());
                    long eklenmeZamani = Long.parseLong(parcalar[3].trim());
                    liste.sonunaEkle(new Gorev(id, ad, oncelik, eklenmeZamani));
                    if (id > maxId) maxId = id;
                    yuklenen++;
                } catch (NumberFormatException ex) {
                    System.out.println("⚠ Bozuk satır atlandı: " + satir);
                }
            }
            if (yuklenen > 0)
                System.out.println("✔ " + yuklenen + " görev dosyadan yüklendi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya okuma sırasında hata: " + e.getMessage());
        }
        return maxId;
    }
}
=======
import java.io.*;

/**
 * Görev Planlayıcı Sistemi - Dosya Yöneticisi
 *
 * <p>Bekleyen görevleri {@code gorevler.txt} dosyasına kaydeder
 * ve program başladığında yeniden yükler (Veri Kalıcılığı / Bonus).</p>
 *
 * <p>Dosya formatı (CSV benzeri, her satır bir görev):</p>
 * <pre>id,ad,oncelik,eklenmeZamani</pre>
 */
public class DosyaYoneticisi {

    private static final String DOSYA_ADI = "gorevler.txt";

    /**
     * Bağlı listedeki tüm görevleri dosyaya kaydeder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — n: listedeki eleman sayısı</p>
     *
     * @param liste Kaydedilecek görevleri içeren bağlı liste
     */
    public static void kaydet(OzelBagliListe liste) {
        try (BufferedWriter yazar = new BufferedWriter(new FileWriter(DOSYA_ADI))) {
            Gorev[] gorevler = liste.diziOlarakAl();
            for (Gorev g : gorevler) {
                yazar.write(g.getId() + ","
                        + g.getAd().replace(",", "_;_") + ","   // virgülden kaçış
                        + g.getOncelik() + ","
                        + g.getEklenmeZamani());
                yazar.newLine();
            }
            System.out.println("✔ Görevler '" + DOSYA_ADI + "' dosyasına kaydedildi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya kaydı sırasında hata: " + e.getMessage());
        }
    }

    /**
     * Dosyadan görevleri okuyup bağlı listeye yükler.
     * Dosya yoksa sessizce devam eder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @param liste         Görevlerin yükleneceği bağlı liste
     * @param sonIdSayaci   Mevcut en büyük ID; dosyadaki ID'lere göre güncellenir
     * @return Dosyadan okunan son en büyük ID değeri
     */
    public static int yukle(OzelBagliListe liste, int sonIdSayaci) {
        File dosya = new File(DOSYA_ADI);
        if (!dosya.exists()) return sonIdSayaci;

        int maxId = sonIdSayaci;
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosya))) {
            String satir;
            int yuklenen = 0;
            while ((satir = okuyucu.readLine()) != null) {
                satir = satir.trim();
                if (satir.isEmpty()) continue;
                String[] parcalar = satir.split(",", 4);
                if (parcalar.length < 4) continue;
                try {
                    int id             = Integer.parseInt(parcalar[0].trim());
                    String ad          = parcalar[1].trim().replace("_;_", ",");
                    int oncelik        = Integer.parseInt(parcalar[2].trim());
                    long eklenmeZamani = Long.parseLong(parcalar[3].trim());
                    liste.sonunaEkle(new Gorev(id, ad, oncelik, eklenmeZamani));
                    if (id > maxId) maxId = id;
                    yuklenen++;
                } catch (NumberFormatException ex) {
                    System.out.println("⚠ Bozuk satır atlandı: " + satir);
                }
            }
            if (yuklenen > 0)
                System.out.println("✔ " + yuklenen + " görev dosyadan yüklendi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya okuma sırasında hata: " + e.getMessage());
        }
        return maxId;
    }
}
>>>>>>> 98d6491 (ilk yukleme)
