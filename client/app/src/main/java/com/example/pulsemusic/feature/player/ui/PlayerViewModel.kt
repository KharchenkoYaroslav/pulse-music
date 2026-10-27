package com.example.pulsemusic.feature.player.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pulsemusic.core.data.CoreRepository
import com.example.pulsemusic.core.data.LocalSongManager
import com.example.pulsemusic.core.data.model.CategoryListResponse
import com.example.pulsemusic.core.util.AppToastManager
import com.example.pulsemusic.feature.player.MusicPlayerManager
import com.example.pulsemusic.feature.player.data.PlayerRepository
import com.example.pulsemusic.feature.player.data.model.PlayerSource
import com.example.pulsemusic.feature.player.data.model.Song
import com.example.pulsemusic.feature.player.data.model.UserPlaylistState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isBuffering: Boolean = false,
    val userPlaylists: List<UserPlaylistState> = emptyList(),
    val categories: List<CategoryListResponse> = emptyList(),
    val showPlaylistSheet: Boolean = false,
    val showQueueSheet: Boolean = false,
    val queueSongs: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val currentSource: PlayerSource = PlayerSource.EXTERNAL,
    val isCurrentSongDownloaded: Boolean = false,
    val isDownloading: Boolean = false,
)

// Модель представлення плеєра
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: MusicPlayerManager,
    private val repository: PlayerRepository,
    private val coreRepository: CoreRepository,
    private val localSongManager: LocalSongManager,
) : ViewModel() {

    // Стан UI плеєру
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // Для використання в MainAppScaffold
    val currentSong: StateFlow<Song?> = _uiState
        .map { it.currentSong }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Id пісень в черзі
    private var currentQueueSongIds: List<String> = emptyList()

    // Завдання завантаження пісні, черги та перевірки плейлистів
    private var loadSongJob: Job? = null
    private var loadQueueMetadataJob: Job? = null
    private var checkPlaylistsJob: Job? = null

    init {
        playerManager.onSongEnded = { playNext() }

        viewModelScope.launch {
            coreRepository.refreshCategories()
        }

        if (checkLoggedIn()) {
            loadUserPlaylists()
        }

        // Спостереження за станами плеєра
        viewModelScope.launch {
            playerManager.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlaying = playing) }
            }
        }
        viewModelScope.launch {
            playerManager.currentPosition.collect { position ->
                _uiState.update { it.copy(currentPositionMs = position) }
            }
        }
        viewModelScope.launch {
            playerManager.duration.collect { dur ->
                _uiState.update { it.copy(durationMs = dur) }
            }
        }
        viewModelScope.launch {
            playerManager.isBuffering.collect { buffering ->
                _uiState.update { it.copy(isBuffering = buffering) }
            }
        }

        // Спостереження за категоріями
        viewModelScope.launch {
            coreRepository.observeCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }

        // Спостереження за завантаженими піснями
        viewModelScope.launch {
            localSongManager.observeDownloadedSongs().collect { downloadedList ->
                val current = _uiState.value.currentSong
                val isDownloaded = (current != null) && downloadedList.any { it.id == current.id }
                _uiState.update { it.copy(isCurrentSongDownloaded = isDownloaded) }

                if (_uiState.value.currentSource == PlayerSource.LOCAL) {
                    val downloadedIds = downloadedList.asSequence().map { it.id }.toSet()
                    val missingIds = currentQueueSongIds.filter { it !in downloadedIds }
                    for (missingId in missingIds) {
                        val index = currentQueueSongIds.indexOf(missingId)
                        if (index != -1) {
                            removeFromQueue(index)
                        }
                    }
                }
            }
        }
    }

    // ## Авторизація та життєвий цикл

    // Перевірка чи користувач авторизований
    fun checkLoggedIn(): Boolean {
        return repository.checkIsUserLoggedIn()
    }

    // Обробка зміни стану авторизації
    fun onAuthChanged() {
        if (checkLoggedIn()) {
            loadUserPlaylists()
        }
    }

    // Очищення ресурсів плеєра при знищенні ViewModel
    override fun onCleared() {
        playerManager.release()
    }

    // ## Управління відтворенням

    // Перемикання відтворення / паузи
    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }

    // Перемотування на вказану позицію
    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    // Відтворення наступного треку з черги
    fun playNext() {
        if (currentQueueSongIds.isEmpty()) return
        val newIndex = (_uiState.value.currentQueueIndex + 1) % currentQueueSongIds.size
        _uiState.update { it.copy(currentQueueIndex = newIndex) }
        loadAndPlaySong(currentQueueSongIds[newIndex], _uiState.value.currentSource)
    }

    // Відтворення попереднього треку з черги
    fun playPrevious() {
        if (currentQueueSongIds.isEmpty()) return
        val currentIndex = _uiState.value.currentQueueIndex
        val newIndex = if ((currentIndex - 1) < 0) currentQueueSongIds.size - 1 else currentIndex - 1
        _uiState.update { it.copy(currentQueueIndex = newIndex) }
        loadAndPlaySong(currentQueueSongIds[newIndex], _uiState.value.currentSource)
    }

    // Завантаження треку та його відтворення
    fun loadAndPlaySong(songId: String, source: PlayerSource) {
        loadSongJob?.cancel()
        loadSongJob = viewModelScope.launch {
            val playerSong = repository.getSong(songId, source)
            if (this@launch.isActive) {
                if (playerSong != null) {
                    val isDownloaded = localSongManager.isSongDownloaded(playerSong.id)
                    _uiState.update {
                        it.copy(
                            currentSong = playerSong,
                            isCurrentSongDownloaded = isDownloaded,
                        )
                    }
                    val audioUrl = playerSong.song

                    if (!audioUrl.isNullOrBlank()) {
                        playerManager.play(audioUrl)
                    } else {
                        playerManager.stop()
                    }

                    if (source != PlayerSource.LOCAL) {
                        launch { repository.recordView(playerSong.id, source) }
                    }

                    if ((source == PlayerSource.EXTERNAL) && isDownloaded) {
                        launch { updateSong(playerSong) }
                    }
                } else {
                    AppToastManager.showMessage("Пісню видалено або вона недоступна")
                    val targetIndex = currentQueueSongIds.indexOf(songId)
                    if (targetIndex != -1) {
                        removeFromQueue(targetIndex)
                    }
                }
            }
        }
    }


    // ## Управління чергою відтворення


    // Відтворення окремого треку (створює чергу з одного елемента)
    fun playSingleSong(songId: String, source: PlayerSource = PlayerSource.EXTERNAL) {
        setQueue(listOf(songId), 0, source)
    }

    // Встановлення нової черги відтворення
    fun setQueue(songIds: List<String>, startIndex: Int = 0, source: PlayerSource = PlayerSource.EXTERNAL) {
        if (songIds.isEmpty() || (startIndex !in songIds.indices)) return
        currentQueueSongIds = songIds
        _uiState.update {
            it.copy(
                currentQueueIndex = startIndex,
                currentSource = source,
            )
        }
        loadAndPlaySong(songIds[startIndex], source)
        loadQueueMetadata(songIds, source)
    }

    // Відтворення треку за індексом у черзі
    fun playQueueSongAt(index: Int) {
        if (index !in currentQueueSongIds.indices) return
        _uiState.update { it.copy(currentQueueIndex = index) }
        loadAndPlaySong(currentQueueSongIds[index], _uiState.value.currentSource)
    }

    // Додавання треку наступним у чергу відтворення
    fun playNextInQueue(songId: String, source: PlayerSource = PlayerSource.EXTERNAL) {
        val currentList = currentQueueSongIds.toMutableList()
        val currentIndex = _uiState.value.currentQueueIndex
        val insertIndex = if ((currentIndex != -1) && currentList.isNotEmpty()) {
            currentIndex + 1
        } else {
            0
        }
        currentList.add(insertIndex, songId)

        if (currentList.size == 1) {
            setQueue(listOf(songId), 0, source)
        } else {
            currentQueueSongIds = currentList
            loadQueueMetadata(currentQueueSongIds, _uiState.value.currentSource)
        }
        AppToastManager.showMessage("Відтвориться наступним")
    }

    // Додавання треку в кінець черги
    fun addToQueueEnd(songId: String, source: PlayerSource = PlayerSource.EXTERNAL) {
        val currentList = currentQueueSongIds.toMutableList()
        currentList.add(songId)

        if (currentList.size == 1) {
            setQueue(listOf(songId), 0, source)
        } else {
            currentQueueSongIds = currentList
            loadQueueMetadata(currentQueueSongIds, _uiState.value.currentSource)
        }
        AppToastManager.showMessage("Додано до черги")
    }

    // Видалення треку з черги за індексом
    fun removeFromQueue(index: Int) {
        if (index !in currentQueueSongIds.indices) return
        val updatedIds = currentQueueSongIds.toMutableList().apply { removeAt(index) }
        val updatedSongs = _uiState.value.queueSongs.toMutableList().apply {
            if (index in indices) removeAt(index)
        }

        currentQueueSongIds = updatedIds
        _uiState.update { it.copy(queueSongs = updatedSongs.toList()) }

        if (updatedIds.isEmpty()) {
            _uiState.update {
                it.copy(
                    currentQueueIndex = -1,
                    currentSong = null,
                    showQueueSheet = false,
                )
            }
            playerManager.stop()
            return
        }

        val currentIndex = _uiState.value.currentQueueIndex
        if (index < currentIndex) {
            _uiState.update { it.copy(currentQueueIndex = currentIndex - 1) }
        } else if (index == currentIndex) {
            val newIndex = currentIndex.coerceAtMost(updatedIds.size - 1)
            _uiState.update { it.copy(currentQueueIndex = newIndex) }
            loadAndPlaySong(updatedIds[newIndex], _uiState.value.currentSource)
        }
    }

    // Завантаження метаданих для треків у черзі
    private fun loadQueueMetadata(songIds: List<String>, source: PlayerSource) {
        loadQueueMetadataJob?.cancel()
        loadQueueMetadataJob = viewModelScope.launch {
            val currentMap = _uiState.value.queueSongs.associateBy { it.id }
            val updatedList = songIds.asSequence().map { id ->
                currentMap[id] ?: Song(
                    id = id,
                    title = "Завантаження...",
                    description = null,
                    authorId = "",
                    authorLogin = null,
                    categoryIds = emptyList(),
                    likesCount = 0,
                    hasLiked = false,
                    viewsCount = 0,
                    song = null,
                    cover = null,
                )
            }.toMutableList()

            for (i in updatedList.indices) {
                if (updatedList[i].title == "Завантаження...") {
                    localSongManager.getSongById(updatedList[i].id)?.let { localDetails ->
                        updatedList[i] = localDetails
                    }
                }
            }
            _uiState.update { it.copy(queueSongs = updatedList.toList()) }

            for (i in updatedList.indices) {
                if (!isActive) break
                if (updatedList[i].title == "Завантаження...") {
                    val songDetails = repository.getSong(updatedList[i].id, source)
                    if ((songDetails != null) && isActive) {
                        updatedList[i] = songDetails
                        _uiState.update { it.copy(queueSongs = updatedList.toList()) }
                    }
                }
            }
        }
    }

    // ## Робота з плейлистами та модальними вікнами

    // Завантаження плейлистів користувача
    fun loadUserPlaylists() {
        if (!checkLoggedIn()) return
        viewModelScope.launch {
            val playlists = repository.getUserPlaylists()
            val initialStates = playlists.map { p -> UserPlaylistState(p, isInPlaylist = false) }
            _uiState.update { it.copy(userPlaylists = initialStates) }

            val currentSongId = _uiState.value.currentSong?.id
            if (currentSongId != null && playlists.isNotEmpty()) {
                val playlistIds = playlists.map { it.id }
                val inPlaylistsMap = repository.checkSongInPlaylists(currentSongId, playlistIds)
                _uiState.update { state ->
                    state.copy(
                        userPlaylists = state.userPlaylists.map { item ->
                            val isIn = inPlaylistsMap[item.playlist.id] ?: false
                            item.copy(isInPlaylist = isIn)
                        },
                    )
                }
            }
        }
    }

    // Натискання на кнопку додавання до плейлиста (відкриває модальну шторку плейлистів)
    fun onAddToPlaylistClicked() {
        if (!checkLoggedIn()) {
            AppToastManager.showMessage("Авторизуйтесь, щоб додавати пісні до плейлиста")
            return
        }

        val targetSongId = _uiState.value.currentSong?.id ?: return

        _uiState.update { it.copy(showPlaylistSheet = true) }

        checkPlaylistsJob?.cancel()
        checkPlaylistsJob = viewModelScope.launch {
            val currentPlaylists = _uiState.value.userPlaylists
            val playlistIds = currentPlaylists.map { it.playlist.id }

            if (playlistIds.isNotEmpty()) {
                val inPlaylistsMap = repository.checkSongInPlaylists(targetSongId, playlistIds)
                if (isActive && (_uiState.value.currentSong?.id == targetSongId)) {
                    _uiState.update { state ->
                        state.copy(
                            userPlaylists = state.userPlaylists.map { item ->
                                val isIn = inPlaylistsMap[item.playlist.id] ?: item.isInPlaylist
                                item.copy(isInPlaylist = isIn)
                            },
                        )
                    }
                }
            }
        }
    }

    // Перемикання наявності треку у плейлисті (додати/видалити)
    fun toggleSongInPlaylist(playlistId: String, songId: String? = null, onResult: ((Boolean) -> Unit)? = null) {
        val targetSongId = songId ?: _uiState.value.currentSong?.id ?: return
        val currentItem = _uiState.value.userPlaylists.find { it.playlist.id == playlistId } ?: return
        val isCurrentlyIn = currentItem.isInPlaylist

        checkPlaylistsJob?.cancel()

        _uiState.update { state ->
            state.copy(
                userPlaylists = state.userPlaylists.map { item ->
                    if (item.playlist.id == playlistId) item.copy(isInPlaylist = !isCurrentlyIn) else item
                },
            )
        }

        viewModelScope.launch {
            val success = if (isCurrentlyIn) {
                repository.removeSongFromPlaylist(playlistId, targetSongId)
            } else {
                repository.addSongToPlaylist(playlistId, targetSongId)
            }

            if (!success) {
                _uiState.update { state ->
                    state.copy(
                        userPlaylists = state.userPlaylists.map { item ->
                            if (item.playlist.id == playlistId) item.copy(isInPlaylist = isCurrentlyIn) else item
                        },
                    )
                }
            }
            onResult?.invoke(success)
        }
    }

    // Відкриття шторки черги відтворення
    fun openQueueSheet() {
        _uiState.update { it.copy(showQueueSheet = true) }
    }

    // Закриття шторки черги відтворення
    fun closeQueueSheet() {
        _uiState.update { it.copy(showQueueSheet = false) }
    }

    // Закриття шторки плейлистів
    fun closePlaylistSheet() {
        _uiState.update { it.copy(showPlaylistSheet = false) }
    }

    // ## Лайки, завантаження та локальне оновлення

    // Перемикання лайка для треку
    fun toggleLike(songId: String? = null) {
        if (!checkLoggedIn()) {
            AppToastManager.showMessage("Авторизуйтесь, щоб ставити лайки")
            return
        }
        val targetId = songId ?: _uiState.value.currentSong?.id ?: return
        viewModelScope.launch {
            val newLikedState = coreRepository.toggleLike(targetId)
            val song = _uiState.value.currentSong
            if ((song != null) && (song.id == targetId)) {
                val delta = if (newLikedState && !song.hasLiked) 1 else if (!newLikedState && song.hasLiked) -1 else 0
                val updatedSong = song.copy(
                    hasLiked = newLikedState,
                    likesCount = (song.likesCount + delta).coerceAtLeast(0),
                )
                _uiState.update { it.copy(currentSong = updatedSong) }
            }
        }
    }

    // Завантаження треку для офлайн-прослуховування
    fun downloadSong(song: Song? = null) {
        val targetSong = song ?: _uiState.value.currentSong ?: return
        if (_uiState.value.isDownloading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloading = true) }
            val success = repository.downloadSong(targetSong)
            if (success) {
                AppToastManager.showMessage("Пісню успішно завантажено")
                _uiState.update { it.copy(isCurrentSongDownloaded = true) }
            } else {
                AppToastManager.showMessage("Помилка завантаження пісні")
            }
            _uiState.update { it.copy(isDownloading = false) }
        }
    }

    // Видалення завантаженого треку з локального сховища
    fun deleteSong(songId: String? = null) {
        val targetId = songId ?: _uiState.value.currentSong?.id ?: return
        if (_uiState.value.isDownloading) return

        viewModelScope.launch {
            val success = repository.deleteSong(targetId)
            if (success) {
                AppToastManager.showMessage("Завантажену пісню видалено")
                if (targetId == _uiState.value.currentSong?.id) {
                    _uiState.update { it.copy(isCurrentSongDownloaded = false) }
                }

                if (_uiState.value.currentSource == PlayerSource.LOCAL) {
                    val index = currentQueueSongIds.indexOf(targetId)
                    if (index != -1) {
                        removeFromQueue(index)
                    }
                }
            } else {
                AppToastManager.showMessage("Помилка при видаленні завантаження")
            }
        }
    }

    // Оновлення метаданих треку в локальному сховищі
    fun updateSong(song: Song) {
        viewModelScope.launch {
            val latestSong = repository.getSong(song.id, _uiState.value.currentSource) ?: song
            repository.updateSong(latestSong)
        }
    }
}
