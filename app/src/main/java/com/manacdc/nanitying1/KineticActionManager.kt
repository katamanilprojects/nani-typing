package com.manacdc.nanityping1

import java.util.Locale

/**
 * Level 3: Action Logic & Physics Engine.
 *
 * Maps typed action words directly to physical kinematic dynamics:
 * - FAST: Swift acceleration across the TV screen
 * - SLOW: Deliberate, measured crawl across the screen
 * - STOP: Screeches to an abrupt halt with suspension recoil
 * - DOWN: Gravitational descent along a physical incline/ramp
 * - UP: Climb up an incline overcoming gravity
 * - BOUNCE: Drops and rebounds with elastic spring physics
 * - JUMP: Upward leap with parabolic arc
 */
enum class KineticActionType {
    FAST,
    SLOW,
    STOP,
    DOWN,
    UP,
    BOUNCE,
    JUMP
}

object KineticActionManager {

    private val actionKeywords = mapOf(
        "FAST" to KineticActionType.FAST,
        "SPEED" to KineticActionType.FAST,
        "RUN" to KineticActionType.FAST,
        "ZOOM" to KineticActionType.FAST,

        "SLOW" to KineticActionType.SLOW,
        "CRAWL" to KineticActionType.SLOW,

        "STOP" to KineticActionType.STOP,
        "HALT" to KineticActionType.STOP,
        "WAIT" to KineticActionType.STOP,

        "DOWN" to KineticActionType.DOWN,
        "DROP" to KineticActionType.DOWN,
        "FALL" to KineticActionType.DOWN,

        "UP" to KineticActionType.UP,
        "CLIMB" to KineticActionType.UP,

        "BOUNCE" to KineticActionType.BOUNCE,
        "HOP" to KineticActionType.BOUNCE,

        "JUMP" to KineticActionType.JUMP
    )

    fun findAction(word: String): KineticActionType? {
        val normalized = word.trim().uppercase(Locale.ROOT)
        return actionKeywords[normalized]
    }
}
