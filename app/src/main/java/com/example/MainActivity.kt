package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.screens.AgentsScreen
import com.example.ui.screens.EdgeQuantizerScreen
import com.example.ui.screens.InstancesScreen
import com.example.ui.screens.KnowledgeRagScreen
import com.example.ui.screens.McpAndSimulatorScreen
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
    var currentTabIndex by remember { mutableIntStateOf(0) }
    var simulatorTargetInstanceId by remember { mutableStateOf<String?>(null) }

    val instances by viewModel.instances.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val knowledgeSources by viewModel.knowledgeSources.collectAsState()
    val mcpTools by viewModel.mcpTools.collectAsState()
    val recentMessages by viewModel.recentMessages.collectAsState()
    val webhooks by viewModel.webhooks.collectAsState()
    val quantizationStatus by viewModel.quantizationStatus.collectAsState()

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
                                    .background(ElegantPurpleAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Agent Core",
                                    tint = ElegantPurpleOnAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Agent Core",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElegantTextPrimary
                                )
                                Text(
                                    text = "AI EDGE QUANTIZER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = ElegantPurpleAccent,
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
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(thickness = 1.dp, color = ElegantDarkBorder)
                NavigationBar(
                    containerColor = ElegantDarkSurface,
                    tonalElevation = 0.dp
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElegantPurpleAccent,
                        selectedTextColor = ElegantPurpleAccent,
                        indicatorColor = ElegantDarkSurfaceVariant,
                        unselectedIconColor = ElegantTextSecondary.copy(alpha = 0.7f),
                        unselectedTextColor = ElegantTextSecondary.copy(alpha = 0.7f)
                    )

                    NavigationBarItem(
                        selected = currentTabIndex == 0,
                        onClick = { currentTabIndex = 0 },
                        icon = { Icon(Icons.Default.Hub, contentDescription = "Instances") },
                        label = { Text("Instances", fontSize = 11.sp, fontWeight = if (currentTabIndex == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_instances")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 1,
                        onClick = { currentTabIndex = 1 },
                        icon = { Icon(Icons.Default.SmartToy, contentDescription = "Agents") },
                        label = { Text("Agents", fontSize = 11.sp, fontWeight = if (currentTabIndex == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_agents")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 2,
                        onClick = { currentTabIndex = 2 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Knowledge") },
                        label = { Text("Knowledge", fontSize = 11.sp, fontWeight = if (currentTabIndex == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_knowledge")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 3,
                        onClick = { currentTabIndex = 3 },
                        icon = { Icon(Icons.Default.Memory, contentDescription = "Quantizer") },
                        label = { Text("Quantizer", fontSize = 11.sp, fontWeight = if (currentTabIndex == 3) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_quantizer")
                    )
                    NavigationBarItem(
                        selected = currentTabIndex == 4,
                        onClick = { currentTabIndex = 4 },
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Threads") },
                        label = { Text("Threads", fontSize = 11.sp, fontWeight = if (currentTabIndex == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = navItemColors,
                        modifier = Modifier.testTag("tab_simulator")
                    )
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
            when (currentTabIndex) {
                0 -> InstancesScreen(
                    viewModel = viewModel,
                    instances = instances,
                    onOpenSimulatorForInstance = { instId ->
                        simulatorTargetInstanceId = instId
                        currentTabIndex = 4 // switch to simulator tab
                    }
                )
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
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
