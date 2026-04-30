
import java.util.Scanner;

/**
 * Görev Planlayıcı Sistemi - Ana Sınıf
 *
 * <p>İçerir:</p>
 * <ul>
 *   <li>3 otomatik test senaryosu (normal, sınır değer, hata durumu)</li>
 *   <li>İnteraktif konsol menüsü (try-catch hata yönetimi)</li>
 *   <li>Aging (yaşlandırma) mekanizması</li>
 *   <li>İstatistik sayacı</li>
 *   <li>Dosyaya kaydet / yükle (Veri Kalıcılığı)</li>
 * </ul>
 */
public class Main {

    // ── Sistem Nesneleri ──────────────────────────────────────────────────────

    /** Bekleyen görevlerin listesi */
    private static OzelBagliListe bekleyenler = new OzelBagliListe();

    /** İşlenecek görevlerin öncelikli kuyruğu */
    private static MinHeap islemKuyrugu = new MinHeap();

    /** Geri alma (Undo) için yığın */
    private static OzelYigin geriAlYigini = new OzelYigin();

    /** Otomatik artan görev ID sayacı */
    private static int idSayaci = 0;

    /** Tamamlanan görev sayısı (istatistik) */
    private static int tamamlananSayisi = 0;

    /** Aging eşiği: bu millisaniyeden uzun bekleyen görevler yükseltilir (30 sn) */
    private static final long AGING_ESIGI_MS = 30_000L;

    // ─────────────────────────────────────────────────────────────────────────
    // TEST SENARYOLARI
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sistemin temel işlevlerini otomatik test eder.
     * Main menüsü açılmadan önce çalışır.
     */
    private static void testleriniCalistir() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          OTOMATİK TEST SENARYOLARI  ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");

        int basarili = 0;
        int basarisiz = 0;

        // ── Test 1: Normal Durum ──────────────────────────────────────────────
        System.out.println("▶ TEST 1: Normal Durum — Ekleme & MinHeap Sıralaması");
        try {
            OzelBagliListe testListe = new OzelBagliListe();
            MinHeap testHeap = new MinHeap();

            Gorev g1 = new Gorev(1, "Rapor Yaz",    3, System.currentTimeMillis());
            Gorev g2 = new Gorev(2, "Sunumu Hazırla",1, System.currentTimeMillis());
            Gorev g3 = new Gorev(3, "E-posta At",   5, System.currentTimeMillis());

            testListe.sonunaEkle(g1);
            testListe.sonunaEkle(g2);
            testListe.sonunaEkle(g3);
            testHeap.ekle(g1);
            testHeap.ekle(g2);
            testHeap.ekle(g3);

            // Heap kökte en küçük öncelik (1) bekleniyor
            boolean kosul1 = testHeap.tepeyeBak().getId() == 2;
            // Liste boyutu 3 olmalı
            boolean kosul2 = testListe.getBoyut() == 3;

            if (kosul1 && kosul2) {
                System.out.println("  ✔ BAŞARILI — Heap kökte Öncelik:1 görevi, Liste boyutu:3\n");
                basarili++;
            } else {
                System.out.println("  ✘ BAŞARISIZ — Beklenen koşullar sağlanmadı\n");
                basarisiz++;
            }
        } catch (Exception e) {
            System.out.println("  ✘ BAŞARISIZ — Beklenmeyen istisna: " + e.getMessage() + "\n");
            basarisiz++;
        }

        // ── Test 2: Sınır Değer ───────────────────────────────────────────────
        System.out.println("▶ TEST 2: Sınır Değer — Öncelik aralığı dışı değerler & boş yapı");
        try {
            // Öncelik 0 → 1'e, öncelik 99 → 5'e kısıtlanmalı
            Gorev asagiFix = new Gorev(10, "Test A", 0,  System.currentTimeMillis());
            Gorev yukariFix = new Gorev(11, "Test B", 99, System.currentTimeMillis());
            boolean sinir1 = asagiFix.getOncelik() == 1;
            boolean sinir2 = yukariFix.getOncelik() == 5;

            // Boş heap & yığından çekme null döndürmeli
            MinHeap bosHeap = new MinHeap();
            OzelYigin bosYigin = new OzelYigin();
            boolean bos1 = bosHeap.enOncelikliyiCek() == null;
            boolean bos2 = bosYigin.cek() == null;

            if (sinir1 && sinir2 && bos1 && bos2) {
                System.out.println("  ✔ BAŞARILI — Sınır değerler düzeltildi, boş yapılar null döndürdü\n");
                basarili++;
            } else {
                System.out.println("  ✘ BAŞARISIZ — Sınır değer koşulları sağlanmadı\n");
                basarisiz++;
            }
        } catch (Exception e) {
            System.out.println("  ✘ BAŞARISIZ — Beklenmeyen istisna: " + e.getMessage() + "\n");
            basarisiz++;
        }

