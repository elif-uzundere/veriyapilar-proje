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
    private static OzelBagliListe bekleyenler = new OzelBagliListe();//sisteme eklenen her görev önce bu listeye eklenir.
    private static MinHeap islemKuyrugu = new MinHeap();//görevleri önceliğe göre sıralar
    private final static OzelYigin geriAlYigini = new OzelYigin();//geri al için son işlemleri tutan yığın
    private static int idSayaci = 0;//gorevlere id atama
    private static int tamamlananSayisi = 0;//tamamlanan görev sayısı
    private static final long AGING_ESIGI_MS = 30_000L;//yaşlandırma 30 saniye olarak kilitlenmiştir

    // --- Analiz Verileri (Yeni) ---
    private final static List<Long> tamamlanmaSureleri = new ArrayList<>();
    private final static List<Gorev> tamamlananGorevler = new ArrayList<>();

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
        /*nimbus , Java Swing tabanlı uygulamalara modern bir görünüm 
        kazandırmak için kullanılan, vektör grafik tabanlı arayüz kütüphanesidir. */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Nimbus bulunamazsa hata yönetimi
        }

        /* dosya yoneticisi .cvs dosyalarindan eski verileri yukler */
        idSayaci = DosyaYoneticisi.yukle(bekleyenler, idSayaci);
        gecmisiYukle();
        yenidenHeapOlustur();

        /* arayuz olusturma islemlerini javanin baska parcacigina iletiyor*/
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
        frame.addWindowListener(new WindowAdapter() { //program kapanirken verilerin ram'den direkt silinmesini engelleyip kaydediyor 
            @Override //program kapatilirken calisacak metadu override ediyoruz
            public void windowClosing(WindowEvent e) {
                verileriKaydetVeCik();//verileri .cvs dosyasina kaydediyor ve programi kapatiyor
            }
        });

        //  ust paneldeki kartlari olusturup ekliyor
        JPanel cardContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        cardContainer.setBorder(new EmptyBorder(25, 25, 25, 25));
        cardContainer.setBackground(Color.WHITE);
        lblBekleyen = createStatCard(cardContainer, "Bekleyen", "0", new Color(59, 130, 246));
        lblKuyruk = createStatCard(cardContainer, "Öncelikli", "0", PRIMARY_COLOR);
        lblTamamlanan = createStatCard(cardContainer, "Tamamlanan", "0", SUCCESS_COLOR);
        frame.add(cardContainer, BorderLayout.NORTH);

        // ana tablomuzu yaratiyoruz. 
        String[] kolonlar = {"ID", "Görev Adı", "Öncelik", "Teslim Tarihi", "Durum"};
        tabloModeli = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablo = new JTable(tabloModeli);
        tablo.setRowHeight(40);
        
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() { //ilk satir rengini yesil yaparak odagi cekiyor
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

        // ekranin sag tarafindaki butonlari ve islemleri ekliyoruz
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(new EmptyBorder(20, 10, 25, 25));


//lambda ifadeleriyle butonlara islemlerini ekliyoruz. Her butonun arka plan rengi ve yazisi farkli olarak tasarlandi.
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
        for (long sure : tamamlanmaSureleri) { //dongu listeyi bastan sona geziyor O(n) 
            toplam += sure;//toplam sureyi hesapliyor
            if (sure < enKisa) enKisa = sure;//en kisa sureyi hesapliyor
            if (sure > enUzun) enUzun = sure;//en uzun sureyi hesapliyor
        }
        double ortalama = (double) toplam / tamamlanmaSureleri.size() / 1000.0;//ortalama sureyi saniye cinsinden hesapliyor

        int enKisaIndex = 0;
        int enUzunIndex = 0;
        for (int i = 0; i < tamamlanmaSureleri.size(); i++) {//en kisa ve en uzun surelerin indexlerini(sira numarasini) buluyor
            long sure = tamamlanmaSureleri.get(i);
            if (sure < tamamlanmaSureleri.get(enKisaIndex)) enKisaIndex = i;
            if (sure > tamamlanmaSureleri.get(enUzunIndex)) enUzunIndex = i;
        }

        String enKisaGorev = tamamlananGorevler.get(enKisaIndex).getAd();//en kisa sureye sahip görevin adini buluyor
        String enUzunGorev = tamamlananGorevler.get(enUzunIndex).getAd();//en uzun sureye sahip görevin adini buluyor

        String mesaj = String.format( 
            """
            GÖREV ANALİZ RAPORU 
            -----------------------------------
            Toplam Tamamlanan: %d
            Ortalama İşlem Süresi: %.2f saniye
            En Kısa İşlem: %s (%.2f saniye)
            En Uzun İşlem: %s (%.2f saniye)
            -----------------------------------""",
            tamamlanmaSureleri.size(), ortalama, enKisaGorev, enKisa / 1000.0, enUzunGorev, enUzun / 1000.0
        );

        //hazirlanan raporu guzel bir mesaj kutusunda gosteriyoruz
        JOptionPane.showMessageDialog(frame, mesaj, "Gerçek Zamanlı Görev Zamanlayıcı - Analiz Merkezi", JOptionPane.INFORMATION_MESSAGE);
    }

    //gorevIsle metodunda minheap , stack ve bagli liste ayni anda birbirleriyle tam entegre calisiyor.
    //  Min heap en oncelikli gorevi O(log n) surede cekiyor, 
    // tamamlanan gorevlerin surelerini analiz icin listelere ekliyor, 
    // bagli listeden silip geri al yiginina ekliyor ve arayuzu guncelliyor. 
    private static void gorevIsle() {   
        Gorev islenen = islemKuyrugu.enOncelikliyiCek();//min heap veri yapisinin kok elemanini cekiyor O(log n) surede gerceklesir
       // islenen bos degilse tamamlanma suresini hesapliyor 
        if (islenen != null) {
            long gecenSure = System.currentTimeMillis() - islenen.getEklenmeZamani();
            //gorevi ve tamamlanma suresini analiz icin hafizaya ekliyor
            tamamlanmaSureleri.add(gecenSure);
            tamamlananGorevler.add(islenen);
// gorevi bekleyenler listesinden siliyor ve geri al yiginina ekliyor
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
    //bilgi kartlarini uretip arayuze ekliyor
    private static JLabel createStatCard(JPanel container, String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)), new EmptyBorder(15, 15, 15, 15)));
        JLabel t = new JLabel(title.toUpperCase()); t.setFont(new Font("SansSerif", Font.BOLD, 10)); t.setForeground(Color.GRAY);
        JLabel v = new JLabel(value); v.setFont(new Font("SansSerif", Font.BOLD, 25)); v.setForeground(accent);
        card.add(t, BorderLayout.NORTH); card.add(v, BorderLayout.SOUTH);
        container.add(card);
        return v;//deger labelini donduruyoruz ki daha sonra bu labelin textini guncelleyebilelim
    }

    //sidebar butonlarini uretip arayuze ekliyor
    private static JButton createSidebarButton(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setBackground(bg); btn.setForeground(Color.WHITE); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); /*fare butonun uzerine gelince el isareti oluyor */
        btn.addActionListener(action);//tiklaninca ne yapilacagini belirtiyor
        return btn;//butonu donduruyoruz ki daha sonra bu butona ek islemler ekleyebilelim
    }
