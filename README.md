# MonoVoice Android Guard 🛡️⚡

**MonoVoice Android Guard** — довірений Android-застосунок власника для апаратної атестації ключів (Hardware Key Attestation) та біометричного підтвердження критичних дій системи **MonoVoice**.

---

## 🚀 Основний функціонал

- **🔐 Апаратна безпека (TEE / StrongBox):**
  Генерація та захищене зберігання ключів у `AndroidKeyStore` з апаратною атестацією сертифікатів (X.509 cert chain) та прив'язкою до `attestationChallenge`.
- **📷 Спарювання з адмін-панеллю:**
  - Сканування QR-коду через CameraX + ZXing.
  - Або ручне введення 6-значного коду авторизації.
- **👆 Біометричне підтвердження:**
  Використання `androidx.biometric.BiometricPrompt` (Class 3 / Strong Biometrics) для підпису кожного запиту на зміну стану чи передачу даних.
- **⚡ Кіберпанк UI на Jetpack Compose:**
  Сучасний реактивний інтерфейс із векторним інтерактивним маскотом, градієнтами та звуковим/візуальним зворотним зв'язком.
- **🗄️ Локальне сховище інцидентів:**
  Журнал підтверджень та спроб доступу на базі Room DB / Encrypted Preferences.

---

## 🛠 Стек технологій

- **Мова:** Kotlin 2.x
- **UI:** Jetpack Compose (Material 3)
- **Архітектура:** MVVM, Coroutines, StateFlow
- **Безпека:** Android KeyStore, KeyGenParameterSpec, TEE/StrongBox, BiometricPrompt
- **Камера & Сканер:** CameraX, ZXing Core
- **Мережа:** OkHttp 4, Retrofit 2, Moshi
- **База даних:** Room Database

---

## 📦 Як зібрати та запустити проєкт

### Вимоги:
- [Android Studio](https://developer.android.com/studio) (Ladybug / Iguana або новіша)
- JDK 17 або новіша
- Android SDK 34–36 (мінімальна версія Android: 7.0 / API 24)

### Інструкція з запуску:

1. **Клонуйте репозиторій:**
   ```bash
   git clone https://github.com/amyvibesato-prog/MonoVoice.git
   cd MonoVoice
   ```

2. **Налаштуйте змінні середовища:**
   Створіть файл `.env` у корені проєкту (скопіювавши з прикладів):
   ```bash
   cp .env.example .env
   ```
   *(за потреби вкажіть додаткові API ключі або endpoint за замовчуванням)*

3. **Збірка через Gradle:**
   - **Linux / macOS:**
     ```bash
     ./gradlew assembleDebug
     ```
   - **Windows:**
     ```cmd
     gradlew.bat assembleDebug
     ```

4. **Запуск через Android Studio:**
   Відкрийте кореневу папку проєкту в Android Studio, дочекайтеся завершення Gradle Sync і запустіть застосунок на фізичному пристрої або емуляторі.
   > **Примітка для тестування атестації:** Рекомендується тестувати на реальному пристрої з підтримкою TEE/StrongBox та налаштованою біометрією (відбиток пальця).

---

## 🔒 Модель безпеки

1. Сервер генерує одноразовий 64-символьний hex challenge (`attestationChallenge`) із терміном дії.
2. Android Guard отримує challenge через QR/код і створює криптографічну пару ключів `EC secp256r1` у безпечному середовищі чипа пристрою.
3. Ланцюжок сертифікатів X.509 (`Certificate Chain`) передається на сервер для валідації кореневого сертифіката Google.
4. Після успішної прив'язки будь-яка критична дія MonoVoice вимагає підтвердження біометрією користувача та криптографічного підпису.

---

## 📄 Ліцензія

Розробка для екосистеми MonoVoice. Всі права захищено.
