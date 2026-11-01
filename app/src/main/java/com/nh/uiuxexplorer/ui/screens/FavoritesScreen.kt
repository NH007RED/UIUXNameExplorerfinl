package com.nh.uiuxexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.ui.common.DesignCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.vm.FavoritesViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onOpen: (String) -> Unit, vm: FavoritesViewModel = hiltViewModel()) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()
    val searches by vm.searches.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var catFilter by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    Screen("Favorites & History") {
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                modifier = Modifier.height(36.dp),
                text = { Text("Favorites", fontSize = 12.sp) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                modifier = Modifier.height(36.dp),
                text = { Text("Recent", fontSize = 12.sp) }
            )
            Tab(
                selected = tab == 2,
                onClick = { tab = 2 },
                modifier = Modifier.height(36.dp),
                text = { Text("Searches", fontSize = 12.sp) }
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (tab) {
                0 -> {
                    val cats = favorites.groupingBy { it.category }.eachCount()
                    if (cats.isNotEmpty()) {
                        item {
                            Text("Favorite categories", style = MaterialTheme.typography.labelMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                            ) {
                                cats.forEach { (c, n) ->
                                    FilterChip(
                                        selected = catFilter == c,
                                        onClick = { catFilter = if (catFilter == c) null else c },
                                        label = { Text("$c ($n)", fontSize = 11.sp) },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }
                    val shown = favorites.filter { catFilter == null || it.category == catFilter }
                    if (shown.isEmpty()) {
                        item {
                            Text(
                                "No favorites yet. Tap the heart on any design.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }
                    items(shown, key = { it.id }) { e -> DesignCard(e, onClick = { onOpen(e.id) }) }
                }
                1 -> {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recently viewed", style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = vm::clearRecent,
                                enabled = recent.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) { Text("Clear", fontSize = 11.sp) }
                        }
                    }
                    if (recent.isEmpty()) {
                        item { Text("Nothing viewed yet.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant) }
                    }
                    items(recent, key = { it.id }) { e -> DesignCard(e, onClick = { onOpen(e.id) }) }
                }
                else -> {
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Search history (tap a chip to remove it)", style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = vm::clearSearches,
                                enabled = searches.isNotEmpty(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) { Text("Clear all", fontSize = 11.sp) }
                        }
                    }
                    if (searches.isEmpty()) {
                        item { Text("No searches yet.", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant) }
                    }
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            searches.forEach { q ->
                                InputChip(
                                    selected = false,
                                    onClick = { vm.deleteSearch(q) },
                                    label = { Text(q, fontSize = 11.sp) },
                                    trailingIcon = { Icon(Icons.Default.Close, "Remove $q", modifier = Modifier.size(14.dp)) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
