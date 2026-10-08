package com.manacdc.nanityping1

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class SentenceAndPhonicsLogicTest {

    @Test
    fun testWordExtractionOnSpace() {
        val typedText = "DOG"
        val lastWord = typedText.split("\\s+|\n".toRegex()).lastOrNull()?.uppercase() ?: ""
        assertEquals("DOG", lastWord)

        val isNumber = lastWord.all { it.isDigit() }
        val isSingleLetter = lastWord.length == 1
        assertFalse(isNumber)
        assertFalse(isSingleLetter)

        val wordStartIndex = typedText.length - lastWord.length
        assertEquals(0, wordStartIndex)
    }

    @Test
    fun testSentenceExtractionOnEnter_WithTrailingSpace() {
        // Child types "I SEE A DOG " and hits Enter
        val typedText = "I SEE A DOG "
        val hasTrailingSpace = typedText.endsWith(" ") || typedText.endsWith("\n")
        assertTrue(hasTrailingSpace)

        val currentLine = typedText.lines().lastOrNull()?.trim() ?: ""
        assertEquals("I SEE A DOG", currentLine)
    }

    @Test
    fun testSentenceExtractionOnEnter_MidWord() {
        // Child types "I SEE A DOG" (no space after DOG) and hits Enter
        val typedText = "I SEE A DOG"
        val hasTrailingSpace = typedText.endsWith(" ") || typedText.endsWith("\n")
        assertFalse(hasTrailingSpace)

        val lastWord = typedText.split("\\s+|\n".toRegex()).lastOrNull()?.uppercase() ?: ""
        assertEquals("DOG", lastWord)

        val currentLine = typedText.lines().lastOrNull()?.trim() ?: ""
        assertEquals("I SEE A DOG", currentLine)

        val wordStartIndex = typedText.length - lastWord.length
        assertEquals(8, wordStartIndex)
    }

    @Test
    fun testFilenameNormalizationForRealWorldPhotos() {
        val inputWord = "BAT"
        val normalized = inputWord.lowercase(Locale.ROOT).trim()
        assertEquals("bat", normalized)

        // Verifying Telugu/regional naming normalization
        val inputRegional = "KODI"
        val normalizedRegional = inputRegional.lowercase(Locale.ROOT).trim()
        assertEquals("kodi", normalizedRegional)
    }

    @Test
    fun testKineticActionManagerMatching() {
        assertEquals(KineticActionType.FAST, KineticActionManager.findAction("FAST"))
        assertEquals(KineticActionType.FAST, KineticActionManager.findAction("ZOOM"))
        assertEquals(KineticActionType.SLOW, KineticActionManager.findAction("SLOW"))
        assertEquals(KineticActionType.STOP, KineticActionManager.findAction("STOP"))
        assertEquals(KineticActionType.DOWN, KineticActionManager.findAction("DOWN"))
        assertEquals(KineticActionType.DOWN, KineticActionManager.findAction("FALL"))
        assertNull(KineticActionManager.findAction("APPLE"))
    }
}
