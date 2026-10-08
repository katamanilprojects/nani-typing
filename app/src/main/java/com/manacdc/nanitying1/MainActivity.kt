package com.manacdc.nanityping1

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class AppScreen {
    MENU,
    TYPING_LEVEL_1,
    SETTINGS
}

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isEngineReady by mutableStateOf(false)
    private var pendingSpeechText: String? = null

    // Navigation & Screen State
    private var currentScreen by mutableStateOf(AppScreen.MENU)
    private var selectedMenuCard by mutableStateOf(MenuCard.LEVEL_1)
    private var selectedSettingsRow by mutableStateOf(SettingsRow.SPEED)
    private var photoCount by mutableIntStateOf(0)
    private var soundCount by mutableIntStateOf(0)
    private var hasAllFilesAccess by mutableStateOf(false)
    private var speechSpeed by mutableFloatStateOf(AppSettings.DEFAULT_SPEED)
    private var speechPitch by mutableFloatStateOf(AppSettings.DEFAULT_PITCH)
    private var speechLocaleCode by mutableStateOf(AppSettings.DEFAULT_LOCALE)

    private var typedText by mutableStateOf("")
    private var activeSpeakingIndex by mutableStateOf(-1)

    // Level 2: Real-World Image Association state
    private var activeImageBitmap by mutableStateOf<ImageBitmap?>(null)
    private var activeImageWord by mutableStateOf("")
    private var pendingImageBitmap: ImageBitmap? = null
    private var pendingImageWord: String = ""
    private var photoLoadingJob: Job? = null
    private var activePhotoWord: String? = null
    private var lastSpokenWord: String = ""

    // Level 2+: Real-World Custom Voice Recording state
    private var pendingCustomSound: SoundEntry? = null
    private var pendingSentenceAfterCustomSound: String? = null

    // Level 3: Action Logic & Physics Dynamics state
    private var activeKineticAction by mutableStateOf<KineticActionType?>(null)
    private var pendingKineticAction: KineticActionType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speechSpeed = AppSettings.getSpeed(applicationContext)
        speechPitch = AppSettings.getPitch(applicationContext)
        speechLocaleCode = AppSettings.getLocaleCode(applicationContext)
        hasAllFilesAccess = ImageManager.hasAllFilesAccess()

        try {
            tts = TextToSpeech(applicationContext, this)
        } catch (e: Throwable) {
            Log.e("MainActivity", "TTS setup safety failure: ${e.message}")
        }

        // Request storage/media permissions at runtime on TV if needed
        checkAndRequestStoragePermissions()

        // Index real-world trigger images & custom voice recordings on startup asynchronously
        reloadMediaAsync()

        setContent {
            when (currentScreen) {
                AppScreen.MENU -> {
                    MainMenuScreen(
                        selectedCard = selectedMenuCard,
                        onSelectCard = { selectedMenuCard = it },
                        onLaunchCard = { card ->
                            currentScreen = if (card == MenuCard.LEVEL_1) AppScreen.TYPING_LEVEL_1 else AppScreen.SETTINGS
                        }
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        selectedRow = selectedSettingsRow,
                        photoCount = photoCount,
                        soundCount = soundCount,
                        speed = speechSpeed,
                        pitch = speechPitch,
                        localeCode = speechLocaleCode,
                        hasAllFilesAccess = hasAllFilesAccess,
                        isTtsReady = isTtsReady,
                        onCycleSpeed = {
                            handleSettingsRowAction(SettingsRow.SPEED, direction = 1)
                        },
                        onCyclePitch = {
                            handleSettingsRowAction(SettingsRow.PITCH, direction = 1)
                        },
                        onCycleLocale = {
                            handleSettingsRowAction(SettingsRow.LOCALE, direction = 1)
                        },
                        onReloadPhotos = {
                            reloadMediaAsync()
                        },
                        onRequestAllFilesAccess = {
                            ImageManager.requestAllFilesAccess(this)
                        },
                        onBackToMenu = {
                            currentScreen = AppScreen.MENU
                        }
                    )
                }

                AppScreen.TYPING_LEVEL_1 -> {
                    val scrollState = rememberScrollState()

                    LaunchedEffect(typedText) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    // Auto-dismiss real-world photo after 2.8 seconds
                    LaunchedEffect(activeImageBitmap) {
                        if (activeImageBitmap != null) {
                            delay(2800)
                            activeImageBitmap = null
                            activeImageWord = ""
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFFFDD0)) // Warm cream background
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (typedText.isEmpty()) {
                            Text(
                                text = "NANI TYPING!",
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.SansSerif,
                                color = if (isEngineReady) Color.LightGray else Color.LightGray.copy(alpha = 0.35f)
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    maxItemsInEachRow = 100
                                ) {
                                    // 🟢 FLAT INDEX FIXED LOOP: Direct 1:1 match with string indices
                                    typedText.forEachIndexed { index, char ->
                                        if (char == '\n') {
                                            // Forces an organic layout break to a new line without breaking string index synchronization
                                            Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))
                                        } else {
                                            val isCurrentSpeaking = index == activeSpeakingIndex

                                            val animatedScale by animateFloatAsState(
                                                targetValue = if (isCurrentSpeaking) 1.5f else 1.0f,
                                                animationSpec = tween(durationMillis = 100),
                                                label = "FontScale"
                                            )

                                            Text(
                                                text = char.toString(),
                                                fontSize = 55.sp,
                                                fontWeight = if (isCurrentSpeaking) FontWeight.ExtraBold else FontWeight.Black,
                                                fontFamily = FontFamily.SansSerif,
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .scale(animatedScale),
                                                color = if (isCurrentSpeaking) Color(0xFFFF4500) else Color(0xFF0B1B3D)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 🟢 LEVEL 2: REAL-WORLD IMAGE ASSOCIATION OVERLAY (MINIMALIST)
                        AnimatedVisibility(
                            visible = activeImageBitmap != null,
                            enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.88f, animationSpec = tween(250)),
                            exit = fadeOut(tween(350)) + scaleOut(targetScale = 0.95f, animationSpec = tween(350))
                        ) {
                            activeImageBitmap?.let { bmp ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = Color.White,
                                        shadowElevation = 10.dp,
                                        modifier = Modifier
                                            .padding(32.dp)
                                            .wrapContentSize()
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Image(
                                                bitmap = bmp,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .sizeIn(maxWidth = 640.dp, maxHeight = 400.dp)
                                                    .padding(8.dp),
                                                contentScale = ContentScale.Fit
                                            )
                                            if (activeImageWord.isNotEmpty()) {
                                                Text(
                                                    text = activeImageWord,
                                                    fontSize = 42.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = Color(0xFF0B1B3D),
                                                    letterSpacing = 1.sp,
                                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 🟢 LEVEL 3: KINETIC ACTION PHYSICS OVERLAY (MINIMALIST)
                        activeKineticAction?.let { action ->
                            KineticPhysicsOverlay(
                                action = action,
                                onFinish = { activeKineticAction = null }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 🔴 PRE-SCHOOL DEFENSIVE FILTER: Ignore hardware key auto-repeat before anything else
        if (event != null && event.repeatCount > 0) {
            return true
        }

        // Cancel any pending asynchronous photo decodes from previous keystroke
        photoLoadingJob?.cancel()
        photoLoadingJob = null
        activePhotoWord = null

        // 🟢 PRE-SCHOOL DEFENSIVE: Stop any ongoing custom audio immediately so typing is never blocked
        SoundManager.stop()
        try { tts?.stop() } catch (e: Exception) {}
        pendingCustomSound = null
        pendingSentenceAfterCustomSound = null

        // Clear visible image & kinetic actions immediately on any user action so typing is never blocked
        if (activeImageBitmap != null) {
            activeImageBitmap = null
            activeImageWord = ""
        }
        pendingImageBitmap = null
        pendingImageWord = ""

        if (activeKineticAction != null) {
            activeKineticAction = null
        }
        pendingKineticAction = null

        event?.let {
            val pressedChar = it.unicodeChar.toChar()

            when (currentScreen) {
                AppScreen.MENU -> {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        selectedMenuCard = MenuCard.LEVEL_1
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        selectedMenuCard = MenuCard.SETTINGS
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
                        currentScreen = if (selectedMenuCard == MenuCard.LEVEL_1) AppScreen.TYPING_LEVEL_1 else AppScreen.SETTINGS
                        return true
                    }
                    // Child-friendly auto-enter: If child strikes any letter/digit directly on menu, launch Level 1 and type it!
                    if (it.unicodeChar != 0 && pressedChar.isLetterOrDigit()) {
                        currentScreen = AppScreen.TYPING_LEVEL_1
                        activeSpeakingIndex = -1
                        speakText(pressedChar.uppercaseChar().toString(), TextToSpeech.QUEUE_FLUSH)
                        typedText += pressedChar
                        return true
                    }
                    return super.onKeyDown(keyCode, event)
                }

                AppScreen.SETTINGS -> {
                    val rows = SettingsRow.entries
                    val currentIndex = rows.indexOf(selectedSettingsRow)
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        selectedSettingsRow = rows[(currentIndex - 1 + rows.size) % rows.size]
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        selectedSettingsRow = rows[(currentIndex + 1) % rows.size]
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        handleSettingsRowAction(selectedSettingsRow, direction = -1)
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        handleSettingsRowAction(selectedSettingsRow, direction = 1)
                        return true
                    }
                    if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                        currentScreen = AppScreen.MENU
                        return true
                    }
                    return super.onKeyDown(keyCode, event)
                }

                AppScreen.TYPING_LEVEL_1 -> {
                    // Consume TV remote DPAD arrows on canvas so TV doesn't emit error beeps or lose canvas focus
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        return true
                    }

                    if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                        activeSpeakingIndex = -1
                        checkAndSpeakOnEnter()
                        typedText += "\n"
                        if (typedText.length > 800) typedText = typedText.takeLast(600)
                        return true
                    }

                    if (keyCode == KeyEvent.KEYCODE_SPACE) {
                        activeSpeakingIndex = -1
                        checkAndSpeakOnSpace()
                        typedText += " "
                        if (typedText.length > 800) typedText = typedText.takeLast(600)
                        return true
                    }

                    if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                        if (typedText.isNotEmpty()) {
                            try { tts?.stop() } catch (e: Exception) {}
                            typedText = ""
                            activeSpeakingIndex = -1
                            speakText("Reset", TextToSpeech.QUEUE_FLUSH)
                            return true
                        } else {
                            // Canvas is already empty: Escape / Back returns to Main Menu
                            currentScreen = AppScreen.MENU
                            return true
                        }
                    }

                    if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
                        if (typedText.isNotEmpty()) {
                            try { tts?.stop() } catch (e: Exception) {}
                            activeSpeakingIndex = -1
                            typedText = typedText.dropLast(1)
                        }
                        return true
                    }

                    if (it.unicodeChar != 0 && (pressedChar.isLetterOrDigit() || pressedChar.isWhitespace())) {
                        activeSpeakingIndex = -1
                        if (pressedChar.isWhitespace()) {
                            checkAndSpeakOnSpace()
                        } else {
                            speakText(pressedChar.uppercaseChar().toString(), TextToSpeech.QUEUE_FLUSH)
                        }

                        typedText += pressedChar
                        if (typedText.length > 800) typedText = typedText.takeLast(600)
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun handleSettingsRowAction(row: SettingsRow, direction: Int = 1) {
        when (row) {
            SettingsRow.SPEED -> {
                val idx = AppSettings.SPEED_OPTIONS.indexOfFirst { it.first == speechSpeed }
                val nextIdx = (idx + direction + AppSettings.SPEED_OPTIONS.size) % AppSettings.SPEED_OPTIONS.size
                val next = AppSettings.SPEED_OPTIONS[nextIdx].first
                speechSpeed = next
                AppSettings.setSpeed(applicationContext, next)
                AppSettings.applyToTts(applicationContext, tts)
            }
            SettingsRow.PITCH -> {
                val idx = AppSettings.PITCH_OPTIONS.indexOfFirst { it.first == speechPitch }
                val nextIdx = (idx + direction + AppSettings.PITCH_OPTIONS.size) % AppSettings.PITCH_OPTIONS.size
                val next = AppSettings.PITCH_OPTIONS[nextIdx].first
                speechPitch = next
                AppSettings.setPitch(applicationContext, next)
                AppSettings.applyToTts(applicationContext, tts)
            }
            SettingsRow.LOCALE -> {
                val idx = AppSettings.LOCALE_OPTIONS.indexOfFirst { it.first == speechLocaleCode }
                val nextIdx = (idx + direction + AppSettings.LOCALE_OPTIONS.size) % AppSettings.LOCALE_OPTIONS.size
                val next = AppSettings.LOCALE_OPTIONS[nextIdx].first
                speechLocaleCode = next
                AppSettings.setLocaleCode(applicationContext, next)
                AppSettings.applyToTts(applicationContext, tts)
            }
            SettingsRow.RELOAD_STORAGE -> {
                reloadMediaAsync()
            }
            SettingsRow.USB_PERMISSION -> {
                ImageManager.requestAllFilesAccess(this)
            }
            SettingsRow.BACK_TO_MENU -> {
                currentScreen = AppScreen.MENU
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-index images & sounds on resume asynchronously so hot-plugged USB drives appear immediately
        reloadMediaAsync()
    }

    override fun onPause() {
        super.onPause()
        // Cleanly stop any ongoing audio if home button is pressed or user switches away
        try {
            tts?.stop()
            SoundManager.stop()
            activeSpeakingIndex = -1
        } catch (e: Exception) {
            Log.w("MainActivity", "Error stopping audio onPause: ${e.message}")
        }
    }

    private fun reloadMediaAsync() {
        hasAllFilesAccess = ImageManager.hasAllFilesAccess()
        lifecycleScope.launch(Dispatchers.IO) {
            val pCount = ImageManager.reloadRegistry(applicationContext)
            val sCount = SoundManager.reloadRegistry(applicationContext)
            withContext(Dispatchers.Main) {
                photoCount = pCount
                soundCount = sCount
            }
        }
    }

    private fun checkAndRequestStoragePermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1001)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        reloadMediaAsync()
    }

    private fun checkAndSpeakOnSpace() {
        if (typedText.isNotEmpty() && !typedText.endsWith(" ") && !typedText.endsWith("\n")) {
            val lastWord = typedText.split("\\s+|\n".toRegex()).lastOrNull()?.uppercase() ?: ""

            if (lastWord.isNotEmpty()) {
                val isNumber = lastWord.all { c -> c.isDigit() }
                val isSingleLetter = lastWord.length == 1

                if (!isNumber && !isSingleLetter) {
                    photoLoadingJob?.cancel()
                    activePhotoWord = lastWord
                    pendingImageWord = lastWord

                    val matchingImage = ImageManager.findImage(lastWord)
                    if (matchingImage != null) {
                        photoLoadingJob = lifecycleScope.launch {
                            val bmp = ImageManager.loadSampledBitmap(applicationContext, matchingImage)
                            if (activePhotoWord == lastWord) {
                                pendingImageBitmap = bmp
                            }
                        }
                    } else {
                        pendingImageBitmap = null
                    }

                    pendingKineticAction = KineticActionManager.findAction(lastWord)
                    val matchingSound = SoundManager.findSound(lastWord)

                    val wordStartIndex = typedText.length - lastWord.length
                    speakWholeWordFlow(lastWord, wordStartIndex, customSound = matchingSound, sentenceToSpeakAfter = null)
                }
            }
        }
    }

    private fun checkAndSpeakOnEnter() {
        val currentLine = typedText.lines().lastOrNull()?.trim() ?: ""
        if (currentLine.isEmpty()) return

        // If the last word was just being typed without space, spell it first, then speak sentence!
        if (!typedText.endsWith(" ") && !typedText.endsWith("\n")) {
            val lastWord = typedText.split("\\s+|\n".toRegex()).lastOrNull()?.uppercase() ?: ""

            if (lastWord.isNotEmpty()) {
                val isNumber = lastWord.all { c -> c.isDigit() }
                val isSingleLetter = lastWord.length == 1

                if (!isNumber && !isSingleLetter) {
                    photoLoadingJob?.cancel()
                    activePhotoWord = lastWord
                    pendingImageWord = lastWord

                    val matchingImage = ImageManager.findImage(lastWord)
                    if (matchingImage != null) {
                        photoLoadingJob = lifecycleScope.launch {
                            val bmp = ImageManager.loadSampledBitmap(applicationContext, matchingImage)
                            if (activePhotoWord == lastWord) {
                                pendingImageBitmap = bmp
                            }
                        }
                    } else {
                        pendingImageBitmap = null
                    }

                    pendingKineticAction = KineticActionManager.findAction(lastWord)
                    val matchingSound = SoundManager.findSound(lastWord)

                    val wordStartIndex = typedText.length - lastWord.length
                    // Spell word -> Speak word / Custom Voice -> Pause -> Read all words in sentence!
                    speakWholeWordFlow(lastWord, wordStartIndex, customSound = matchingSound, sentenceToSpeakAfter = currentLine)
                    return
                }
            }
        }

        // If word was already finished on space, read the full sentence directly!
        if (isTtsReady && tts != null) {
            try {
                tts?.speak(currentLine, TextToSpeech.QUEUE_FLUSH, null, "sentence_pronunciation")
            } catch (e: Exception) {
                Log.e("MainActivity", "checkAndSpeakOnEnter failed: ${e.message}")
            }
        }
    }

    private fun speakWholeWordFlow(
        word: String,
        startIndex: Int,
        customSound: SoundEntry? = null,
        sentenceToSpeakAfter: String? = null
    ) {
        lastSpokenWord = word

        // 🟢 DECOUPLING: If TTS engine is not ready or missing on this TV, photos, physics, and custom voice still execute!
        if (!isTtsReady || tts == null) {
            activeImageBitmap = pendingImageBitmap
            pendingImageBitmap = null
            activeImageWord = pendingImageWord
            pendingImageWord = ""

            activeKineticAction = pendingKineticAction
            pendingKineticAction = null

            if (customSound != null) {
                SoundManager.play(applicationContext, customSound)
            }
            return
        }

        try {
            var queueMode = TextToSpeech.QUEUE_FLUSH

            word.forEachIndexed { i, ch ->
                val utteranceId = "highlight_${startIndex + i}"
                tts?.speak(ch.toString(), queueMode, null, utteranceId)
                queueMode = TextToSpeech.QUEUE_ADD
                tts?.playSilentUtterance(250, TextToSpeech.QUEUE_ADD, "silence_${startIndex + i}")
            }

            tts?.playSilentUtterance(400, TextToSpeech.QUEUE_ADD, "clear_highlight")

            if (customSound != null) {
                // Prioritize real-world human voice recording over synthetic TTS
                pendingCustomSound = customSound
                pendingSentenceAfterCustomSound = sentenceToSpeakAfter
                tts?.playSilentUtterance(50, TextToSpeech.QUEUE_ADD, "play_custom_voice")
            } else {
                // Standard synthetic TTS pronunciation
                tts?.speak(word, TextToSpeech.QUEUE_ADD, null, "final_word_pronunciation")

                // 🟢 ENTER KEY ENHANCEMENT: Read all words together like a sentence!
                if (!sentenceToSpeakAfter.isNullOrBlank()) {
                    tts?.playSilentUtterance(450, TextToSpeech.QUEUE_ADD, "pause_before_sentence")
                    tts?.speak(sentenceToSpeakAfter, TextToSpeech.QUEUE_ADD, null, "sentence_pronunciation")
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "speakWholeWordFlow failed: ${e.message}")
            activeSpeakingIndex = -1
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 1. Force low-latency accessibility audio stream to prevent TV HDMI power-saving mute/sleep
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)

            // 2. Resilient locale fallback: en_IN -> default device locale -> en_US
            val localeIndianEnglish = Locale("en", "IN")
            var result = tts?.setLanguage(localeIndianEnglish)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    result = tts?.setLanguage(Locale.US)
                }
            }

            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                AppSettings.applyToTts(applicationContext, tts)

                // Immediate low-latency silence pulse to wake up TV HDMI sink
                tts?.playSilentUtterance(50, TextToSpeech.QUEUE_FLUSH, "warmup_audio_sink")

                runOnUiThread {
                    isEngineReady = true
                }

                // 3. Play any keystroke buffered during the cold start
                pendingSpeechText?.let { bufferedText ->
                    try {
                        tts?.speak(bufferedText, TextToSpeech.QUEUE_ADD, null, null)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed playing buffered keystroke: ${e.message}")
                    }
                    pendingSpeechText = null
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        if (utteranceId != null && utteranceId.startsWith("highlight_")) {
                            val targetIndex = utteranceId.substringAfter("highlight_").toIntOrNull()
                            if (targetIndex != null) {
                                runOnUiThread {
                                    activeSpeakingIndex = targetIndex
                                }
                            }
                        } else if (utteranceId == "final_word_pronunciation" || utteranceId == "play_custom_voice") {
                            // 🟢 LEVEL 2 & 3: Synchronize photo appearance & kinetic physics
                            runOnUiThread {
                                activeImageBitmap = pendingImageBitmap
                                pendingImageBitmap = null
                                activeImageWord = pendingImageWord
                                pendingImageWord = ""

                                activeKineticAction = pendingKineticAction
                                pendingKineticAction = null
                            }
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "clear_highlight" || utteranceId == "final_word_pronunciation" || utteranceId == "sentence_pronunciation") {
                            runOnUiThread {
                                activeSpeakingIndex = -1
                            }
                        } else if (utteranceId == "play_custom_voice") {
                            runOnUiThread {
                                activeSpeakingIndex = -1
                                val soundToPlay = pendingCustomSound
                                val sentenceAfter = pendingSentenceAfterCustomSound
                                val fallbackWord = lastSpokenWord
                                pendingCustomSound = null
                                pendingSentenceAfterCustomSound = null

                                if (soundToPlay != null) {
                                    SoundManager.play(
                                        context = applicationContext,
                                        sound = soundToPlay,
                                        onCompletion = {
                                            // On sound completion, read full sentence if requested
                                            if (!sentenceAfter.isNullOrBlank() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    try {
                                                        tts?.playSilentUtterance(350, TextToSpeech.QUEUE_FLUSH, "pause_before_sentence")
                                                        tts?.speak(sentenceAfter, TextToSpeech.QUEUE_ADD, null, "sentence_pronunciation")
                                                    } catch (e: Exception) {
                                                        Log.e("MainActivity", "Error speaking sentence after custom voice: ${e.message}")
                                                    }
                                                }
                                            }
                                        },
                                        onError = {
                                            // Fallback to synthetic TTS if recording was unreadable or corrupt!
                                            if (fallbackWord.isNotEmpty() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    try {
                                                        tts?.speak(fallbackWord, TextToSpeech.QUEUE_FLUSH, null, "final_word_pronunciation")
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        runOnUiThread {
                            activeSpeakingIndex = -1
                        }
                        if (utteranceId == "play_custom_voice") {
                            runOnUiThread {
                                val soundToPlay = pendingCustomSound
                                val sentenceAfter = pendingSentenceAfterCustomSound
                                val fallbackWord = lastSpokenWord
                                pendingCustomSound = null
                                pendingSentenceAfterCustomSound = null
                                if (soundToPlay != null) {
                                    SoundManager.play(
                                        context = applicationContext,
                                        sound = soundToPlay,
                                        onCompletion = {
                                            if (!sentenceAfter.isNullOrBlank() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    tts?.speak(sentenceAfter, TextToSpeech.QUEUE_FLUSH, null, "sentence_pronunciation")
                                                }
                                            }
                                        },
                                        onError = {
                                            if (fallbackWord.isNotEmpty() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    try {
                                                        tts?.speak(fallbackWord, TextToSpeech.QUEUE_FLUSH, null, "final_word_pronunciation")
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        runOnUiThread {
                            activeSpeakingIndex = -1
                        }
                        if (utteranceId == "play_custom_voice") {
                            runOnUiThread {
                                val soundToPlay = pendingCustomSound
                                val sentenceAfter = pendingSentenceAfterCustomSound
                                val fallbackWord = lastSpokenWord
                                pendingCustomSound = null
                                pendingSentenceAfterCustomSound = null
                                if (soundToPlay != null) {
                                    SoundManager.play(
                                        context = applicationContext,
                                        sound = soundToPlay,
                                        onCompletion = {
                                            if (!sentenceAfter.isNullOrBlank() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    tts?.speak(sentenceAfter, TextToSpeech.QUEUE_FLUSH, null, "sentence_pronunciation")
                                                }
                                            }
                                        },
                                        onError = {
                                            if (fallbackWord.isNotEmpty() && isTtsReady && tts != null) {
                                                runOnUiThread {
                                                    try {
                                                        tts?.speak(fallbackWord, TextToSpeech.QUEUE_FLUSH, null, "final_word_pronunciation")
                                                    } catch (e: Exception) {}
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                })
            } else {
                Log.e("MainActivity", "TTS language configuration failed: unsupported on this device")
            }
        } else {
            Log.e("MainActivity", "TTS onInit failed with status: $status")
        }
    }

    private fun speakText(text: String, queueMode: Int) {
        if (isTtsReady && tts != null) {
            try {
                tts?.speak(text, queueMode, null, null)
            } catch (e: Exception) {
                Log.e("MainActivity", "speakText failed: ${e.message}")
            }
        } else {
            // Buffer keystroke during cold start so no initial letters are lost
            pendingSpeechText = text
        }
    }

    override fun onDestroy() {
        try {
            SoundManager.release()
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error closing speech engines: ${e.message}")
        }
        super.onDestroy()
    }
}