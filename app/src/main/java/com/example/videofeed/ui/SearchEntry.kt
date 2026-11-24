package com.example.videofeed.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videofeed.R
import com.example.videofeed.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────────

private val UsernameRegex = Regex("[A-Za-z0-9_]{1,15}")
private const val MAX_INPUT_LENGTH = 20
private val ContentMaxWidth = 480.dp
private val FieldShape = RoundedCornerShape(16.dp)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
//
// Layout: top bar  →  card showcase (fills the free space)  →  docked bottom panel.
// The input and button sit at the bottom, in easy thumb reach. When the keyboard
// opens, the showcase shrinks and the panel rides up above it.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SearchEntry(onStart: (String) -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val clean = username.trim().trimStart('@')
    val valid = clean.matches(UsernameRegex)

    fun go() {
        if (valid) {
            keyboard?.hide()
            onStart(clean)
        }
    }

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val reveal by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(500),
        label = "reveal"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueBlack)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(Mint.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(size.width / 2f, size.height * 0.28f),
                        radius = size.width * 0.95f
                    )
                )
            }
            .statusBarsPadding()
            .imePadding()
    ) {
        TopBar(modifier = Modifier.graphicsLayer { alpha = reveal })

        CardShowcase(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer { alpha = reveal }
        )

        EntryPanel(
            username = username,
            onUsernameChange = { if (it.length <= MAX_INPUT_LENGTH) username = it },
            showError = username.isNotBlank() && !valid,
            valid = valid,
            onGo = ::go,
            modifier = Modifier.graphicsLayer {
                alpha = reveal
                translationY = (1f - reveal) * 72.dp.toPx()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val logoShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(logoShape)
                    .background(DarkElevated)
                    .border(1.dp, Hairline, logoShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp)),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "VideoFeed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        StatusPill(text = "No account needed")
    }
}

@Composable
private fun StatusPill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Mint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Mint.copy(alpha = 0.20f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).background(Mint, CircleShape))
            Spacer(Modifier.width(7.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = Mint
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card showcase (decorative preview of a vertical video feed)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CardShowcase(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Hide the decoration when space is tight (small screens, keyboard open).
        if (maxHeight >= 180.dp) {
            val cardHeight = minOf(maxHeight * 0.8f, 300.dp)
            val cardWidth = cardHeight * 0.58f

            // Back left
            PreviewCard(
                width = cardWidth * 0.88f,
                height = cardHeight * 0.88f,
                featured = false,
                modifier = Modifier
                    .offset(x = -(cardWidth * 0.72f), y = 16.dp)
                    .rotate(-10f)
                    .alpha(0.6f)
            )
            // Back right
            PreviewCard(
                width = cardWidth * 0.88f,
                height = cardHeight * 0.88f,
                featured = false,
                modifier = Modifier
                    .offset(x = cardWidth * 0.72f, y = 16.dp)
                    .rotate(10f)
                    .alpha(0.6f)
            )
            // Front
            PreviewCard(
                width = cardWidth,
                height = cardHeight,
                featured = true
            )
        }
    }
}

@Composable
private fun PreviewCard(
    width: Dp,
    height: Dp,
    featured: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    val bar = Modifier.background(Hairline, CircleShape)

    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(DarkElevated, DarkCard)))
            .border(1.dp, if (featured) Mint.copy(alpha = 0.35f) else Hairline, shape)
    ) {
        // Author row
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(22.dp)
                    .background(Mint.copy(alpha = if (featured) 0.9f else 0.35f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.width(56.dp).height(8.dp).then(bar))
        }

        // Play button
        if (featured) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(56.dp)
                    .background(Mint.copy(alpha = 0.16f), CircleShape)
                    .border(1.dp, Mint.copy(alpha = 0.40f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Mint,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // Caption + progress
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        ) {
            Box(Modifier.fillMaxWidth(0.8f).height(8.dp).then(bar))
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(0.5f).height(8.dp).then(bar))
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).then(bar)) {
                Box(
                    Modifier
                        .fillMaxWidth(0.4f)
                        .fillMaxHeight()
                        .background(Mint, CircleShape)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EntryPanel(
    username: String,
    onUsernameChange: (String) -> Unit,
    showError: Boolean,
    valid: Boolean,
    onGo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DarkCard)
            .border(1.dp, Hairline, shape)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ContentMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 28.dp, bottom = 20.dp)
        ) {
            Text(
                text = "Every video from any X profile, in one clean feed.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Start,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(Modifier.height(22.dp))
            UsernameField(
                value = username,
                onValueChange = onUsernameChange,
                showError = showError,
                onGo = onGo
            )

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = "Explore videos",
                enabled = valid,
                onClick = onGo
            )

            Spacer(Modifier.height(18.dp))
            FooterNote(text = "Videos are fetched from public Nitter instances.")
        }
    }
}

@Composable
private fun UsernameField(
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    onGo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val borderColor by animateColorAsState(
        targetValue = when {
            showError -> errorColor
            focused -> Mint
            else -> Hairline
        },
        animationSpec = tween(180),
        label = "fieldBorder"
    )

    Column(modifier = modifier) {
        Text(
            text = "X username",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, FieldShape)
                .semantics { contentDescription = "X username" },
            placeholder = { Text("@elonmusk") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.AlternateEmail,
                    contentDescription = null,
                    tint = if (showError) errorColor else Mint
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear username",
                            tint = TextSecondary
                        )
                    }
                }
            },
            isError = showError,
            singleLine = true,
            shape = FieldShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = { onGo() }),
            interactionSource = interaction,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkElevated,
                unfocusedContainerColor = DarkElevated,
                errorContainerColor = DarkElevated,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                errorTextColor = TextPrimary,
                focusedPlaceholderColor = TextTertiary,
                unfocusedPlaceholderColor = TextTertiary,
                errorPlaceholderColor = TextTertiary,
                cursorColor = Mint,
                errorCursorColor = errorColor
            )
        )

        if (showError) {
            Text(
                text = "Use 1–15 letters, numbers or underscores.",
                style = MaterialTheme.typography.labelSmall,
                color = errorColor,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

/**
 * Pill button: label on the left, arrow in a circle on the right.
 * Animates between its disabled and enabled looks and scales slightly when pressed.
 */
@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.975f else 1f,
        animationSpec = tween(120),
        label = "ctaScale"
    )
    val active by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        animationSpec = tween(220),
        label = "ctaActive"
    )

    val textColor = lerp(TextTertiary, TextOnMint, active)
    val circleColor = lerp(DarkCard, TextOnMint, active)
    val arrowColor = lerp(TextTertiary, Mint, active)
    val gradient = Brush.linearGradient(
        listOf(
            lerp(DarkElevated, MintBright, active),
            lerp(DarkElevated, Mint, active)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = 16.dp * active,
                shape = CircleShape,
                ambientColor = Mint.copy(alpha = 0.40f),
                spotColor = Mint.copy(alpha = 0.40f)
            )
            .clip(CircleShape)
            .background(gradient)
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = TextOnMint.copy(alpha = 0.30f)),
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 26.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(circleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = arrowColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun FooterNote(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}
