package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.AffiliatesScreen
import com.example.ui.screens.AgentsScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.EdgeQuantizerScreen
import com.example.ui.screens.InstancesScreen
import com.example.ui.screens.KnowledgeRagScreen
import com.example.ui.screens.McpAndSimulatorScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PriceContactsScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShippingAgenciesScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.screens.TelegramScreen
import kotlinx.coroutines.launch
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantDarkBorder
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantGreenActive
import com.example.ui.theme.ElegantPurpleAccent
import com.example.ui.theme.ElegantPurpleOnAccent
import com.example.ui.theme.ElegantPurpleSecondary
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    // Navigation Modes:
    // navBarPagerState: page 0 = Main Bar (Instances, Agents, Knowledge, Quantizer, Threads)
    //                   page 1 = Secondary Commerce Bar (Telegram, Produits, Catégories, Fournisseurs, Contacts & Tarifs, Agences Livraison, Affiliés, Commandes)
    val navBarPagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var currentTabIndex by remember { mutableIntStateOf(0) }
    var secondaryTabIndex by remember { mutableIntStateOf(0) }
    // activeNavSet: 0 = Main, 1 = Secondary
    var activeNavSet by remember { mutableIntStateOf(0) }

    LaunchedEffect(navBarPagerState.currentPage) {
        activeNavSet = navBarPagerState.currentPage
    }

    var selectedNetworkChannel by remember { mutableIntStateOf(0) } // 0 = WhatsApp, 1 = Telegram
    var simulatorTargetInstanceId by remember { mutableStateOf<String?>(null) }

    val instances by viewModel.instances.collectAsState()
    val telegramAccounts by viewModel.telegramAccounts.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val knowledgeSources by viewModel.knowledgeSources.collectAsState()
    val mcpTools by viewModel.mcpTools.collectAsState()
    val recentMessages by viewModel.recentMessages.collectAsState()
    val webhooks by viewModel.webhooks.collectAsState()
    val quantizationStatus by viewModel.quantizationStatus.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val connectedInstancesCount = instances.count { it.status == "CONNECTED" }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ElegantDarkBg,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular Neurological Accent Avatar (as in Elegant Dark design)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (activeNavSet) {
                                            0 -> ElegantPurpleAccent
                                            1 -> Color(0xFF2AABEE)
                                            else -> Color(0xFFFFA000)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (activeNavSet) {
                                        0 -> Icons.Default.Psychology
                                        1 -> Icons.Default.Store
                                        else -> Icons.Default.Settings
                                    },
                                    contentDescription = "Module Header",
                                    tint = ElegantPurpleOnAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = when (activeNavSet) {
                                        0 -> "Agent Core"
                                        1 -> "Commerce & Telegram"
                                        else -> "Paramètres & Réglages"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = when (activeNavSet) {
                                        0 -> "AI EDGE QUANTIZER"
                                        1 -> "TELETHON & BUSINESS ENGINE"
                                        else -> "${appSettings.currency} (${appSettings.currencySymbol}) • ${appSettings.defaultCountryCode} ${appSettings.countryName.uppercase()}"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = when (activeNavSet) {
                                        0 -> ElegantPurpleAccent
                                        1 -> Color(0xFF2AABEE)
                                        else -> Color(0xFFFFA000)
                                    },
                                    fontSize = 10.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Live Engine Status Pill
                        Surface(
                            shape = CircleShape,
                            color = ElegantDarkSurfaceVariant,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (connectedInstancesCount > 0) ElegantGreenActive else ElegantPurpleSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (connectedInstancesCount > 0) "$connectedInstancesCount ONLINE" else "NPU ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElegantPurpleAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ElegantDarkBg
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = ElegantDarkBorder)

                // Sélecteur d'espace de travail : Agent Core (5) | Commerce & Telegram (8) | Paramètres (⚙️)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ElegantDarkSurface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isCore = (activeNavSet == 0)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCore) ElegantPurpleAccent.copy(alpha = 0.22f) else ElegantDarkBg,
                        border = BorderStroke(1.dp, if (isCore) ElegantPurpleAccent else ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                activeNavSet = 0
                                coroutineScope.launch { navBarPagerState.animateScrollToPage(0) }
                            }
                            .testTag("switch_to_agent_core")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = if (isCore) ElegantPurpleAccent else ElegantTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Core (5)",
                                color = if (isCore) ElegantTextPrimary else ElegantTextSecondary,
                                fontWeight = if (isCore) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    val isCommerce = (activeNavSet == 1)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCommerce) Color(0xFF2AABEE).copy(alpha = 0.22f) else ElegantDarkBg,
                        border = BorderStroke(1.dp, if (isCommerce) Color(0xFF2AABEE) else ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable {
                                activeNavSet = 1
                                coroutineScope.launch { navBarPagerState.animateScrollToPage(1) }
                            }
                            .testTag("switch_to_commerce")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Store,
                                contentDescription = null,
                                tint = if (isCommerce) Color(0xFF2AABEE) else ElegantTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Commerce (8)",
                                color = if (isCommerce) ElegantTextPrimary else ElegantTextSecondary,
                                fontWeight = if (isCommerce) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }

                    val isSettings = (activeNavSet == 2)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSettings) Color(0xFFFFA000).copy(alpha = 0.22f) else ElegantDarkBg,
                        border = BorderStroke(1.dp, if (isSettings) Color(0xFFFFA000) else ElegantDarkBorder),
                        modifier = Modifier
                            .weight(1.1f)
                            .clickable {
                                activeNavSet = 2
                            }
                            .testTag("switch_to_settings")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = if (isSettings) Color(0xFFFFA000) else ElegantTextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Réglages",
                                color = if (isSettings) ElegantTextPrimary else ElegantTextSecondary,
                                fontWeight = if (isSettings) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = ElegantDarkBorder)

                // Secondary Channel Selector when on Instances Tab (Barre 1, Tab 0)
                if (activeNavSet == 0 && currentTabIndex == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ElegantDarkSurface)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val waSelected = (selectedNetworkChannel == 0)
                        val isTgConnected = telegramAccounts.any { it.status == "CONNECTED" }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (waSelected) WhatsAppGreen.copy(alpha = 0.2f) else ElegantDarkBg,
                            border = BorderStroke(1.dp, if (waSelected) WhatsAppGreen else ElegantDarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedNetworkChannel = 0 }
                                .testTag("channel_tab_whatsapp")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = if (waSelected) WhatsAppGreen else ElegantTextSecondary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "WhatsApp Baileys",
                                    color = if (waSelected) ElegantTextPrimary else ElegantTextSecondary,
                                    fontWeight = if (waSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        val tgSelected = (selectedNetworkChannel == 1)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (tgSelected) Color(0xFF2AABEE).copy(alpha = 0.2f) else ElegantDarkBg,
                            border = BorderStroke(1.dp, if (tgSelected) Color(0xFF2AABEE) else ElegantDarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedNetworkChannel = 1 }
                                .testTag("channel_tab_telegram")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = if (tgSelected) Color(0xFF2AABEE) else ElegantTextSecondary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isTgConnected) "Telegram ●" else "Telegram Telethon",
                                    color = if (tgSelected) ElegantTextPrimary else ElegantTextSecondary,
                                    fontWeight = if (tgSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = ElegantDarkBorder)
                }
            }
        },
        bottomBar = {
            if (activeNavSet != 2) {
                Surface(
                    color = ElegantDarkSurface,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, ElegantDarkBorder)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    HorizontalPager(
                        state = navBarPagerState,
                        modifier = Modifier.fillMaxWidth().testTag("navigation_horizontal_pager")
                    ) { pageIndex ->
                        if (pageIndex == 0) {
                            // =============================================================
                            // BARRE 1 : NAVIGATION ORIGINALE (Instances, Agents, Knowledge, Quantizer, Threads)
                            // =============================================================
                            NavigationBar(
                                containerColor = ElegantDarkSurface,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0),
                                modifier = Modifier.height(64.dp)
                            ) {
                                val navItemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElegantPurpleAccent,
                                    selectedTextColor = ElegantPurpleAccent,
                                    indicatorColor = ElegantDarkSurfaceVariant,
                                    unselectedIconColor = ElegantTextSecondary.copy(alpha = 0.7f),
                                    unselectedTextColor = ElegantTextSecondary.copy(alpha = 0.7f)
                                )

                                NavigationBarItem(
                                    selected = (activeNavSet == 0 && currentTabIndex == 0),
                                    onClick = {
                                        activeNavSet = 0
                                        currentTabIndex = 0
                                    },
                                    icon = { Icon(Icons.Default.Hub, contentDescription = "Instances") },
                                    label = { Text("Instances", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (activeNavSet == 0 && currentTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("tab_instances")
                                )
                                NavigationBarItem(
                                    selected = (activeNavSet == 0 && currentTabIndex == 1),
                                    onClick = {
                                        activeNavSet = 0
                                        currentTabIndex = 1
                                    },
                                    icon = { Icon(Icons.Default.SmartToy, contentDescription = "Agents") },
                                    label = { Text("Agents", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (activeNavSet == 0 && currentTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("tab_agents")
                                )
                                NavigationBarItem(
                                    selected = (activeNavSet == 0 && currentTabIndex == 2),
                                    onClick = {
                                        activeNavSet = 0
                                        currentTabIndex = 2
                                    },
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Knowledge") },
                                    label = { Text("Knowledge", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (activeNavSet == 0 && currentTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("tab_knowledge")
                                )
                                NavigationBarItem(
                                    selected = (activeNavSet == 0 && currentTabIndex == 3),
                                    onClick = {
                                        activeNavSet = 0
                                        currentTabIndex = 3
                                    },
                                    icon = { Icon(Icons.Default.Memory, contentDescription = "Quantizer") },
                                    label = { Text("Quantizer", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (activeNavSet == 0 && currentTabIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("tab_quantizer")
                                )
                                NavigationBarItem(
                                    selected = (activeNavSet == 0 && currentTabIndex == 4),
                                    onClick = {
                                        activeNavSet = 0
                                        currentTabIndex = 4
                                    },
                                    icon = { Icon(Icons.Default.Chat, contentDescription = "Threads") },
                                    label = { Text("Threads", fontSize = 10.sp, maxLines = 1, softWrap = false, fontWeight = if (activeNavSet == 0 && currentTabIndex == 4) FontWeight.Bold else FontWeight.Normal) },
                                    colors = navItemColors,
                                    modifier = Modifier.testTag("tab_simulator")
                                )
                            }
                        } else {
                            // =============================================================
                            // BARRE 2 : DEUXIÈME BARRE DEMANDÉE (Swipe horizontal)
                            // Telegram · Produits · Catégories · Fournisseurs · Contacts & Tarifs · Agences Livraison · Affiliés · Commandes
                            // =============================================================
                            val secondaryTabs = listOf(
                                Triple("Telegram", Icons.Default.Send, 0),
                                Triple("Produits", Icons.Default.Inventory2, 1),
                                Triple("Catégories", Icons.Default.Category, 2),
                                Triple("Fournisseurs", Icons.Default.Store, 3),
                                Triple("Contacts & Tarifs", Icons.Default.RequestQuote, 4),
                                Triple("Agences Livraison", Icons.Default.LocalShipping, 5),
                                Triple("Affiliés", Icons.Default.Group, 6),
                                Triple("Commandes", Icons.Default.ReceiptLong, 7)
                            )

                            ScrollableTabRow(
                                selectedTabIndex = secondaryTabIndex,
                                containerColor = ElegantDarkSurface,
                                contentColor = Color(0xFF2AABEE),
                                edgePadding = 12.dp,
                                modifier = Modifier.fillMaxWidth().height(64.dp).testTag("secondary_nav_tab_row"),
                                indicator = { tabPositions ->
                                    if (secondaryTabIndex < tabPositions.size) {
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[secondaryTabIndex]),
                                            color = Color(0xFF2AABEE),
                                            height = 3.dp
                                        )
                                    }
                                }
                            ) {
                                secondaryTabs.forEach { (title, icon, index) ->
                                    val isSelected = (activeNavSet == 1 && secondaryTabIndex == index)
                                    Tab(
                                        selected = isSelected,
                                        onClick = {
                                            activeNavSet = 1
                                            secondaryTabIndex = index
                                        },
                                        modifier = Modifier.padding(horizontal = 4.dp).testTag("sec_tab_$index"),
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    icon,
                                                    contentDescription = title,
                                                    tint = if (isSelected) Color(0xFF2AABEE) else ElegantTextSecondary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = title,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) ElegantTextPrimary else ElegantTextSecondary.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ElegantDarkBg)
                .padding(innerPadding)
        ) {
            when (activeNavSet) {
                0 -> {
                    // Contenu Barre Principale (1)
                    when (currentTabIndex) {
                        0 -> {
                            if (selectedNetworkChannel == 0) {
                                InstancesScreen(
                                    viewModel = viewModel,
                                    instances = instances,
                                    onOpenSimulatorForInstance = { instId ->
                                        simulatorTargetInstanceId = instId
                                        currentTabIndex = 4 // switch to simulator tab
                                    }
                                )
                            } else {
                                TelegramScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                        1 -> AgentsScreen(
                            viewModel = viewModel,
                            agents = agents,
                            instances = instances
                        )
                        2 -> KnowledgeRagScreen(
                            viewModel = viewModel,
                            sources = knowledgeSources,
                            agents = agents
                        )
                        3 -> EdgeQuantizerScreen(
                            viewModel = viewModel,
                            quantizationStatus = quantizationStatus
                        )
                        4 -> McpAndSimulatorScreen(
                            viewModel = viewModel,
                            instances = instances,
                            mcpTools = mcpTools,
                            messages = recentMessages,
                            webhooks = webhooks,
                            initialInstanceId = simulatorTargetInstanceId
                        )
                    }
                }
                1 -> {
                    // Contenu Deuxième Barre E-commerce & Telegram (2)
                    // Telegram · Produits · Catégories · Fournisseurs · Contacts & Tarifs · Agences Livraison · Affiliés · Commandes
                    when (secondaryTabIndex) {
                        0 -> TelegramScreen(viewModel = viewModel)
                        1 -> ProductsScreen(viewModel = viewModel)
                        2 -> CategoriesScreen(viewModel = viewModel)
                        3 -> SuppliersScreen(viewModel = viewModel)
                        4 -> PriceContactsScreen(viewModel = viewModel)
                        5 -> ShippingAgenciesScreen(viewModel = viewModel)
                        6 -> AffiliatesScreen(viewModel = viewModel)
                        7 -> OrdersScreen(viewModel = viewModel)
                    }
                }
                else -> {
                    // Module Paramètres & Réglages Globaux (B)
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
