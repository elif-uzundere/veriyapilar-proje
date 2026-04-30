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

    private static class Dugum {
        Gorev veri;
        Dugum alt;   // Bir önceki eleman (alt katman)

        Dugum(Gorev veri, Dugum alt) {
            this.veri = veri;
            this.alt  = alt;
        }
    }

    // ── Alanlar ───────────────────────────────────────────────────────────────

    private Dugum tepe;   // Yığının en üst elemanı
    private int boyut;    // Eleman sayısı

    /** Boş bir yığın oluşturur. */
    public OzelYigin() {
        tepe  = null;
        boyut = 0;
    }

    // ── Temel Operasyonlar ────────────────────────────────────────────────────

    /**
     * Yığına yeni bir görevi en üste ekler (push).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @param gorev Yığına itilecek görev
     */
    public void it(Gorev gorev) {
        tepe = new Dugum(gorev, tepe);
        boyut++;
    }

    /**
     * Yığının tepesindeki görevi çıkarıp döndürür (pop).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Tepedeki Gorev nesnesi; yığın boşsa null
     */
    public Gorev cek() {
        if (tepe == null) return null;
        Gorev geri = tepe.veri;
        tepe = tepe.alt;
        boyut--;
        return geri;
    }

    /**
     * Yığının tepesine bakar, çıkarmaz (peek).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Tepedeki Gorev; yığın boşsa null
     */
    public Gorev tepeyeBak() {
        return (tepe == null) ? null : tepe.veri;
    }

    /**
     * Yığının boş olup olmadığını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return true → boş
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Yığındaki eleman sayısını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Eleman sayısı
     */
    public int getBoyut() { return boyut; }

    /**
     * Yığının tüm içeriğini (tepeden tabana doğru) konsola yazar.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     */
    public void listele() {
        if (tepe == null) {
            System.out.println("  (Geri alma yığını boş)");
            return;
        }
        Dugum gecici = tepe;
        int sira = 1;
        while (gecici != null) {
            System.out.println("  " + sira + ". " + gecici.veri);
            gecici = gecici.alt;
            sira++;
        }
    }
}
