# ⌨️ Nani Typing — The Minimalist TV-Typer for Young Explorers

> **A low-stimulation, child-led phonetic typing terminal built for Android TV, tablets, and physical keyboards.**  
> *Zero cartoons. Zero ads. Zero subscriptions. 100% offline-first.*

---

## 🌟 The Philosophy & Origin Story

Commercial children's educational apps have become hyperactive dopamine traps: flashing stars, cartoon explosions, gamified reward loops, and noisy sound effects that shatter young attention spans.

**Nani Typing** was born from a simple real-world observation:
* Our son (LKG stage) refused rigid flashcard drills, but loved building physical car tracks and ramps out of pillows and doormats to test gravity and velocity.
* He wanted to mimic his parents typing on their laptops.
* Instead of giving him a smartphone with cartoon apps, we gave him a **physical, wired USB keyboard** connected to our **Android TV**.

The result is a calm, distraction-free phonetic typing workstation where children lead their own organic exploration without adult interruption.

---

## 🎴 The Card Launcher & Navigation Flow

The app opens into a clean, high-contrast, tactile launcher designed specifically for Android TV remote D-pads and physical keyboards:

```
┌──────────────────────────────────────────────────────────────────┐
│                           NANI TYPING                            │
│              Minimalist Phonics & Engineering Typer              │
│                                                                  │
│   ┌─────────────────────────────┐   ┌─────────────────────────┐  │
│   │ [LEVEL 1]                   │   │ [PREFERENCES]           │  │
│   │ Free Typing                 │   │ Settings                │  │
│   │ Phonics, Words & Physics    │   │ Voice, Pitch & USB      │  │
│   │ (Press Enter / Start Typing)│   │ (Configure Voice)       │  │
│   └─────────────────────────────┘   └─────────────────────────┘  │
│                                                                  │
│        [ ← → Arrow Keys to select • Enter / Space to start ]      │
└──────────────────────────────────────────────────────────────────┘
```

* **Card 1: Level 1 (Free Typing) — Selected by Default:** Instant entry into the typing canvas.
* **Card 2: Settings (Voice & Preferences):** Configure speech pace, voice pitch, regional accent, and USB drive storage.
* **Child-First Zero-Friction Rule:** If your child sits down and strikes any letter or digit key directly from the main menu, the app **instantly bypasses the menu**, launches Level 1, and types that character. There is zero menu trapping.

---

## 🚀 The 3 Progressive Educational Levels

```
Level 1: Phonics, Letter Spelling & Sentence Synthesis
  ├─ Real-time letter pronunciation on keypress
  ├─ Spacebar: Word spelling loop (1.5x orange zoom) → Whole-word speech
  └─ Enter key: Full-line sentence synthesis → Clean line break

Level 2: Real-World Image Association (Zero-DB Filename Trigger)
  ├─ Real photographs of family, toys, neighborhood hens (kodi.webp, hen.png)
  └─ Dual-engine scanner (MediaStore + USB OTG volume discovery with subfolder support)

Level 3: Action Logic & Kinetic Physics
  └─ Active words (FAST, SLOW, STOP, DOWN, UP, BOUNCE, JUMP, ZOOM) trigger real physics dynamics
```

### 1. Level 1: Phonics, Words & Sentence Synthesis

* **Immediate Key Speech:** Pressing any physical key immediately speaks the uppercase letter name aloud (`"A"`, `"B"`, `"7"`).
* **Spacebar (Word Phonics Loop):** When finishing a word, hitting **Space**:
  1. Cycles back through the word letter-by-letter (`C - A - T`).
  2. Each letter zooms up to $1.5\times$ scale and turns vibrant orange in sync with audio.
  3. Pauses and pronounces the complete word (`"CAT"`).
  4. Triggers Level 2 real photo or Level 3 physics action if matched.
* **Enter Key (Sentence Reading):** Hitting **Enter** spells and pronounces the current word, pauses $450\text{ms}$, and then **synthesizes and reads all words in the line together as a fluent, continuous sentence**, followed by an organic line break (`\n`).

### 2. Level 2: Real-World Image Association (Zero-DB Engine)

* **Zero Database Overhead:** No databases, cloud setups, or complex configurations.
* **Filename-as-Trigger:** A photo named `kodi.webp` or `hen.png` automatically triggers when typing `KODI` or `HEN`.
* **Real Photos, No Cartoons:** Anchors written language to concrete real-world objects in the child's actual environment (family members, pets, household items).
* **Subfolder Support:** Organizes photos into subfolders up to 3 levels deep (`NaniTyping/Family/mamma.webp`, `NaniTyping/Vehicles/car.jpg`, `NaniTyping/Animals/hen.png`).
* **TV Memory Guard:** Downsamples photos to `RGB_565` format to prevent Out-Of-Memory (OOM) crashes on budget Android TV chipsets.

### 3. Level 3: Action Logic & Kinetic Physics

Harmonizes typing with the child's engineering play. Active words trigger real physical movements on screen using an architectural wooden vehicle:

