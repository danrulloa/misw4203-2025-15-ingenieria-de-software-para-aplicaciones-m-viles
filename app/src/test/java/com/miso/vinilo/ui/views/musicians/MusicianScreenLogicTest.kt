package com.miso.vinilo.ui.views.musicians

import com.miso.vinilo.BuildConfig
import org.junit.Assert.*
import org.junit.Test

class MusicianScreenLogicTest {

    @Test
    fun `resolveImageUrl returns null for null or blank`() {
        assertNull(resolveImageUrl(null))
        assertNull(resolveImageUrl(""))
        assertNull(resolveImageUrl("   "))
    }

    @Test
    fun `resolveImageUrl keeps absolute http and https URLs`() {
        val http = "http://example.com/image.png"
        val https = "https://cdn.example.com/pic.jpg"
        assertEquals(http, resolveImageUrl(http))
        assertEquals(https, resolveImageUrl(https))
    }

    @Test
    fun `resolveImageUrl replaces localhost and 127 with emulator host`() {
        val urlLocalhost = "http://localhost:3000/assets/img.png"
        val url127 = "http://127.0.0.1:4000/other.png"
        assertEquals("http://10.0.2.2:3000/assets/img.png", resolveImageUrl(urlLocalhost))
        assertEquals("http://10.0.2.2:4000/other.png", resolveImageUrl(url127))
    }

    @Test
    fun `resolveImageUrl prepends base url for root and relative paths`() {
        // Use the BuildConfig.BASE_URL from the project; ensure it ends without slash in expectation
        val base = BuildConfig.BASE_URL.trimEnd('/')
        val rootPath = "/images/hero.png"
        val relPath = "avatars/1.png"
        assertEquals("$base$rootPath", resolveImageUrl(rootPath))
        assertEquals("$base/$relPath", resolveImageUrl(relPath))
    }

    @Test
    fun `formatBirthDate returns empty for null or blank`() {
        assertEquals("", formatBirthDate(null))
        assertEquals("", formatBirthDate(""))
        assertEquals("", formatBirthDate("   "))
    }

    @Test
    fun `formatBirthDate formats ISO offset datetime to medium date`() {
        val iso = "1988-05-05T00:00:00.000Z"
        val formatted = formatBirthDate(iso)
        // The exact format depends on the default locale, but should contain the year
        assertTrue(formatted.contains("1988"))
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun `formatBirthDate returns raw string on parse error`() {
        val bad = "not-a-date"
        assertEquals(bad, formatBirthDate(bad))
    }
}

