/**
 * Görev Planlayıcı Sistemi - Görev Veri Sınıfı
 * Bir göreve ait tüm özellikleri tutar.
 */
public class Gorev {

    /// Görevin benzersiz kimliği
    private final int id;

    /// Görevin adı 
    private final String ad;

    /**
     * Öncelik değeri: 1 = En acil, 5 = En düşük.
     * MinHeap küçük değeri yüksek öncelik olarak işler.
     */
    private int oncelik;

    // Görevin sisteme eklendiği zaman (milisaniye cinsinden) 
    private final long eklenmeZamani;

    /** Görevin teslim edilmesi gereken zaman (milisaniye cinsinden) */
    // final degil cunku aging ve deadline update edilebilir
    private long teslimZamani;

    /**
     * Tüm alanlarla birlikte Gorev nesnesi oluşturur.
     *
     * @param id            Görev kimliği
     * @param ad            Görev adı
     * @param oncelik       Öncelik değeri (1-5 arası)
     * @param eklenmeZamani Eklenme zamanı (System.currentTimeMillis())
     * @param teslimZamani  Teslim zamanı (milisaniye cinsinden)
     */
    public Gorev(int id, String ad, int oncelik, long eklenmeZamani, long teslimZamani) {
        this.id = id;
        this.ad = ad;

        //ternary operatoru ile oncelik sinirlamasi yapiliyor
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
        this.eklenmeZamani = eklenmeZamani;
        this.teslimZamani = teslimZamani;
    }

    // Getter / Setter ile private olan verilere kontrollu erisim saglaniyor

    // her seyin get metodu var cunku okumak serbest
    // set ise sadece oncelik ve teslim zamani icin var cunku bunlar gorevin yaslanmasi ve deadline update edilmesi icin gerekli olabilir

    public int getId()                    { return id; }
    public String getAd()                 { return ad; }
    public int getOncelik()               { return oncelik; }
    public long getEklenmeZamani()        { return eklenmeZamani; }
    public long getTeslimZamani()         { return teslimZamani; }

    
    public void setOncelik(int oncelik) {
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
    }

    // teslim zamani update edilebilir cunku aging ve deadline update edilebilir
    public void setTeslimZamani(long teslimZamani) {
        this.teslimZamani = teslimZamani;
    }

    // object sinifinin varsayilan yazdirma metodu override ediliyor
    // bu sayede gorev nesnesi yazdirilirken anlamsiz RAM adresleri vermeden direkt bilgileri yazar
    @Override
    public String toString() {
        return String.format("[ID:%d | Oncelik:%d | Ad: %s]", id, oncelik, ad);
    }
}