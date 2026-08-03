package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.translation.Translations
import com.example.ui.viewmodel.WheelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: WheelViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()

    val labelWheel = Translations.getString("nav_wheel", appLanguage)
    val labelCoin = Translations.getString("nav_coin_flip", appLanguage)
    val labelSaved = Translations.getString("nav_saved", appLanguage)
    val labelProfile = Translations.getString("nav_profile", appLanguage)
    val labelSettings = Translations.getString("nav_settings", appLanguage)

    // Responsive design using BoxWithConstraints
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth

        if (widthDp > 600.dp) {
            // TABLET / LANDSCAPE LAYOUT: Side Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // App Logo inside Rail
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = "Wheel App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))

                    // 1. Wheel Screen Item
                    NavigationRailItem(
                        selected = currentTab == "wheel",
                        onClick = { viewModel.setTab("wheel") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "wheel") Icons.Filled.Adjust else Icons.Outlined.Adjust,
                                contentDescription = labelWheel
                            )
                        },
                        label = { Text(labelWheel, fontSize = 11.sp) }
                    )

                    // 2. Coin Flip Item
                    NavigationRailItem(
                        selected = currentTab == "coin_flip",
                        onClick = { viewModel.setTab("coin_flip") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "coin_flip") Icons.Filled.MonetizationOn else Icons.Outlined.MonetizationOn,
                                contentDescription = labelCoin
                            )
                        },
                        label = { Text(labelCoin, fontSize = 11.sp) }
                    )

                    // 3. Favorites/History Item
                    NavigationRailItem(
                        selected = currentTab == "favorites",
                        onClick = { viewModel.setTab("favorites") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "favorites") Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = labelSaved
                            )
                        },
                        label = { Text(labelSaved, fontSize = 11.sp) }
                    )

                    // 4. User Profile Item
                    NavigationRailItem(
                        selected = currentTab == "profile",
                        onClick = { viewModel.setTab("profile") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = labelProfile
                            )
                        },
                        label = { Text(labelProfile, fontSize = 11.sp) }
                    )

                    // 5. Settings Item
                    NavigationRailItem(
                        selected = currentTab == "settings",
                        onClick = { viewModel.setTab("settings") },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = labelSettings
                            )
                        },
                        label = { Text(labelSettings, fontSize = 11.sp) }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                }

                // Main screen tab content switcher on the right side
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    TabContent(tab = currentTab, viewModel = viewModel)
                }
            }
        } else {
            // PHONE MOBILE LAYOUT: Standard Bottom Navigation Bar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // 1. Wheel Item
                        NavigationBarItem(
                            selected = currentTab == "wheel",
                            onClick = { viewModel.setTab("wheel") },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == "wheel") Icons.Filled.Adjust else Icons.Outlined.Adjust,
                                    contentDescription = labelWheel
                                )
                            },
                            label = { Text(labelWheel, fontSize = 11.sp) }
                        )

                        // 2. Coin Flip Item
                        NavigationBarItem(
                            selected = currentTab == "coin_flip",
                            onClick = { viewModel.setTab("coin_flip") },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == "coin_flip") Icons.Filled.MonetizationOn else Icons.Outlined.MonetizationOn,
                                    contentDescription = labelCoin
                                )
                            },
                            label = { Text(labelCoin, fontSize = 11.sp) }
                        )

                        // 3. Saved Favorites Item
                        NavigationBarItem(
                            selected = currentTab == "favorites",
                            onClick = { viewModel.setTab("favorites") },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == "favorites") Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = labelSaved
                                )
                            },
                            label = { Text(labelSaved, fontSize = 11.sp) }
                        )

                        // 4. User Profile Item
                        NavigationBarItem(
                            selected = currentTab == "profile",
                            onClick = { viewModel.setTab("profile") },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = labelProfile
                                )
                            },
                            label = { Text(labelProfile, fontSize = 11.sp) }
                        )

                        // 5. Settings Item
                        NavigationBarItem(
                            selected = currentTab == "settings",
                            onClick = { viewModel.setTab("settings") },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == "settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = labelSettings
                                )
                            },
                            label = { Text(labelSettings, fontSize = 11.sp) }
                        )
                    }
                },
                contentWindowInsets = WindowInsets.safeDrawing
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    TabContent(tab = currentTab, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TabContent(tab: String, viewModel: WheelViewModel) {
    Crossfade(
        targetState = tab,
        label = "Screen Transitions"
    ) { currentTabState ->
        when (currentTabState) {
            "wheel" -> WheelScreen(viewModel = viewModel)
            "coin_flip" -> CoinFlipScreen(viewModel = viewModel)
            "favorites" -> FavoritesHistoryScreen(viewModel = viewModel)
            "profile" -> ProfileScreen(viewModel = viewModel)
            "settings" -> SettingsScreen(viewModel = viewModel)
        }
    }
}
