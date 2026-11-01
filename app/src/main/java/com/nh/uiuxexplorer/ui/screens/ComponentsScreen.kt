package com.nh.uiuxexplorer.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nh.uiuxexplorer.ui.common.GlassCard
import com.nh.uiuxexplorer.ui.common.PillLabel
import com.nh.uiuxexplorer.ui.common.Screen
import com.nh.uiuxexplorer.ui.preview.StylePreview

private class Demo(val title: String, val designName: String, val description: String, val content: @Composable () -> Unit)

private val demos: List<Demo> = listOf(
    Demo("Buttons", "Filled / Tonal / Outlined / Text", "Emphasis levels from highest (filled) to lowest (text).") { ButtonsDemo() },
    Demo("Cards", "Elevated / Filled / Outlined Card", "Containers that group related content.") { CardsDemo() },
    Demo("Navigation bar", "Top App Bar (center-aligned)", "Title and primary actions at the top of a screen.") { TopBarDemo() },
    Demo("Bottom navigation", "Bottom Navigation Bar", "Three to five top-level destinations, thumb-reachable.") { BottomNavDemo() },
    Demo("Side navigation", "Navigation Rail", "Vertical destinations for tablets and large windows.") { RailDemo() },
    Demo("Search bar", "Pill search field", "A rounded field with a leading search icon.") { SearchDemo() },
    Demo("Text fields", "Outlined / Filled Text Field", "Single-line inputs with labels.") { TextFieldsDemo() },
    Demo("Checkboxes", "Checkbox / Tri-state", "Multiple selections, including a parent state.") { CheckboxDemo() },
    Demo("Radio buttons", "Radio Button group", "Exactly one choice from a short list.") { RadioDemo() },
    Demo("Switches", "Switch", "Immediate on/off settings.") { SwitchDemo() },
    Demo("Dialogs", "Alert Dialog", "Interrupts to confirm or decide. Tap the button.") { DialogDemo() },
    Demo("Bottom sheets", "Modal Bottom Sheet", "Supplementary content from the bottom. Tap the button.") { SheetDemo() },
    Demo("Floating action buttons", "FAB / Small FAB / Extended FAB", "The primary action of a screen.") { FabDemo() },
    Demo("Tabs", "Tab Row", "Switch between related views.") { TabsDemo() },
    Demo("Menus", "Dropdown Menu", "Contextual list of actions. Tap the button.") { MenuDemo() },
    Demo("Progress indicators", "Linear / Circular (determinate and indeterminate)", "Drag the slider to change progress.") { ProgressDemo() },
    Demo("Loading animations", "Pulsing dots / Skeleton shimmer", "Feedback while content loads.") { LoadingDemo() }
)

private class Sample(val key: String, val title: String, val note: String)

private val samples = listOf(
    Sample("glass", "Glassmorphism", "Translucent layers with border highlights."),
    Sample("neumorph", "Neumorphism", "Raised monochrome surfaces with paired shadows."),
    Sample("material", "Material 3", "Live Material 3 widgets you can tap."),
    Sample("liquid", "Liquid Glass", "Fluid translucent bar and card with specular edges."),
    Sample("minimal", "Minimalist", "White space and restraint."),
    Sample("cyber", "Cyberpunk", "Neon, cut corners, monospace."),
    Sample("bento", "Bento Grid", "Modular tiles of varied sizes."),
    Sample("flat", "Flat Design", "Solid color blocks."),
    Sample("brutal", "Neo-Brutalism", "Thick outlines and hard shadows."),
    Sample("clay", "Claymorphism", "Puffy soft 3D forms."),
    Sample("dark", "Dark UI", "Dark surfaces with one accent."),
    Sample("retro", "Retro UI", "Beveled window with a title bar.")
)

@Composable
fun ComponentsScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Screen("Explorer") {
        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                modifier = Modifier.height(36.dp),
                text = { Text("Components", fontSize = 12.sp) }
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                modifier = Modifier.height(36.dp),
                text = { Text("Style samples", fontSize = 12.sp) }
            )
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (tab == 0) {
                items(demos, key = { it.title }) { d ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(d.title, style = MaterialTheme.typography.titleMedium)
                                PillLabel(d.designName)
                            }
                            Text(
                                d.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(Modifier.fillMaxWidth().padding(top = 2.dp)) { d.content() }
                        }
                    }
                }
            } else {
                items(samples, key = { it.key }) { s ->
                    GlassCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(s.title, style = MaterialTheme.typography.titleMedium)
                                Text(s.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StylePreview(s.key)
                        }
                    }
                }
            }
        }
    }
}

