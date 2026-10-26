package mx.vendaval.core.data.seed

import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.GeoPoint
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.Municipality.AGUASCALIENTES
import mx.vendaval.core.domain.model.Municipality.CALVILLO
import mx.vendaval.core.domain.model.Municipality.JESUS_MARIA
import mx.vendaval.core.domain.model.Municipality.PABELLON_DE_ARTEAGA
import mx.vendaval.core.domain.model.Municipality.RINCON_DE_ROMOS
import mx.vendaval.core.domain.model.Municipality.SAN_FRANCISCO_DE_LOS_ROMO
import mx.vendaval.core.domain.model.Municipality.SAN_JOSE_DE_GRACIA
import mx.vendaval.core.domain.model.PhoneNumber
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.model.VenueType.CASA_DE_CULTURA
import mx.vendaval.core.domain.model.VenueType.CENTRO_COMUNITARIO
import mx.vendaval.core.domain.model.VenueType.CENTRO_CULTURAL
import mx.vendaval.core.domain.model.VenueType.GALERIA_DE_ARTE
import mx.vendaval.core.domain.model.VenueType.TEATRO

/**
 * The catalogue compiled into the binary.
 *
 * Ported verbatim from the `spacesData` array in the original `app.js`, ids
 * included, so a record created on the web portal and the same record here share
 * an identity. This is what makes the app useful on first launch with no network
 * and what it falls back to if the backend is ever unreachable or emptied.
 *
 * Ids 1-27 are reserved for these entries; the remote sequence continues above
 * them and locally created drafts use negative ids.
 */
object SeedCatalogue {

