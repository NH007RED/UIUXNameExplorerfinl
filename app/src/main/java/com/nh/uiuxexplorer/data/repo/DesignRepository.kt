package com.nh.uiuxexplorer.data.repo

import com.nh.uiuxexplorer.data.catalog.CatalogDataSource
import com.nh.uiuxexplorer.data.local.FavoriteDao
import com.nh.uiuxexplorer.data.local.FavoriteEntity
import com.nh.uiuxexplorer.data.local.RecentViewDao
import com.nh.uiuxexplorer.data.local.RecentViewEntity
import com.nh.uiuxexplorer.data.local.SearchHistoryDao
import com.nh.uiuxexplorer.data.local.SearchHistoryEntity
import com.nh.uiuxexplorer.domain.model.DesignEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DesignRepository @Inject constructor(
    private val source: CatalogDataSource,
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentViewDao,
    private val searchDao: SearchHistoryDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _designs = MutableStateFlow<List<DesignEntry>>(emptyList())
    private val _loaded = MutableStateFlow(false)

    val designs: StateFlow<List<DesignEntry>> = _designs.asStateFlow()
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        scope.launch {
            runCatching { source.load() }.onSuccess { _designs.value = it }
            _loaded.value = true
        }
    }

    fun observeEntry(id: String): Flow<DesignEntry?> =
        designs.map { list -> list.firstOrNull { it.id == id } }

    val favoriteIds: Flow<List<String>> = favoriteDao.observeIds()

    val favorites: Flow<List<DesignEntry>> =
        combine(designs, favoriteDao.observeIds()) { all, ids ->
            ids.mapNotNull { id -> all.firstOrNull { it.id == id } }
        }

    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.observeCount(id).map { it > 0 }

    suspend fun toggleFavorite(id: String) {
        if (favoriteDao.count(id) > 0) favoriteDao.delete(id)
        else favoriteDao.insert(FavoriteEntity(id, System.currentTimeMillis()))
    }

    suspend fun clearFavorites() = favoriteDao.clear()

    val recent: Flow<List<DesignEntry>> =
        combine(designs, recentDao.observeIds()) { all, ids ->
            ids.mapNotNull { id -> all.firstOrNull { it.id == id } }
        }

    suspend fun markViewed(id: String) {
        recentDao.upsert(RecentViewEntity(id, System.currentTimeMillis()))
        recentDao.trim()
    }

    suspend fun clearRecent() = recentDao.clear()

    val searchHistory: Flow<List<String>> = searchDao.observe().map { list -> list.map { it.query } }

    suspend fun saveSearch(query: String) {
        val q = query.trim()
        if (q.length >= 2) searchDao.upsert(SearchHistoryEntity(q, System.currentTimeMillis()))
    }

    suspend fun deleteSearch(query: String) = searchDao.delete(query)

    suspend fun clearSearchHistory() = searchDao.clear()
}
