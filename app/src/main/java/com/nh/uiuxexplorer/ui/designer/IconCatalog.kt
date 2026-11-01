package com.nh.uiuxexplorer.ui.designer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** Icons the designer can place. Keys are stored in saved screens and used in generated prompts. */
object IconCatalog {
    val icons: Map<String, ImageVector> = linkedMapOf(
        "home" to Icons.Default.Home, "search" to Icons.Default.Search, "favorite" to Icons.Default.Favorite,
        "star" to Icons.Default.Star, "settings" to Icons.Default.Settings, "person" to Icons.Default.Person,
        "notifications" to Icons.Default.Notifications, "email" to Icons.Default.Email, "phone" to Icons.Default.Phone,
        "call" to Icons.Default.Call, "chat" to Icons.Default.Chat, "camera" to Icons.Default.CameraAlt,
        "image" to Icons.Default.Image, "share" to Icons.Default.Share, "edit" to Icons.Default.Edit,
        "delete" to Icons.Default.Delete, "add" to Icons.Default.Add, "check" to Icons.Default.Check,
        "close" to Icons.Default.Close, "menu" to Icons.Default.Menu, "shopping_cart" to Icons.Default.ShoppingCart,
        "location" to Icons.Default.LocationOn, "map" to Icons.Default.Map, "explore" to Icons.Default.Explore,
        "lock" to Icons.Default.Lock, "play" to Icons.Default.PlayArrow, "info" to Icons.Default.Info,
        "warning" to Icons.Default.Warning, "cloud" to Icons.Default.Cloud, "calendar" to Icons.Default.CalendarToday,
        "schedule" to Icons.Default.Schedule, "bookmark" to Icons.Default.Bookmark, "wifi" to Icons.Default.Wifi,
        "bluetooth" to Icons.Default.Bluetooth, "language" to Icons.Default.Language, "public" to Icons.Default.Public,
        "dashboard" to Icons.Default.Dashboard, "wallet" to Icons.Default.AccountBalanceWallet,
        "credit_card" to Icons.Default.CreditCard, "thumb_up" to Icons.Default.ThumbUp,
        "visibility" to Icons.Default.Visibility, "music" to Icons.Default.MusicNote, "movie" to Icons.Default.Movie,
        "refresh" to Icons.Default.Refresh, "work" to Icons.Default.Work, "school" to Icons.Default.School,
        "restaurant" to Icons.Default.Restaurant, "car" to Icons.Default.DirectionsCar, "flight" to Icons.Default.Flight,
        "fitness" to Icons.Default.FitnessCenter, "idea" to Icons.Default.Lightbulb
    )

    fun get(key: String): ImageVector = icons[key] ?: Icons.Default.Star

    fun label(key: String): String = key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
