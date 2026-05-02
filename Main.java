import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class Main {
    // --- Sistem Nesneleri ---
    private static OzelBagliListe bekleyenler = new OzelBagliListe();
    private static MinHeap islemKuyrugu = new MinHeap();
    private static OzelYigin geriAlYigini = new OzelYigin();
    private static int idSayaci = 0;
    private static int tamamlananSayisi = 0;
    private static final long AGING_ESIGI_MS = 30_000L;

    // --- Analiz Verileri (Yeni) ---
    private static List<Long> tamamlanmaSureleri = new ArrayList<>();
    private static List<Gorev> tamamlananGorevler = new ArrayList<>();

    // --- Arayüz Bileşenleri ---
    private static JFrame frame;
    private static JTable tablo;
    private static DefaultTableModel tabloModeli;
    private static JLabel lblBekleyen, lblKuyruk, lblTamamlanan;

    // --- Tasarım Renkleri ---
    private static final Color PRIMARY_COLOR = new Color(37, 99, 235);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color SECONDARY_COLOR = new Color(55, 65, 81);
    private static final Color SIDEBAR_COLOR = new Color(249, 250, 251);
    private static final Color DANGER_COLOR = new Color(220, 38, 38);

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        idSayaci = DosyaYoneticisi.yukle(bekleyenler, idSayaci);
        gecmisiYukle();
        yenidenHeapOlustur();

        SwingUtilities.invokeLater(() -> {
            arayuzuOlustur();
            tabloyuGuncelle();
        });
    }

    private static void arayuzuOlustur() {
        frame = new JFrame("Gerçek Zamanlı Görev Zamanlayıcı");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout());
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                verileriKaydetVeCik();
            }
        });

        // Üst Panel: Kartlar
        JPanel cardContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        cardContainer.setBorder(new EmptyBorder(25, 25, 25, 25));
        cardContainer.setBackground(Color.WHITE);
        lblBekleyen = createStatCard(cardContainer, "Bekleyen", "0", new Color(59, 130, 246));
        lblKuyruk = createStatCard(cardContainer, "Öncelikli", "0", PRIMARY_COLOR);
        lblTamamlanan = createStatCard(cardContainer, "Tamamlanan", "0", SUCCESS_COLOR);
        frame.add(cardContainer, BorderLayout.NORTH);

        // Orta Panel: Tablo
        String[] kolonlar = {"ID", "Görev Adı", "Öncelik", "Teslim Tarihi", "Durum"};
        tabloModeli = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablo = new JTable(tabloModeli);
        tablo.setRowHeight(40);
        
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    setBackground(row == 0 ? new Color(235, 249, 228) : Color.WHITE);
                }
                return this;
            }
        };
        for (int i = 0; i < tablo.getColumnCount(); i++) tablo.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);

        JScrollPane scrollPane = new JScrollPane(tablo);
        scrollPane.getViewport().setBackground(Color.WHITE);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(0, 25, 25, 25));
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.add(scrollPane);
        frame.add(tableWrapper, BorderLayout.CENTER);

        // Sağ Panel: Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(new EmptyBorder(20, 10, 25, 25));

        sidebar.add(createSidebarButton("🔗 Yeni Görev", PRIMARY_COLOR, e -> gorevEkleDialog()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("✅ Görevi Tamamla", SUCCESS_COLOR, e -> gorevIsle()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("📈 Analiz Raporu", SECONDARY_COLOR, e -> raporGoster()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("✅ Tamamlanan Görevler", SECONDARY_COLOR, e -> tamamlananGorevleriGoster()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("🔙 Geri Al", SECONDARY_COLOR, e -> geriAl()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("⏳ Aging Uygula", SECONDARY_COLOR, e -> { yaslandirmaUygula(); tabloyuGuncelle(); }));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("⏰ Deadline Kontrolü", new Color(168, 85, 247), e -> { deadlineKontrolEt(); tabloyuGuncelle(); }));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("🗑️ ID ile Sil", DANGER_COLOR, e -> silmeDialog()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("⚠️ Tüm Bekleyenleri Sil", DANGER_COLOR, e -> tumBekleyenleriSil()));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarButton("💾 Kaydet ve Çık", new Color(31, 41, 55), e -> verileriKaydetVeCik()));

        frame.add(sidebar, BorderLayout.EAST);
        frame.setVisible(true);
    }

    // --- Yeni Fonksiyon: İstatistiksel Raporlama ---
    private static void raporGoster() {
        if (tamamlanmaSureleri.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Henüz analiz edilecek tamamlanmış görev yok!", "Gerçek Zamanlı Görev Zamanlayıcı - Analiz Raporu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        long toplam = 0, enKisa = Long.MAX_VALUE, enUzun = 0;
        for (long sure : tamamlanmaSureleri) {
            toplam += sure;
            if (sure < enKisa) enKisa = sure;
            if (sure > enUzun) enUzun = sure;
        }
        double ortalama = (double) toplam / tamamlanmaSureleri.size() / 1000.0;

        int enKisaIndex = 0;
        int enUzunIndex = 0;
        for (int i = 0; i < tamamlanmaSureleri.size(); i++) {
            long sure = tamamlanmaSureleri.get(i);
            if (sure < tamamlanmaSureleri.get(enKisaIndex)) enKisaIndex = i;
            if (sure > tamamlanmaSureleri.get(enUzunIndex)) enUzunIndex = i;
        }

        String enKisaGorev = tamamlananGorevler.get(enKisaIndex).getAd();
        String enUzunGorev = tamamlananGorevler.get(enUzunIndex).getAd();

        String mesaj = String.format(
            "GÖREV ANALİZ RAPORU\n" +
            "-----------------------------------\n" +
            "Toplam Tamamlanan: %d\n" +
            "Ortalama İşlem Süresi: %.2f saniye\n" +
            "En Kısa İşlem: %s (%.2f saniye)\n" +
            "En Uzun İşlem: %s (%.2f saniye)\n" +
            "-----------------------------------",
            tamamlanmaSureleri.size(), ortalama, enKisaGorev, enKisa / 1000.0, enUzunGorev, enUzun / 1000.0
        );

        JOptionPane.showMessageDialog(frame, mesaj, "Gerçek Zamanlı Görev Zamanlayıcı - Analiz Merkezi", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void gorevIsle() {
        Gorev islenen = islemKuyrugu.enOncelikliyiCek();
        if (islenen != null) {
            // Süre Analizi Yap (Şimdi - Eklenme Zamanı)
            long gecenSure = System.currentTimeMillis() - islenen.getEklenmeZamani();
            tamamlanmaSureleri.add(gecenSure);
            tamamlananGorevler.add(islenen);

            bekleyenler.idIleSil(islenen.getId());
            geriAlYigini.it(islenen);
            tamamlananSayisi++;
            tabloyuGuncelle();
            JOptionPane.showMessageDialog(frame, islenen.getAd() + " işlendi.\nSüre: " + (gecenSure / 1000.0) + " sn.");
        } else {
            JOptionPane.showMessageDialog(frame, "Kuyruk boş!");
        }
    }

    // --- Yardımcı Metodlar ---
    private static JLabel createStatCard(JPanel container, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 15, 15, 15)));
        JLabel t = new JLabel(title.toUpperCase()); t.setFont(new Font("SansSerif", Font.BOLD, 10)); t.setForeground(Color.GRAY);
        JLabel v = new JLabel(value); v.setFont(new Font("SansSerif", Font.BOLD, 25)); v.setForeground(accent);
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.SOUTH);
        container.add(card);
        return v;
    }

    private static JButton createSidebarButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.addActionListener(action);
        return btn;
    }

    private static void tabloyuGuncelle() {
        tabloModeli.setRowCount(0);
        
        // 1. Bekleyen görevleri dizi olarak alıyoruz
        Gorev[] gosterilecekler = bekleyenler.diziOlarakAl();
        
        // 2. TABLOYU KUSURSUZ SIRALAYAN KISIM (İnsan okuması için)
        java.util.Arrays.sort(gosterilecekler, (g1, g2) -> {
            // Eğer öncelikler farklıysa, küçük olanı (Acil olanı) en üste at
            if (g1.getOncelik() != g2.getOncelik()) {
                return Integer.compare(g1.getOncelik(), g2.getOncelik());
            }
            // Eğer öncelikler aynıysa (Örn: iki tane 3 varsa), sisteme ilk ekleneni üste at
            return Long.compare(g1.getEklenmeZamani(), g2.getEklenmeZamani());
        });

        // 3. Sıralanmış görevleri tabloya şık bir metinle ekliyoruz
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Gorev g : gosterilecekler) {
            String oncelikMetni = String.valueOf(g.getOncelik());
            if (g.getOncelik() == 1) oncelikMetni += " (En Acil)";
            else if (g.getOncelik() == 2) oncelikMetni += " (Yüksek)";
            else if (g.getOncelik() == 3) oncelikMetni += " (Normal)";
            else if (g.getOncelik() == 4) oncelikMetni += " (Düşük)";
            else if (g.getOncelik() == 5) oncelikMetni += " (Çok Düşük)";

            String teslimTarihi = dateFormat.format(new Date(g.getTeslimZamani()));

            tabloModeli.addRow(new Object[]{
                "#" + g.getId(), 
                g.getAd(), 
                oncelikMetni, 
                teslimTarihi,
                "Bekliyor"
            });
        }
        
        // Sayaçları güncelle
        lblBekleyen.setText(String.valueOf(bekleyenler.getBoyut()));
        lblKuyruk.setText(String.valueOf(islemKuyrugu.getBoyut()));
        lblTamamlanan.setText(String.valueOf(tamamlananSayisi));
    }

    private static void gorevEkleDialog() {
        // Özel panel oluştur
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Görev adı için JTextField
        JTextField adField = new JTextField();
        panel.add(new JLabel("Görev Adı:"));
        panel.add(adField);

        // Öncelik için JComboBox
        String[] oncelikler = {"1 (En Yüksek)", "2", "3", "4", "5 (En Düşük)"};
        JComboBox<String> oncelikCombo = new JComboBox<>(oncelikler);
        oncelikCombo.setSelectedIndex(2); // Varsayılan 3
        panel.add(new JLabel("Öncelik:"));
        panel.add(oncelikCombo);

        // Teslim tarihi için JSpinner
        Date currentDate = new Date();
        Date defaultDeadline = new Date(currentDate.getTime() + 3600000L); // 1 saat sonrası
        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDeadline, currentDate, null, java.util.Calendar.MINUTE);
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");
        dateSpinner.setEditor(dateEditor);
        panel.add(new JLabel("Teslim Tarihi:"));
        panel.add(dateSpinner);

        // Dialog göster
        int result = JOptionPane.showConfirmDialog(frame, panel, "Yeni Görev Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        // Verileri al
        String ad = adField.getText().trim();
        if (ad.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Görev adı boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int oncelik = oncelikCombo.getSelectedIndex() + 1; // 0-based to 1-based

        Date selectedDate = (Date) dateSpinner.getValue();
        long teslimZamani = selectedDate.getTime();

        // Yeni görevi oluştur
        idSayaci++;
        long eklenmeZamani = System.currentTimeMillis();
        Gorev yeni = new Gorev(idSayaci, ad, oncelik, eklenmeZamani, teslimZamani);

        // Veri yapılarına ekle
        bekleyenler.sonunaEkle(yeni);
        islemKuyrugu.ekle(yeni);

        // Arayüzü tazele
        tabloyuGuncelle();
        verileriKaydet();
    }

    private static void geriAl() {
        Gorev g = geriAlYigini.cek();
        if (g != null) {
            bekleyenler.sonunaEkle(g);
            islemKuyrugu.ekle(g);
            tamamlananSayisi = Math.max(0, tamamlananSayisi - 1);
            if (!tamamlanmaSureleri.isEmpty()) {
                tamamlanmaSureleri.remove(tamamlanmaSureleri.size() - 1);
                tamamlananGorevler.remove(tamamlananGorevler.size() - 1);
            }
            tabloyuGuncelle();
            verileriKaydet();
        }
    }

    private static void silmeDialog() {
        String idStr = JOptionPane.showInputDialog(frame, "ID:");
        try {
            int id = Integer.parseInt(idStr);
            if (bekleyenler.idIleSil(id) != null) {
                yenidenHeapOlustur();
                tabloyuGuncelle();
                verileriKaydet();
            }
        } catch (Exception e) {}
    }

    private static void tumBekleyenleriSil() {
        if (bekleyenler.bosmu()) {
            JOptionPane.showMessageDialog(frame, "Şu anda bekleyen görev yok.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int secim = JOptionPane.showConfirmDialog(
            frame,
            "Tüm bekleyen görevleri silmek istediğinizden emin misiniz? Tamamlanan görevler korunacaktır.",
            "Tüm Bekleyenleri Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (secim == JOptionPane.YES_OPTION) {
            Gorev[] silinenler = bekleyenler.diziOlarakAl();
            for (int i = silinenler.length - 1; i >= 0; i--) {
                geriAlYigini.it(silinenler[i]);
            }
            bekleyenler = new OzelBagliListe();
            islemKuyrugu = new MinHeap();
            tabloyuGuncelle();
            verileriKaydet();
        }
    }

    private static void yaslandirmaUygula() {
        long simdi = System.currentTimeMillis();
        for (Gorev g : bekleyenler.diziOlarakAl()) {
            if (simdi - g.getEklenmeZamani() >= AGING_ESIGI_MS && g.getOncelik() > 1) {
                g.setOncelik(g.getOncelik() - 1);
                islemKuyrugu.oncelikGuncelle(g.getId(), g.getOncelik());
            }
        }
        verileriKaydet();
    }

    private static void deadlineKontrolEt() {
        long simdi = System.currentTimeMillis();
        int acilSayisi = 0;
        for (Gorev g : bekleyenler.diziOlarakAl()) {
            long kalanSure = g.getTeslimZamani() - simdi;
            if (kalanSure <= 3_600_000L && kalanSure > 0 && g.getOncelik() > 1) { // 1 saat = 3,600,000 ms
                g.setOncelik(1); // En acil yap
                islemKuyrugu.oncelikGuncelle(g.getId(), 1);
                acilSayisi++;
            }
        }
        if (acilSayisi > 0) {
            tabloyuGuncelle();
            verileriKaydet();
            JOptionPane.showMessageDialog(frame, acilSayisi + " görev deadline'a yaklaştığı için öncelik 1 yapıldı.", "Deadline Kontrolü", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "Yaklaşan deadline olan görev bulunamadı.", "Deadline Kontrolü", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void tamamlananGorevleriGoster() {
        if (tamamlananGorevler.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Henüz tamamlanan görev yok!", "Tamamlanan Görevler", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tamamlananGorevler.size(); i++) {
            Gorev g = tamamlananGorevler.get(i);
            long sure = tamamlanmaSureleri.get(i);
            sb.append(String.format("%d. #%d %s (Öncelik: %d) - %.2f sn\n", i + 1, g.getId(), g.getAd(), g.getOncelik(), sure / 1000.0));
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 320));
        JOptionPane.showMessageDialog(frame, scrollPane, "Tamamlanan Görevler", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void gecmisiKaydet() {
        try (BufferedWriter yazar = new BufferedWriter(new FileWriter("gecmis.txt"))) {
            for (int i = 0; i < tamamlananGorevler.size(); i++) {
                Gorev g = tamamlananGorevler.get(i);
                long sure = tamamlanmaSureleri.get(i);
                yazar.write(g.getId() + ","
                        + g.getAd().replace(",", "_;_") + ","
                        + g.getOncelik() + ","
                        + g.getEklenmeZamani() + ","
                        + sure + ","
                        + g.getTeslimZamani());
                yazar.newLine();
            }
            System.out.println("✔ Tamamlanan görev geçmişi 'gecmis.txt' dosyasına kaydedildi.");
        } catch (IOException e) {
            System.out.println("⚠ Geçmiş kaydetme sırasında hata: " + e.getMessage());
        }
    }

    private static void gecmisiYukle() {
        File dosya = new File("gecmis.txt");
        if (!dosya.exists()) return;

        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosya))) {
            String satir;
            int yuklenen = 0;
            while ((satir = okuyucu.readLine()) != null) {
                satir = satir.trim();
                if (satir.isEmpty()) continue;
                String[] parcalar = satir.split(",", 6);
                if (parcalar.length < 6) continue;
                try {
                    int id = Integer.parseInt(parcalar[0].trim());
                    String ad = parcalar[1].trim().replace("_;_", ",");
                    int oncelik = Integer.parseInt(parcalar[2].trim());
                    long eklenmeZamani = Long.parseLong(parcalar[3].trim());
                    long sure = Long.parseLong(parcalar[4].trim());
                    long teslimZamani = Long.parseLong(parcalar[5].trim());
                    Gorev g = new Gorev(id, ad, oncelik, eklenmeZamani, teslimZamani);
                    tamamlananGorevler.add(g);
                    tamamlanmaSureleri.add(sure);
                    tamamlananSayisi++;
                    yuklenen++;
                } catch (NumberFormatException ex) {
                    System.out.println("⚠ Bozuk geçmiş satırı atlandı: " + satir);
                }
            }
            if (yuklenen > 0)
                System.out.println("✔ " + yuklenen + " geçmiş görevi yüklendi.");
        } catch (IOException e) {
            System.out.println("⚠ Geçmiş yükleme sırasında hata: " + e.getMessage());
        }
    }

    private static void verileriKaydet() {
        DosyaYoneticisi.kaydet(bekleyenler);
        gecmisiKaydet();
    }

    private static void verileriKaydetVeCik() {
        verileriKaydet();
        System.exit(0);
    }

    private static void yenidenHeapOlustur() {
        islemKuyrugu = new MinHeap();
        for (Gorev g : bekleyenler.diziOlarakAl()) islemKuyrugu.ekle(g);
    }
}
