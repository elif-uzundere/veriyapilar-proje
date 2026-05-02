// ozel bagli liste sınıfı, gorevleri tutmak için kullanılır
public class OzelBagliListe {

    // ── İç Sınıf: Dugum ──
    // dugum tek bir vagon. disaridan erisilemez, sadece OzelBagliListe'nin icinde kullanilir
    private static class Dugum {
        Gorev veri; // Vagonun icindeki gorev
        Dugum sonraki; // Vagondaki baglantı kancasi

        Dugum(Gorev veri) {// yeni bir vagon yaparken gorev bilgisini veriyoruz
            this.veri = veri;
            this.sonraki = null; // Yeni vagonun arkası simdilik bos
        }
    }

    // ── Alanlar ───

    private Dugum bas;   // İlk düğüm
    private int boyut;   // Eleman sayısı

    /** Boş bir bağlı liste oluşturur. */
    public OzelBagliListe() {
        bas = null;
        boyut = 0;// Başlangıçta liste boş
    }

  
    public void sonunaEkle(Gorev g) {
        // yeni vagon oluşturuyoruz
        Dugum yeni = new Dugum(g);
        // eger tren hiç yoksa (liste boşsa)
        if (bas == null) {
            bas = yeni; // ilk vagon bu olur
        } else {
            // tren varsa , en son vagonu bulmamız gerekiyor 
            // bas vagonu kaybetmemek için 'temp' kullanıyoruz
            Dugum temp = bas;
            // sonraki kancasi bos olan vagonu bulana kadar ilerle
            while (temp.sonraki != null) {
                temp = temp.sonraki;
            }
            // en sondaki vagonun kancasina yenş vagonu tak
            temp.sonraki = yeni;
        }
        boyut++; // tren bir vagon büyüdü
    }

    /**
     * Bu metot, ID numarasina bakarak listeden görev silmemizi saglar
     */
    public Gorev idIleSil(int id) {
        if (bas == null) return null;

        // Silmek istediğim Id başta mı?
        if (bas.veri.getId() == id) {
            Gorev silinen = bas.veri;
            bas = bas.sonraki;
            boyut--;
            return silinen;
        }

        Dugum onceki = bas;
        Dugum guncel = bas.sonraki;
        while (guncel != null) {
            if (guncel.veri.getId() == id) {
                onceki.sonraki = guncel.sonraki;
                boyut--;
                return guncel.veri;
            }
            onceki = guncel;
            guncel = guncel.sonraki;
        }
        return null; // Bulunamadı
    }

    /**
     * Belirtilen ID numarasina göre bütün bağli listeyi tarar ve o ID'ye sahip görevi döndürür bulamazsa null döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     */
    public Gorev idIleBul(int id) {
        Dugum gecici = bas;
        while (gecici != null) {
            if (gecici.veri.getId() == id) return gecici.veri;
            gecici = gecici.sonraki;
        }
        return null;
    }

    /**
     * Listenin başındaki görevi döndürür. Liste boşsa null döner.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     */
    public Gorev basinaBak() {
        return (bas == null) ? null : bas.veri;
    }

    /**
     * Tüm listeyi konsola yazdırır.
     *
     */
    public void listele() {
        if (bas == null) {
            System.out.println("  (Bekleme listesi bos)");
            return;
        }
        Dugum gecici = bas;
        int sira = 1;
        while (gecici != null) {
            System.out.println("  " + sira + ". " + gecici.veri);
            gecici = gecici.sonraki;
            sira++;
        }
    }

    /**
     * Listeyi dosyaya kaydetmek için dizi formatina ceviriyoruz
     * Dosyaya kaydetme işleminde kullanılır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     */
    public Gorev[] diziOlarakAl() {
        Gorev[] dizi = new Gorev[boyut];
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
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Listede toplam kac gorev olduğunu döndürür
     */
    public int getBoyut() {
         return boyut; }
}