    val spaces: List<CulturalSpace> = listOf(
        seed(
            id = 1,
            name = "Instituto Cultural de Aguascalientes",
            address = "Venustiano Carranza 101, Centro, 20000 Aguascalientes, Ags.",
            phone = "449 910 2010",
            type = CASA_DE_CULTURA,
            municipality = AGUASCALIENTES,
            lat = 21.8804,
            lng = -102.2965,
            mapQuery = "Instituto+Cultural+de+Aguascalientes",
        ),
        seed(
            id = 2,
            name = "Centro Cultural Los Arquitos",
            address = "Alameda S/N, Barrio de la Purísima, 20000 Aguascalientes, Ags.",
            phone = "449 916 9201",
            type = CASA_DE_CULTURA,
            municipality = AGUASCALIENTES,
            lat = 21.8845,
            lng = -102.2818,
            mapQuery = "Centro+Cultural+Los+Arquitos+Aguascalientes",
        ),
        seed(
            id = 3,
            name = "Casa Terán",
            address = "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 915 1741",
            type = CASA_DE_CULTURA,
            municipality = AGUASCALIENTES,
            lat = 21.8827,
            lng = -102.2974,
            mapQuery = "Casa+Teran+Aguascalientes",
        ),
        seed(
            id = 4,
            name = "Centro Cultural Ángel",
            address = "Rivero y Gutiérrez 110, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 915 1741",
            type = CASA_DE_CULTURA,
            municipality = AGUASCALIENTES,
            lat = 21.8828,
            lng = -102.2975,
            mapQuery = "Centro+Cultural+Angel+Aguascalientes",
        ),
        seed(
            id = 5,
            name = "Centro de Artes Visuales",
            address = "Venustiano Carranza 111, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 916 1456",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8805,
            lng = -102.2970,
            mapQuery = "Centro+de+Artes+Visuales+Aguascalientes",
        ),
        seed(
            id = 6,
            name = "Artefacto / Espacio Experimental",
            address = "Av. Francisco I. Madero 457, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 125 6259",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8831,
            lng = -102.2928,
            mapQuery = "Artefacto+Espacio+Experimental+Aguascalientes",
        ),
        seed(
            id = 7,
            name = "Casa Refugio Reyes",
            address = "C. Juan de Montoro 423, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 929 4201",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8833,
            lng = -102.2920,
            mapQuery = "Casa+Refugio+Reyes+Aguascalientes",
        ),
        seed(
            id = 8,
            name = "Galería de Arte \"El Obraje\"",
            address = "C. Juan de Montoro 222, Centro, 20000 Aguascalientes, Ags.",
            phone = "449 994 0074",
            type = GALERIA_DE_ARTE,
            municipality = AGUASCALIENTES,
            lat = 21.8833,
            lng = -102.2950,
            mapQuery = "Galeria+de+Arte+El+Obraje+Aguascalientes",
        ),
        seed(
            id = 9,
            name = "Teatro Morelos",
            address = "Nieto 113, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 915 1941",
            type = TEATRO,
            municipality = AGUASCALIENTES,
            lat = 21.8806,
            lng = -102.2961,
            mapQuery = "Teatro+Morelos+Aguascalientes",
        ),
        seed(
            id = 10,
            name = "Teatro Antonio Leal y Romero",
            address = "Venustiano Carranza, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 910 2010",
            type = TEATRO,
            municipality = AGUASCALIENTES,
            lat = 21.8804,
            lng = -102.2964,
            mapQuery = "Teatro+Antonio+Leal+y+Romero+Aguascalientes",
        ),
        seed(
            id = 11,
            name = "Galería Benjamín Manzo",
            address = "Venustiano Carranza 101, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 915 1691",
            type = GALERIA_DE_ARTE,
            municipality = AGUASCALIENTES,
            lat = 21.8804,
            lng = -102.2965,
            mapQuery = "Galeria+Benjamin+Manzo+Aguascalientes",
        ),
        seed(
            id = 12,
            name = "Teatro Aguascalientes",
            address = "Av. José María Chávez S/N, Jardines de la Asunción, 20270 Aguascalientes, Ags.",
            phone = "449 978 5414",
            type = TEATRO,
            municipality = AGUASCALIENTES,
            lat = 21.8601,
            lng = -102.2905,
            mapQuery = "Teatro+Aguascalientes",
        ),
        seed(
            id = 13,
            name = "Centro CRECER Obraje",
            address = "Prof. Enrique Olivares Santana 119, Obraje, 20230 Aguascalientes, Ags.",
            phone = "449 910 2121 ext. 4255",
            type = CENTRO_COMUNITARIO,
            municipality = AGUASCALIENTES,
            lat = 21.8745,
            lng = -102.2910,
            mapQuery = "Centro+CRECER+Obraje+Aguascalientes",
        ),
        seed(
            id = 14,
            name = "Casa de la Cultura de San José de Gracia (ICA)",
            address = "Juan Domínguez 214, Col. Las Cañadas, 20500 San José de Gracia, Ags.",
            phone = "465 967 3436",
            type = CASA_DE_CULTURA,
            municipality = SAN_JOSE_DE_GRACIA,
            lat = 22.1506,
            lng = -102.4158,
            mapQuery = "Casa+de+la+Cultura+San+Jose+de+Gracia+Aguascalientes",
        ),
        seed(
            id = 15,
            name = "Casa del Peregrino",
            address = "San José de Gracia, Ags.",
            phone = null,
            type = CASA_DE_CULTURA,
            municipality = SAN_JOSE_DE_GRACIA,
            lat = 22.1520,
            lng = -102.4160,
            mapQuery = "Casa+del+Peregrino+San+Jose+de+Gracia+Aguascalientes",
        ),
        seed(
            id = 16,
            name = "Casa de Cultura de San Francisco de los Romo",
            address = "Juárez 410, Centro, 20300 San Francisco de los Romo, Ags.",
            phone = "465 967 0427",
            type = CASA_DE_CULTURA,
            municipality = SAN_FRANCISCO_DE_LOS_ROMO,
            lat = 22.0738,
            lng = -102.2710,
            mapQuery = "Casa+de+Cultura+San+Francisco+de+los+Romo",
        ),
        seed(
            id = 17,
            name = "Casa de la Cultura de Rincón de Romos",
            address = "Dolores Hidalgo 309, Centro Histórico, 20400 Rincón de Romos, Ags.",
            phone = "465 951 1482",
            type = CASA_DE_CULTURA,
            municipality = RINCON_DE_ROMOS,
            lat = 22.2289,
            lng = -102.3241,
            mapQuery = "Casa+de+la+Cultura+Rincon+de+Romos",
        ),
        seed(
            id = 18,
            name = "Casa de la Cultura Jesús María",
            address = "Ignacio Allende 707, Ayuntamiento, 20920 Jesús María, Ags.",
            phone = "449 965 0017",
            type = CASA_DE_CULTURA,
            municipality = JESUS_MARIA,
            lat = 21.9612,
            lng = -102.3435,
            mapQuery = "Casa+de+la+Cultura+Jesus+Maria+Aguascalientes",
        ),
        seed(
            id = 19,
            name = "Casa de Cultura de Calvillo",
            address = "C. Zaragoza, Zona Centro, 20800 Calvillo, Ags.",
            phone = "495 956 0807",
            type = CASA_DE_CULTURA,
            municipality = CALVILLO,
            lat = 21.8465,
            lng = -102.7188,
            mapQuery = "Casa+de+Cultura+Calvillo+Aguascalientes",
        ),
        seed(
            id = 20,
            name = "Casa de la Cultura de Pabellón de Arteaga",
            address = "Centro, Pabellón de Arteaga, Ags.",
            phone = null,
            type = CASA_DE_CULTURA,
            municipality = PABELLON_DE_ARTEAGA,
            lat = 22.1415,
            lng = -102.2762,
            mapQuery = "Casa+de+la+Cultura+Pabellon+de+Arteaga+Aguascalientes",
        ),
        seed(
            id = 21,
            name = "Biblioteca Pública Enrique Fernández Ledesma",
            address = "Calle Galeana Norte 202, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 910 2010",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8807271,
            lng = -102.2977607,
            mapQuery = "Biblioteca+Enrique+Fernandez+Ledesma+Aguascalientes",
        ),
        seed(
            id = 22,
            name = "Biblioteca Jaime Torres Bodet",
            address = "Andador Benito Juárez No. 122, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 910 2010",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8823929,
            lng = -102.2960414,
            mapQuery = "Biblioteca+Jaime+Torres+Bodet+Aguascalientes",
        ),
        seed(
            id = 23,
            name = "Biblioteca Pública Central Centenario-Bicentenario",
            address = "Av. Manuel Gómez Morín S/N, Col. Ferronales, 20180 Aguascalientes, Ags.",
            phone = "449 910 2645",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.888821,
            lng = -102.279617,
            mapQuery = "Biblioteca+Centenario+Bicentenario+Aguascalientes",
        ),
        seed(
            id = 24,
            name = "Sala Alternativa",
            address = "Edén 205, Aguascalientes, Ags.",
            phone = "449 960 2333",
            type = TEATRO,
            municipality = AGUASCALIENTES,
            lat = 21.8983049,
            lng = -102.3178328,
            mapQuery = "Sala+Alternativa+Cine+Aguascalientes",
        ),
        seed(
            id = 25,
            name = "Aquelarre, espacio cultural",
            address = "Calle Francisco G. Hornedo No. 336, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = "449 190 0616",
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8800881,
            lng = -102.2924548,
            mapQuery = "Aquelarre+espacio+cultural+Aguascalientes",
        ),
        seed(
            id = 26,
            name = "Cafecheetoh",
            address = "Talamantes 213, Barrio de San Marcos, 20070 Aguascalientes, Ags.",
            phone = null,
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8804204,
            lng = -102.305894,
            mapQuery = "Cafecheetoh+Café+Cultural+Aguascalientes",
        ),
        seed(
            id = 27,
            name = "Hogar Espejo",
            address = "Gral. Francisco Villa 245, Zona Centro, 20000 Aguascalientes, Ags.",
            phone = null,
            type = CENTRO_CULTURAL,
            municipality = AGUASCALIENTES,
            lat = 21.8898675,
            lng = -102.2867643,
            mapQuery = "Hogar+Espejo+Centro+Cultural+Aguascalientes",
        ),
    )

    /** Highest seeded id, so local drafts and remote rows never collide with it. */
    val highestSeededId: Long = spaces.maxOf { it.id.value }
}

private fun seed(
    id: Long,
    name: String,
    address: String,
    phone: String?,
    type: VenueType,
    municipality: Municipality,
    lat: Double,
    lng: Double,
    mapQuery: String,
): CulturalSpace = CulturalSpace(
    id = SpaceId(id),
    name = name,
    address = address,
    phone = PhoneNumber.orNull(phone),
    type = type,
    municipality = municipality,
    location = GeoPoint(lat, lng),
    mapQuery = mapQuery,
    origin = SpaceOrigin.SEED,
)
