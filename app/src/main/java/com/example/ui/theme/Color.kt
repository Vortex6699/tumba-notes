package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// TUMBA brand identity
//
// The app stays on a restrained graphite base, but every interactive highlight
// uses the brand gradient (violet -> aqua) with a warm amber companion for
// reminders. This is what makes the product recognisable instead of "another
// grey notes app".
// ---------------------------------------------------------------------------

val TumbaViolet = Color(0xFF7B6CFF)
val TumbaVioletDeep = Color(0xFF5B4BE0)
val TumbaAqua = Color(0xFF3DD9C8)
val TumbaAquaDeep = Color(0xFF11A99B)
val TumbaAmber = Color(0xFFFFB35C)
val TumbaAmberDeep = Color(0xFFE08824)
val TumbaRose = Color(0xFFFF5F7E)
val TumbaMint = Color(0xFF5EE6A8)

// Obsidian (Dark) Palette
val ObsidianBg = Color(0xFF0B0C10)
val ObsidianSurface = Color(0xFF101218)
val ObsidianCard = Color(0xFF16181F)
val ObsidianElevated = Color(0xFF1E2129)
val ObsidianHover = Color(0xFF262A34)
val ObsidianBorder = Color(0xFF2C3140)
val ObsidianTextPrimary = Color(0xFFF4F4F7)
val ObsidianTextSecondary = Color(0xFF9BA1AF)
val ObsidianTextMuted = Color(0xFF6A7181)

// Slate (Dim) Palette
val SlateBg = Color(0xFF14161C)
val SlateSurface = Color(0xFF181B22)
val SlateCard = Color(0xFF1E222B)
val SlateElevated = Color(0xFF262B36)
val SlateBorder = Color(0xFF39404F)
val SlateTextPrimary = Color(0xFFEFF1F5)
val SlateTextSecondary = Color(0xFFA3A9B7)

// Paper (Light) Palette
val PaperBg = Color(0xFFF5F6FA)
val PaperSurface = Color(0xFFFFFFFF)
val PaperCard = Color(0xFFFFFFFF)
val PaperElevated = Color(0xFFEEF0F6)
val PaperHover = Color(0xFFE4E8F1)
val PaperBorder = Color(0xFFDCE0EA)
val PaperTextPrimary = Color(0xFF12141A)
val PaperTextSecondary = Color(0xFF4E5462)
val PaperTextMuted = Color(0xFF7C8394)

// Legacy accent names kept so existing settings/backups keep working.
// Each one now maps onto a rich brand hue instead of another shade of grey.
val AccentSilver = TumbaViolet
val AccentTitanium = Color(0xFF8B98FF)
val AccentPlatinum = Color(0xFFC9C3FF)
val AccentGold = TumbaAmber
val AccentIce = TumbaAqua

// Light variants (strong contrast on paper backgrounds)
val LightAccentCharcoal = TumbaVioletDeep
val LightAccentTitanium = Color(0xFF4640C9)
val LightAccentPlatinum = Color(0xFF6A5AE0)
val LightAccentGold = TumbaAmberDeep
val LightAccentIce = TumbaAquaDeep

// Status & Special
val GraphiteRed = TumbaRose
val GraphiteRedBg = Color(0x22FF5F7E)
val GraphiteGreen = TumbaMint
val GraphiteAmber = TumbaAmber

/** Signature vertical/diagonal brand gradient used by primary buttons and highlights. */
fun brandGradient(accent: Color = TumbaViolet, secondary: Color = TumbaAqua): Brush =
    Brush.linearGradient(listOf(accent, secondary))

/** Soft ambient gradient that gives headers and heroes depth without noise. */
fun heroGradient(accent: Color = TumbaViolet, isLight: Boolean = false): Brush =
    if (isLight) {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.16f),
                TumbaAqua.copy(alpha = 0.08f),
                Color.Transparent
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                accent.copy(alpha = 0.28f),
                TumbaAqua.copy(alpha = 0.10f),
                Color.Transparent
            )
        )
    }

/** Gradient used for the little accent bar on note cards. */
fun noteAccentGradient(accent: Color, secondary: Color): Brush =
    Brush.verticalGradient(listOf(accent, secondary))
