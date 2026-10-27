package com.example.pulsemusic.feature.library.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp

// Ряд навігації між вкладками
@Composable
fun LibraryTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current

    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }

    val textMeasurer = rememberTextMeasurer()

    val tabWidth = remember(tabs, screenWidth, density) {
        var maxW = 0
        for (tab in tabs) {
            val width = textMeasurer.measure(
                text = tab,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            ).size.width
            if (width > maxW) maxW = width
        }
        val maxTextWidthDp = with(density) { maxW.toDp() }
        val paddingDp = 32.dp
        val requiredWidth = maxTextWidthDp + paddingDp

        max(screenWidth / 3, requiredWidth)
    }

    val tabListState = rememberLazyListState()

    Box(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = tabListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = tabListState),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tabs.size) { index ->
                val title = tabs[index]
                val selected = selectedTabIndex == index

                Tab(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.width(tabWidth),
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.Gray
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(vertical = 14.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(if (selected) Color.White else Color.Transparent)
                        )
                    }
                }
            }
        }

        val showLeftShadow by remember { derivedStateOf { tabListState.canScrollBackward } }
        val showRightShadow by remember { derivedStateOf { tabListState.canScrollForward } }

        if (showLeftShadow || showRightShadow) {
            Box(modifier = Modifier.matchParentSize()) {
                if (showLeftShadow) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.LightGray.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                    )
                }
                if (showRightShadow) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(4.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.LightGray.copy(alpha = 0.4f))
                                )
                            )
                    )
                }
            }
        }
    }
}
