package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Universal 3D Tactile Card Container
 * Features:
 * - Spring-press physical scale feedback (0.96f scale on tap)
 * - Elevation drop for realistic depth
 * - Metallic gold & emerald rim bevel border with breathing glow
 * - Rich deep emerald institutional gradient background
 */
@Composable
fun Madrassa3dCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 6.dp,
    shape: Shape = RoundedCornerShape(16.dp),
    glowBorder: Boolean = true,
    enable3dTilt: Boolean = true,
    gradientColors: List<Color> = listOf(Color(0xFF0E533A), Color(0xFF073020), Color(0xFF031A11)),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.94f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    val pressTiltX by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 3.5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pressTiltX"
    )
    val currentElevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 1.5.dp else elevation,
        label = "cardElevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "card3dAmbient")
    val ambientTiltY by infiniteTransition.animateFloat(
        initialValue = -2.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltY"
    )
    val ambientTiltX by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = -1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltX"
    )
    val sheenOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenOffset"
    )
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    val borderStroke = if (glowBorder) {
        BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF0C2).copy(alpha = borderGlow),
                    Color(0xFFE5C07B).copy(alpha = borderGlow * 0.9f),
                    Color(0xFF0E6647).copy(alpha = 0.4f),
                    Color(0xFF8B6B23).copy(alpha = 0.65f),
                    Color(0xFFE5C07B).copy(alpha = borderGlow)
                )
            )
        )
    } else {
        BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.35f))
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                cameraDistance = 14f * density
                if (enable3dTilt) {
                    rotationX = pressTiltX + ambientTiltX
                    rotationY = ambientTiltY
                }
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = currentElevation),
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = gradientColors))
        ) {
            content()

            // Dynamic 3D metallic light sheen reflection sweeping across surface
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFF0C2).copy(alpha = 0.05f),
                                Color(0xFFE5C07B).copy(alpha = 0.12f),
                                Color(0xFFFFF0C2).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(
                                x = sheenOffset * 700f,
                                y = 0f
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                x = (sheenOffset + 0.35f) * 700f,
                                y = 500f
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 3D Embossed Icon Orb with Metallic Gold Ring Border
 */
@Composable
fun Madrassa3dIconOrb(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    tint: Color = Color(0xFFE5C07B),
    borderColor: Color = Color(0xFFE5C07B).copy(alpha = 0.85f)
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1B6B4F), Color(0xFF052B1D))
                )
            )
            .border(1.5.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * 3D Metric Card Tile for Top Bars / Headers
 */
@Composable
fun Madrassa3dMetricTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trendText: String? = null,
    accentColor: Color = Color(0xFFE5C07B),
    onClick: (() -> Unit)? = null
) {
    Madrassa3dCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 5.dp,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Madrassa3dIconOrb(
                    icon = icon,
                    size = 32.dp,
                    iconSize = 16.dp,
                    tint = accentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            if (!trendText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = trendText,
                    fontSize = 9.5.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 3D Status Pill / Badge
 */
@Composable
fun Madrassa3dBadge(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFE5C07B).copy(alpha = 0.2f),
    contentColor: Color = Color(0xFFFFF0C2),
    borderColor: Color = Color(0xFFE5C07B).copy(alpha = 0.5f)
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
    }
}