        // ── Test 3: Hata Durumu ───────────────────────────────────────────────
        System.out.println("▶ TEST 3: Hata Durumu — Olmayan ID ile silme & Yığın Geri Al");
        try {
            OzelBagliListe testListe2 = new OzelBagliListe();
            Gorev g = new Gorev(20, "Geçici Görev", 2, System.currentTimeMillis());
            testListe2.sonunaEkle(g);

            // Olmayan ID ile silme → null dönmeli
            Gorev bulunamayan = testListe2.idIleSil(999);
            boolean hata1 = bulunamayan == null;

            // Yığın Geri Al: ekle ve geri al
            OzelYigin yigin = new OzelYigin();
            yigin.it(g);
            Gorev geriAlinan = yigin.cek();
            boolean hata2 = geriAlinan != null && geriAlinan.getId() == 20;
            boolean hata3 = yigin.bosmu();

            if (hata1 && hata2 && hata3) {
                System.out.println("  ✔ BAŞARILI — Olmayan ID null döndürdü, Yığın geri alma doğru çalıştı\n");
                basarili++;
            } else {
                System.out.println("  ✘ BAŞARISIZ — Hata senaryosu koşulları sağlanmadı\n");
                basarisiz++;
            }
        } catch (Exception e) {
            System.out.println("  ✘ BAŞARISIZ — Beklenmeyen istisna: " + e.getMessage() + "\n");
            basarisiz++;
        }