private val NoInsets = WindowInsets(0, 0, 0, 0)

@Composable
private fun ButtonsDemo() {
    val pad = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Button(onClick = {}, modifier = Modifier.height(30.dp), contentPadding = pad) { Text("Filled", fontSize = 11.sp) }
        FilledTonalButton(onClick = {}, modifier = Modifier.height(30.dp), contentPadding = pad) { Text("Tonal", fontSize = 11.sp) }
        OutlinedButton(onClick = {}, modifier = Modifier.height(30.dp), contentPadding = pad) { Text("Outlined", fontSize = 11.sp) }
        ElevatedButton(onClick = {}, modifier = Modifier.height(30.dp), contentPadding = pad) { Text("Elevated", fontSize = 11.sp) }
        TextButton(onClick = {}, modifier = Modifier.height(30.dp), contentPadding = pad) { Text("Text", fontSize = 11.sp) }
    }
}

@Composable
private fun CardsDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ElevatedCard(Modifier.weight(1f)) { Text("Elevated", Modifier.padding(10.dp), fontSize = 11.sp) }
        Card(Modifier.weight(1f)) { Text("Filled", Modifier.padding(10.dp), fontSize = 11.sp) }
        OutlinedCard(Modifier.weight(1f)) { Text("Outlined", Modifier.padding(10.dp), fontSize = 11.sp) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarDemo() {
    CenterAlignedTopAppBar(
        title = { Text("Title", fontSize = 14.sp) },
        navigationIcon = { IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Menu, "Menu", modifier = Modifier.size(18.dp)) } },
        actions = { IconButton(onClick = {}, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Search, "Search", modifier = Modifier.size(18.dp)) } },
        windowInsets = NoInsets,
        modifier = Modifier.height(44.dp).clip(RoundedCornerShape(10.dp))
    )
}

@Composable
private fun BottomNavDemo() {
    var sel by remember { mutableIntStateOf(0) }
    val items = listOf("Home" to Icons.Default.Home, "Favorites" to Icons.Default.Favorite, "Settings" to Icons.Default.Settings)
    NavigationBar(windowInsets = NoInsets, modifier = Modifier.height(56.dp).clip(RoundedCornerShape(10.dp))) {
        items.forEachIndexed { i, (label, icon) ->
            NavigationBarItem(
                selected = sel == i,
                onClick = { sel = i },
                icon = { Icon(icon, null, modifier = Modifier.size(17.dp)) },
                label = { Text(label, fontSize = 10.sp) }
            )
        }
    }
}

