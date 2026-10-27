package com.example.pulsemusic.core.data

import android.content.Context
import com.example.pulsemusic.core.database.dao.SongDao
import com.example.pulsemusic.core.database.mapper.toEntity
import com.example.pulsemusic.core.database.mapper.toSong
import com.example.pulsemusic.core.network.BackendHostManager
import com.example.pulsemusic.feature.player.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

interface LocalSongManager {
    fun observeDownloadedSongs(): Flow<List<Song>>
    suspend fun getSongById(id: String): Song?
    suspend fun isSongDownloaded(id: String): Boolean
    suspend fun saveSong(song: Song): Boolean
    suspend fun deleteSong(id: String): Boolean
    suspend fun updateSong(serverSong: Song): Boolean
}

// Управління локально збереженими піснями
class LocalSongManagerImpl(
    private val context: Context,
    private val backendHostManager: BackendHostManager,
    private val songDao: SongDao,
) : LocalSongManager {

    // Множина ID пісень, позначених для видалення або скасування завантаження
    private val pendingDeletions = ConcurrentHashMap.newKeySet<String>()

    override fun observeDownloadedSongs(): Flow<List<Song>> {
        return songDao.observeDownloadedSongs().map { list ->
            list.filter { entity ->
                entity.localAudioPath != null && File(entity.localAudioPath).exists()
            }.map { it.toSong() }
        }
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        val entity = songDao.getSongById(id) ?: return@withContext null
        if (entity.localAudioPath != null) {
            if (File(entity.localAudioPath).exists()) {
                entity.toSong()
            } else {
                songDao.removeLocalPaths(id)
                null
            }
        } else {
            null
        }
    }

    override suspend fun isSongDownloaded(id: String): Boolean = withContext(Dispatchers.IO) {
        val entity = songDao.getSongById(id) ?: return@withContext false
        entity.localAudioPath != null && File(entity.localAudioPath).exists()
    }

    override suspend fun saveSong(song: Song): Boolean = withContext(Dispatchers.IO) {
        try {
            // Видаляємо ID зі списку скасованих перед початком завантаження
            pendingDeletions.remove(song.id)

            val songsDir = File(context.filesDir, "songs").apply { mkdirs() }
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

            var localSongPath: String? = null
            var localCoverPath: String? = null

            val songDownloadUrl = resolveUrl(song.song)
            val coverDownloadUrl = resolveUrl(song.cover)

            var songSuccess = true

            // Завантаження або копіювання аудіофайлу
            if (songDownloadUrl.isNotBlank()) {
                val songFile = File(songsDir, "${song.id}.mp3")
                if (songDownloadUrl.startsWith("http")) {
                    if (downloadFile(songDownloadUrl, songFile)) {
                        localSongPath = songFile.absolutePath
                    } else {
                        songSuccess = false
                    }
                } else if (File(songDownloadUrl).exists()) {
                    localSongPath = songDownloadUrl
                }
            }

            // Перевіряємо, чи не було скасовано завантаження під час виконання
            if (pendingDeletions.contains(song.id)) {
                cleanUpFiles(song.id)
                return@withContext false
            }

            // Завантаження або копіювання обкладинки
            if (coverDownloadUrl.isNotBlank()) {
                val coverFile = File(coversDir, "${song.id}.jpg")
                if (coverDownloadUrl.startsWith("http")) {
                    if (downloadFile(coverDownloadUrl, coverFile)) {
                        localCoverPath = coverFile.absolutePath
                    }
                } else if (File(coverDownloadUrl).exists()) {
                    localCoverPath = coverDownloadUrl
                }
            }

            // Перевіряємо повторно, чи не було скасовано процес
            if (pendingDeletions.contains(song.id)) {
                cleanUpFiles(song.id)
                return@withContext false
            }

            // Зберігаємо запис у БД, якщо аудіо успішно збережено
            if (songSuccess) {
                val songEntity = song.toEntity(
                    localAudioPath = localSongPath,
                    localCoverPath = localCoverPath
                )
                songDao.upsertSong(songEntity)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteSong(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Позначаємо ID як відхилений/видалений та очищаємо локальні файли й БД
            pendingDeletions.add(id)
            cleanUpFiles(id)
            songDao.removeLocalPaths(id)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Видаляє локальні аудіофайли, обкладинки та відповідні тимчасові файли для заданого ID пісні
    private fun cleanUpFiles(id: String) {
        val songsDir = File(context.filesDir, "songs")
        val coversDir = File(context.filesDir, "covers")

        val audioFile = File(songsDir, "$id.mp3")
        val tmpAudioFile = File(songsDir, "$id.mp3.tmp")
        val coverFile = File(coversDir, "$id.jpg")
        val tmpCoverFile = File(coversDir, "$id.jpg.tmp")

        if (audioFile.exists()) audioFile.delete()
        if (tmpAudioFile.exists()) tmpAudioFile.delete()
        if (coverFile.exists()) coverFile.delete()
        if (tmpCoverFile.exists()) tmpCoverFile.delete()
    }

    override suspend fun updateSong(serverSong: Song): Boolean = withContext(Dispatchers.IO) {
        try {
            val localSongEntity = songDao.getSongById(serverSong.id) ?: return@withContext false

            val songsDir = File(context.filesDir, "songs").apply { mkdirs() }
            val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

            val songDownloadUrl = resolveUrl(serverSong.song)
            val coverDownloadUrl = resolveUrl(serverSong.cover)

            val songFile = File(songsDir, "${serverSong.id}.mp3")
            val coverFile = File(coversDir, "${serverSong.id}.jpg")

            var fileUpdated = false

            // Якщо змінився URL аудіо або файл відсутній — перезавантажуємо його
            val audioUrlChanged = localSongEntity.serverAudioUrl != serverSong.song
            if (localSongEntity.localAudioPath != null && (!songFile.exists() || audioUrlChanged) && songDownloadUrl.isNotBlank() && songDownloadUrl.startsWith("http")) {
                if (downloadFile(songDownloadUrl, songFile)) {
                    fileUpdated = true
                }
            }

            // Якщо змінився URL обкладинки або файл відсутній — перезавантажуємо його
            val coverUrlChanged = localSongEntity.serverCoverUrl != serverSong.cover
            if (localSongEntity.localCoverPath != null && (!coverFile.exists() || coverUrlChanged) && coverDownloadUrl.isNotBlank() && coverDownloadUrl.startsWith("http")) {
                if (downloadFile(coverDownloadUrl, coverFile)) {
                    fileUpdated = true
                }
            }

            // Перевіряємо, чи змінилися метадані пісні
            val hasMetadataChanges = localSongEntity.title != serverSong.title ||
                    localSongEntity.description != serverSong.description ||
                    localSongEntity.authorId != serverSong.authorId ||
                    localSongEntity.categoryIds != serverSong.categoryIds ||
                    localSongEntity.originalAuthor != serverSong.originalAuthor ||
                    localSongEntity.authorLogin != serverSong.authorLogin ||
                    localSongEntity.serverAudioUrl != serverSong.song ||
                    localSongEntity.serverCoverUrl != serverSong.cover

            // Оновлюємо сутність в БД при наявності змін метаданих або завантажених файлів
            if (hasMetadataChanges || fileUpdated) {
                val updatedEntity = serverSong.toEntity(
                    localAudioPath = if (songFile.exists()) songFile.absolutePath else localSongEntity.localAudioPath,
                    localCoverPath = if (coverFile.exists()) coverFile.absolutePath else localSongEntity.localCoverPath,
                    serverAudioUrl = serverSong.song,
                    serverCoverUrl = serverSong.cover
                )
                songDao.upsertSong(updatedEntity)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Формує повний URL з відносного шляху, використовуючи BackendHostManager
    private fun resolveUrl(path: String?): String {
        return backendHostManager.buildFullUrl(path) ?: ""
    }

    // Завантажує файл за посиланням urlString у тимчасовий .tmp файл, а після успішного завантаження перейменовує або копіює його в destFile
    private fun downloadFile(urlString: String, destFile: File): Boolean {
        val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
        return try {
            val connection = URL(urlString).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 30000

            connection.getInputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (tempFile.exists() && tempFile.length() > 0) {
                if (destFile.exists()) {
                    destFile.delete()
                }
                val renamed = tempFile.renameTo(destFile)
                if (!renamed) {
                    tempFile.copyTo(destFile, overwrite = true)
                    tempFile.delete()
                }
                true
            } else {
                tempFile.delete()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (tempFile.exists()) {
                tempFile.delete()
            }
            false
        }
    }
}
