package com.example.utils

import androidx.compose.ui.graphics.Color

object NoteColorHelper {

    // Crisp, modern light pastel palette inspired by Google Keep & Apple Notes
    val LIGHT_PALETTE = listOf(
        "#FFFFFF", // Pure White
        "#FCE8E6", // Coral Rose
        "#FEF7E0", // Amber Yellow
        "#E6F4EA", // Sage Green
        "#E2F2E9", // Mint Teal
        "#E8F0FE", // Sky Blue
        "#F3E8FD", // Lavender Purple
        "#F1F3F4"  // Chalk Gray
    )

    // Deep graphite palette for dark mode
    val DARK_PALETTE = listOf(
        "#202124", // Default Dark Graphite
        "#33242A", // Muted Wine / Coral
        "#332B21", // Warm Bronze / Amber
        "#232F2A", // Forest Pine / Sage
        "#203033", // Dark Teal / Mint
        "#262A33", // Slate Blue / Sky
        "#292535", // Deep Iris / Lavender
        "#2A2B2E"  // Charcoal / Chalk
    )

    // Two-way mapping so notes look great whether in light or dark theme
    private val darkToLightMap = mapOf(
        "#202124" to "#FFFFFF",
        "#33242A" to "#FCE8E6",
        "#332B21" to "#FEF7E0",
        "#232F2A" to "#E6F4EA",
        "#203033" to "#E2F2E9",
        "#262A33" to "#E8F0FE",
        "#292535" to "#F3E8FD",
        "#2A2B2E" to "#F1F3F4"
    )

    private val lightToDarkMap = darkToLightMap.entries.associate { (k, v) -> v.lowercase() to k }

    fun getEffectiveNoteBg(rawColor: String, isLight: Boolean, defaultCardBg: Color): Color {
        val trimmed = rawColor.trim()
        if (trimmed.isEmpty()) return defaultCardBg

        val upper = trimmed.uppercase()
        val mappedHex = if (isLight) {
            darkToLightMap[upper] ?: trimmed
        } else {
            lightToDarkMap[trimmed.lowercase()] ?: trimmed
        }

        return runCatching { Color(android.graphics.Color.parseColor(mappedHex)) }
            .getOrDefault(defaultCardBg)
    }

    fun getPalette(isLight: Boolean): List<String> = if (isLight) LIGHT_PALETTE else DARK_PALETTE

    fun getTextColorForBackground(bgColor: Color): Color {
        val luminance = 0.2126f * bgColor.red + 0.7152f * bgColor.green + 0.0722f * bgColor.blue
        return if (luminance > 0.45f) {
            Color(0xFF14171A) // High contrast dark charcoal
        } else {
            Color(0xFFF1F1F1) // High contrast light text
        }
    }

    fun getSecondaryTextColorForBackground(bgColor: Color): Color {
        val luminance = 0.2126f * bgColor.red + 0.7152f * bgColor.green + 0.0722f * bgColor.blue
        return if (luminance > 0.45f) {
            Color(0xFF535862) // Crisp medium gray
        } else {
            Color(0xFF9AA0A6) // Crisp light gray
        }
    }
}
