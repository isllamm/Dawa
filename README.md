# Dawa (دوا) 💊
### Visual & Audio Medication Schedule for Elderly Caregivers (Non-Reading UX)

**Dawa** is a specialized Android application built specifically for an **elderly caregiver (80+ years old)** who can operate a smartphone but **cannot reliably read or write**. It ensures she administers medications to an elderly family member at the exact scheduled times with zero ambiguity and maximum safety.

> **Core Product Principle:**  
> *"Make it extremely difficult for the caregiver to give the wrong medication at the wrong time, without relying on reading or writing."*

---

## 📱 Technical Overview
* **Project Directory:** `/Users/islam/AndroidStudioProjects/Dawa`
* **Direct APK File:** `app/build/outputs/apk/debug/app-debug.apk`
* **Tech Stack:** Kotlin 2.0 + Jetpack Compose (Material 3)
* **Architecture:** Clean Architecture + MVI (Model-View-Intent) with strict Unidirectional Data Flow (UDF)
* **Dependency Injection:** Koin (Koin Android & Koin Compose)
* **Local Persistence:** Room 2.6.1 (100% Offline — No `INTERNET` permission declared in the manifest)
* **Scheduling Engine:** Android `AlarmManager` with exact `setAlarmClock()` (exempt from Doze mode)
* **Audio Engine:** Melodic chime + family-recorded audio clips + on-device Arabic Text-to-Speech (TTS)
* **Compatibility:** Android 8.0 (API 26) through Android 15 (API 35), with dedicated support for **Xiaomi HyperOS / MIUI**.
* **Language & Typography:** Arabic-only UI, forced RTL, Arabic-Indic numerals (`٠١٢٣٤٥٦٧٨٩`), and bundled Cairo font.

---

## 🏗️ Architecture & Package Organization

The application strictly implements **Clean Architecture**, **MVI (Model-View-Intent)**, and **Dependency Injection via Koin**:

```
app/src/main/java/com/family/dawa/
├── core/                       # Cross-cutting primitives
│   ├── base/                   # MviViewModel, ViewIntent, ViewState, ViewEffect
│   ├── time/                   # TimeProvider, ArabicFormatters, DebugTimeProvider
│   ├── permissions/            # Xiaomi & system permission health checker
│   ├── audio/                  # AudioRecorder, VoicePlayer, TtsEngine
│   └── image/                  # ImageStore (local photo scaling & storage)
│
├── domain/                     # Pure business logic (no Android UI dependencies)
│   ├── model/                  # Domain models (Medication, Slot, DoseItem, DoseEvent, Contact, AppSettings)
│   ├── repository/             # Repository interfaces (IMedicationRepository, IDoseRepository, ...)
│   ├── scheduler/              # IAlarmScheduler abstraction
│   ├── engine/                 # Pure scheduling resolution engine (DoseEngine)
│   ├── ledger/                 # Pure dose transaction ledger (DoseLedger)
│   └── usecase/                # Single-responsibility use cases
│       ├── caregiver/          # GetCaregiverHomeStateUseCase, ConfirmDoseSlotUseCase, ...
│       ├── medications/        # GetMedicationsUseCase, SaveMedicationUseCase, ...
│       ├── history/            # GetTodayTimelineUseCase, GetDoseHistoryUseCase, ...
│       ├── settings/           # GetSettingsUseCase, VerifyAdminPinUseCase, ...
│       ├── contact/            # GetPrimaryContactUseCase, SavePrimaryContactUseCase
│       └── debug/              # SetDebugTimeOffsetUseCase, ResetTodayEventsUseCase
│
├── data/                       # Data layer & infrastructure
│   ├── db/                     # Room AppDatabase, DAOs, Entities
│   ├── repo/                   # Repository implementations (MedicationRepository, DoseRepository, ...)
│   ├── settings/               # DataStore SettingsRepository implementation
│   └── demo/                   # DemoSeeder
│
├── di/                         # Koin Dependency Injection modules
│   ├── AppModule.kt            # Core utilities, Audio, TimeProvider, AlarmScheduler
│   ├── DatabaseModule.kt       # Room Database, DAOs, DoseLedger
│   ├── RepositoryModule.kt     # Binds domain repository interfaces to data implementations
│   ├── UseCaseModule.kt        # Binds domain use cases
│   └── ViewModelModule.kt      # Binds MVI ViewModels
│
├── alarm/                      # Android AlarmManager & BroadcastReceivers
│   ├── AlarmReceiver.kt        # KoinComponent exact alarm receiver
│   ├── AndroidAlarmScheduler.kt# IAlarmScheduler implementation
│   ├── AlarmSync.kt            # Resync engine for exact alarms
│   ├── NotificationHelper.kt   # High-priority full-screen intent notifications
│   └── SystemReceivers.kt      # Boot & time change receivers
│
└── presentation/               # Jetpack Compose UI & MVI Presentation Layer
    ├── components/             # Reusable accessible components (BigActionButton, DoseCard, ...)
    ├── navigation/             # DawaNavGraph and Routes
    ├── theme/                  # DawaTheme, Colors, Typography (Cairo)
    │
    │   # Each feature is strictly split into 3 files:
    │   # 1. *Contract.kt   -> Pure UDF definitions (ViewIntent, ViewState, ViewEffect)
    │   # 2. *ViewModel.kt  -> ViewModel extending MviViewModel
    │   # 3. *Screen.kt     -> Composable screen collecting state & emitting intents
    │
    ├── caregiver/              # Grandma's screen & lock-screen ReminderActivity
    │   ├── CaregiverContract.kt
    │   ├── CaregiverViewModel.kt
    │   ├── CaregiverScreen.kt
    │   └── reminder/ReminderActivity.kt
    │
    └── admin/                  # Family Admin features
        ├── dashboard/          # AdminDashboardContract, AdminDashboardViewModel, AdminDashboardScreen
        ├── pin/                # AdminPinContract, AdminPinViewModel, AdminPinScreen
        ├── medications/        # AdminMedListContract, AdminMedListViewModel, AdminMedListScreen
        │   └── editor/         # AdminMedEditorContract, AdminMedEditorViewModel, AdminMedEditorScreen
        ├── history/            # AdminHistoryContract, AdminHistoryViewModel, AdminHistoryScreen
        ├── contact/            # AdminContactContract, AdminContactViewModel, AdminContactScreen
        ├── settings/           # AdminSettingsContract, AdminSettingsViewModel, AdminSettingsScreen
        ├── health/             # AdminHealthContract, AdminHealthViewModel, AdminHealthCheckScreen
        └── debug/              # DebugContract, DebugViewModel, DebugScreen
```

