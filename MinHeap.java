/**
 * Görev Planlayıcı Sistemi - Min-Heap (Öncelik Kuyruğu)
 *
 * <p>Hazır koleksiyon kütüphanesi KULLANILMADAN sıfırdan yazılmış
 * dizi tabanlı Min-Heap yapısı. Görevleri öncelik değerine göre
 * sıralar: <b>küçük öncelik numarası = yüksek aciliyet</b>.</p>
 *
 * <p>Heap özellikleri:</p>
 * <ul>
 *   <li>Her düğümün değeri, çocuklarından küçük ya da eşittir.</li>
 *   <li>Kök her zaman en acil görevi tutar.</li>
 * </ul>
 */
public class MinHeap {

    // ── Sabitler ve Alanlar ───────────────────────────────────────────────────

    private static final int BASLANGIC_KAPASITE = 16;

    private Gorev[] dizi;    // Heap dizisi
    private int boyut;       // Geçerli eleman sayısı

    /** Varsayılan kapasitede boş bir MinHeap oluşturur. */
    public MinHeap() {
        dizi  = new Gorev[BASLANGIC_KAPASITE];
        boyut = 0;
    }

    // ── Yardımcı İndeks Hesaplayıcılar ───────────────────────────────────────

    private int ebeveyn(int i)    { return (i - 1) / 2; }
    private int solCocuk(int i)   { return 2 * i + 1; }
    private int sagCocuk(int i)   { return 2 * i + 2; }

    /** İki dizin konumundaki elemanları yer değiştirir. */
    private void takas(int a, int b) {
        Gorev gecici = dizi[a];
        dizi[a] = dizi[b];
        dizi[b] = gecici;
    }

    // ── Kapasite Yönetimi ─────────────────────────────────────────────────────

    /**
     * Dizi dolduğunda kapasiteyi iki katına çıkarır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) — amortize O(1)</p>
     */
    private void kapasiteyiIkiyeKatla() {
        Gorev[] yeniDizi = new Gorev[dizi.length * 2];
        for (int i = 0; i < boyut; i++) {
            yeniDizi[i] = dizi[i];
        }
        dizi = yeniDizi;
    }

    // ── Heap Sıfırlama Operasyonları ──────────────────────────────────────────

    /**
     * Yeni eklenen elemanı yukarı kaydırarak heap özelliğini korur (sift-up).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(log n)</p>
     *
     * @param i Yukarı kaydırılacak elemanın dizin konumu
     */
    private void yukariKaydir(int i) {
        while (i > 0 && dizi[i].getOncelik() < dizi[ebeveyn(i)].getOncelik()) {
            takas(i, ebeveyn(i));
            i = ebeveyn(i);
        }
    }

    /**
     * Tepedeki eleman kaldırıldıktan sonra heap özelliğini korur (sift-down).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(log n)</p>
     *
     * @param i Aşağı kaydırılacak elemanın dizin konumu
     */
    private void asagiKaydir(int i) {
        int enKucuk = i;
        int sol = solCocuk(i);
        int sag = sagCocuk(i);

        if (sol < boyut && dizi[sol].getOncelik() < dizi[enKucuk].getOncelik())
            enKucuk = sol;
        if (sag < boyut && dizi[sag].getOncelik() < dizi[enKucuk].getOncelik())
            enKucuk = sag;

        if (enKucuk != i) {
            takas(i, enKucuk);
            asagiKaydir(enKucuk);
        }
    }

    // ── Genel API ─────────────────────────────────────────────────────────────

    /**
     * Heap'e yeni bir görev ekler.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(log n)</p>
     *
     * @param gorev Eklenecek görev
     */
    public void ekle(Gorev gorev) {
        if (boyut == dizi.length) kapasiteyiIkiyeKatla();
        dizi[boyut] = gorev;
        yukariKaydir(boyut);
        boyut++;
    }

    /**
     * En yüksek öncelikli (en küçük öncelik numaralı) görevi döndürür
     * ve heap'ten çıkarır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(log n)</p>
     *
     * @return En acil Gorev; heap boşsa null
     */
    public Gorev enOncelikliyiCek() {
        if (boyut == 0) return null;
        Gorev kok = dizi[0];
        dizi[0] = dizi[boyut - 1];
        dizi[boyut - 1] = null;
        boyut--;
        if (boyut > 0) asagiKaydir(0);
        return kok;
    }

    /**
     * En yüksek öncelikli göreve bakar, heap'ten çıkarmaz.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     *
     * @return Kök Gorev; heap boşsa null
     */
    public Gorev tepeyeBak() {
        return (boyut == 0) ? null : dizi[0];
    }

    /**
     * Heap boş mu?
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Heap'teki eleman sayısını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     */
    public int getBoyut() { return boyut; }

    /**
     * Heap'teki tüm görevleri konsola yazar (sırasız liste).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     */
    public void listele() {
        if (boyut == 0) {
            System.out.println("  (Islem kuyrugu bos)");
            return;
        }
        for (int i = 0; i < boyut; i++) {
            System.out.println("  " + (i + 1) + ". " + dizi[i]);
        }
    }

    /**
     * Heap'teki belirli bir ID'ye sahip görevin önceliğini günceller.
     * Aging (yaşlandırma) mekanizması için kullanılır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) arama + O(log n) düzeltme = O(n)</p>
     *
     * @param id          Güncellenecek görev kimliği
     * @param yeniOncelik Yeni öncelik değeri
     * @return İşlem başarılıysa true
     */
    public boolean oncelikGuncelle(int id, int yeniOncelik) {
        for (int i = 0; i < boyut; i++) {
            if (dizi[i].getId() == id) {
                int eskiOncelik = dizi[i].getOncelik();
                dizi[i].setOncelik(yeniOncelik);
                if (yeniOncelik < eskiOncelik) yukariKaydir(i);
                else asagiKaydir(i);
                return true;
            }
        }
        return false;
    }
}
