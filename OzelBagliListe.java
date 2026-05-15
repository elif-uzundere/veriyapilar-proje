// ozel bagli liste sınıfı, gorevleri tutmak için kullanılır
public class OzelBagliListe {

    // ── İç Sınıf: Dugum ──
    // dugum disaridan erisilemez, sadece OzelBagliListe'nin icinde kullanilir
    private static final class Dugum {
        private final Gorev veri; // dugumun içindeki görev bilgisi
        private Dugum sonraki; // Sonraki dugumun adresi

        Dugum(Gorev veri) { // dugumun tasidigi asil bilgi
            this.veri = veri; //dugumun icine gorev koyuluyor
            this.sonraki = null; // yeni dugumun sonraki kancasini baslangicta bos birakiyoruz
        }
    }


    private Dugum bas;   // İlk düğüm
    private int boyut;   // Eleman sayısı

    /** Boş bir bağlı liste oluşturur. */
    public OzelBagliListe() {
        bas = null;
        boyut = 0;// Başlangıçta liste boş
    }

  
    /**
     * Sonuna yeni bir görev eklediginde listenin sonuna gider.
     *
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü listenin sonuna ulaşmak için tüm elemanlar gezilir.
     *
     * en son vagonu bulmak için tüm listeyi tarar,
     * 
     *  bu yüzden zaman karmaşıklığı O(n) olur.
     */
    public void sonunaEkle(Gorev g) {
        // yeni dugum oluşturuyoruz
        final Dugum yeni = new Dugum(g);
        // eger liste boşsa
        if (bas == null) {
            bas = yeni; // ilk vagon bu olur
        } else {
            // liste boş değilse, son dugumu bulmamız gerekiyor
            // bas dugumu kaybetmemek için 'temp' (gecici pointer) kullanıyoruz
            Dugum temp = bas;
            // sonraki dugum null olana kadar ilerle
            while (temp.sonraki != null) {
                temp = temp.sonraki;
            }
            //en sonki dugumun sonraki pointer'ini yeni duguma bagliyoruz
            temp.sonraki = yeni;
        }
        boyut++; // yeni bir görev ekledik, boyutu arttırıyoruz
    }

    /**
     * Bu metot, ID numarasina bakarak listeden görev silmemizi saglar
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü silinecek elemanı bulmak için tüm liste taranır.
     */
    public Gorev idIleSil(int id) { // eğer liste boşsa silinecek bir şey yok, null döndür
        if (bas == null) return null;

        // Eğer silinecek görev ilk düğümdeyse dugum etiketini ikinci dugum yaparak bas'ı güncelliyoruz
        if (bas.veri.getId() == id) { // silinecek görev ilk düğümdeyse
            Gorev silinen = bas.veri;// silinecek görevi kaydediyoruz
            bas = bas.sonraki; // bas'ı ikinci dugum yaparak ilk dugumu listeden çıkarıyoruz
            boyut--; // boyutu azaltıyoruz
            return silinen; // silinen gorevi döndürüyoruz
        }

        /**
         * eger silinecek gorev ortalardaysa,
         * iki pointer kullanilir: 'onceki' ve 'guncel'.
         * 'guncel' silinecek görevi bulana kadar ilerler,
         * 'onceki' ise onu takip eder.
         * guncel silinecek görevi bulduğunda, 
         * onceki'nin sonraki pointer'ini guncel'in sonraki pointer'ine baglayarak
         *  guncel'i listeden çıkarırız.
         */
        Dugum onceki = bas; 
        Dugum guncel = bas.sonraki;
        while (guncel != null) { // liste sonuna kadar ilerle
            if (guncel.veri.getId() == id) { // silinecek görevi bulduk
                onceki.sonraki = guncel.sonraki; // onceki'nin sonraki pointer'ini guncel'in sonraki pointer'ine baglayarak guncel'i listeden çıkarıyoruz
                boyut--;
                return guncel.veri;
            }
            onceki = guncel; // onceki'yi guncel yaparak ilerliyoruz
            guncel = guncel.sonraki; // guncel'i sonraki dugum yaparak ilerliyoruz
        }
        return null; // silinecek görev bulunamadıysa null döndürüyoruz
    }

    /**
     * Belirtilen ID numarasina göre bütün bağli listeyi tarar
     * 
     *  ve o ID'ye sahip görevi döndürür bulamazsa null döndürür.
     *
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü aranan elemanı bulmak için tüm liste taranır.
     */
    public Gorev idIleBul(int id) { 
        Dugum gecici = bas; 
        while (gecici != null) { // liste sonuna kadar ilerle
            if (gecici.veri.getId() == id) return gecici.veri; // aranan ID'ye sahip görevi bulduk, döndürüyoruz
            gecici = gecici.sonraki; // sonraki dugum yaparak ilerliyoruz
        }
        return null; 
    }

    /**
     * Listenin başındaki görevi döndürür. Liste boşsa null döner.
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece baş düğümüne erişim yapılır.
     *
     */
    public Gorev basinaBak() {
        return (bas == null) ? null : bas.veri; // liste boşsa null, değilse baştaki görevi döndürüyoruz
    }

    /**
     * Tüm listeyi konsola yazdırır.
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü tüm elemanlar gezilir.
     *
     */
    public void listele() {
        if (bas == null) {
            System.out.println("  (Bekleme listesi bos)");
            return;
        }
        Dugum gecici = bas; // listeyi taramak için gecici pointer
        int sira = 1; // görevleri sırayla numaralandırmak için sayaç
        while (gecici != null) { // liste sonuna kadar ilerle
            System.out.println("  " + sira + ". " + gecici.veri); // geçici düğümün içindeki görevi yazdırıyoruz
            gecici = gecici.sonraki; // sonraki düğüm yaparak ilerliyoruz
            sira++; // sırayı artırıyoruz
        }
    }

    /**
     * Listeyi dosyaya kaydetmek için dizi formatina ceviriyoruz
     * Dosyaya kaydetme işleminde kullanılır.
     *
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü tüm elemanlar kopyalanır.
     */
    public Gorev[] diziOlarakAl() {
        Gorev[] dizi = new Gorev[boyut]; // boyut kadar bir dizi oluşturuyoruz
        Dugum gecici = bas;
        int i = 0;
        while (gecici != null) {
            dizi[i++] = gecici.veri;
            gecici = gecici.sonraki;
        }
        return dizi;
    }

    /**
     * Listenin boş olup olmadığına bakar
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece boyut değişkeni kontrol edilir.
     *
     */
    public boolean bosmu() { return boyut == 0; } 

    /**
     * Listede toplam kac gorev olduğunu döndürür
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece boyut değişkeni döndürülür.
     */
    public int getBoyut() { return boyut; }
}
