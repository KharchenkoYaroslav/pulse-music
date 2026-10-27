package com.example.pulsemusic.core.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ExtractedSongMetadata(
    val title: String? = null,
    val author: String? = null,
    val coverUri: Uri? = null
)

// Екстрактор метаданих з файлу пісні(щоб не вводити/шукати все вручну)
suspend fun extractSongMetadata(context: Context, uri: Uri): ExtractedSongMetadata = withContext(Dispatchers.IO) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val extractedAuthor = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val artBytes = retriever.embeddedPicture
        var extractedCoverUri: Uri? = null

        if (artBytes != null) {
            val tempFile = File.createTempFile("extracted_cover", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { it.write(artBytes) }
            extractedCoverUri = Uri.fromFile(tempFile)
        }

        ExtractedSongMetadata(
            title = extractedTitle?.takeIf { it.isNotBlank() },
            author = extractedAuthor?.takeIf { it.isNotBlank() },
            coverUri = extractedCoverUri
        )
    } catch (e: Exception) {
        e.printStackTrace()
        ExtractedSongMetadata()
    } finally {
        try { retriever.release() } catch (_: Exception) {}
    }
}
