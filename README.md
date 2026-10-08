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

The result is a calm, distraction-free phonetic typing workstation where children lead their own organic exploration.

---

## 🚀 The 3 Progressive Levels

```
Level 1: Phonics & Letter-by-Letter Spelling Loop
  └─ Real-time letter pronunciation → Spacebar word spellout → Full word speech

Level 2: Real-World Image Association (Zero-DB Filename Trigger)
  └─ Real photographs of family, toys, neighborhood hens (kodi.webp, hen.png)

Level 3: Action Logic & Kinetic Physics
  └─ Active words (FAST, SLOW, STOP, DOWN, UP, BOUNCE) trigger real physics dynamics
```

### 1. Level 1: Phonics & Spelling Loop
* **Immediate Key Speech:** Pressing any physical key immediately speaks the uppercase letter name aloud (`"A"`, `"B"`, `"7"`).
* **Word Spelling Cycle:** When the child finishes typing a word and presses **Spacebar** or **Enter**:
  1. Each letter is read aloud one by one (`C - A - T`).
  2. The letter zooms up to $1.5\times$ scale and turns vibrant orange in sync with the audio.
  3. The TTS engine pauses and pronounces the complete word (`"CAT"`).

### 2. Level 2: Real-World Image Association (Zero-DB Engine)
* **Zero Database Overhead:** No database or cloud configuration.
* **Filename-as-Trigger:** A photo named `kodi.webp` or `hen.png` automatically triggers when typing `KODI` or `HEN`.
* **Real Photos, No Cartoons:** Anchors written language to concrete real-world objects in the child's actual environment (family members, pets, household items).
* **TV Memory Guard:** Downsamples photos to `RGB_565` format to prevent Out-Of-Memory (OOM) crashes on budget 1.5GB–2GB RAM Android TV chips.

### 3. Level 3: Action Logic & Kinetic Physics
Harmonizes typing with the child's engineering play. Active words trigger real physical movements on screen using an architectural wooden vehicle:
* **`FAST` / `SPEED` / `RUN`:** High-velocity linear rush across the canvas ($450\text{ms}$).
* **`SLOW` / `CRAWL`:** Deliberate, measured crawl ($3.5\text{s}$).
* **`STOP` / `HALT`:** Screeches to a halt with realistic braking suspension tilt and recoil.
* **`DOWN` / `FALL`:** Descends an architectural ramp under simulated gravity.
* **`UP` / `CLIMB`:** Powers uphill against gravity.
* **`BOUNCE` / `HOP`:** Drops with natural spring rebound and squash-and-stretch.
* **`JUMP`:** Smooth parabolic leap and return to ground.

*Typing agency is always first:* If the child hits any key while an image or animation is playing, it dismisses instantly without blocking typing flow.

---

## 🔌 Zero-Maintenance Setup for Public Schools & Families

Designed to scale effortlessly to public school computer labs and rural learning centers without technical maintenance:

1. **Grab any USB thumb drive.**
2. **Create a folder named:** `NaniTyping`
3. **Drop photos inside**, naming each file after the target word:
   * `kodi.png` &rarr; Triggers on **KODI** (Telugu)
   * `hen.png` &rarr; Triggers on **HEN** (English)
   * `mamma.jpg` &rarr; Triggers on **MAMMA**
4. **Plug into the Android TV / Tablet and launch.** The app auto-discovers and indexes the files immediately.

For full instructions, read the [USB Setup Guide](USB_SETUP_GUIDE.md).

---

## 🛡️ Privacy & Family Safety

* **No Internet Permission:** Does not declare `android.permission.INTERNET`. The app is physically incapable of transmitting data off the device.
* **Zero Tracking or Analytics:** No Firebase, no third-party SDKs, no advertising.
* **COPPA & Google Play Families Compliant:** Fully safe for children under 13.
* Official Privacy Policy: [docs/privacy.html](docs/privacy.html)

---

## 🛠️ Tech Stack & Architecture

* **Platform:** Android TV (Leanback) + Android Tablets, Chromebooks, and Phones
* **Language:** Kotlin 2.0+
* **UI Toolkit:** Jetpack Compose (Material 3) with hardware-accelerated `Animatable` physics
* **Audio Engine:** Android Text-to-Speech (TTS) with low-latency `USAGE_ASSISTANCE_ACCESSIBILITY` audio attributes and HDMI sleep pre-warming
* **Storage:** Android App-Specific External Storage + USB OTG volume auto-scanning

---

## 🌐 Public Website & Documentation

The project includes a responsive website located in the [`docs/`](docs/) directory:
* `index.html` — Public landing page and origin story
* `privacy.html` — Google Play Families Policy compliant Privacy Policy
* `guide.html` — Comprehensive Parent & Teacher setup guide
* `terms.html` — Terms of use and open educational license
* `styles.css` — Minimalist, responsive design system

---

## 📄 License

Open-source and non-profit under the **Apache 2.0 / MIT License**. Free for families, public schools, and community learning centers worldwide.