---

## 👵 The Caregiver Experience (Single-Screen Architecture)
Grandma never navigates through complex menus, tabs, or settings. The application consists of **one single screen** that deterministically displays one of four distinct visual states:

```
┌─────────────────────────────────┐      ┌─────────────────────────────────┐
│ ① IDLE (Green)                  │      │ ② DUE (Warm Amber)              │
│                                 │      │                                 │
│        🟢                       │      │        🔔                       │
│    مفيش دوا دلوقتي              │      │    دلوقتي معاد الدوا            │
│                                 │      │ ┌───────────┐ ┌───────────┐     │
│  ┌───────────────────────────┐  │      │ │  PHOTO A  │ │  PHOTO B  │     │
│  │ الدوا الجاي: ☀️ ٢:٠٠        │  │      │ │    💊     │ │   💊💊    │     │
│  │ [صورة الدواء]             │  │      │ └───────────┘ └───────────┘     │
│  └───────────────────────────┘  │      │                                 │
│                                 │      │ ┌─────────────────────────────┐ │
│         [ 🔊 اسمعي ]            │      │ │   ✔  اديت الدوا (Huge)       │ │
│   ✅ ⚪ ⚪ (Today Dots)        │      │ └─────────────────────────────┘ │
└─────────────────────────────────┘      └─────────────────────────────────┘

┌─────────────────────────────────┐      ┌─────────────────────────────────┐
│ ③ DONE (Celebratory Green - 4s) │      │ ④ MISSED (Calm Red)             │
│                                 │      │                                 │
│               ✅                 │      │               ⛔                │
│              تمام               │      │        اتأخر معاده              │
│      تسلم إيدك يا غالية         │      │     [صورة الدواء بالأحمر]       │
│                                 │      │ ┌─────────────────────────────┐ │
│    🔊 "تمام، تسلم إيدك.. خلصنا" │      │ │ 📞 كلّمي ماما (One-Tap Call) │ │
│                                 │      │ ├─────────────────────────────┤ │
│                                 │      │ │ 👍 حاضر (Acknowledge)       │ │
└─────────────────────────────────┘      └─────────────────────────────────┘
```

### 1. State A: IDLE (مفيش دوا دلوقتي — Green)
* A large, calming green circle (🟢).
* Clear reassurance: *"No medication right now. Everything is fine, take some rest."*
* **Next Dose Card:** Visual time-of-day indicator (🌅 morning / ☀️ noon / 🌙 night), time in large Arabic numerals (e.g. `٢:٠٠ مساءً`), and the photo of the upcoming medicine.
* **Today Dots Strip:** Glanceable progress dots showing today's timeline (✅ completed, ⚪ upcoming).
* **Speaker Button (🔊):** Plays the spoken announcement on demand.

### 2. State B: DUE (دلوقتي معاد الدوا — Warm Amber Alert)
* When a dose is due, the app rings with a pleasant harmonic chime, vibrates, and **launches over the lock screen** via `ReminderActivity` (even if the phone is locked).
* Spoken voice guidance automatically announces: *"It is medicine time... take the medicines shown on screen."*
* Features **giant photos** of the physical medications.
* Quantities are rendered as visual pill pictograms:
  * 💊 = 1 pill
  * 💊 💊 = 2 pills
  * ½ 💊 = half a pill