//arayuz tablosunun bagli listeyle anlik olarak senkronize olmasini sagliyor.
    private static void tabloyuGuncelle() {
        tabloModeli.setRowCount(0);//tabloyu guncellemeden once icindekileri temizliyor. yapmasaydi tabloya her guncellemede yeni satirlar eklenirdi ve tablo cok uzun olurdu.
        
        // ozelBagliListe'deki gorevleri diziye ceviriyor
        Gorev[] gosterilecekler = bekleyenler.diziOlarakAl();
        
        //once oncelik karsilastirmasi yapiyor eger ayniysa eklenme zamanina gore siraliyor
        java.util.Arrays.sort(gosterilecekler, (g1, g2) -> {
            // Eğer öncelikler farklıysa
            if (g1.getOncelik() != g2.getOncelik()) {
                return Integer.compare(g1.getOncelik(), g2.getOncelik());
            }
            // Eğer öncelikler aynıysa
            return Long.compare(g1.getEklenmeZamani(), g2.getEklenmeZamani());
        });

        //gorevdeki ms cinsinden teslim zamanini okunabilir formata ceviriyor
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Gorev g : gosterilecekler) {
            String oncelikMetni = String.valueOf(g.getOncelik());
            //oncelik numarasina gore metne ek bilgi ekliyor
            switch (g.getOncelik()) { 
                case 1 -> oncelikMetni += " (En Acil)";
                case 2 -> oncelikMetni += " (Yüksek)";
                case 3 -> oncelikMetni += " (Normal)";
                case 4 -> oncelikMetni += " (Düşük)";
                case 5 -> oncelikMetni += " (Çok Düşük)";
            }

            String teslimTarihi = dateFormat.format(new Date(g.getTeslimZamani()));

            //hazirlanan siralanmis verileri bir paket dizisi olarak tabloya ekliyor
            tabloModeli.addRow(new Object[]{
                "#" + g.getId(), //gorev id'sini # ile birlikte gosteriyor
                g.getAd(), //gorev adini gosteriyor
                oncelikMetni, //gorev onceligini metin olarak gosteriyor
                teslimTarihi,//gorev teslim tarihini okunabilir formatta gosteriyor
                "Bekliyor" //
            });
        }
        
        // bilgi kartlarindaki sayilari guncelliyor.
        lblBekleyen.setText("" + bekleyenler.getBoyut());//ozelBagliListeden getBoyut() metodu ile bekleyen gorev sayisini gosteriyor
        lblKuyruk.setText("" + islemKuyrugu.getBoyut()); //min heap'ten getBoyut() metodu ile oncelikli gorev sayisini gosteriyor       
        lblTamamlanan.setText("" + tamamlananSayisi);//tamamlanan gorev sayisini gosteriyor
    }
