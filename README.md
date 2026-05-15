# Gerçek Zamanlı Görev Zamanlayıcı (Real-Time Task Scheduler)

Bu proje, bir işletim sistemi görev planlama motorunu simüle eden, Java tabanlı bir masaüstü uygulamasıdır. Hazır kütüphaneler yerine temel veri yapılarının (Linked List, Min-Heap, Stack) sıfırdan kodlanmasıyla oluşturulmuş, yüksek performanslı ve hata toleranslı bir mimariye sahiptir.

## 🚀 Öne Çıkan Özellikler

- **Mühendislik Harikası Veri Yapıları:** `java.util.*` koleksiyonları yerine, bellek yönetimi bizzat yönetilen özel veri yapıları kullanılmıştır.
- **Akıllı Önceliklendirme:** Min-Heap algoritması ile en acil göreve O(1) erişim ve O(log n) güncelleme hızı.
- **Starvation (Açlık) Koruması:** Bekleyen görevlerin önceliğini zamanla artıran **Aging (Yaşlandırma)** algoritması.
- **Kritik Zaman Takibi:** Teslim tarihi yaklaşan görevleri otomatik olarak en yüksek öncelik seviyesine çeken **Deadline Kontrol** mekanizması.
- **Hata Telafisi (Undo):** Yanlışlıkla silinen veya tamamlanan görevleri Stack yapısı kullanarak O(1) hızında geri yükleme.
- **Veri Kalıcılığı:** Sistem kapatıldığında verileri otomatik olarak diske (CSV/TXT) senkronize eden File I/O modülü.

## 🛠️ Kullanılan Teknolojiler

- **Dil:** Java (JDK 17+)
- **Arayüz:** Java Swing
- **Veri Yönetimi:** File I/O (CSV tabanlı kalıcı hafıza)
- **Modelleme:** UML Sınıf Diyagramları ve Big-O Analizi

## 📋 Teknik Mimari (Karmaşıklık Analizi)

| İşlem | Veri Yapısı | Zaman Karmaşıklığı (Big-O) |
| :--- | :--- | :--- |
| Yeni Görev Ekleme | Linked List | O(1) |
| En Acil Görevi Bulma | Min-Heap (Kök) | O(1) |
| Öncelik Güncelleme | Min-Heap | O(log n) |
| Geri Al (Undo) | Stack | O(1) |
| Veri Tarama / Kayıt | Traversal | O(n) |

## ⚙️ Çalıştırma Talimatları

### Gereksinimler
- Bilgisayarınızda **JDK 17** veya üzeri bir sürüm yüklü olmalıdır.

### Adımlar
1. **Dosyaları İndirin:** Proje dosyalarını bilgisayarınıza indirin veya klonlayın.
2. **Derleme:** Terminal veya CMD üzerinden projenin ana dizinine gidin ve şu komutu çalıştırın:
   `javac *.java`
3. **Çalıştırma:** Derleme işlemi tamamlandıktan sonra uygulamayı başlatmak için:
   `java Main`
   *(Not: Uygulamanın giriş sınıfı `Main.java` olarak belirlenmiştir.)*

## 🧑‍💻 Hazırlayanlar
- **Sude Arslan**
- Elif Uzundere
- Elif Bayrakoğlu

## 📄 Lisans
Bu proje eğitim amaçlı geliştirilmiş bir akademik çalışmadır.