* **Huge Action Button: [ ✔ اديت الدوا ]** (Administered the medication).
* **3-Second Accidental Tap Guard:** The button stays disabled for the first 3 seconds after the screen turns on to prevent unintentional taps when picking up the phone.

### 3. State C: DONE (تمام — Green Confirmation)
* Tapping "اديت الدوا" immediately triggers an atomic transaction marking all medicines in the slot as completed.
* A full-screen celebratory card appears with a giant ✅ and the voice announcement: *"تمام، تسلم إيدك يا غالية.. خلصنا"*.
* Automatically dismisses after 4.5 seconds and returns to the Idle screen.
* **No confusing undo dialog:** If grandma taps the button by accident, family members can correct the entry from the Admin History screen.

### 4. State D: MISSED (الدوا اتأخر معاده — Calm Red Warning)
* If a dose is not confirmed within the grace period (default: 60 minutes):
* The screen turns into a calm red state (⛔) with dimmed medicine photos.
* **Strict Medical Safety:** The app **never** advises taking a double or late dose.
* Shows a massive one-tap call button: **[ 📞 كلّمي ماما ]** to contact the family, plus an **[ 👍 حاضر ]** button to acknowledge and return to Idle.

---

## 👨‍👩‍👦 Admin Mode (For Family Members Who Read)
To prevent the caregiver from accidentally altering settings:
* Entry requires a **3-second long-press** on the subtle gear icon at the top corner.
* Protected by a 4-digit PIN pad (default demo PIN: `1234`).

### Admin Capabilities:
1. **Medication Management:**
   * Add, edit, or archive medications.
   * Attach high-resolution photos of boxes, blisters, or pills with local cropping and compression.
   * Configure multiple schedules, meal relations (*Before Breakfast, After Breakfast, After Lunch, After Dinner, Before Sleep*), and quantities.
   * **Custom Family Voice Recordings:** Record a personal audio instruction per medication (e.g., *"Grandma, this is grandpa's blood pressure pill after breakfast"*).
2. **Medication History:**
   * 30-day historical ledger recording scheduled times, actual confirmation timestamps, and who confirmed.
   * Allows the family to correct mistakes (e.g., mark a missed dose as "taken late").
3. **Emergency Contact:**
   * Configure the primary family contact (name, phone number, and photo) for the one-tap call button.
4. **Settings:**
   * Configure meal baseline times, grace periods, re-alert intervals, change PIN, and view the medical disclaimer.
5. **Health Check (Xiaomi & Permissions):**
   * Live validator for exact alarm permissions, notification channels, battery optimization, and Xiaomi Autostart.
6. **Developer & Debug Tools:**
   * Test a real lock-screen alarm 10 seconds into the future.
   * Fast-forward virtual time (+15m, +1h, +1d) to simulate a complete daily schedule in under two minutes.
   * Reset today's events.

---

## ⚙️ Xiaomi Android 15 (HyperOS / MIUI) Configuration
Xiaomi devices feature aggressive background task management. To guarantee alarms fire and display over the lock screen:
1. **Autostart (التشغيل التلقائي):**
   * Open the app -> Admin -> "فحص الصلاحيات" -> tap **"فتح إعدادات شاومي"** -> enable **Autostart** for **دوا**.
2. **Battery Saver (موفر البطارية):**
   * In app battery settings, set to **"No restrictions" (بدون قيود)**.
3. **Lock Screen Display:**
   * In Xiaomi App Permissions, ensure **"Show on Lock screen"** and **"Display pop-up windows while running in the background"** are granted.

---

## 🧪 Pre-Seeded Demo Data
On first install, the database automatically populates with realistic test data:
1. **بانادول أزرق للصداع (Panadol):** 08:00 AM (1 pill) — Before Breakfast
2. **كونكور للضغط (Concor):** 08:00 AM (2 pills) — After Breakfast
3. **مكمل فيتامين د (Vitamin D):** 02:00 PM (1 pill) — After Lunch
4. **أوميجا ٣ مسائي (Omega 3):** 08:00 PM (1 pill) — After Dinner
5. **Primary Contact:** ماما (01012345678)
6. **Admin PIN:** `1234`

---

## 🚀 Building & Installation

### Install the Ready APK Directly
If your Android device is connected via USB with USB Debugging enabled:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
*Alternatively, transfer `app/build/outputs/apk/debug/app-debug.apk` to your phone via USB, WhatsApp, or Bluetooth and tap to install.*

### Build from Source
```bash
cd /Users/islam/AndroidStudioProjects/Dawa
./gradlew testDebugUnitTest  # Execute pure unit tests
./gradlew assembleDebug      # Compile and package debug APK
```
