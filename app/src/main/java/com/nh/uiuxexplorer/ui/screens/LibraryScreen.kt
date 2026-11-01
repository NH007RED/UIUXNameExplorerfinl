package com.nh.uiuxexplorer.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nh.uiuxexplorer.domain.model.Categories
import com.nh.uiuxexplorer.domain.model.SortMode
import com.nh.uiuxexplorer.ui.common.DesignCard
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.vm.LibraryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(onOpen: (String) -> Unit, vm: LibraryViewModel = hiltViewModel()) {
    val filter by vm.filter.collectAsStateWithLifecycle()
    val results by vm.results.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val favIds by vm.favoriteIds.collectAsStateWithLifecycle()
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var sortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (vm.autoFocus) runCatching { focus.requestFocus() }
    }

    Screen("Library") {
        OutlinedTextField(
            value = filter.query,
            onValueChange = vm::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .focusRequester(focus),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text("Try: Glass UI, Blur UI, 3D Design, Modern Dashboard", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (filter.query.isNotEmpty()) {
                    IconButton(onClick = { vm.setQuery("") }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.commitSearch(); keyboard?.hide() })
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = filter.category == null,
                    onClick = { vm.setCategory(null) },
                    label = { Text("All", fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
            items(Categories.names) { c ->
                FilterChip(
                    selected = filter.category == c,
                    onClick = { vm.setCategory(if (filter.category == c) null else c) },
                    label = { Text(c, fontSize = 11.sp) },
                    modifier = Modifier.height(28.dp)
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AssistChip(
                    onClick = { sortMenu = true },
                    label = { Text("Sort: ${filter.sort.label}", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp)) },
                    modifier = Modifier.height(28.dp)
                )
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    SortMode.entries.forEach { s ->
                        DropdownMenuItem(text = { Text(s.label, fontSize = 12.sp) }, onClick = { vm.setSort(s); sortMenu = false })
                    }
                }
            }
            FilterChip(
                selected = filter.favoritesOnly,
                onClick = vm::toggleFavoritesOnly,
                label = { Text("Favorites (${favIds.size})", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        if (filter.favoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                modifier = Modifier.height(28.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${results.size} result${if (results.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (filter.query.isBlank() && history.isNotEmpty()) {
            Row(
                Modifier.padding(start = 12.dp, end = 6.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text("  Recent searches", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = vm::clearSearchHistory,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) { Text("Clear", fontSize = 11.sp) }
            }
            FlowRow(
                Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                history.take(8).forEach { q ->
                    AssistChip(
                        onClick = { vm.setQuery(q) },
                        label = { Text(q, fontSize = 11.sp) },
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }
        if (results.isEmpty()) {
            Text(
                "No matches. Try a shorter term, an alias, or clear the filters.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(results, key = { it.id }) { e ->
                DesignCard(e, onClick = { vm.commitSearch(); onOpen(e.id) })
            }
        }
    }
}
