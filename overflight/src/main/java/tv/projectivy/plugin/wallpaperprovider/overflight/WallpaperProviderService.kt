package tv.projectivy.plugin.wallpaperprovider.overflight

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tv.projectivy.plugin.wallpaperprovider.api.Event
import tv.projectivy.plugin.wallpaperprovider.api.IWallpaperProviderService
import tv.projectivy.plugin.wallpaperprovider.api.Wallpaper
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperDisplayMode
import tv.projectivy.plugin.wallpaperprovider.api.WallpaperType
import java.lang.reflect.Type

class WallpaperProviderService: Service() {

    val gson by lazy { Gson() }
    val videoListType: Type = object : TypeToken<List<Video>>() {}.type

    override fun onCreate() {
        super.onCreate()
        PreferencesManager.init(this)
        NetClientManager.init(this)
    }

    override fun onBind(intent: Intent): IBinder {
        // Return the interface.
        return binder
    }

    private val binder = object : IWallpaperProviderService.Stub() {
        override fun getWallpapers(event: Event?): List<Wallpaper> {
            // Don't care about the event : as the updateMode was declared as update_mode_time_elapsed
            // We will only receive this event, so no need to filter out other events
            return runBlocking {
                fetchWallpapers()
            }
        }

        override fun getPreferences(): String {
            return PreferencesManager.export()
        }

        override fun setPreferences(params: String) {
            PreferencesManager.import(params)
        }

    }

    suspend fun fetchWallpapers(): List<Wallpaper> {
        val jsonString = loadJson(PreferencesManager.videoSourceUrl)
        return parseJson(jsonString)
    }

    suspend fun loadJson(url: String): String? {
        return withContext(Dispatchers.IO) {
            NetClientManager.request(url)
        }
    }

    suspend fun parseJson(jsonString: String?): List<Wallpaper> {
        return withContext(Dispatchers.Default) {
            try {
                // Parse the JSON into a list of Video objects
                gson.fromJson<List<Video>?>(jsonString, videoListType)
                // Convert the list of Video objects into a list of Wallpaper objects
                ?.map { Wallpaper(it.preferredVideoUri, WallpaperType.VIDEO,
                    WallpaperDisplayMode.DEFAULT, it.title, it.location) }
                // Filter out any wallpapers with an empty URI
                ?.filter { it.uri.isNotBlank() }
                ?: emptyList()
            } catch (e: Exception) {
                Log.e("JSON Parsing", "Error parsing JSON: $e")
                emptyList()
            }
        }
    }
}