import java.io.*;

/*
  Dosya Yöneticisi
 Bekleyen görevleri gorevler.csv dosyasına kaydeder ve program başladığında yeniden yükler 
 (Veri Kalıcılığı / Bonus) */
 
public class DosyaYoneticisi {

    private static final String DOSYA_ADI = "gorevler.csv"; // dosya adini sabit (constant) olarak tanımladık

    /*RAM'deki bağlı listeyi alıp Hard Disk'e (CSV formatında) yazar
    Big-O Notasyonu: O(n) — n: listedeki eleman sayısı - Bu işlem doğrusal zamanda gerçekleşir çünkü tüm elemanlar dosyaya yazılır */
 
    public static void kaydet(OzelBagliListe liste) {
        // Dosyaya yazarken BufferedWriter kullanarak verimli bir şekilde yazıyoruz
        try (BufferedWriter yazar = new BufferedWriter(new FileWriter(DOSYA_ADI))) {
            Gorev[] gorevler = liste.diziOlarakAl(); // bağlı listeyi diziye çeviriyoruz, böylece sırayla yazabiliriz
            for (Gorev g : gorevler) { // her görevi tek tek yazıyoruz
                yazar.write(g.getId() + "," // ID'yi yazıyoruz
                        + g.getAd().replace(",", "_;_") + ","   // Adı yazarken virgül varsa sorun çıkarmaması için özel bir karakterle değiştiriyoruz
                        + g.getOncelik() + "," // Önceliği yazıyoruz
                        + g.getEklenmeZamani() + "," // Eklenme zamanını yazıyoruz
                        + g.getTeslimZamani()); // Teslim zamanını yazıyoruz
                yazar.newLine(); // her görevi yeni bir satıra yazıyoruz
            }
            System.out.println("✔ Gorevler '" + DOSYA_ADI + "' dosyasina kaydedildi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya kaydi sirasinda hata: " + e.getMessage());
        }
    }

    /**
      Dosyadan görevleri okuyup bağlı listeye yükler.
      Dosya yoksa sessizce devam eder.
     
      Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü dosyanın tüm satırları okunur ve işlenir.
     
      @param liste         Görevlerin yükleneceği bağlı liste
      @param sonIdSayaci   Mevcut en büyük ID; dosyadaki ID'lere göre güncellenir
      @return Dosyadan okunan son en büyük ID değeri
     */

    public static int yukle(OzelBagliListe liste, int sonIdSayaci) {
        File dosya = new File(DOSYA_ADI);

        // Dosya yoksa yeni görevler eklenirken ID'lerin doğru şekilde devam etmesi için mevcut sonIdSayaci'yı döndürüyoruz
        if (!dosya.exists()) return sonIdSayaci;

        // onceden kalan gorevler varsa yeni gorevin id sini belirlemek icin en buyuk id yi baz aliyoruz
        int maxId = sonIdSayaci;

        
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosya))) {
            String satir;
            int yuklenen = 0;

            // Dosyayı satır satır okuyarak görevleri bağlı listeye ekliyoruz
            while ((satir = okuyucu.readLine()) != null) {
                satir = satir.trim(); // veri temizleme(sanitization) yaparak boşlukları tirasliyoruz (trim)
                if (satir.isEmpty()) continue; // boş satırları atlıyoruz
                String[] parcalar = satir.split(",", 5); // sadece ilk 5 parçaya bölüyoruz, böylece ad kısmında virgül varsa sorun olmaz
                if (parcalar.length < 5) continue; // eksik veri varsa o satırı atlıyoruz
              
              // ID, Ad, Öncelik, Eklenme Zamanı ve Teslim Zamanı bilgilerini sayilara ceviriyoruz
                try {
                    int id             = Integer.parseInt(parcalar[0].trim());
                    String ad          = parcalar[1].trim().replace("_;_", ",");
                    int oncelik        = Integer.parseInt(parcalar[2].trim());
                    long eklenmeZamani = Long.parseLong(parcalar[3].trim());
                    long teslimZamani  = Long.parseLong(parcalar[4].trim());

                    // Yeni bir Gorev nesnesi oluşturup bağlı listenin sonuna(RAM) ekliyoruz
                    liste.sonunaEkle(new Gorev(id, ad, oncelik, eklenmeZamani, teslimZamani));

                    //ayni zamanda okunan idnin diger idlerden buyuk olup olmadigini kontrol ediyoruz
                    if (id > maxId) maxId = id;
                    yuklenen++; 
                }
                // ID, Öncelik veya zaman bilgileri sayıya çevrilemezse o satırı atlıyoruz ve kullanıcıya uyarı veriyoruz
                catch (NumberFormatException ex) {
                    System.out.println("⚠ Bozuk satir atlandi: " + satir);
                }
            }

            
            if (yuklenen > 0)
                System.out.println("✔ " + yuklenen + " gorev dosyadan yuklendi.");
        } catch (IOException e) {
            System.out.println("⚠ Dosya okuma sirasinda hata: " + e.getMessage());
        }
        return maxId; // dosyadan okunan en büyük ID'yi döndürüyoruz, böylece yeni görevler eklenirken bu ID'nin devamı gelir
    }
}