* **`FAST` / `SPEED` / `RUN` / `ZOOM`:** High-velocity linear rush across the canvas ($450\text{ms}$).
* **`SLOW` / `CRAWL`:** Deliberate, measured crawl across the screen ($3.5\text{s}$).
* **`STOP` / `HALT` / `WAIT`:** Screeches to an immediate halt with realistic braking suspension tilt and recoil.
* **`DOWN` / `DROP` / `FALL`:** Descends an architectural ramp under simulated gravity.
* **`UP` / `CLIMB`:** Powers uphill against gravitational resistance.
* **`BOUNCE` / `HOP`:** Drops with natural spring rebound and squash-and-stretch.
* **`JUMP`:** Smooth parabolic upward leap and return to ground.

*Typing agency is always first:* If the child strikes any key while an image or animation is playing, it dismisses instantly without blocking typing flow.

---

## ⚙️ Built-In TV Settings Screen

Easily accessible via physical keyboard or TV remote D-pad:

* **Speech Speed:** `Very Slow (0.55x)`, `Child Pace (0.70x)` *(default)*, `Normal (1.00x)`.
* **Voice Pitch:** `Normal (1.0x)`, `Child Friendly (1.12x)` *(default)*, `Higher (1.25x)`.
* **Voice Accent:** `Indian English (en-IN)` *(default)*, `US English (en-US)`, `UK English (en-GB)`.
* **Real-World Photos:** Live indexed count (e.g. `12 photos indexed`) + instant refresh.
* **USB Drive Access:** One-click launcher for Android TV's *All Files Access* permission screen.

---

## 🔌 Plug-and-Play Setup for USB Drives

1. **Insert any USB drive** into your computer or phone.
2. **Create a folder named:** `NaniTyping` (case-insensitive: `nanityping`, `NANITYPING` all work).
3. **Drop photos inside**, named after the target word:
   * `kodi.png` &rarr; Triggers on **KODI** (Telugu)
   * `hen.png` &rarr; Triggers on **HEN** (English)
   * `mamma.jpg` &rarr; Triggers on **MAMMA**
   * `Vehicles/car.webp` &rarr; Triggers on **CAR**
4. **Plug into the Android TV.** In Settings, ensure **USB Drive Access** is granted. The app indexes all photos immediately!

For full instructions, read the [USB Setup Guide](USB_SETUP_GUIDE.md).

---

## ⌨️ Physical TV Keyboard Controls Reference

| Key | Context | Behavior |
| :--- | :--- | :--- |
| **Any Letter / Digit** | Main Menu | Bypasses menu, immediately enters Level 1 and types character |
| **Any Letter / Digit** | Level 1 Canvas | Types character + speaks letter name aloud |
| **Arrow Keys (`←` `→`)** | Main Menu | Switches selection between Level 1 and Settings cards |
| **Arrow Keys (`↑` `↓`)** | Settings | Highlights setting row |
| **Enter / Space** | Main Menu | Launches the highlighted card |
| **Spacebar** | Level 1 Canvas | Spells word letter-by-letter $\rightarrow$ Speaks full word $\rightarrow$ Triggers photo/physics $\rightarrow$ Appends `" "` |
| **Enter / Numpad Enter** | Level 1 Canvas | Spells word (if mid-word) $\rightarrow$ Pronounces word $\rightarrow$ **Reads entire sentence** $\rightarrow$ Newline (`\n`) |
| **Backspace / Delete** | Level 1 Canvas | Erases last character and mutes ongoing speech immediately |
| **Escape / Back** | Level 1 Canvas | If text is present, resets canvas with *"Reset"*. If canvas is empty, returns to Main Menu |
| **Escape / Back** | Main Menu | Exits app to Android TV Home screen |

---

## 🛡️ Privacy & Family Safety

* **No Internet Permission:** Does not declare `android.permission.INTERNET`. The app is physically incapable of transmitting data off the device.
* **Zero Tracking or Analytics:** No Firebase, no third-party SDKs, no advertising.
* **COPPA & Google Play Families Compliant:** Fully safe for children under 13.
* **Local Offline Storage Only:** Storage permission is used solely to read local photos from USB drives.
* Official Privacy Policy: [docs/privacy.html](docs/privacy.html)

---

## 🛠️ Tech Stack & Architecture

* **Platform:** Android TV (Leanback) + Android Tablets, Chromebooks, and Phones
* **Language:** Kotlin 2.0+
* **UI Toolkit:** Jetpack Compose (Material 3) with hardware-accelerated `Animatable` physics
* **Audio Engine:** Android Text-to-Speech (TTS) with low-latency `USAGE_ASSISTANCE_ACCESSIBILITY` audio attributes and HDMI sleep pre-warming
* **Storage Engine:** Android MediaStore + Scoped Storage USB OTG recursive volume indexer
* **Debounce Filter:** Hardware key repeat filter (`repeatCount > 0` debounce) for pre-school hand pressure

---

## 🌐 Public Website & Documentation

The project includes a responsive website located in [`docs/`](docs/) and [`website/`](website/):
* `index.html` — Public landing page and origin story
* `privacy.html` — Google Play Families Policy compliant Privacy Policy
* `guide.html` — Comprehensive Parent & Teacher setup guide
* `terms.html` — Terms of use and open educational license
* `styles.css` — Minimalist, responsive design system

---

## 📄 License

Open-source and non-profit under the **Apache 2.0 / MIT License**. Free for families, public schools, and community learning centers worldwide.
