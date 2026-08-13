package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.remote.dto.AnimeDto
import com.jetbrains.kmpapp.data.remote.dto.AnimeGenreDto
import com.jetbrains.kmpapp.data.remote.dto.AnimeImagesDto
import com.jetbrains.kmpapp.data.remote.dto.AnimeJpgDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiMapperTest {
    @Test
    fun convierteIdATexto() {
        assertEquals("25", anime().copy(id = 25).toItem().id)
    }

    @Test
    fun convierteTitulo() {
        val item = anime().copy(titulo = "Cowboy Bebop").toItem()
        assertEquals("Cowboy Bebop", item.titulo)
    }

    @Test
    fun aceptaMetricaNula() {
        assertNull(anime().copy(puntuacion = null).toItem().metrica)
    }

    @Test
    fun aceptaImagenNula() {
        assertNull(anime().copy(imagenes = null).toItem().imagenUrl)
    }

    @Test
    fun limitaTagsACinco() {
        val generos = (1..7).map { AnimeGenreDto("Genero $it") }
        assertEquals(
            5,
            anime()
                .copy(generos = generos)
                .toItem()
                .tags.size,
        )
    }

    @Test
    fun convierteListaVaciaDeTags() {
        assertTrue(
            anime()
                .copy(generos = emptyList())
                .toItem()
                .tags
                .isEmpty(),
        )
    }

    @Test
    fun detalleUsaImagenGrande() {
        val jpg = AnimeJpgDto("pequena.jpg", "grande.jpg")
        val anime = anime().copy(imagenes = AnimeImagesDto(jpg))
        assertEquals("grande.jpg", anime.toDetalle().item.imagenUrl)
    }

    @Test
    fun detalleSiempreTieneCincoAtributos() {
        val detalle = anime().copy(tipo = null, episodios = null).toDetalle()
        assertEquals(5, detalle.atributos.size)
    }

    private fun anime() =
        AnimeDto(
            id = 1,
            titulo = "Anime",
            puntuacion = 8.0,
            tipo = "TV",
            episodios = 12,
        )
}
