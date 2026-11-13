package com.example.utils

import com.example.data.model.Channel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object M3uParser {
    suspend fun parseFromUrl(m3uUrl: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        try {
            val url = URL(m3uUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            var line: String?
            var currentMeta: String? = null
            
            while (reader.readLine().also { line = it } != null) {
                val trimmed = line!!.trim()
                if (trimmed.isEmpty()) continue
                
                if (trimmed.startsWith("#EXTINF:")) {
                    currentMeta = trimmed
                } else if (!trimmed.startsWith("#")) {
                    // This is a stream URL, merge with previous metadata
                    val extinf = currentMeta
                    val name = if (extinf != null) {
                        extractAttribute(extinf, "tvg-name")
                            ?: extractNameFromExtinf(extinf)
                            ?: "Channel ${channels.size + 1}"
                    } else {
                        "Channel ${channels.size + 1}"
                    }
                    
                    val logo = if (extinf != null) {
                        extractAttribute(extinf, "tvg-logo") ?: ""
                    } else {
                        ""
                    }
                    
                    val group = if (extinf != null) {
                        extractAttribute(extinf, "group-title") ?: ""
                    } else {
                        ""
                    }
                    
                    channels.add(
                        Channel(
                            name = name,
                            logoUrl = logo,
                            streamUrl = trimmed,
                            category = group
                        )
                    )
                    currentMeta = null
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        channels
    }

    private fun extractAttribute(extinf: String, attributeName: String): String? {
        val regex = """$attributeName=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(extinf)
        return match?.groupValues?.get(1)
    }

    private fun extractNameFromExtinf(extinf: String): String? {
        val commaIndex = extinf.lastIndexOf(',')
        if (commaIndex != -1 && commaIndex < extinf.length - 1) {
            val name = extinf.substring(commaIndex + 1).trim()
            if (name.isNotEmpty()) return name
        }
        return null
    }
}
