# Noxiapp - Alışkanlık Takibi ve Fitness Uygulaması

Alışkanlıkları takip etmek, antrenmanları yönetmek ve kişisel hedeflere ulaşmak için kapsamlı bir Android uygulaması.

## Özellikler

### Alışkanlık Takibi
- **Önceden Tanımlı Alışkanlıklar**: Hazır yapılandırılmış alışkanlıklarla hızlı kurulum
  - Su tüketimi takibi
  - Okuma (sayfa veya dakika)
  - Vitamin takibi
  - Meditasyon seansları
  - Egzersiz tamamlama
- **Özel Alışkanlıklar**: Esnek takip seçenekleriyle kişiselleştirilmiş alışkanlıklar oluşturun
- **İlerleme Görselleştirme**: Günlük, haftalık ve aylık ilerlemenizi takip edin
- **Seri Takibi**: Alışkanlık serilerinizi koruyun ve görselleştirin

### Antrenman Yönetimi
- **Egzersiz Kütüphanesi**: Geniş önceden tanımlı egzersiz koleksiyonu
- **Özel Antrenmanlar**: Kişiselleştirilmiş antrenman programları oluşturun ve kaydedin
- **İlerleme Takibi**: Her egzersiz için ağırlık, tekrar ve notlar kaydedin
- **Takvim Entegrasyonu**: Antrenman geçmişini tarihe göre görüntüleyin
- **Program Yönetimi**: Farklı antrenman rutinlerini kaydedin ve yükleyin

### Başarılar Sistemi
- İlerledikçe başarıların kilidini açın
- Başarılarınızı takip edin
- Kilometre taşı ödülleriyle motive olun

### Analitik ve İçgörüler
- **Takvim Görünümü**: Aktivitenizin görsel temsili
- **İstatistikler**: Alışkanlıklarınız ve antrenmanlarınız hakkında detaylı bilgiler
- **Profil Yönetimi**: Kişisel bilgileri ve hedefleri takip edin

### Kullanıcı Kimlik Doğrulama
- Güvenli Firebase Kimlik Doğrulama
- E-posta/Şifre girişi
- Kullanıcı profili yönetimi

## Teknoloji Yığını

- **Dil**: Kotlin
- **UI Framework**: Jetpack Compose
- **Mimari**: MVVM (Model-View-ViewModel)
- **Veritabanı**: 
  - Room (Yerel depolama)
  - Firebase Firestore (Bulut senkronizasyonu)
- **Kimlik Doğrulama**: Firebase Auth
- **Bağımlılık Enjeksiyonu**: Hilt/Dagger (varsa)
- **Minimum SDK**: Android 8.0 (API 26)
- **Hedef SDK**: Android 14 (API 34)

## Proje Yapısı

```
com.noxi.noxiapp/
├── data/
│   ├── local/          # Room veritabanı DAOs
│   ├── repository/     # Veri depoları
│   └── models/         # Veri sınıfları
├── ui/
│   ├── screens/        # Compose ekranları
│   ├── components/     # Yeniden kullanılabilir UI bileşenleri
│   └── theme/          # Uygulama teması ve stilleri
└── MainActivity.kt     # Ana giriş noktası
```

## Başlangıç

### Gereksinimler
- Android Studio Hedgehog veya üzeri
- JDK 17 veya üzeri
- Android SDK 34
- Firebase hesabı

### Kurulum

1. **Depoyu klonlayın**
   ```bash
   git clone https://github.com/Noxiuur/Noxiapp.git
   cd Noxiapp
   ```

2. **Firebase Kurulumu**
   - [Firebase Console](https://console.firebase.google.com) adresine gidin
   - Yeni bir proje oluşturun veya mevcut olanı kullanın
   - `google-services.json` dosyasını indirin
   - `app/` dizinine yerleştirin

3. **Firebase Güvenlik Kurallarını Yapılandırın**
   
   **Firestore Kuralları:**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{userId}/{document=**} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

4. **Derleyin ve Çalıştırın**
   ```bash
   ./gradlew build
   ```
   Veya projeyi Android Studio'da açın ve Çalıştır butonuna tıklayın

## Ekran Görüntüleri

> Uygulamanızın ekran görüntülerini buraya ekleyin

## Güvenlik

- Firebase API anahtarları versiyon kontrolünden hariç tutulmuştur
- Kullanıcı verileri Firebase Güvenlik Kuralları ile korunmaktadır
- Tüm veri işlemleri için kimlik doğrulama gereklidir
- Yerel veriler Room ile şifrelenir

## Katkıda Bulunma

Katkılar memnuniyetle karşılanır! Lütfen bir Pull Request göndermekten çekinmeyin.

## Lisans

Bu proje özel ve tescillidir.

## Yazar

**Noxiuur**
- GitHub: [@Noxiuur](https://github.com/Noxiuur)

## Destek

Destek için lütfen GitHub deposunda bir issue açın.

---

**Kotlin ve Jetpack Compose kullanılarak yapılmıştır**
