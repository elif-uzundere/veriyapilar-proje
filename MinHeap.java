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

    private static final int BASLANGIC_KAPASITE = 16;//heap in arka planda kullandığı dizinin başlangıç kapasitesi

    private Gorev[] dizi;    // Heap dizisi
    private int boyut;       // Geçerli eleman sayısı

    /* constructor */
    public MinHeap() {
        dizi  = new Gorev[BASLANGIC_KAPASITE];
        boyut = 0;
    }

    // agac yapısında ebeveyn ve çocuk indekslerini hesaplamak
    // dugumleri baglamak yerine dizi indeksleriyle agac cizilir
    private int ebeveyn(int i)    { return (i - 1) / 2; }
    private int solCocuk(int i)   { return 2 * i + 1; }
    private int sagCocuk(int i)   { return 2 * i + 2; }

    /*yardimci metodlar
    takas dizideki iki elemanin yerini onceliklerini degistirir */
    private void takas(int a, int b) {
        Gorev gecici = dizi[a];
        dizi[a] = dizi[b];
        dizi[b] = gecici;
    }

   /* kapasiteyi ikiye katlar, heap dolduğunda çağrılır (dinamik dizi mantigi)*/
    private void kapasiteyiIkiyeKatla() {
        Gorev[] yeniDizi = new Gorev[dizi.length * 2];
        for (int i = 0; i < boyut; i++) {
            yeniDizi[i] = dizi[i];
        }
        dizi = yeniDizi;
    }
    
    //yukarı kaydır ve aşağı kaydır metodları heap özelliklerini korumak için kullanılır

    /*yukarı kaydir: yeni gorev eklendiginde en sona gider. ancak aciliyeti 
    yuksek olabilir(oncelik numarasi kucuk) .bu durumda yeni gorev 
    ebeveynleriyle karsilastirilir ve gerekirse yukari kaydirilir
     */
    private void yukariKaydir(int i) {
        while (i > 0 && dizi[i].getOncelik() < dizi[ebeveyn(i)].getOncelik()) {
            takas(i, ebeveyn(i));
            i = ebeveyn(i);
        }
    }

    /* aşağı kaydır: en oncelikli gorev cekildiginde kok silinir ve bos kalir.orada son eleman kok olur.
   muhtelemen aciliteyeti dusuktur,  bu durumda yeni kokun cocuklariyla karsilastirilir ve
    gerektigi yere kadar asagi kaydirilir. 
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

  

    /* disariya acik metodlar */
    //gorev gelir dizi doluysa kapasite ikiye katlanir, gorev dizinin sonuna eklenir 
    // ve yukariKaydir ile gercek yerine tirmandirilir bir boyut artirilir
        public void ekle(Gorev gorev) {
        if (boyut == dizi.length) kapasiteyiIkiyeKatla();
        dizi[boyut] = gorev;
        yukariKaydir(boyut);
        boyut++;
    }

    /**tepdeki elemani dizi[0] alir yerine son eleman gelir, boyut azalir ve asagiKaydir
     *  ile gercek yerine indirilir
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

    //AGING 
    /* cok uzun sure bekleyen gorevlerin onceligi artar,
     bu metod belirli bir id ye sahip gorevi bulur ve onceligini gunceller
     * @return Kök Gorev; heap boşsa null
     */
    public Gorev tepeyeBak() {
        return (boyut == 0) ? null : dizi[0];
    }

    /* Heap'in boş olup olmadığını kontrol eder.
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     */
    public boolean bosmu() { return boyut == 0; }

    /**
     * Heap'teki eleman sayısını döndürür.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(1)</p>
     */
    public int getBoyut() { return boyut; }

    public Gorev[] diziOlarakAl() {
        Gorev[] sonuc = new Gorev[boyut];
        for (int i = 0; i < boyut; i++) {
            sonuc[i] = dizi[i];
        }
        return sonuc;
    }

    /* heap'teki tüm görevleri öncelik sırasına göre listeler (en acil ilk).
     * <p><b>Zaman Karmaşıklığı:</b> O(n)</p>
     */
    /* kuyruk bos mu diye bakar bossa islem kuyrugu bos yazar ve return ile metodu bitirir.
    eger doluysa 0. indeksten baslar boyuta kadar devam eder ve icerideki her gorevin toString() metodunu basar.
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
        //1. arama: id'ye sahip gorevi bul
        for (int i = 0; i < boyut; i++) {
            if (dizi[i].getId() == id) { // Görevi bul
                //2. eski degeri hafizaya at
                int eskiOncelik = dizi[i].getOncelik();
                //3. yeni oncelik degerini ata
                dizi[i].setOncelik(yeniOncelik);
                //4. yeni oncelik eski oncelikten daha acil ise yukari kaydir, degilse asagi kaydir
                if (yeniOncelik < eskiOncelik) yukariKaydir(i);
                else asagiKaydir(i);
                return true;//5. islem basariliysa true
            }
        }
        return false;// ID bulunamazsa false
    }
}
