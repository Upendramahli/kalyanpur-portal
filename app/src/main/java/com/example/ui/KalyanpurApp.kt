package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Project
import com.example.ui.screens.*

sealed class Screen(val route: String, val titleHindi: String, val titleEnglish: String, val icon: ImageVector) {
    object Home : Screen("home", "होम", "Home", Icons.Default.Home)
    object Yojnas : Screen("yojnas", "योजनाएं", "Yojnas", Icons.Default.Engineering)
    object Notices : Screen("notices", "सूचनाएं", "Notices", Icons.Default.Notifications)
    object Admin : Screen("admin", "एडमिन", "Admin", Icons.Default.AdminPanelSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalyanpurApp(viewModel: KalyanpurViewModel) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.init(context)
    }

    val isHindi by viewModel.isHindi.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val donors by viewModel.donators.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var selectedYojana by remember { mutableStateOf<Project?>(null) }
    var subScreenMode by remember { mutableStateOf<String?>(null) } // null, "donors", "expenses"

    val items = listOf(
        Screen.Home,
        Screen.Yojnas,
        Screen.Notices,
        Screen.Admin
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isHindi) "ग्राम पंचायत कल्याणपुर" else "Gram Panchayat Kalyanpur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isHindi) "बोकारो, झारखण्ड" else "Bokaro, Jharkhand",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(
                            text = if (isHindi) "ENGLISH" else "हिन्दी",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (selectedYojana == null) {
                NavigationBar {
                    items.forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(if (isHindi) screen.titleHindi else screen.titleEnglish, fontSize = 11.sp) },
                            selected = selected,
                            onClick = {
                                currentScreen = screen
                                selectedYojana = null
                                subScreenMode = null
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                selectedYojana != null && subScreenMode == "donors" -> {
                    DonorsListScreen(
                        project = selectedYojana!!,
                        donors = donors.filter { it.projectId == selectedYojana!!.id },
                        isHindi = isHindi,
                        onBack = { subScreenMode = null }
                    )
                }
                selectedYojana != null && subScreenMode == "expenses" -> {
                    ExpensesListScreen(
                        project = selectedYojana!!,
                        expenses = expenses.filter { it.projectId == selectedYojana!!.id },
                        isHindi = isHindi,
                        onBack = { subScreenMode = null }
                    )
                }
                selectedYojana != null -> {
                    YojanaDetailScreen(
                        project = selectedYojana!!,
                        donors = donors.filter { it.projectId == selectedYojana!!.id },
                        expenses = expenses.filter { it.projectId == selectedYojana!!.id },
                        isHindi = isHindi,
                        onBack = { selectedYojana = null },
                        onOpenDonors = { subScreenMode = "donors" },
                        onOpenExpenses = { subScreenMode = "expenses" }
                    )
                }
                else -> {
                    when (currentScreen) {
                        is Screen.Home -> HomeScreen(
                            isHindi = isHindi,
                            projects = projects,
                            donors = donors,
                            expenses = expenses,
                            notices = notices,
                            onYojanaClick = { proj -> selectedYojana = proj },
                            onNavigateYojnas = { currentScreen = Screen.Yojnas },
                            onNavigateNotices = { currentScreen = Screen.Notices }
                        )
                        is Screen.Yojnas -> ProjectsScreen(
                            isHindi = isHindi,
                            projects = projects,
                            donors = donors,
                            expenses = expenses,
                            onYojanaClick = { proj -> selectedYojana = proj }
                        )
                        is Screen.Notices -> NoticesScreen(
                            isHindi = isHindi,
                            notices = notices
                        )
                        is Screen.Admin -> AdminScreen(
                            isHindi = isHindi,
                            viewModel = viewModel,
                            projects = projects,
                            donors = donors,
                            expenses = expenses,
                            notices = notices
                        )
                    }
                }
            }
        }
    }
}
