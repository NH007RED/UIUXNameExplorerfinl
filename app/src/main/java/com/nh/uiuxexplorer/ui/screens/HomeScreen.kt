package com.nh.uiuxexplorer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.model.Categories
import com.nh.uiuxexplorer.domain.model.DesignEntry
import com.nh.uiuxexplorer.ui.common.AppBackground
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.PaletteBadge
import com.nh.uiuxexplorer.ui.common.SectionHeader
import com.nh.uiuxexplorer.ui.vm.HomeViewModel

private val categoryIcons: List<ImageVector> = listOf(
    Icons.Default.Palette, Icons.Default.Star, Icons.Default.Layers, Icons.Default.Apps,
    Icons.Default.Widgets, Icons.Default.Dashboard, Icons.Default.Style, Icons.Default.Category
)

fun categoryIcon(index: Int): ImageVector = categoryIcons[index.coerceAtLeast(0) % categoryIcons.size]

@Composable
fun HomeScreen(
    onOpenDesign: (String) -> Unit,
    onOpenLibrary: (category: Int, focusSearch: Boolean) -> Unit,
    onOpenScreens: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    vm: HomeViewModel = hiltViewModel()
) {
    val designs by vm.designs.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val rtState by vm.realtimeState.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme
    val popular = remember(designs) { designs.filter { it.popular } }
    val trending = remember(designs) { designs.filter { it.trending } }
    val counts = remember(designs) { designs.groupingBy { it.category }.eachCount() }
    var showAdminLogin by remember { mutableStateOf(false) }

    AppBackground {
        LazyColumn(
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Brush.linearGradient(listOf(Color(0xFF00C2D1), Color(0xFF5B2EFF)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("UI/UX Name Explorer", style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "${designs.size} terms",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF00C853)))
                            Icon(Icons.Default.Groups, null, tint = cs.primary, modifier = Modifier.size(12.dp))
                            Text(
                                "${rtState.totalUserCount} users",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    // Admin Icon ONLY appears when currentDeviceId matches Realtime Database adminDeviceIds!
                    if (rtState.isAdminDevice) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cs.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .border(0.8.dp, cs.primary.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (rtState.isAdminAuthenticated) onOpenAdmin() else showAdminLogin = true
                                }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin Control Panel",
                                    tint = cs.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Admin",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cs.primary
                                )
                            }
                        }
                    }
                }
            }
            item {
                GlassCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    onClick = { onOpenLibrary(-1, true) }
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, null, tint = cs.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Search styles, aliases, keywords...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                GlassCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    onClick = onOpenScreens
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(cs.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Layers, null, tint = cs.primary, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Screen Designer", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Pick a style, arrange UI with fingers, export PNG & prompt",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
            }
            if (popular.isNotEmpty()) {
                item { SectionHeader("Popular design styles") }
                item { DesignRow(popular, onOpenDesign) }
            }
            item { SectionHeader("Categories") }
            items(Categories.names.withIndex().toList().chunked(2)) { pair ->
                Row(
                    Modifier.padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { (i, name) ->
                        GlassCard(Modifier.weight(1f), onClick = { onOpenLibrary(i, false) }) {
                            Row(
                                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cs.primary.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(categoryIcon(i), null, tint = cs.primary, modifier = Modifier.size(15.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${counts[name] ?: 0} entries",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cs.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (recent.isNotEmpty()) {
                item { SectionHeader("Recently viewed") }
                item { DesignRow(recent, onOpenDesign) }
            }
            if (favorites.isNotEmpty()) {
                item { SectionHeader("Favorites") }
                item { DesignRow(favorites, onOpenDesign) }
            }
            if (trending.isNotEmpty()) {
                item {
                    SectionHeader("Trending terminology (curated)") {
                        Icon(Icons.Default.TrendingUp, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                    }
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(trending, key = { it.id }) { e ->
                            AssistChip(
                                onClick = { onOpenDesign(e.id) },
                                label = { Text(e.name, fontSize = 11.sp) },
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { onOpenLibrary(-1, false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Explore all designs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showAdminLogin) {
        AdminLoginDialog(
            onVerify = vm::verifyAdminLogin,
            onSuccess = {
                showAdminLogin = false
                onOpenAdmin()
            },
            onDismiss = { showAdminLogin = false }
        )
    }
}

@Composable
private fun DesignRow(list: List<DesignEntry>, onOpen: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list, key = { it.id }) { e ->
            GlassCard(Modifier.width(124.dp), onClick = { onOpen(e.id) }) {
                Column(Modifier.padding(9.dp)) {
                    PaletteBadge(e, 34.dp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        e.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        e.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
