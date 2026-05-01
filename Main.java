import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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

    // --- Arayüz Bileşenleri ---
    private static JFrame frame;
    private static JTable tablo;
    private static DefaultTableModel tabloModeli;
    private static JLabel lblBekleyen, lblKuyruk, lblTamamlanan;

    // --- Tasarım Renkleri ---
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color SIDEBAR_COLOR = new Color(249, 250, 251);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);

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
        yenidenHeapOlustur();

        SwingUtilities.invokeLater(() -> {
            arayuzuOlustur();
            tabloyuGuncelle();
        });
    }

    private static void arayuzuOlustur() {
        frame = new JFrame("Gerçek Zamanlı Görev Zamanlayıcı");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout());

        // Üst Panel: Kartlar
        JPanel cardContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        cardContainer.setBorder(new EmptyBorder(25, 25, 25, 25));
        cardContainer.setBackground(Color.WHITE);
        lblBekleyen = createStatCard(cardContainer, "Bekleyen", "0", new Color(59, 130, 246));
        lblKuyruk = createStatCard(cardContainer, "Öncelikli", "0", PRIMARY_COLOR);
        lblTamamlanan = createStatCard(cardContainer, "Tamamlanan", "0", SUCCESS_COLOR);
        frame.add(cardContainer, BorderLayout.NORTH);

        // Orta Panel: Tablo
        String[] kolonlar = {"ID", "Görev Adı", "Öncelik", "Durum"};
        tabloModeli = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablo = new JTable(tabloModeli);
        tablo.setRowHeight(40);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tablo.getColumnCount(); i++) tablo.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(tablo);
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBorder(new EmptyBorder(0, 25, 25, 10));
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.add(scrollPane);
        frame.add(tableWrapper, BorderLayout.CENTER);

        // Sağ Panel: Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(new EmptyBorder(20, 10, 25, 25));

        sidebar.add(createSidebarButton("➕ Yeni Görev", PRIMARY_COLOR, e -> gorevEkleDialog()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("⚙️ Görevi İşle", SUCCESS_COLOR, e -> gorevIsle()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("📊 Analiz Raporu", new Color(245, 158, 11), e -> raporGoster()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("↩️ Geri Al", Color.DARK_GRAY, e -> geriAl()));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("⏳ Aging Uygula", new Color(124, 58, 237), e -> { yaslandirmaUygula(); tabloyuGuncelle(); }));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(createSidebarButton("🗑️ ID ile Sil", DANGER_COLOR, e -> silmeDialog()));
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSidebarButton("💾 Kaydet ve Çık", new Color(31, 41, 55), e -> { DosyaYoneticisi.kaydet(bekleyenler); System.exit(0); }));

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

        String mesaj = String.format(
            "GÖREV ANALİZ RAPORU\n" +
            "-----------------------------------\n" +
            "Toplam Tamamlanan: %d\n" +
            "Ortalama İşlem Süresi: %.2f saniye\n" +
            "En Hızlı İşlem: %.2f saniye\n" +
            "En Yavaş İşlem: %.2f saniye\n" +
            "-----------------------------------",
            tamamlanmaSureleri.size(), ortalama, enKisa / 1000.0, enUzun / 1000.0
        );

        JOptionPane.showMessageDialog(frame, mesaj, "Gerçek Zamanlı Görev Zamanlayıcı - Analiz Merkezi", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void gorevIsle() {
        Gorev islenen = islemKuyrugu.enOncelikliyiCek();
        if (islenen != null) {
            // Süre Analizi Yap (Şimdi - Eklenme Zamanı)
            long gecenSure = System.currentTimeMillis() - islenen.getEklenmeZamani();
            tamamlanmaSureleri.add(gecenSure);

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
        for (Gorev g : bekleyenler.diziOlarakAl()) tabloModeli.addRow(new Object[]{"#" + g.getId(), g.getAd(), "Öncelik: " + g.getOncelik(), "Bekliyor"});
        lblBekleyen.setText(String.valueOf(bekleyenler.getBoyut()));
        lblKuyruk.setText(String.valueOf(islemKuyrugu.getBoyut()));
        lblTamamlanan.setText(String.valueOf(tamamlananSayisi));
    }

   private static void gorevEkleDialog() {
        // Görev adı girişi
        String ad = JOptionPane.showInputDialog(frame, "Görev Adı Giriniz:", "Yeni Görev", JOptionPane.PLAIN_MESSAGE);
        if (ad == null || ad.trim().isEmpty()) return;

        // Öncelik seçme seçenekleri
        String[] oncelikler = {"1 (En Yüksek)", "2", "3", "4", "5 (En Düşük)"};
        
        // Seçim kutusu (Combobox) içeren dialog
        Object secim = JOptionPane.showInputDialog(
            frame, 
            "Öncelik Seviyesini Seçin:", 
            "Öncelik Belirle", 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            oncelikler, 
            oncelikler[2] // Varsayılan olarak 3 seçili gelir
        );
        
        if (secim != null) {
            String secimStr = secim.toString().trim();
            int oncelik = -1;
            switch (secimStr) {
                case "1 (En Yüksek)": oncelik = 1; break;
                case "2": oncelik = 2; break;
                case "3": oncelik = 3; break;
                case "4": oncelik = 4; break;
                case "5 (En Düşük)": oncelik = 5; break;
                default: break;
            }

            if (oncelik < 1 || oncelik > 5) {
                JOptionPane.showMessageDialog(frame, "Lütfen 1 ile 5 arasında bir öncelik seçin.", "Geçersiz Öncelik", JOptionPane.ERROR_MESSAGE);
                return;
            }

            idSayaci++;
            // Yeni görevi oluştur (ID, Ad, Öncelik, Mevcut Zaman)
            Gorev yeni = new Gorev(idSayaci, ad, oncelik, System.currentTimeMillis());
            
            // Veri yapılarına ekle
            bekleyenler.sonunaEkle(yeni);
            islemKuyrugu.ekle(yeni);
            
            // Arayüzü tazele
            tabloyuGuncelle();
        }
    }

    private static void geriAl() {
        Gorev g = geriAlYigini.cek();
        if (g != null) {
            bekleyenler.sonunaEkle(g);
            islemKuyrugu.ekle(g);
            tamamlananSayisi = Math.max(0, tamamlananSayisi - 1);
            if (!tamamlanmaSureleri.isEmpty()) tamamlanmaSureleri.remove(tamamlanmaSureleri.size() - 1);
            tabloyuGuncelle();
        }
    }

    private static void silmeDialog() {
        String idStr = JOptionPane.showInputDialog(frame, "ID:");
        try {
            int id = Integer.parseInt(idStr);
            if (bekleyenler.idIleSil(id) != null) { yenidenHeapOlustur(); tabloyuGuncelle(); }
        } catch (Exception e) {}
    }

    private static void yaslandirmaUygula() {
        long simdi = System.currentTimeMillis();
        for (Gorev g : bekleyenler.diziOlarakAl()) {
            if (simdi - g.getEklenmeZamani() >= AGING_ESIGI_MS && g.getOncelik() > 1) {
                g.setOncelik(g.getOncelik() - 1);
                islemKuyrugu.oncelikGuncelle(g.getId(), g.getOncelik());
            }
        }
    }

    private static void yenidenHeapOlustur() {
        islemKuyrugu = new MinHeap();
        for (Gorev g : bekleyenler.diziOlarakAl()) islemKuyrugu.ekle(g);
    }
}