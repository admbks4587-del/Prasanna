package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AnimeVerseViewModel
import com.example.ui.AppNavTab
import com.example.ui.components.AnimeHeader
import com.example.ui.screens.CharacterScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StoryboardScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.TheaterScreen
import com.example.ui.theme.CyberVioletPrimary
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonIndigoBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AnimeVerseApp()
            }
        }
    }
}

@Composable
fun AnimeVerseApp(
    viewModel: AnimeVerseViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    val activeProject = allProjects.find { it.id == selectedProjectId } ?: allProjects.firstOrNull()

    // BackHandler: return to Studio if in any subscreen
    BackHandler(enabled = currentTab != AppNavTab.STUDIO) {
        viewModel.selectTab(AppNavTab.STUDIO)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonIndigoBg),
        containerColor = NeonIndigoBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AnimeHeader(
                currentProjectTitle = activeProject?.title,
                modifier = Modifier.statusBarsPadding()
            )
        },
        bottomBar = {
            AnimeNavigationBar(
                currentTab = currentTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    AppNavTab.STUDIO -> StudioScreen(viewModel = viewModel)
                    AppNavTab.STORYBOARD -> StoryboardScreen(viewModel = viewModel)
                    AppNavTab.CHARACTERS -> CharacterScreen(viewModel = viewModel)
                    AppNavTab.THEATER -> TheaterScreen(viewModel = viewModel)
                    AppNavTab.COMPRESSION -> ExportScreen(viewModel = viewModel)
                    AppNavTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AnimeNavigationBar(
    currentTab: AppNavTab,
    onSelectTab: (AppNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(AppNavTab.STUDIO, "Studio", Icons.Default.AutoAwesome),
            Triple(AppNavTab.STORYBOARD, "Script", Icons.Default.Videocam),
            Triple(AppNavTab.CHARACTERS, "Cast", Icons.Default.People),
            Triple(AppNavTab.THEATER, "Theater", Icons.Default.PlayCircle),
            Triple(AppNavTab.COMPRESSION, "Render", Icons.Default.Compress),
            Triple(AppNavTab.SETTINGS, "Config", Icons.Default.Settings)
        )

        navItems.forEach { (tab, label, icon) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) ElectricCyan else TextSecondary
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = CyberVioletPrimary.copy(alpha = 0.35f),
                    selectedIconColor = ElectricCyan,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = TextPrimary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
