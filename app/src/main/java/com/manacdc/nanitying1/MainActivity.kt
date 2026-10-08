package com.manacdc.nanityping1

import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var isEngineReady by mutableStateOf(false)
    private var pendingSpeechText: String? = null

    private var typedText by mutableStateOf("")
    private var activeSpeakingIndex by mutableStateOf(-1)

    // Level 2: Real-World Image Association state
    private var activeImageBitmap by mutableStateOf<ImageBitmap?>(null)
    private var pendingImageBitmap: ImageBitmap? = null

    // Level 3: Action Logic & Physics Dynamics state
    private var activeKineticAction by mutableStateOf<KineticActionType?>(null)
    private var pendingKineticAction: KineticActionType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            tts = TextToSpeech(applicationContext, this)
        } catch (e: Throwable) {
            Log.e("MainActivity", "TTS setup safety failure: ${e.message}")
        }

        // Index real-world trigger images on startup (Zero-DB Filename-as-Trigger)
        ImageManager.reloadRegistry(applicationContext)

        setContent {
            val scrollState = rememberScrollState()

            LaunchedEffect(typedText) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            // Auto-dismiss real-world photo after 2.8 seconds
            LaunchedEffect(activeImageBitmap) {
                if (activeImageBitmap != null) {
                    delay(2800)
                    activeImageBitmap = null
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
                                    Spacer(modifier = Modifier.fillMaxWidth())
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
                                Image(
                                    bitmap = bmp,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .sizeIn(maxWidth = 640.dp, maxHeight = 480.dp)
                                        .padding(12.dp),
                                    contentScale = ContentScale.Fit
                                )
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Clear visible image & kinetic actions immediately on any user action so typing is never blocked
        if (activeImageBitmap != null) {
            activeImageBitmap = null
        }
        pendingImageBitmap = null

        if (activeKineticAction != null) {
            activeKineticAction = null
        }
        pendingKineticAction = null

        event?.let {
            // 🔴 PRE-SCHOOL DEFENSIVE FILTER: Ignore hardware key auto-repeat
            // When a young child presses and holds a plastic key, hardware auto-repeat triggers 30 times/sec.
            // Ignoring repeat events prevents machine-gun audio stutter and runaway text.
            if (it.repeatCount > 0) {
                return true
            }

            val pressedChar = it.unicodeChar.toChar()

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                activeSpeakingIndex = -1
                checkAndSpeakLastWord()
                typedText += "\n"
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
                if (typedText.isNotEmpty()) {
                    tts?.stop()
                    typedText = ""
                    activeSpeakingIndex = -1
                    speakText("Reset", TextToSpeech.QUEUE_FLUSH)
                    return true
                } else {
                    return super.onKeyDown(keyCode, event)
                }
            }

            if (it.unicodeChar != 0 && (pressedChar.isLetterOrDigit() || pressedChar.isWhitespace())) {
                activeSpeakingIndex = -1
                if (pressedChar.isWhitespace()) {
                    checkAndSpeakLastWord()
                } else {
                    speakText(pressedChar.uppercaseChar().toString(), TextToSpeech.QUEUE_FLUSH)
                }

                typedText += pressedChar
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (typedText.isNotEmpty()) {
                    tts?.stop()
                    activeSpeakingIndex = -1
                    typedText = typedText.dropLast(1)
                }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        // Re-index images on resume so hot-plugged USB drives or newly copied photos appear immediately
        ImageManager.reloadRegistry(applicationContext)
    }

    private fun checkAndSpeakLastWord() {
        if (typedText.isNotEmpty() && !typedText.endsWith(" ") && !typedText.endsWith("\n")) {
            val lastWord = typedText.split("\\s+|\n".toRegex()).lastOrNull()?.uppercase() ?: ""

            if (lastWord.isNotEmpty()) {
                val isNumber = lastWord.all { c -> c.isDigit() }
                val isSingleLetter = lastWord.length == 1

                if (!isNumber && !isSingleLetter) {
                    // 🟢 LEVEL 2: Find matching real-world photo and preload asynchronously
                    val matchingFile = ImageManager.findImageFile(lastWord)
                    if (matchingFile != null) {
                        lifecycleScope.launch {
                            val bmp = ImageManager.loadSampledBitmap(matchingFile)
                            pendingImageBitmap = bmp
                        }
                    } else {
                        pendingImageBitmap = null
                    }

                    // 🟢 LEVEL 3: Check for kinetic action physical dynamics
                    pendingKineticAction = KineticActionManager.findAction(lastWord)

                    // 🟢 PURE REVERT INDEXING: Matches exactly how it functioned in your early builds
                    val wordStartIndex = typedText.length - lastWord.length
                    speakWholeWordFlow(lastWord, wordStartIndex)
                }
            }
        }
    }

    private fun speakWholeWordFlow(word: String, startIndex: Int) {
        if (isTtsReady && tts != null) {
            var queueMode = TextToSpeech.QUEUE_FLUSH

            word.forEachIndexed { i, ch ->
                val utteranceId = "highlight_${startIndex + i}"
                tts?.speak(ch.toString(), queueMode, null, utteranceId)
                queueMode = TextToSpeech.QUEUE_ADD
                tts?.playSilentUtterance(250, TextToSpeech.QUEUE_ADD, "silence_${startIndex + i}")
            }

            tts?.playSilentUtterance(400, TextToSpeech.QUEUE_ADD, "clear_highlight")
            tts?.speak(word, TextToSpeech.QUEUE_ADD, null, "final_word_pronunciation")
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
                tts?.setSpeechRate(0.70f)
                tts?.setPitch(1.12f)

                // Immediate low-latency silence pulse to wake up TV HDMI sink
                tts?.playSilentUtterance(50, TextToSpeech.QUEUE_FLUSH, "warmup_audio_sink")

                runOnUiThread {
                    isEngineReady = true
                }

                // 3. Play any keystroke buffered during the cold start
                pendingSpeechText?.let { bufferedText ->
                    tts?.speak(bufferedText, TextToSpeech.QUEUE_ADD, null, null)
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
                        } else if (utteranceId == "final_word_pronunciation") {
                            // 🟢 LEVEL 2 & 3: Synchronize photo appearance & kinetic physics with whole-word audio pronunciation
                            runOnUiThread {
                                activeImageBitmap = pendingImageBitmap
                                pendingImageBitmap = null

                                activeKineticAction = pendingKineticAction
                                pendingKineticAction = null
                            }
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        if (utteranceId == "clear_highlight") {
                            runOnUiThread {
                                activeSpeakingIndex = -1
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        runOnUiThread {
                            activeSpeakingIndex = -1
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
            tts?.speak(text, queueMode, null, null)
        } else {
            // Buffer keystroke during cold start so no initial letters are lost
            pendingSpeechText = text
        }
    }

    override fun onDestroy() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error closing speech engines: ${e.message}")
        }
        super.onDestroy()
    }
}