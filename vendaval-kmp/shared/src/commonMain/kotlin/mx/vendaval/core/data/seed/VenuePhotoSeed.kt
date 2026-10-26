package mx.vendaval.core.data.seed

import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.VenuePhoto

/**
 * GENERATED FILE — do not edit by hand.
 *
 * Regenerate with `python3 tools/ingest/photos.py`. Last run resolved
 * 13 of 27 spaces (48%); the rest have no freely-licensed
 * photograph and keep their generated letter cover.
 *
 * Every entry cleared the relevance guard in the ingestor: the file title must
 * name the kind of venue, carry all of the venue's distinguishing words, and not
 * describe an event held there. That guard exists because the naive first result
 * for real venues in this catalogue included a civil servant's portrait, a poster
 * for an unrelated event, and a page from an 1846 book — each of which would have
 * been published as 'this is what that building looks like'.
 *
 * The images themselves live in `composeResources/files/venues/`, downscaled at
 * ingest time to 720 px. They ship in the binary rather than being
 * fetched at display time: the app is offline-first, and Wikimedia rate-limits a
 * burst of requests with a 429 whose HTML body decodes to null — indistinguishable
 * from a venue that genuinely has no photograph.
 *
 * Licences are CC BY-SA only. The artist and licence must be shown wherever the
 * image is, which is why they travel inside VenuePhoto.
 */
object VenuePhotoSeed {

    val photos: Map<SpaceId, VenuePhoto> = listOf(
        VenuePhoto(
            spaceId = SpaceId(2L),
            assetPath = "files/venues/2.jpg",
            width = 540,
            height = 720,
            licence = "CC BY-SA 4.0",
            artist = "Nadia Cisneros",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Centro_Cultural_Los_Arquitos,_Aguascalientes.jpg",
        ), // Centro Cultural Los Arquitos
        VenuePhoto(
            spaceId = SpaceId(3L),
            assetPath = "files/venues/3.jpg",
            width = 720,
            height = 404,
            licence = "CC BY-SA 4.0",
            artist = "WCHZ",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Casa_Teran.jpg",
        ), // Casa Terán
        VenuePhoto(
            spaceId = SpaceId(5L),
            assetPath = "files/venues/5.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 4.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Centro_de_Artes_Visuales_del_ICA.jpg",
        ), // Centro de Artes Visuales
        VenuePhoto(
            spaceId = SpaceId(7L),
            assetPath = "files/venues/7.jpg",
            width = 540,
            height = 720,
            licence = "CC BY-SA 3.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Casa_de_Refugio_Reyes_Rivas,_en_la_Calle_Montoro,_Aguascalientes,_Ags..jpg",
        ), // Casa Refugio Reyes
        VenuePhoto(
            spaceId = SpaceId(8L),
            assetPath = "files/venues/8.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 4.0",
            artist = "Luis Alvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Fachada_de_la_Galer%C3%ADa_El_Obraje_02.jpg",
        ), // Galería de Arte \"El Obraje\"
        VenuePhoto(
            spaceId = SpaceId(9L),
            assetPath = "files/venues/9.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 4.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Teatro_Morelos_de_Aguascalientes_06.JPG",
        ), // Teatro Morelos
        VenuePhoto(
            spaceId = SpaceId(11L),
            assetPath = "files/venues/11.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 4.0",
            artist = "Luis Alvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Galer%C3%ADa_Benjam%C3%ADn_Manzo_durante_el_XLII_ENAJ.jpg",
        ), // Galería Benjamín Manzo
        VenuePhoto(
            spaceId = SpaceId(12L),
            assetPath = "files/venues/12.jpg",
            width = 720,
            height = 405,
            licence = "CC BY-SA 3.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Fachada_del_Teatro_Aguascalientes.jpg",
        ), // Teatro Aguascalientes
        VenuePhoto(
            spaceId = SpaceId(14L),
            assetPath = "files/venues/14.jpg",
            width = 720,
            height = 213,
            licence = "CC BY-SA 4.0",
            artist = "Carlos.guemer.ags",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Casa_de_la_Cultura_de_San_Jos%C3%A9_de_Gracia.jpg",
        ), // Casa de la Cultura de San José de Gracia (ICA)
        VenuePhoto(
            spaceId = SpaceId(17L),
            assetPath = "files/venues/17.jpg",
            width = 540,
            height = 720,
            licence = "CC BY-SA 4.0",
            artist = "Luis Alvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Interior_de_la_Casa_de_la_Cultura_de_Rinc%C3%B3n_de_Romos,_Ags._03.jpg",
        ), // Casa de la Cultura de Rincón de Romos
        VenuePhoto(
            spaceId = SpaceId(19L),
            assetPath = "files/venues/19.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 4.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Casa_de_cultura_de_Calvillo,_Aguascalientes_01.jpg",
        ), // Casa de Cultura de Calvillo
        VenuePhoto(
            spaceId = SpaceId(22L),
            assetPath = "files/venues/22.jpg",
            width = 540,
            height = 720,
            licence = "CC BY-SA 3.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:La_Casa_de_la_Gardenia_(1916),_actualmente_Biblioteca_Jaime_Torres_Bodet,_de_Refugio_Reyes_Rivas_2.jpg",
        ), // Biblioteca Jaime Torres Bodet
        VenuePhoto(
            spaceId = SpaceId(23L),
            assetPath = "files/venues/23.jpg",
            width = 720,
            height = 540,
            licence = "CC BY-SA 3.0",
            artist = "Luisalvaz",
            sourceUrl = "https://commons.wikimedia.org/wiki/File:Biblioteca_Publica_Central_Centenario_Bicentenario_de_Aguascalientes.JPG",
        ), // Biblioteca Pública Central Centenario-Bicentenario
    ).associateBy { it.spaceId }

    fun forSpace(id: SpaceId): VenuePhoto? = photos[id]
}
