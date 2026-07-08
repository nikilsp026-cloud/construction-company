package com.construction.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a YouTube watch/share URL (as pasted by an admin) into an
 * embeddable player URL. Supports youtube.com/watch?v=, youtu.be/, and
 * youtube.com/shorts/ links; unrecognized URLs are returned unchanged so an
 * already-valid embed URL (or another provider's embeddable URL) still works.
 */
public final class VideoUtil {

    private VideoUtil() {
    }

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtube\\.com/shorts/|youtu\\.be/|youtube\\.com/embed/)([A-Za-z0-9_-]{11})"
    );

    public static String toEmbedUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher m = YOUTUBE_ID.matcher(url);
        if (m.find()) {
            return "https://www.youtube.com/embed/" + m.group(1);
        }
        return url;
    }

    private static String extractId(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        Matcher m = YOUTUBE_ID.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    /**
     * YouTube-hosted thumbnail for a video URL - no server storage needed
     * since it's served straight from YouTube's own CDN. Returns null for
     * non-YouTube URLs, since we have no generic way to derive a thumbnail.
     */
    public static String toThumbnailUrl(String url) {
        String id = extractId(url);
        return id != null ? "https://img.youtube.com/vi/" + id + "/hqdefault.jpg" : null;
    }
}
