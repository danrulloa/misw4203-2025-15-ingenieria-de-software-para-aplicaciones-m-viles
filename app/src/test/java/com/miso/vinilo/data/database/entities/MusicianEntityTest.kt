package com.miso.vinilo.data.database.entities

import com.miso.vinilo.data.dto.MusicianDto
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicianEntityTest {

    @Test
    fun `toDto converts entity to dto correctly`() {
        val entity = MusicianEntity(
            id = 1,
            name = "Adele",
            image = "http://example.com/image.jpg",
            description = "Singer",
            birthDate = "1988-05-05T00:00:00.000Z",
            lastUpdated = 123456789L
        )

        val dto = entity.toDto()

        assertEquals(1, dto.id)
        assertEquals("Adele", dto.name)
        assertEquals("http://example.com/image.jpg", dto.image)
        assertEquals("Singer", dto.description)
        assertEquals("1988-05-05T00:00:00.000Z", dto.birthDate)
    }

    @Test
    fun `fromDto converts dto to entity correctly`() {
        val dto = MusicianDto(
            id = 2,
            name = "Metallica",
            image = null,
            description = "Band",
            birthDate = "1981-10-28T00:00:00.000Z"
        )

        val entity = MusicianEntity.fromDto(dto)

        assertEquals(2, entity.id)
        assertEquals("Metallica", entity.name)
        assertEquals(null, entity.image)
        assertEquals("Band", entity.description)
        assertEquals("1981-10-28T00:00:00.000Z", entity.birthDate)
    }

    @Test
    fun `fromDto sets lastUpdated timestamp`() {
        val dto = MusicianDto(1, "Test", null, null, null)
        val beforeTime = System.currentTimeMillis()

        val entity = MusicianEntity.fromDto(dto)

        val afterTime = System.currentTimeMillis()

        // lastUpdated should be set to current time
        assert(entity.lastUpdated >= beforeTime)
        assert(entity.lastUpdated <= afterTime)
    }

    @Test
    fun `toDto and fromDto are symmetric`() {
        val originalDto = MusicianDto(
            id = 3,
            name = "Queen",
            image = "http://example.com/queen.jpg",
            description = "Rock Band",
            birthDate = "1970-01-01T00:00:00.000Z"
        )

        val entity = MusicianEntity.fromDto(originalDto)
        val resultDto = entity.toDto()

        assertEquals(originalDto.id, resultDto.id)
        assertEquals(originalDto.name, resultDto.name)
        assertEquals(originalDto.image, resultDto.image)
        assertEquals(originalDto.description, resultDto.description)
        assertEquals(originalDto.birthDate, resultDto.birthDate)
    }
}

