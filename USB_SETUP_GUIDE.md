# 🔌 USB Plug-and-Play & Teacher Setup Guide

> **Nani Typing:** Zero-Database, Zero-Maintenance Educational Architecture for Android TV

---

## 🚀 Quick Start in 60 Seconds

1. **Insert any USB drive** into your computer or phone.
2. **Create a folder named:** `NaniTyping` *(Case-insensitive: `nanityping`, `NANITYPING`, `Nani Typing` all work)*.
3. **Drop your photos & custom voice recordings inside**, naming each file after the word you want to trigger:
   * **Photos:**
     * `kodi.webp` &rarr; Triggers photo when your child types **KODI**
     * `hen.png` &rarr; Triggers photo when your child types **HEN**
     * `amma.jpg` &rarr; Triggers photo when your child types **AMMA**
     * `car.webp` &rarr; Triggers photo when your child types **CAR**
   * **Custom Voice Recordings (Parents / Grandparents / Teachers):**
     * `amma.m4a` / `amma.mp3` &rarr; Plays your warm real-world recorded voice saying **"Amma"**!
     * `nanna.m4a` / `nanna.wav` &rarr; Plays dad's voice saying **"Nanna"**!
     * `kodi.ogg` / `kodi.m4a` &rarr; Plays grandma's voice saying **"Kodi"**!
     * *Supported audio formats:* `.m4a`, `.mp3`, `.wav`, `.ogg`, `.aac`, `.3gp`, `.flac`.
4. **Subfolders are supported!** You can organize files cleanly up to 4 levels deep:
   * `NaniTyping/Family/amma.webp` & `NaniTyping/Family/amma.m4a`
   * `NaniTyping/sounds/kodi.mp3` or `NaniTyping/voices/nanna.m4a`
   * `NaniTyping/Animals/hen.jpg`
5. **Plug the USB drive into your Android TV or tablet.**
6. **One-Time TV Permission Grant (Android 11+ / Google TV):**
   * Open Nani Typing.
   * Go to the **Settings** card.
   * Select **USB Drive Access** and press **Enter**.
   * Toggle **Allow** on the Android TV system screen, then press Back.
   * You will see the counter update to **"X photos • Y voices indexed"**!

---

## 🌍 Instant Multilingual Learning (Zero Translation Tables)

Because the trigger is directly the filename, you can teach any language or dialect without code changes:

| Language | Example Filename | Trigger Word |
| :--- | :--- | :--- |
| **Telugu** | `kukka.jpg` | **KUKKA** (Dog) |
| **Hindi** | `kutta.jpg` | **KUTTA** (Dog) |
| **Tamil** | `naai.jpg` | **NAAI** (Dog) |
| **English** | `dog.jpg` | **DOG** |
| **Telugu** | `kodi.png` | **KODI** (Hen) |
| **English** | `hen.png` | **HEN** |

*Matching is completely case-insensitive (`DOG`, `dog`, `Dog` all match).*

---

## 🎙️ Zero-Latency Custom Voice Recordings (Parent Voice Priority)

Parents and teachers often ask: *"Will there be any delay or latency when playing our custom voice recordings?"*

**Answer: No! It is practically zero latency (< 10ms).**
* Pre-recorded compressed audio files (`.m4a`, `.mp3`, `.wav`, `.ogg`, `.flac`) play **faster than synthetic Text-to-Speech**.
* The app routes custom audio through Android's `USAGE_ASSISTANCE_ACCESSIBILITY` stream, keeping TV HDMI audio chips awake.
* When your child finishes typing a word (e.g. **AMMA**):
  1. The phonics engine spells each letter (`A - M - M - A`) with visual orange magnification.
  2. The parent's recorded voice immediately plays: *"Amma!"* while the family photo flashes on screen.
  3. If **Enter** was pressed, after the voice finishes, the engine speaks the entire typed sentence together!
* **Silent Fallback:** If you haven't recorded a word yet, the system smoothly falls back to synthetic TTS without any stutter or delay.
* **Child Typing Agency First:** If your child hits any keyboard key while your voice is playing, it mutes immediately so their typing rhythm is never interrupted.

---

## 🏎️ Kinetic Action Words Reference

Typing these active words triggers real physical kinetic dynamics on the screen using an architectural vehicle:

* `FAST`, `SPEED`, `RUN`, `ZOOM` &rarr; High-velocity linear rush across the canvas ($450\text{ms}$)
* `SLOW`, `CRAWL` &rarr; Measured deliberate crawl across the canvas ($3.5\text{s}$)
* `STOP`, `HALT`, `WAIT` &rarr; Screeches to an immediate halt with braking recoil and suspension tilt
* `DOWN`, `DROP`, `FALL` &rarr; Gravitational descent down an architectural ramp
* `UP`, `CLIMB` &rarr; Uphill climb overcoming gravity
* `BOUNCE`, `HOP` &rarr; Free-fall drop with elastic spring rebound and squash-and-stretch
* `JUMP` &rarr; Smooth parabolic upward leap and return to ground

*Typing is always prioritized:* If your child hits any key while a photo or animation is playing, it dismisses instantly so typing is never blocked.

---

## ⌨️ Physical Keyboard Controls

* **Any Key on Main Menu:** Automatically starts Level 1 and types the pressed letter.
* **Any Key on Typing Canvas:** Speaks uppercase letter name immediately.
* **Spacebar:** Spells completed word letter-by-letter with orange magnification &rarr; plays custom parent voice or speaks whole word &rarr; flashes photo or physics action &rarr; appends `" "`.
* **Enter / Numpad Enter:** Spells and pronounces word (or plays custom parent voice), then **reads all words in the line together as a full fluent sentence** &rarr; moves to a new line (`\n`).
* **Backspace (`DEL` / `FORWARD_DEL`):** Erases last letter and mutes any ongoing audio immediately.
* **Escape / Back:** If text is present, resets canvas with *"Reset"*. If canvas is empty, returns to Main Menu cards.
