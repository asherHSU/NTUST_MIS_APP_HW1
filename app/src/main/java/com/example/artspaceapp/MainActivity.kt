package com.example.artspaceapp // 替換成你的實際包名！

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ArtGalleryScreen()
            }
        }
    }
}

// 你的資料類
data class ArtworkItem(
    val imageResourceId: Int,
    val title: String,
    val artist: String,
    val description: String
)

// 完整畫面
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArtGalleryScreen() {
    val artworkList = listOf(
        ArtworkItem(R.drawable.image1, "懷疑貓", "藝術家一", "你最好有帶罐罐來"),
        ArtworkItem(R.drawable.image2, "哭哭貓", "藝術家二", "期中考週到了..."),
        ArtworkItem(R.drawable.image3, "比讚哭哭貓", "藝術家三", "老師說的都對TT"),
        ArtworkItem(R.drawable.image4, "蛤?貓", "藝術家四", "你確定???"),
        ArtworkItem(R.drawable.image5, "波!貓", "藝術家五", "POP!"),
        ArtworkItem(R.drawable.image6, "生氣貓", "藝術家六", "這爛code誰寫的=="),
        ArtworkItem(R.drawable.image7, "香蕉貓", "藝術家七", "「蕉慮」中...請勿打擾"),
        ArtworkItem(R.drawable.image8, "咳嗽貓", "藝術家八", "爛code 警報！立即迴避！"),
        ArtworkItem(R.drawable.image9, "小明劍魔", "藝術家九", "回答我!!"),
        ArtworkItem(R.drawable.image10, "小明劍魔", "藝術家十", "老爸得了MVP!")
    )

    var currentIndex by remember { mutableStateOf(0) }
    var previousIndex by remember { mutableStateOf(0) }
    var randomButtonRotation by remember { mutableStateOf(0f) }
    var dragOffset by remember { mutableStateOf(0f) }
    var imageOffset by remember { mutableStateOf(0f) }

    // ✅ 圖片震動效果
    val imageShake = remember { androidx.compose.animation.core.Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 畫作展示區
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            previousIndex = currentIndex
                            if (dragOffset > 100) {
                                currentIndex = if (currentIndex > 0) currentIndex - 1 else artworkList.lastIndex
                            } else if (dragOffset < -100) {
                                currentIndex = if (currentIndex < artworkList.lastIndex) currentIndex + 1 else 0
                            }
                            dragOffset = 0f
                            imageOffset = 0f
                            coroutineScope.launch {
                                imageShake.animateTo(
                                    targetValue = 0f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                            }
                        },
                        onDrag = { _, dragAmount ->
                            dragOffset += dragAmount.x
                            imageOffset = dragOffset * 0.1f
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = artworkList[currentIndex],
                transitionSpec = {
                    if (currentIndex > previousIndex) {
                        slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                    } else if (currentIndex < previousIndex) {
                        slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                }
            ) { artwork ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = artwork.imageResourceId),
                        contentDescription = null,
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = imageOffset + imageShake.value
                            }
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = artwork.title,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = artwork.artist,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = artwork.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 頁數顯示，拆分 + 動畫
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Page ",
                    style = MaterialTheme.typography.bodySmall
                )

                AnimatedContent(
                    targetState = currentIndex + 1,
                    transitionSpec = {
                        if (currentIndex > previousIndex) {
                            (slideInVertically { height -> height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> -height } + fadeOut())
                        } else if (currentIndex < previousIndex) {
                            (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                                slideOutVertically { height -> height } + fadeOut())
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    }
                ) { pageNumber ->
                    Text(
                        text = pageNumber.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.graphicsLayer {
                            scaleX = 1.2f
                            scaleY = 1.2f
                        }
                    )
                }

                Text(
                    text = " / ${artworkList.size}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 按鈕區域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val buttonWidth = 110.dp

            CustomTooltipButton(
                text = "Previous",
                tooltipText = "上一頁",
                buttonWidth = buttonWidth,
                onClick = {
                    previousIndex = currentIndex
                    currentIndex = if (currentIndex > 0) currentIndex - 1 else artworkList.lastIndex
                    coroutineScope.launch {
                        imageShake.snapTo(10f)
                        imageShake.animateTo(0f, tween(300))
                    }
                }
            )

            CustomTooltipButton(
                text = "Random",
                tooltipText = "隨機跳轉",
                buttonWidth = buttonWidth,
                rotation = randomButtonRotation,
                onClick = {
                    previousIndex = currentIndex
                    val targetIndex = (artworkList.indices).filter { it != currentIndex }.random()

                    randomButtonRotation += 360f

                    coroutineScope.launch {
                        // ✅ 數字亂數閃爍效果
                        repeat(8) {
                            currentIndex = (artworkList.indices).random()
                            delay(40)
                        }
                        currentIndex = targetIndex

                        imageShake.snapTo(10f)
                        imageShake.animateTo(0f, tween(300))
                    }
                }
            )

            CustomTooltipButton(
                text = "Next",
                tooltipText = "下一頁",
                buttonWidth = buttonWidth,
                onClick = {
                    previousIndex = currentIndex
                    currentIndex = if (currentIndex < artworkList.lastIndex) currentIndex + 1 else 0
                    coroutineScope.launch {
                        imageShake.snapTo(10f)
                        imageShake.animateTo(0f, tween(300))
                    }
                }
            )
        }
    }
}




// 完整 Tooltip 按鈕組件
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomTooltipButton(
    text: String,
    tooltipText: String,
    modifier: Modifier = Modifier,
    buttonWidth: Dp,
    tooltipDuration: Long = 1500L,
    tooltipOffsetY: Dp = (-32).dp,
    rotation: Float = 0f,
    onClick: () -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }
    var isLongPress by remember { mutableStateOf(false) }
    var buttonScale by remember { mutableStateOf(1f) }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.width(buttonWidth)) {
        Button(
            onClick = {
                if (!isLongPress) {
                    onClick()
                }
                isLongPress = false
                isPressing = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                    rotationZ = rotation
                }
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isPressing = true
                            isLongPress = false
                            buttonScale = 0.95f

                            coroutineScope.launch {
                                delay(500)
                                if (isPressing) {
                                    showTooltip = true
                                    isLongPress = true
                                    delay(tooltipDuration)
                                    showTooltip = false
                                }
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            isPressing = false
                            buttonScale = 1f
                        }
                    }
                    false
                }
        ) {
            Text(text)
        }

        AnimatedVisibility(
            visible = showTooltip,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = tooltipOffsetY)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .shadow(4.dp, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = tooltipText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