@Composable
private fun RailDemo() {
    var sel by remember { mutableIntStateOf(0) }
    val items = listOf("Home" to Icons.Default.Home, "Search" to Icons.Default.Search, "Settings" to Icons.Default.Settings)
    Row(Modifier.height(148.dp).clip(RoundedCornerShape(10.dp))) {
        NavigationRail(windowInsets = NoInsets) {
            items.forEachIndexed { i, (label, icon) ->
                NavigationRailItem(
                    selected = sel == i,
                    onClick = { sel = i },
                    icon = { Icon(icon, null, modifier = Modifier.size(16.dp)) },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }
        Box(Modifier.weight(1f).height(148.dp), contentAlignment = Alignment.Center) {
            Text("Selected: ${items[sel].first}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SearchDemo() {
    var q by remember { mutableStateOf("") }
    TextField(
        value = q,
        onValueChange = { q = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        shape = CircleShape,
        placeholder = { Text("Search", fontSize = 12.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
    )
}

@Composable
private fun TextFieldsDemo() {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            a,
            { a = it },
            label = { Text("Outlined", fontSize = 11.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        TextField(
            b,
            { b = it },
            label = { Text("Filled", fontSize = 11.sp) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CheckboxDemo() {
    var a by remember { mutableStateOf(true) }
    var b by remember { mutableStateOf(false) }
    val parent = when {
        a && b -> ToggleableState.On
        !a && !b -> ToggleableState.Off
        else -> ToggleableState.Indeterminate
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(state = parent, onClick = { val v = parent != ToggleableState.On; a = v; b = v })
            Text("All", fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = a, onCheckedChange = { a = it })
            Text("Option A", fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = b, onCheckedChange = { b = it })
            Text("Option B", fontSize = 12.sp)
        }
    }
}

@Composable
private fun RadioDemo() {
    var sel by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("Small", "Medium", "Large").forEachIndexed { i, s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = sel == i, onClick = { sel = i })
                Text(s, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SwitchDemo() {
    var a by remember { mutableStateOf(true) }
    var b by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(a, { a = it }, modifier = Modifier.scale(0.8f))
            Text("  Wi-Fi", fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(b, { b = it }, modifier = Modifier.scale(0.8f))
            Text("  Bluetooth", fontSize = 12.sp)
        }
    }
}

@Composable
private fun DialogDemo() {
    var show by remember { mutableStateOf(false) }
    Button(
        onClick = { show = true },
        modifier = Modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
    ) { Text("Show dialog", fontSize = 11.sp) }
    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            title = { Text("Discard draft?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = { TextButton(onClick = { show = false }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SheetDemo() {
    var show by remember { mutableStateOf(false) }
    Button(
        onClick = { show = true },
        modifier = Modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
    ) { Text("Show bottom sheet", fontSize = 11.sp) }
    if (show) {
        ModalBottomSheet(onDismissRequest = { show = false }) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Share via", style = MaterialTheme.typography.titleMedium)
                Text("Sheets keep the previous screen visible behind them.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { show = false }, modifier = Modifier.height(32.dp)) { Text("Close", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun FabDemo() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        SmallFloatingActionButton(onClick = {}) { Icon(Icons.Default.Add, "Add", modifier = Modifier.size(18.dp)) }
        FloatingActionButton(onClick = {}, modifier = Modifier.size(44.dp)) { Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(20.dp)) }
        ExtendedFloatingActionButton(
            text = { Text("Create", fontSize = 12.sp) },
            icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) },
            onClick = {},
            modifier = Modifier.height(40.dp)
        )
    }
}

@Composable
private fun TabsDemo() {
    var sel by remember { mutableIntStateOf(0) }
    TabRow(selectedTabIndex = sel, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
        listOf("Posts", "Replies", "Media").forEachIndexed { i, t ->
            Tab(selected = sel == i, onClick = { sel = i }, modifier = Modifier.height(32.dp), text = { Text(t, fontSize = 11.sp) })
        }
    }
}

@Composable
private fun MenuDemo() {
    var open by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { open = true },
            modifier = Modifier.height(30.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
        ) { Text("Open menu", fontSize = 11.sp) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            listOf("Edit", "Duplicate", "Delete").forEach { DropdownMenuItem(text = { Text(it, fontSize = 12.sp) }, onClick = { open = false }) }
        }
    }
}

@Composable
private fun ProgressDemo() {
    var p by remember { mutableFloatStateOf(0.4f) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Slider(value = p, onValueChange = { p = it }, modifier = Modifier.height(26.dp))
        LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth())
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(progress = { p }, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
        }
    }
}

@Composable
private fun LoadingDemo() {
    val t = rememberInfiniteTransition(label = "loading")
    val primary = MaterialTheme.colorScheme.primary
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) { i ->
                val a by t.animateFloat(
                    initialValue = 0.25f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(600, delayMillis = i * 150), RepeatMode.Reverse),
                    label = "dot$i"
                )
                Box(Modifier.size(10.dp).alpha(a).background(primary, CircleShape))
            }
        }
        val x by t.animateFloat(
            initialValue = -300f, targetValue = 700f,
            animationSpec = infiniteRepeatable(tween(1300, easing = LinearEasing)),
            label = "shimmer"
        )
        val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        val hi = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(1f, 0.8f, 0.55f).forEach { w ->
                Box(
                    Modifier.fillMaxWidth(w).height(10.dp).clip(RoundedCornerShape(5.dp))
                        .background(Brush.linearGradient(listOf(base, hi, base), start = Offset(x, 0f), end = Offset(x + 300f, 0f)))
                )
            }
        }
    }
}
