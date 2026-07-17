package com.construction.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VideoUtilTest {

    @Test
    void toEmbedUrl_convertsStandardWatchUrl() {
        assertEquals(
                "https://www.youtube.com/embed/dQw4w9WgXcQ",
                VideoUtil.toEmbedUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        );
    }

    @Test
    void toEmbedUrl_convertsShortUrl() {
        assertEquals(
                "https://www.youtube.com/embed/dQw4w9WgXcQ",
                VideoUtil.toEmbedUrl("https://youtu.be/dQw4w9WgXcQ")
        );
    }

    @Test
    void toEmbedUrl_convertsUrlWithExtraQueryParams() {
        // e.g. a link copied from a playlist ("&list=...&start_radio=1")
        assertEquals(
                "https://www.youtube.com/embed/RVMnT4nq9NU",
                VideoUtil.toEmbedUrl("https://www.youtube.com/watch?v=RVMnT4nq9NU&list=RDRVMnT4nq9NU&start_radio=1")
        );
    }

    @Test
    void toEmbedUrl_returnsNullForBlankInput() {
        assertNull(VideoUtil.toEmbedUrl(null));
        assertNull(VideoUtil.toEmbedUrl(""));
        assertNull(VideoUtil.toEmbedUrl("   "));
    }

    @Test
    void toEmbedUrl_returnsUnrecognizedUrlUnchanged() {
        assertEquals("https://example.com/video.mp4", VideoUtil.toEmbedUrl("https://example.com/video.mp4"));
    }

    @Test
    void toThumbnailUrl_buildsYoutubeCdnUrl() {
        assertEquals(
                "https://img.youtube.com/vi/dQw4w9WgXcQ/hqdefault.jpg",
                VideoUtil.toThumbnailUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        );
    }

    @Test
    void toThumbnailUrl_returnsNullForNonYoutubeUrl() {
        assertNull(VideoUtil.toThumbnailUrl("https://example.com/video.mp4"));
    }
}
