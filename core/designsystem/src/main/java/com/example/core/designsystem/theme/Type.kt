package com.example.core.designsystem.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.R

// ── Font families ─────────────────────────────────────────────────────────────

val Newsreader = FontFamily(
    Font(R.font.newsreader_regular, FontWeight.Normal),   // 400
    Font(R.font.newsreader_medium, FontWeight.Medium),   // 500
    Font(R.font.newsreader_semibold, FontWeight.SemiBold), // 600
)

val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk_regular, FontWeight.Normal),   // 400
    Font(R.font.hanken_grotesk_medium, FontWeight.Medium),   // 500
    Font(R.font.hanken_grotesk_semibold, FontWeight.SemiBold), // 600
    Font(R.font.hanken_grotesk_bold, FontWeight.Bold),     // 700
)

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun tight() = PlatformTextStyle(includeFontPadding = false)

private fun lineHeightStyle() = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

// ── App text styles ───────────────────────────────────────────────────────────

object AppTextStyle {

    // Newsreader — editorial
    val GreetingTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 28.sp,
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val DetailHeadline = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 32.2.sp,        // 1.15
        letterSpacing = (-0.2).sp,
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val SectionTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 27.sp,
        platformStyle = tight(),
    )

    val SheetTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 23.sp,
        platformStyle = tight(),
    )

    val ComposeTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        platformStyle = tight(),
    )

    val NoteTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 21.25.sp,       // 1.25
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val NoteBody = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 17.5.sp,
        lineHeight = 28.sp,          // 1.6
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val LiveTranscript = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal, // switches to Medium on finalised word
        fontSize = 18.sp,
        lineHeight = 28.8.sp,
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val ReminderTitle = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        platformStyle = tight(),
    )

    val DockPlaceholder = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        platformStyle = tight(),
    )

    // Hanken Grotesk — UI
    val BodySnippet = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        platformStyle = tight(),
        lineHeightStyle = lineHeightStyle(),
    )

    val Eyebrow = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        letterSpacing = 1.3.sp,
        platformStyle = tight(),
    )

    val Metadata = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        platformStyle = tight(),
    )

    val Button = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        platformStyle = tight(),
    )

    val Chip = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp,
        platformStyle = tight(),
    )

    val VoiceReminderPill = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        platformStyle = tight(),
    )
}