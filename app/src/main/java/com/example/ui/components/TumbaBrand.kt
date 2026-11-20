package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.LocalTumbaColors

/**
 * The original TUMBA artwork, framed exactly like the launcher icon so the
 * in-app brand mark and the home-screen icon read as one product.
 */
@Composable
fun TumbaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    cornerRadius: Dp = 10.dp
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "TUMBA NOTES",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    )
}

/** Logo + wordmark lockup used in headers and the drawer. */
@Composable
fun TumbaBrandLockup(
    modifier: Modifier = Modifier,
    logSize: Dp = 34.dp,
    subtitle: String? = null
) {
    val colors = LocalTumbaColors.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        TumbaLogo(size = logSize, cornerRadius = logSize / 3.4f)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = "TUMBA",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = 2.6.sp,
                color = colors.textPrimary
            )
            Text(
                text = "NOTES",
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp,
                letterSpacing = 4.2.sp,
                color = colors.accent
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 9.5.sp,
                    color = colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Small pill label with an optional icon, used for status/metadata. */
@Composable
fun TumbaPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color? = null,
    filled: Boolean = false
) {
    val colors = LocalTumbaColors.current
    val accent = tint ?: colors.accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) accent.copy(alpha = 0.16f) else colors.bgElevated.copy(alpha = 0.7f))
            .border(0.8.dp, accent.copy(alpha = if (filled) 0.35f else 0.18f), RoundedCornerShape(50))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.3.sp,
            color = if (filled) accent else colors.textSecondary,
            maxLines = 1
        )
    }
}

/**
 * Primary action button with the brand gradient, soft glow shadow and a
 * spring press animation.
 */
@Composable
fun TumbaGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    brush: Brush? = null,
    height: Dp = 46.dp
) {
    val colors = LocalTumbaColors.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 900f, dampingRatio = 0.7f),
        label = "button-scale"
    )

    val contentColor = if (colors.isLight) Color.White else colors.bgMain
    Row(
        modifier = modifier
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(brush ?: colors.brandBrush)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
            color = contentColor,
            maxLines = 1
        )
    }
}

/** Section label with a small gradient tick, replacing flat monospace captions. */
@Composable
fun TumbaSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val colors = LocalTumbaColors.current
    val tint = accent ?: colors.accent
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 13.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(tint, colors.accentSecondary)))
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = colors.textSecondary,
            letterSpacing = 1.4.sp
        )
    }
}

/** Round icon button with a tinted translucent surface. */
@Composable
fun TumbaCircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    size: Dp = 34.dp,
    background: Color? = null
) {
    val colors = LocalTumbaColors.current
    val tintColor = tint ?: colors.textSecondary
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background ?: tintColor.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}