//yeni gorev butonuna tiklandiginda acilan form penceresini tasarliyor
    private static void gorevEkleDialog() {
        //gorev ekleme formunu olusturuyoruz.
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        //gorev adini almak icin JTextField ekliyoruz
        JTextField adField = new JTextField();
        panel.add(new JLabel("Görev Adı:"));
        panel.add(adField);

        //1-5 arasinda oncelik secimi yapmak icin JComboBox ekliyoruz
        String[] oncelikler = {"1 (En Yüksek)", "2", "3", "4", "5 (En Düşük)"};
        JComboBox<String> oncelikCombo = new JComboBox<>(oncelikler);
        oncelikCombo.setSelectedIndex(2); //indekster 0dan basladigi icin 2. index 3 (Normal) onceligi olarak seciyor
        panel.add(new JLabel("Öncelik:"));
        panel.add(oncelikCombo);

        //Deadline icin JSpinner ekliyoruz ve varsayılan olarak şu anki zamandan 1 saat sonrasını gösteriyoruz
        Date currentDate = new Date();//su anki tarihi aliyor
        Date defaultDeadline = new Date(currentDate.getTime() + 3600000L); // 1 saat = 3600000 ms, bu sekilde varsayılan deadline su anki zamandan 1 saat sonrasina ayarlanmis oluyor
        SpinnerDateModel dateModel = new SpinnerDateModel(defaultDeadline, currentDate, null, java.util.Calendar.MINUTE);//dateModel ile JSpinner'a tarih secme ozelligi kazandiriyoruz. minimum degeri su anki tarih olarak belirliyoruz ve adim araligi olarak dakikayi seciyoruz
        JSpinner dateSpinner = new JSpinner(dateModel);//dateSpinner ile kullanicinin gorev teslim tarihini secmesini sagliyoruz
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yyyy HH:mm");//dateEditor ile dateSpinner'da gosterilecek tarih formatini belirliyoruz
        dateSpinner.setEditor(dateEditor);//dateSpinner'a dateEditor'u ekliyoruz ki kullanici tarih secerken belirledigimiz formatta gorsun
        panel.add(new JLabel("Teslim Tarihi:")); //gorev teslim tarihini secmek icin label ekliyoruz
        panel.add(dateSpinner);//dateSpinner'i formumuza ekliyoruz

        // hazirlanan 3 satirlik tablo formunu alip ekranda ok /cancel butonlari olan pop-up bir pencerede gosteriyoruz
        int result = JOptionPane.showConfirmDialog(frame, panel, "Yeni Görev Ekle", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        // formdan girilen verileri aliyoruz ve gorev objesi olusturmak icin kullaniyoruz
        String ad = adField.getText().trim();
        //gorev adinin bos olup olmadigini kontrol ediyoruz
        if (ad.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Görev adı boş olamaz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int oncelik = oncelikCombo.getSelectedIndex() + 1; //oncelikCombo'da secilen index 0-4 arasinda oldugu icin +1 ekleyerek 1-5 arasinda oncelik degeri elde ediyoruz

        Date selectedDate = (Date) dateSpinner.getValue(); //dateSpinner'dan secilen tarihi aliyoruz
        long teslimZamani = selectedDate.getTime(); // takvimden secilen tarihi min-heap in anlayabilecegi ms cinsine ceviriyoruz

        //gorev objesi olusturuyoruz ve id sayacini bir arttiriyoruz ki her gorevin kendine ozel bir id'si olsun
        idSayaci++;
        long eklenmeZamani = System.currentTimeMillis();//gorevin sisteme eklendigi zamani ms cinsinden aliyoruz
        Gorev yeni = new Gorev(idSayaci, ad, oncelik, eklenmeZamani, teslimZamani); //yeni gorev objesi olusturuyoruz

        //yeni gorevi hem bagli listeye hem de min-heap'e ekliyoruz ki gorev islendiginde her iki yapidan da silinebilsin
        bekleyenler.sonunaEkle(yeni);
        islemKuyrugu.ekle(yeni);

        //gorev eklendigi an tabloya yansitip aninda .cvs dosyasina kaydediyoruz
        tabloyuGuncelle();
        verileriKaydet();
    }

    //kullanıcı yanlışlıkla "Görevi Tamamla" veya "Sil" tuşuna bastığında çalışan "Ctrl+Z" (Geri Al) mekanizmasıdır.
    //  Burada başrolde Stack (Yığın) veri yapısı var.
    private static void geriAl() {
        Gorev g = geriAlYigini.cek();//ozelYigin nesnesinden en ustteki gorevi cekiyor(pop islemi)
        if (g != null) { //eger geri alacak gorev varsa
            bekleyenler.sonunaEkle(g); //geri alinan gorevi bagli listenin sonuna ekliyor
            islemKuyrugu.ekle(g); //geri alinan gorevi min heap'e ekliyor ki oncelik siralamasi bozulmasin
            tamamlananSayisi = Math.max(0, tamamlananSayisi - 1); //tamamlanan gorev sayisini bir azaltarak guncelliyor
            //geri alinan gorevin tamamlanma suresini analiz listelerinden cikartiyor
            if (!tamamlanmaSureleri.isEmpty()) { 
                tamamlanmaSureleri.remove(tamamlanmaSureleri.size() - 1);
                tamamlananGorevler.remove(tamamlananGorevler.size() - 1);
            }
            tabloyuGuncelle();
            verileriKaydet();
        }
    }

    //kullanicinin sistemden belirli bir idye sahip gorevi silmesini saglar
    private static void silmeDialog() {
        String idStr = JOptionPane.showInputDialog(frame, "ID:");
        if (idStr == null) { //kullanici iptal ettiyse veya bos biraktiysa islemi durduruyoruz
            return; 
        }
        //girilen id'nin gecerli olup olmadigini try-catch blogu ile kontrol ediyoruz
        try {
            int id = Integer.parseInt(idStr);
            if (bekleyenler.idIleSil(id) != null) { //bagli listeden id ile gorevi silmeye calisiyor ve eger silme islemi basariliysa
                yenidenHeapOlustur(); //min heap'i yeniden olusturuyoruz cunku bagli listeden silinen gorev min heap'te de var ve oncelik siralamasi bozulmasin diye min heap'i tamamen yeniden kuruyoruz
                tabloyuGuncelle(); //tabloyu guncelliyoruz ki silinen gorev arayuzden de kaybolsun
                verileriKaydet(); //verileri kaydediyoruz ki silme islemi kalici olsun
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Geçerli bir ID girin.", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    //ekrani tamamen temizleyen fonksiyon
    private static void tumBekleyenleriSil() {
        if (bekleyenler.bosmu()) {
            JOptionPane.showMessageDialog(frame, "Şu anda bekleyen görev yok.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //kullaniciya tum bekleyen gorevleri silmek istediginden emin olup olmadigini soran bir onay penceresi gosteriyoruz
        int secim = JOptionPane.showConfirmDialog(
            frame,
            "Tüm bekleyen görevleri silmek istediğinizden emin misiniz? Tamamlanan görevler korunacaktır.",
            "Tüm Bekleyenleri Sil",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        //eger kullanici evet secerse tum bekleyen gorevleri siliyoruz ve geri al yiginina ekliyoruz ki kullanici isterse geri alabilir
        if (secim == JOptionPane.YES_OPTION) {
            Gorev[] silinenler = bekleyenler.diziOlarakAl();
            for (int i = silinenler.length - 1; i >= 0; i--) { //dongu yigin oldugu icin tersten kuruluyoruz ki ilk silinen gorev yiginin en ustune gelsin
                geriAlYigini.it(silinenler[i]);//silinen gorevleri geri al yiginina ekliyoruz
            }
            bekleyenler = new OzelBagliListe();//bagli listeyi tamamen temizliyoruz
            islemKuyrugu = new MinHeap();//min heap'i tamamen temizliyoruz
            tabloyuGuncelle();
            verileriKaydet();
        }
    }

    //yaşlandırma mekanizması, belirli bir süre boyunca bekleyen görevlerin önceliklerini artırarak onları daha acil hale getirir.
    private static void yaslandirmaUygula() {
        long simdi = System.currentTimeMillis();//simdi degiskeni ile yaslandirma islemi yaparken kullanacagimiz zamani aliyoruz
        for (Gorev g : bekleyenler.diziOlarakAl()) {//bagli listeden gorevleri dizi olarak aliyoruz ki uzerlerinde kolayca gezinebilelim
            if (simdi - g.getEklenmeZamani() >= AGING_ESIGI_MS && g.getOncelik() > 1) {//eger gorev eklenme zamanindan itibaren belirli bir sure beklemisse ve onceligi 1'den buyukse (yani zaten en acil degilse)
                g.setOncelik(g.getOncelik() - 1); //gorevin onceligini bir azaltarak daha acil hale getiriyoruz 
                islemKuyrugu.oncelikGuncelle(g.getId(), g.getOncelik());//gorevin onceligini min heap'te de guncelliyoruz ki oncelik siralamasi bozulmasin
            }
        }
        verileriKaydet();
    }

    //deadline kontrol mekanizması, zamani daralani one al prensibiyle calisir
    private static void deadlineKontrolEt() {
        long simdi = System.currentTimeMillis();
        int acilSayisi = 0;
        for (Gorev g : bekleyenler.diziOlarakAl()) {
            long kalanSure = g.getTeslimZamani() - simdi;
            if (kalanSure <= 3_600_000L && kalanSure > 0 && g.getOncelik() > 1) { // eger gorevin teslim zamani 1 saatten az ve esitse ve onceligi zaten en acil degilse
                g.setOncelik(1); //gorevin onceligini 1 yaparak en acil hale getiriyoruz
                islemKuyrugu.oncelikGuncelle(g.getId(), 1);//gorevin onceligini min heap'te de guncelliyoruz ki oncelik siralamasi bozulmasin
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

    //sağ menüdeki "Tamamlanan Görevler" butonuna basınca ekrana gelen bilgi ekranıdır.
    private static void tamamlananGorevleriGoster() {
        if (tamamlananGorevler.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Henüz tamamlanan görev yok!", "Tamamlanan Görevler", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        //StringBuilder ile bellegi cok yormadan metin birlestirme motoru 
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tamamlananGorevler.size(); i++) { 
            Gorev g = tamamlananGorevler.get(i);
            long sure = tamamlanmaSureleri.get(i);
            //her tamamlanan gorevi id, ad, oncelik ve tamamlanma suresi ile birlikte listeliyoruz
            sb.append(String.format("%d. #%d %s (Öncelik: %d) - %.2f sn\n", i + 1, g.getId(), g.getAd(), g.getOncelik(), sure / 1000.0));
        }

        //hazirlanan metni kaydirilabilir bir text area icinde gosteriyoruz
        JTextArea textArea = new JTextArea(sb.toString());//text area'ya tamamlanan gorevler listesini ekliyoruz
        textArea.setEditable(false);//text area'nin duzenlenemez olmasini sagliyoruz
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12)); 
        JScrollPane scrollPane = new JScrollPane(textArea);//text area'yi scroll pane'e ekliyoruz ki tamamlanan gorevler cok uzun olursa kaydirarak gorebilelim
        scrollPane.setPreferredSize(new Dimension(500, 320));
        //hazirlanan scroll pane'i bilgi kutusu olarak gosteriyoruz
        JOptionPane.showMessageDialog(frame, scrollPane, "Tamamlanan Görevler", JOptionPane.INFORMATION_MESSAGE);
    }

    //Sistem kapandığında veya görev tamamlandığında verilerin güvenle CSV'ye aktarıldığı yer.
    private static void gecmisiKaydet() {
        //try with resources kullanarak BufferedWriter ile gecmis.csv dosyasina yazma islemi yapiliyor. Bu sayede dosya islemi bittikten sonra kaynaklar otomatik olarak kapatiliyor.
        try (BufferedWriter yazar = new BufferedWriter(new FileWriter("gecmis.csv"))) {
            for (int i = 0; i < tamamlananGorevler.size(); i++) {
                Gorev g = tamamlananGorevler.get(i);
                long sure = tamamlanmaSureleri.get(i);
                yazar.write(g.getId() + ","
                        + g.getAd().replace(",", "_;_") + "," //gorev adinda virgul varsa csv formatini bozmamak icin _;_ ile degistiriyoruz
                        + g.getOncelik() + "," //gorev onceligini kaydediyoruz
                        + g.getEklenmeZamani() + ","//gorevin sisteme eklendigi zamani kaydediyoruz
                        + sure + ","//gorevin tamamlanma suresini kaydediyoruz
                        + g.getTeslimZamani());//gorevin teslim zamanini kaydediyoruz
                yazar.newLine();//her gorevden sonra yeni bir satira geciyoruz
            }
            System.out.println("✔ Tamamlanan görev geçmişi 'gecmis.csv' dosyasına kaydedildi.");
        } catch (IOException e) {
            System.out.println("⚠ Geçmiş kaydetme sırasında hata: " + e.getMessage());
        }
    }

    //Program her açıldığında çalışan, eski başarıları (tamamlanan görevleri) .csv dosyasından okuyup sisteme geri yükleyen bölümdür.
    private static void gecmisiYukle() {
        //gecmis.csv dosyasinin varligini kontrol ediyoruz, eger dosya yoksa hicbir sey yapmadan geri donuyoruz
        File dosya = new File("gecmis.csv");
        if (!dosya.exists()) return;

        //filereader yerine bufferreader kullanarak dosyayi harf harf degil blok blok okuruz
        try (BufferedReader okuyucu = new BufferedReader(new FileReader(dosya))) {
            String satir; //dosyadan okunan her satiri temsil eder, her satir bir tamamlanan gorevi ifade eder
            int yuklenen = 0; //yuklenen gorev sayisini saymak icin bir sayaç
            //dosyadan satir satir okuyarak her bir gorevi geri yukleme islemi yapiliyor
            while ((satir = okuyucu.readLine()) != null) { 
                satir = satir.trim();
                if (satir.isEmpty()) continue;

                //CSV'deki o virgüllü satırı okuyup virgüllerden parçalayarak 6 elemanlı bir Diziye (Array) çeviriyor.
                String[] parcalar = satir.split(",", 6);
                if (parcalar.length < 6) continue;

                /*Biri gidip .csv dosyasını not defteriyle açıp yanlışlıkla harf yazarak bozarsa,
                 program o bozuk satıra geldiğinde çökmez. Sadece konsola uyarı basar 
                 ve diğer satırları okumaya devam eder. */
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

    //tum kaydetme islemlerini tek bir metotta toplayarak kodun okunabilirligini ve bakim kolayligini artiriyoruz
    private static void verileriKaydet() {
        DosyaYoneticisi.kaydet(bekleyenler);
        gecmisiKaydet();
    }

    private static void verileriKaydetVeCik() {
        verileriKaydet(); //verileri kaydediyoruz ki kullanici cikarken hicbir veri kaybolmasin
        System.exit(0); //programi kapatiyoruz
    }

    
    private static void yenidenHeapOlustur() {
        islemKuyrugu = new MinHeap(); //min heap'i tamamen temizliyoruz
        for (Gorev g : bekleyenler.diziOlarakAl()) islemKuyrugu.ekle(g); //bagli listeden gorevleri dizi olarak alarak min heap'e ekliyoruz ki oncelik siralamasi bozulmasin. o(n log n) 
    }
}
