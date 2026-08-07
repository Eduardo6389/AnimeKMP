package com.jetbrains.kmpapp.data.remote

import com.jetbrains.kmpapp.data.dto.GenreDto
import com.jetbrains.kmpapp.data.dto.MovieDetailDto
import com.jetbrains.kmpapp.data.dto.MovieDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Tests del mapper DTO → dominio: la "traducción" de datos crudos a datos limpios. */
class MovieMapperTest {

    @Test
    fun arma_url_de_poster_completa() {
        val dto = MovieDto(id = 1, title = "Dune", posterPath = "/dune.jpg", releaseDate = "2021-10-22")
        val movie = dto.toDomain()
        // La URL debe empezar por el host de imágenes de TMDB y terminar con el path del póster.
        assertTrue(movie.posterUrl!!.startsWith("https://image.tmdb.org/t/p/"), movie.posterUrl!!)
        assertTrue(movie.posterUrl!!.endsWith("/dune.jpg"), movie.posterUrl!!)
    }

    @Test
    fun poster_nulo_produce_url_nula() {
        val dto = MovieDto(id = 1, title = "Sin póster", posterPath = null)
        assertNull(dto.toDomain().posterUrl)
    }

    @Test
    fun extrae_el_anio_de_la_fecha() {
        val dto = MovieDto(id = 1, title = "Matrix", releaseDate = "1999-03-31")
        assertEquals("1999", dto.toDomain().anio)
    }

    @Test
    fun fecha_nula_produce_anio_vacio() {
        val dto = MovieDto(id = 1, title = "X", releaseDate = null)
        assertEquals("", dto.toDomain().anio)
    }

    @Test
    fun copia_campos_basicos() {
        val dto = MovieDto(id = 42, title = "Interstellar", overview = "espacio", voteAverage = 8.6)
        val movie = dto.toDomain(esFavorita = true)
        assertEquals(42, movie.id)
        assertEquals("Interstellar", movie.titulo)
        assertEquals("espacio", movie.overview)
        assertEquals(8.6, movie.rating)
        assertTrue(movie.esFavorita)
    }

    @Test
    fun detalle_mapea_generos_a_strings() {
        val dto = MovieDetailDto(
            id = 1,
            title = "Dune",
            runtime = 155,
            genres = listOf(GenreDto(878, "Ciencia ficción"), GenreDto(12, "Aventura")),
        )
        val detail = dto.toDomain()
        assertEquals(listOf("Ciencia ficción", "Aventura"), detail.generos)
        assertEquals(155, detail.duracionMin)
    }

    @Test
    fun detalle_tagline_en_blanco_se_vuelve_nulo() {
        val dto = MovieDetailDto(id = 1, title = "X", tagline = "   ")
        assertNull(dto.toDomain().tagline)
    }
}
