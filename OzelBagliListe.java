<<<<<<< HEAD
/**
 * Görev Planlayıcı Sistemi - Özel Bağlı Liste
 *
 * <p>Hazır koleksiyon kütüphanesi KULLANILMADAN sıfırdan yazılmış
 * tek yönlü (singly) bağlı liste. Bekleyen görevleri yönetir.</p>
 */
public class OzelBagliListe {

    // ── İç Düğüm Sınıfı ───────────────────────────────────────────────────────

    /** Bağlı listenin her bir halkasını temsil eden düğüm. */
    private static class Dugum {
        Gorev veri;
        Dugum sonraki;

        Dugum(Gorev veri) {
            this.veri = veri;
            this.sonraki = null;
        }
    }

    // ── Alanlar ───────────────────────────────────────────────────────────────

    private Dugum bas;   // İlk düğüm
    private int boyut;   // Eleman sayısı

    /** Boş bir bağlı liste oluşturur. */
    public OzelBagliListe() {
        bas = null;
        boyut = 0;
    }

    // ── Temel Operasyonlar ────────────────────────────────────────────────────

    /**
     * Listeye yeni bir görevi sonuna ekler.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — listenin sonuna gitmek gerekir.</p>
     *
     * @param gorev Eklenecek görev
     */
    public void sonunaEkle(Gorev gorev) {
        Dugum yeni = new Dugum(gorev);
        if (bas == null) {
            bas = yeni;
        } else {
            Dugum gecici = bas;
            while (gecici.sonraki != null) {
                gecici = gecici.sonraki;
            }
            gecici.sonraki = yeni;
        }
        boyut++;
    }

    /**
     * Belirtilen ID'ye sahip görevi listeden siler.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — en kötü durumda tüm liste taranır.</p>
     *
     * @param id Silinecek görevin kimliği
     * @return Silinen Gorev nesnesi; bulunamazsa null
     */
    public Gorev idIleSil(int id) {
        if (bas == null) return null;

        // Baş düğüm hedef mi?
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
     * Belirtilen ID'ye sahip görevi döndürür (silmez).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @param id Aranacak görev kimliği
     * @return Bulunan Gorev; yoksa null
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
     * Listenin başındaki görevi döndürür (silmez).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Baştaki Gorev; liste boşsa null
     */
    public Gorev basinaBak() {
        return (bas == null) ? null : bas.veri;
    }

    /**
     * Tüm listeyi konsola yazdırır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     */
    public void listele() {
        if (bas == null) {
            System.out.println("  (Bekleme listesi boş)");
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
     * Listedeki tüm görevleri Gorev dizisi olarak döndürür.
     * Dosyaya kaydetme işleminde kullanılır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @return Gorev dizisi
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
     * Listenin boş olup olmadığını kontrol eder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return true → boş, false → dolu
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Listedeki eleman sayısını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Eleman sayısı
     */
    public int getBoyut() { return boyut; }
}
=======
/**
 * Görev Planlayıcı Sistemi - Özel Bağlı Liste
 *
 * <p>Hazır koleksiyon kütüphanesi KULLANILMADAN sıfırdan yazılmış
 * tek yönlü (singly) bağlı liste. Bekleyen görevleri yönetir.</p>
 */
public class OzelBagliListe {

    // ── İç Düğüm Sınıfı ───────────────────────────────────────────────────────

    /** Bağlı listenin her bir halkasını temsil eden düğüm. */
    private static class Dugum {
        Gorev veri;
        Dugum sonraki;

        Dugum(Gorev veri) {
            this.veri = veri;
            this.sonraki = null;
        }
    }

    // ── Alanlar ───────────────────────────────────────────────────────────────

    private Dugum bas;   // İlk düğüm
    private int boyut;   // Eleman sayısı

    /** Boş bir bağlı liste oluşturur. */
    public OzelBagliListe() {
        bas = null;
        boyut = 0;
    }

    // ── Temel Operasyonlar ────────────────────────────────────────────────────

    /**
     * Listeye yeni bir görevi sonuna ekler.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — listenin sonuna gitmek gerekir.</p>
     *
     * @param gorev Eklenecek görev
     */
    public void sonunaEkle(Gorev gorev) {
        Dugum yeni = new Dugum(gorev);
        if (bas == null) {
            bas = yeni;
        } else {
            Dugum gecici = bas;
            while (gecici.sonraki != null) {
                gecici = gecici.sonraki;
            }
            gecici.sonraki = yeni;
        }
        boyut++;
    }

    /**
     * Belirtilen ID'ye sahip görevi listeden siler.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — en kötü durumda tüm liste taranır.</p>
     *
     * @param id Silinecek görevin kimliği
     * @return Silinen Gorev nesnesi; bulunamazsa null
     */
    public Gorev idIleSil(int id) {
        if (bas == null) return null;

        // Baş düğüm hedef mi?
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
     * Belirtilen ID'ye sahip görevi döndürür (silmez).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @param id Aranacak görev kimliği
     * @return Bulunan Gorev; yoksa null
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
     * Listenin başındaki görevi döndürür (silmez).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Baştaki Gorev; liste boşsa null
     */
    public Gorev basinaBak() {
        return (bas == null) ? null : bas.veri;
    }

    /**
     * Tüm listeyi konsola yazdırır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     */
    public void listele() {
        if (bas == null) {
            System.out.println("  (Bekleme listesi boş)");
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
     * Listedeki tüm görevleri Gorev dizisi olarak döndürür.
     * Dosyaya kaydetme işleminde kullanılır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     *
     * @return Gorev dizisi
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
     * Listenin boş olup olmadığını kontrol eder.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return true → boş, false → dolu
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Listedeki eleman sayısını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Eleman sayısı
     */
    public int getBoyut() { return boyut; }
}
>>>>>>> 36e7a2c (proje dosyaları ilk yükleme)
