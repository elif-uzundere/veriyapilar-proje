/**
 * Görev Planlayıcı Sistemi - Görev Veri Sınıfı
 * Bir göreve ait tüm özellikleri tutar.
 */
public class Gorev {

    /** Görevin benzersiz kimlik numarası */
    private int id;

    /** Görevin adı */
    private String ad;

    /**
     * Öncelik değeri: 1 = En acil, 5 = En düşük.
     * MinHeap küçük değeri yüksek öncelik olarak işler.
     */
    private int oncelik;

    /** Görevin sisteme eklendiği zaman (milisaniye cinsinden) */
    private long eklenmeZamani;

    /**
     * Tüm alanlarla birlikte Gorev nesnesi oluşturur.
     *
     * @param id            Görev kimliği
     * @param ad            Görev adı
     * @param oncelik       Öncelik değeri (1-5 arası)
     * @param eklenmeZamani Eklenme zamanı (System.currentTimeMillis())
     */
    public Gorev(int id, String ad, int oncelik, long eklenmeZamani) {
        this.id = id;
        this.ad = ad;
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
        this.eklenmeZamani = eklenmeZamani;
    }

    // ── Getter / Setter ────────────────────────────────────────────────────────

    public int getId()                    { return id; }
    public String getAd()                 { return ad; }
    public int getOncelik()               { return oncelik; }
    public long getEklenmeZamani()        { return eklenmeZamani; }

    public void setOncelik(int oncelik) {
        this.oncelik = (oncelik < 1) ? 1 : (oncelik > 5) ? 5 : oncelik;
    }

    /** Görevi okunabilir biçimde döndürür. */
    @Override
    public String toString() {
        return String.format("[ID:%d | Öncelik:%d | Ad: %s]", id, oncelik, ad);
    }
}