        System.out.println("──────────────────────────────────────────────");
        System.out.println("  Sonuç: " + basarili + "/3 test başarılı, "
                + basarisiz + "/3 test başarısız");
        System.out.println("──────────────────────────────────────────────\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AGING (YAŞLANDIRMA) MEKANİZMASI
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Bekleme listesindeki görevleri tarar;
     * eşik süreyi aşanların önceliğini 1 birim düşürür (BONUS).
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n) tarama + O(log n) heap güncellemesi = O(n log n)</p>
     */
    private static void yaslandirmaUygula() {
        long simdi = System.currentTimeMillis();
        Gorev[] gorevler = bekleyenler.diziOlarakAl();
        int sayac = 0;
        for (Gorev g : gorevler) {
            long bekleme = simdi - g.getEklenmeZamani();
            if (bekleme >= AGING_ESIGI_MS && g.getOncelik() > 1) {
                int eskiOncelik = g.getOncelik();
                g.setOncelik(eskiOncelik - 1);
                islemKuyrugu.oncelikGuncelle(g.getId(), g.getOncelik());
                System.out.printf("  ⬆ Aging: [ID:%d '%s'] öncelik %d → %d%n",
                        g.getId(), g.getAd(), eskiOncelik, g.getOncelik());
                sayac++;
            }
        }
        if (sayac == 0) System.out.println("  (Yaşlandırmayı hak eden görev yok)");
        else System.out.println("  Toplam " + sayac + " görev yükseltildi.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MENÜ YARDIMCILARI
    // ─────────────────────────────────────────────────────────────────────────

    private static void menuYazdir() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      GERÇEK ZAMANLI GÖREV PLANLAYICI ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  1. Yeni Görev Ekle                  ║");
        System.out.println("║  2. Sonraki Görevi İşle (MinHeap)    ║");
        System.out.println("║  3. Son İşlemi Geri Al (Undo)        ║");
        System.out.println("║  4. Bekleyen Görevleri Listele       ║");
        System.out.println("║  5. İşlem Kuyruğunu Listele          ║");
        System.out.println("║  6. Aging Uygula (Öncelik Yükselt)   ║");
        System.out.println("║  7. İstatistikleri Göster            ║");
        System.out.println("║  8. Görev Sil (ID ile)               ║");
        System.out.println("║  0. Çıkış (Kaydet & Kapat)           ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.print("Seçiminiz: ");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MAIN
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
 try {
        System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
    } catch (java.io.UnsupportedEncodingException e) {
        // UTF-8 desteklenmiyorsa sessizce devam et
    }
    
        // 1) Kaydedilmiş görevleri yükle
        idSayaci = DosyaYoneticisi.yukle(bekleyenler, idSayaci);

        // Yüklenen görevleri heap'e de ekle
        Gorev[] yuklenenler = bekleyenler.diziOlarakAl();
        for (Gorev g : yuklenenler) {
            islemKuyrugu.ekle(g);
        }

        // 2) Otomatik test senaryolarını çalıştır
        testleriniCalistir();

        // 3) İnteraktif konsol menüsü
        Scanner scanner = new Scanner(System.in);
        boolean calis = true;

        while (calis) {
            menuYazdir();

            int secim = -1;
            try {
                String girdi = scanner.nextLine().trim();
                secim = Integer.parseInt(girdi);
            } catch (NumberFormatException e) {
                System.out.println("⚠ Geçersiz giriş! Lütfen menüdeki sayıları kullanın.");
                continue;
            }

            switch (secim) {

                // ── 1: Görev Ekle ─────────────────────────────────────────────
                case 1: {
                    System.out.print("Görev adı: ");
                    String ad = scanner.nextLine().trim();
                    if (ad.isEmpty()) {
                        System.out.println("⚠ Görev adı boş olamaz!");
                        break;
                    }

                    int oncelik = -1;
                    while (oncelik < 1 || oncelik > 5) {
                        System.out.print("Öncelik (1=En acil … 5=En düşük): ");
                        try {
                            oncelik = Integer.parseInt(scanner.nextLine().trim());
                            if (oncelik < 1 || oncelik > 5)
                                System.out.println("⚠ 1 ile 5 arasında bir değer giriniz!");
                        } catch (NumberFormatException e) {
                            System.out.println("⚠ Sayısal değer giriniz!");
                        }
                    }

                    idSayaci++;
                    Gorev yeni = new Gorev(idSayaci, ad, oncelik, System.currentTimeMillis());
                    bekleyenler.sonunaEkle(yeni);
                    islemKuyrugu.ekle(yeni);
                    System.out.println("✔ Görev eklendi: " + yeni);
                    break;
                }

                // ── 2: Görevi İşle ───────────────────────────────────────────
                case 2: {
                    Gorev islenen = islemKuyrugu.enOncelikliyiCek();
                    if (islenen == null) {
                        System.out.println("⚠ İşlem kuyruğu boş!");
                    } else {
                        bekleyenler.idIleSil(islenen.getId());
                        geriAlYigini.it(islenen);
                        tamamlananSayisi++;
                        System.out.println("✔ İşlendi: " + islenen);
                        System.out.println("  (Toplam tamamlanan: " + tamamlananSayisi + ")");
                    }
                    break;
                }

                // ── 3: Geri Al (Undo) ────────────────────────────────────────
                case 3: {
                    Gorev geriAl = geriAlYigini.cek();
                    if (geriAl == null) {
                        System.out.println("⚠ Geri alınacak işlem yok!");
                    } else {
                        bekleyenler.sonunaEkle(geriAl);
                        islemKuyrugu.ekle(geriAl);
                        tamamlananSayisi = Math.max(0, tamamlananSayisi - 1);
                        System.out.println("↩ Geri alındı: " + geriAl);
                    }
                    break;
                }

                // ── 4: Bekleyen Listele ───────────────────────────────────────
                case 4: {
                    System.out.println("\n── Bekleyen Görevler (" + bekleyenler.getBoyut() + ") ──");
                    bekleyenler.listele();
                    break;
                }

                // ── 5: İşlem Kuyruğu Listele ─────────────────────────────────
                case 5: {
                    System.out.println("\n── İşlem Kuyruğu (MinHeap) ──");
                    islemKuyrugu.listele();
                    break;
                }

                // ── 6: Aging ─────────────────────────────────────────────────
                case 6: {
                    System.out.println("\n── Yaşlandırma Uygulanıyor ──");
                    yaslandirmaUygula();
                    break;
                }

                // ── 7: İstatistik ────────────────────────────────────────────
                case 7: {
                    System.out.println("\n── İstatistikler ──────────────────");
                    System.out.println("  Bekleyen görev sayısı  : " + bekleyenler.getBoyut());
                    System.out.println("  İşlem kuyruğu boyutu   : " + islemKuyrugu.getBoyut());
                    System.out.println("  Geri alma yığını boyutu: " + geriAlYigini.getBoyut());
                    System.out.println("  Tamamlanan görev sayısı: " + tamamlananSayisi);
                    System.out.println("  Bir sonraki görev       : " +
                            (islemKuyrugu.tepeyeBak() == null ? "(kuyruk boş)" : islemKuyrugu.tepeyeBak()));
                    break;
                }

                // ── 8: Görev Sil ─────────────────────────────────────────────
                case 8: {
                    System.out.print("Silinecek görevin ID'si: ");
                    try {
                        int silId = Integer.parseInt(scanner.nextLine().trim());
                        Gorev silinen = bekleyenler.idIleSil(silId);
                        if (silinen == null) {
                            System.out.println("⚠ Bu ID'ye sahip görev bulunamadı: " + silId);
                        } else {
                            // Heap'ten çıkarmak için heap'i yeniden oluştur (O(n log n))
                            yenidenHeapOlustur();
                            System.out.println("✔ Silindi: " + silinen);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("⚠ Geçerli bir ID sayısı giriniz!");
                    }
                    break;
                }

                // ── 0: Çıkış ─────────────────────────────────────────────────
                case 0: {
                    DosyaYoneticisi.kaydet(bekleyenler);
                    System.out.println("İyi çalışmalar! Sistem kapatıldı.");
                    calis = false;
                    break;
                }

                default: {
                    System.out.println("⚠ Geçersiz seçim! Lütfen menüdeki sayıları kullanın.");
                }
            }
        }

        scanner.close();
    }

    /**
     * Heap'i bekleyen listesindeki görevlerden sıfırdan yeniden oluşturur.
     * Görev silindiğinde heap'i tutarlı tutmak için kullanılır.
     *
     * <p><b>Zaman Karmaşıklığı:</b> O(n log n)</p>
     */
    private static void yenidenHeapOlustur() {
        islemKuyrugu = new MinHeap();
        Gorev[] gorevler = bekleyenler.diziOlarakAl();
        for (Gorev g : gorevler) {
            islemKuyrugu.ekle(g);
        }
    }

}
