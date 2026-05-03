/**
 * Görev Planlayıcı Sistemi - Görev Veri Sınıfı
 * Bir göreve ait tüm özellikleri tutar.
 */
public class Gorev {

    /** Görevin benzersiz kimlik numarası */
    private final int id;

    /** Görevin adı */
    private final String ad;

    /**
     * Öncelik değeri: 1 = En acil, 5 = En düşük.
     * MinHeap küçük değeri yüksek öncelik olarak işler.
     */
    private int oncelik;

    /** Görevin sisteme eklendiği zaman (milisaniye cinsinden) */
    private final long eklenmeZamani;

    /** Görevin teslim edilmesi gereken zaman (milisaniye cinsinden) */
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
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
        this.eklenmeZamani = eklenmeZamani;
        this.teslimZamani = teslimZamani;
    }

    // ── Getter / Setter ────────────────────────────────────────────────────────

    public int getId()                    { return id; }
    public String getAd()                 { return ad; }
    public int getOncelik()               { return oncelik; }
    public long getEklenmeZamani()        { return eklenmeZamani; }
    public long getTeslimZamani()         { return teslimZamani; }

    public void setOncelik(int oncelik) {
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
    }

    public void setTeslimZamani(long teslimZamani) {
        this.teslimZamani = teslimZamani;
    }

    /** Görevi okunabilir biçimde döndürür. */
    @Override
    public String toString() {
        return String.format("[ID:%d | Oncelik:%d | Ad: %s]", id, oncelik, ad);
    }
}