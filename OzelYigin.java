/**
 * Görev Planlayıcı Sistemi - Özel Yığın (Stack)
 *
 * <p>Hazır koleksiyon kütüphanesi KULLANILMADAN sıfırdan yazılmış
 * dinamik yığın yapısı. "Geri Al" (Undo) mekanizması için
 * tamamlanan veya silinen görevleri saklar.</p>
 *
 * <p>Dahili yapı olarak bağlı liste düğümü kullanılır;
 * bu sayede kapasite sınırı yoktur.</p>
 */
public class OzelYigin {

    // ── İç Düğüm Sınıfı ───────────────────────────────────────────────────────
/** ic ice siniflarda, icteki sinif distaki sinifin elemanlarina 
 * ihtiyac duymuyorsa static tanimlanir
 * bu sayede RAM'de daha az yer kaplar */
    private static class Dugum {
        Gorev veri;
        Dugum alt;   // Bir önceki eleman (alt katman)

        Dugum(Gorev veri, Dugum alt) {
            this.veri = veri; //gorev nesnesi 
            this.alt  = alt; //bir onceki eleman
        }
    }

    // ── Alanlar ───────────────────────────────────────────────────────────────
    // yiginin sadece en ust noktasini aklinda tutar
    private Dugum tepe;   // Yığının en üst elemanı
    private int boyut;    // Eleman sayısı

    /** Boş bir yığın oluşturur. */
    public OzelYigin() {
        tepe  = null;
        boyut = 0;
    }

    // ── Temel Operasyonlar ────────────────────────────────────────────────────

    /**
     * Yığınin en üstune yeni bir görev ekler (push).
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü bağlı liste yapısında yeni düğüm oluşturup tepe referansını güncellemek yeterlidir.
     *
     * @param gorev Yığına itilecek görev
     */
    public void it(Gorev gorev) {

/**once yeni bir dugum yaratir, alt okunu da o anki tepeye baglar 
 * sonra tepeyi yeni dugume esitleyerek yeni gorevi tepe yapar */
        tepe = new Dugum(gorev, tepe);  
        boyut++;
    }

    /**
     * kullanici geri al butonuna bastiginda calisir
     * Yığının tepesindeki görevi çıkarıp döndürür (pop).
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece tepe referansını güncellemek yeterlidir.
     *
     * @return Tepedeki Gorev nesnesi; yığın boşsa null
     */
    public Gorev cek() {
        if (tepe == null) return null; //once yigin bos mu diye bakar
        Gorev geri = tepe.veri; //tepedeki gorevi geri degiskene atar
        tepe = tepe.alt; //tepeyi bir alt elemana kaydirarak ustteki elemani cikarmis oluruz
        boyut--; //eski tepeyi Garbage collector yakalayip siler
        return geri; 
    }

    /**
     * Sadece Yığının tepesine bakar, çıkarmaz (peek).
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece tepe düğümüne erişim yapılır.
     *
     * @return Tepedeki Gorev; yığın boşsa null
     */
    public Gorev tepeyeBak() {
        return (tepe == null) ? null : tepe.veri;
    }

    /**
     * Yığının boş olup olmadığını döndürür.
     *
     * Big-O Notasyonu: O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece boyut değişkeni kontrol edilir.
     *
     * @return true → boş
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Yığındaki eleman sayısını döndürür.
     *
     * <p><b>Big-O Notasyonu:</b> O(1) - Bu işlem sabit zamanda gerçekleşir çünkü sadece boyut değişkeni döndürülür.</p>
     *
     * @return Eleman sayısı
     */
    public int getBoyut() { return boyut; }

    /**
     * Yığının tüm içeriğini (tepeden tabana doğru) konsola yazar.
     *
     * Big-O Notasyonu: O(n) - Bu işlem doğrusal zamanda gerçekleşir çünkü tüm elemanlar gezilir.
     * */

    /**
     * Yığının tüm içeriğini loglama ve hata ayiklama icin
     * 
     *  (tepeden tabana doğru) konsola yazar.
     *
     * Zaman Karmaşıklığı:O(n) 
     * */
    public void listele() {
        if (tepe == null) {
            System.out.println("  (Geri alma yigini bos)");
            return;
        }
        Dugum gecici = tepe;
        int sira = 1;
        // tepe en ustte oldugu icin sira 1'den baslar
        while (gecici != null) {
            System.out.println("  " + sira + ". " + gecici.veri);
            gecici = gecici.alt;
            sira++;
        }
    }
}
