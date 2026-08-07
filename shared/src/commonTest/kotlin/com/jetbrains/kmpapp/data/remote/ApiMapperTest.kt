package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimeDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiMapperTest {

    @Test
    fun convierteAnimeAItem() {
        val anime = AnimeDto(
            id = 1,
            title = "Cowboy Bebop",
            score = 8.75,
            year = 1998,
        )

        val item = anime.toItem()

        assertEquals("1", item.id)
        assertEquals("Cowboy Bebop", item.titulo)
        assertEquals(8.75, item.metrica)
    }

    @Test
    fun aceptaCamposNulos() {
        val anime = AnimeDto(
            id = 2,
            title = "Anime",
        )

        val item = anime.toItem()

        assertNull(item.imagenUrl)
        assertNull(item.metrica)
    }
}