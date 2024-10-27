package tv.projectivy.plugin.wallpaperprovider.overflight

import tv.projectivy.plugin.wallpaperprovider.overflight.PreferencesManager.video_4k
import tv.projectivy.plugin.wallpaperprovider.overflight.PreferencesManager.video_hdr

data class Video(val location: String,
                 val title: String,
                 val url_1080p: String,
                 val url_1080p_hdr: String,
                 val url_4k: String,
                 val url_4k_hdr: String) {

    val preferredVideoUri: String
        get() = when {
                !video_4k && !video_hdr -> url_1080p
                !video_4k && video_hdr -> url_1080p_hdr
                video_4k && !video_hdr -> url_4k
                video_4k && video_hdr -> url_4k_hdr
                else -> url_1080p
            }
    }