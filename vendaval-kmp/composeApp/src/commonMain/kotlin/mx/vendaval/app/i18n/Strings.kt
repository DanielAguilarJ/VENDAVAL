package mx.vendaval.app.i18n

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import mx.vendaval.core.data.repository.AppLanguage

/**
 * Every user-visible string, as a plain immutable value.
 *
 * Chosen over Compose resource bundles deliberately: the set is exhaustive at
 * compile time (a missing translation is a compile error, not a runtime
 * placeholder), it needs no codegen step, and switching language is a state
 * change rather than a process-level locale change — which is what lets the
 * toggle in settings take effect instantly on all three platforms.
 */
@Immutable
data class Strings(
    val appName: String,
    val appTagline: String,

    // Navigation
    val navDirectory: String,
    val navMap: String,
    val navFavourites: String,
    val navCoverage: String,
    val navSettings: String,

    // Directory
    val searchPlaceholder: String,
    val searchClear: String,
    val filters: String,
    val filterVenueType: String,
    val filterMunicipality: String,
    val filterAll: String,
    val filterClear: String,
    val sortBy: String,
    val resultCount: (Int) -> String,
    val noResultsTitle: String,
    val noResultsBody: String,
    val addSpace: String,

    // Detail
    val detailAddress: String,
    val detailPhone: String,
    val detailNoPhone: String,
    val detailCall: String,
    val detailOpenMap: String,
    val detailShare: String,
    val detailShowOnMap: String,
    /** Photo credit under the detail hero, e.g. `Foto: Luis Alvaz · CC BY-SA 4.0`. */
    /** Inline invitation in the directory to sort by proximity. */
    val nearbyInvite: String,
    val nearbyInviteAction: String,
    val nearbySortAction: String,
    val nearbyLocating: String,
    /** Names the point distances are measured from, e.g. `Desde tu ubicación`. */
    /** Used when the resolved location has no place name to show. */
    val nearbyYourLocation: String,
    val nearbyReference: (String) -> String,
    val photoCredit: (String) -> String,
    /** Accessibility label for the credit row, which opens the source page. */
    val photoCreditOpen: String,
    val detailExtension: (String) -> String,
    val detailFavouriteAdd: String,
    val detailFavouriteRemove: String,
    val detailPendingUpload: String,
    val detailLocalRecord: String,
    val detailDelete: String,

    // Favourites
    val favouritesEmptyTitle: String,
    val favouritesEmptyBody: String,

    // Map
    val mapNearMe: String,
    val mapRecentre: String,
    val mapZoomIn: String,
    val mapZoomOut: String,
    val mapAttribution: String,
    val mapLocationDenied: String,
    val mapLocationUnavailable: String,
    val mapLocationUnsupported: String,
    val mapLegend: String,
    /** Verb on the map sheet's single committed action. */
    val mapOpenDetail: String,
    /** Dismiss label, shared by the map sheet and any overlay. */
    val actionClose: String,

    // Add space
    val formTitle: String,
    val formSubtitle: String,
    val formName: String,
    val formAddress: String,
    val formPhone: String,
    val formPhoneOptional: String,
    val formType: String,
    val formMunicipality: String,
    val formLatitude: String,
    val formLongitude: String,
    val formMapQuery: String,
    val formUseMyLocation: String,
    val formUseMunicipalityCentre: String,
    val formSubmit: String,
    val formCancel: String,
    val formSavedOffline: String,
    val formSavedPublished: String,
    // Multi-step wizard
    val formStepOf: (Int, Int) -> String,
    val formNext: String,
    val formBack: String,
    val formStepIdentityTitle: String,
    val formStepLocationTitle: String,
    val formStepDetailsTitle: String,
    val formStepPhotosTitle: String,
    val formStepReviewTitle: String,
    val formDescription: String,
    val formHours: String,
    val formWebsite: String,
    val formGroupAbout: String,
    val formGroupSocial: String,
    val formPhotosHint: String,
    val formReviewHint: String,
    val formNotProvided: String,
    val formSavedTitle: String,
    val formAddAnother: String,

    // Coverage
    val coverageTitle: String,
    /** Label for the sync-status row. Distinct from the section heading above it. */
    /**
     * Short verbs for the detail screen's quick-action row.
     *
     * Separate from [detailFavouriteAdd] and [detailOpenMap], which are written for
     * a screen reader and run too long to fit a quarter-width stack — "Agregar a
     * favoritos" clipped to "Agregar a favor…". The long forms stay as the
     * accessibility descriptions; these are what the eye reads.
     */
    /**
     * Group headings for the submission form.
     *
     * These name the *question*, not the first field inside it. Passing field names
     * here produced "Nombre del espacio" stacked directly on "Nombre del espacio"
     * for every group in the form.
     */
    val formGroupIdentity: String,
    val formGroupLocation: String,
    val formGroupContact: String,
    /** Placeholder for the municipality picker. Not the directory's "Todos". */
    val formMunicipalityPlaceholder: String,
    val detailActionMap: String,
    val detailActionSave: String,
    val detailActionSaved: String,
    val settingsSyncStatus: String,
    val coverageSpaces: String,
    val coverageMunicipalities: String,
    val coverageTypes: String,
    val coverageWithPhone: String,
    val coverageByType: String,
    val coverageByMunicipality: String,
    val coverageContributions: String,
    val coverageNoData: String,

    // Sync
    val syncIdle: String,
    val syncSyncing: String,
    val syncUpToDate: (Int) -> String,
    val syncOffline: String,
    val syncRejected: String,
    val syncServer: String,
    val syncMalformed: String,
    val syncUnknown: String,
    val syncRetry: String,
    val syncNewSpace: (String) -> String,

    // Settings
    val settingsAppearance: String,
    val settingsTheme: String,
    val settingsThemeSystem: String,
    val settingsThemeDark: String,
    val settingsThemeLight: String,
    val settingsLanguage: String,
    val settingsData: String,
    val settingsRefresh: String,
    val settingsAbout: String,
    val settingsAboutBody: String,
    val settingsPlatform: String,
    val settingsSourceCatalogue: (Int) -> String,
)

private val Spanish = Strings(
    appName = "Vendaval",
    appTagline = "Cartografía cultural de Aguascalientes",

    navDirectory = "Directorio",
    navMap = "Mapa",
    navFavourites = "Favoritos",
    navCoverage = "Cobertura",
    navSettings = "Ajustes",

    searchPlaceholder = "Busca por nombre, calle o municipio",
    searchClear = "Limpiar búsqueda",
    filters = "Filtros",
    filterVenueType = "Recinto",
    filterMunicipality = "Municipio",
    filterAll = "Todos",
    filterClear = "Limpiar filtros",
    sortBy = "Ordenar por",
    resultCount = { count ->
        when (count) {
            0 -> "Sin resultados"
            1 -> "1 espacio"
            else -> "$count espacios"
        }
    },
    noResultsTitle = "No encontramos espacios",
    noResultsBody = "Prueba con menos filtros o revisa la ortografía del nombre.",
    addSpace = "Agregar espacio",

    detailAddress = "Dirección",
    detailPhone = "Teléfono",
    detailNoPhone = "Sin teléfono registrado",
    detailCall = "Llamar",
    detailOpenMap = "Abrir en mapas",
    detailShare = "Compartir",
    detailShowOnMap = "Ver en el mapa",
    nearbyInvite = "Espacios cerca de ti",
    nearbyInviteAction = "Usar mi ubicación",
    nearbySortAction = "Ordenar por cercanía",
    nearbyLocating = "Buscando tu ubicación…",
    nearbyYourLocation = "tu ubicación",
    nearbyReference = { place -> "Distancias desde $place" },
    photoCredit = { credit -> "Foto: $credit" },
    photoCreditOpen = "Abrir la página de la foto",
    detailExtension = { ext -> "Extensión $ext" },
    detailFavouriteAdd = "Agregar a favoritos",
    detailFavouriteRemove = "Quitar de favoritos",
    detailPendingUpload = "Pendiente de publicar",
    detailLocalRecord = "Registro creado en este dispositivo",
    detailDelete = "Eliminar",

    favouritesEmptyTitle = "Aún no tienes favoritos",
    favouritesEmptyBody = "Toca la estrella en cualquier espacio para guardarlo aquí. Funciona sin conexión.",

    mapNearMe = "Cerca de mí",
    mapRecentre = "Centrar el mapa",
    mapZoomIn = "Acercar",
    mapZoomOut = "Alejar",
    mapAttribution = "© OpenStreetMap",
    mapLocationDenied = "Necesitamos permiso de ubicación para ordenar por cercanía.",
    mapLocationUnavailable = "No pudimos obtener tu ubicación. Intenta de nuevo al aire libre.",
    mapLocationUnsupported = "Esta plataforma no tiene servicios de ubicación. Elige un municipio como referencia.",
    mapLegend = "Leyenda",
    mapOpenDetail = "Ver el espacio",
    actionClose = "Cerrar",

    formTitle = "Agregar un espacio",
    formSubtitle = "Tu aportación se guarda en este dispositivo y se publica cuando haya conexión.",
    formName = "Nombre del espacio",
    formAddress = "Dirección",
    formPhone = "Teléfono",
    formPhoneOptional = "Opcional",
    formType = "Tipo de recinto",
    formMunicipality = "Municipio",
    formLatitude = "Latitud",
    formLongitude = "Longitud",
    formMapQuery = "Búsqueda en mapas",
    formUseMyLocation = "Usar mi ubicación",
    formUseMunicipalityCentre = "Usar centro del municipio",
    formSubmit = "Guardar espacio",
    formCancel = "Cancelar",
    formSavedOffline = "Guardado en este dispositivo. Se publicará al reconectar.",
    formSavedPublished = "Espacio publicado en el directorio.",
    formStepOf = { current, total -> "Paso $current de $total" },
    formNext = "Siguiente",
    formBack = "Atrás",
    formStepIdentityTitle = "¿Qué espacio es?",
    formStepLocationTitle = "¿Dónde está?",
    formStepDetailsTitle = "Detalles",
    formStepPhotosTitle = "Fotos",
    formStepReviewTitle = "Revisa y publica",
    formDescription = "Descripción",
    formHours = "Horarios",
    formWebsite = "Sitio web",
    formGroupAbout = "Sobre el espacio",
    formGroupSocial = "Redes sociales",
    formPhotosHint = "Las fotos son opcionales. Pronto podrás agregarlas desde tu galería.",
    formReviewHint = "Revisa la información antes de publicar. Se guarda en este dispositivo y aparece de inmediato en el directorio y el mapa.",
    formNotProvided = "Sin especificar",
    formSavedTitle = "¡Listo!",
    formAddAnother = "Agregar otro",

    coverageTitle = "Cobertura del directorio",
    formGroupIdentity = "Identidad",
    formGroupLocation = "Ubicación",
    formGroupContact = "Contacto",
    formMunicipalityPlaceholder = "Selecciona un municipio",
    detailActionMap = "Mapas",
    detailActionSave = "Guardar",
    detailActionSaved = "Guardado",
    settingsSyncStatus = "Estado",
    coverageSpaces = "Espacios",
    coverageMunicipalities = "Municipios",
    coverageTypes = "Tipos de recinto",
    coverageWithPhone = "Con teléfono",
    coverageByType = "Por tipo de recinto",
    coverageByMunicipality = "Por municipio",
    coverageContributions = "Aportaciones locales",
    coverageNoData = "Sin datos para mostrar",

    syncIdle = "Sin sincronizar",
    syncSyncing = "Sincronizando…",
    syncUpToDate = { count -> "Actualizado · $count en el servidor" },
    syncOffline = "Sin conexión · mostrando datos guardados",
    syncRejected = "El servidor rechazó la petición",
    syncServer = "El servidor no responde",
    syncMalformed = "Respuesta inesperada del servidor",
    syncUnknown = "No se pudo sincronizar",
    syncRetry = "Reintentar",
    syncNewSpace = { name -> "Nuevo espacio: $name" },

    settingsAppearance = "Apariencia",
    settingsTheme = "Tema",
    settingsThemeSystem = "Del sistema",
    settingsThemeDark = "Oscuro",
    settingsThemeLight = "Claro",
    settingsLanguage = "Idioma",
    settingsData = "Datos",
    settingsRefresh = "Sincronizar ahora",
    settingsAbout = "Acerca de",
    settingsAboutBody = "Directorio de espacios públicos culturales del estado de Aguascalientes. " +
        "Proyecto del Colectivo Vendaval en colaboración con el IAJU.",
    settingsPlatform = "Plataforma",
    settingsSourceCatalogue = { count -> "$count espacios en el catálogo base" },
)

private val English = Strings(
    appName = "Vendaval",
    appTagline = "Cultural mapping of Aguascalientes",

    navDirectory = "Directory",
    navMap = "Map",
    navFavourites = "Favourites",
    navCoverage = "Coverage",
    navSettings = "Settings",

    searchPlaceholder = "Search by name, street or municipality",
    searchClear = "Clear search",
    filters = "Filters",
    filterVenueType = "Venue",
    filterMunicipality = "Municipality",
    filterAll = "All",
    filterClear = "Clear filters",
    sortBy = "Sort by",
    resultCount = { count ->
        when (count) {
            0 -> "No results"
            1 -> "1 venue"
            else -> "$count venues"
        }
    },
    noResultsTitle = "No venues found",
    noResultsBody = "Try fewer filters, or check the spelling of the name.",
    addSpace = "Add a venue",

    detailAddress = "Address",
    detailPhone = "Phone",
    detailNoPhone = "No phone on record",
    detailCall = "Call",
    detailOpenMap = "Open in maps",
    detailShare = "Share",
    detailShowOnMap = "Show on map",
    nearbyInvite = "Spaces near you",
    nearbyInviteAction = "Use my location",
    nearbySortAction = "Sort by distance",
    nearbyLocating = "Finding your location…",
    nearbyYourLocation = "your location",
    nearbyReference = { place -> "Distances from $place" },
    photoCredit = { credit -> "Photo: $credit" },
    photoCreditOpen = "Open the photo's source page",
    detailExtension = { ext -> "Extension $ext" },
    detailFavouriteAdd = "Add to favourites",
    detailFavouriteRemove = "Remove from favourites",
    detailPendingUpload = "Pending upload",
    detailLocalRecord = "Created on this device",
    detailDelete = "Delete",

    favouritesEmptyTitle = "No favourites yet",
    favouritesEmptyBody = "Tap the star on any venue to keep it here. Works offline.",

    mapNearMe = "Near me",
    mapRecentre = "Recentre map",
    mapZoomIn = "Zoom in",
    mapZoomOut = "Zoom out",
    mapAttribution = "© OpenStreetMap",
    mapLocationDenied = "Location permission is needed to sort by proximity.",
    mapLocationUnavailable = "Could not get your location. Try again outdoors.",
    mapLocationUnsupported = "This platform has no location services. Pick a municipality as reference.",
    mapLegend = "Legend",
    mapOpenDetail = "View venue",
    actionClose = "Close",

    formTitle = "Add a venue",
    formSubtitle = "Your contribution is stored on this device and published once online.",
    formName = "Venue name",
    formAddress = "Address",
    formPhone = "Phone",
    formPhoneOptional = "Optional",
    formType = "Venue type",
    formMunicipality = "Municipality",
    formLatitude = "Latitude",
    formLongitude = "Longitude",
    formMapQuery = "Map search terms",
    formUseMyLocation = "Use my location",
    formUseMunicipalityCentre = "Use municipality centre",
    formSubmit = "Save venue",
    formCancel = "Cancel",
    formSavedOffline = "Saved on this device. It will publish when back online.",
    formSavedPublished = "Venue published to the directory.",
    formStepOf = { current, total -> "Step $current of $total" },
    formNext = "Next",
    formBack = "Back",
    formStepIdentityTitle = "What is the venue?",
    formStepLocationTitle = "Where is it?",
    formStepDetailsTitle = "Details",
    formStepPhotosTitle = "Photos",
    formStepReviewTitle = "Review and publish",
    formDescription = "Description",
    formHours = "Opening hours",
    formWebsite = "Website",
    formGroupAbout = "About the venue",
    formGroupSocial = "Social media",
    formPhotosHint = "Photos are optional. You'll be able to add them from your gallery soon.",
    formReviewHint = "Check the details before publishing. It's saved on this device and appears immediately in the directory and the map.",
    formNotProvided = "Not provided",
    formSavedTitle = "Done!",
    formAddAnother = "Add another",

    coverageTitle = "Directory coverage",
    formGroupIdentity = "Identity",
    formGroupLocation = "Location",
    formGroupContact = "Contact",
    formMunicipalityPlaceholder = "Select a municipality",
    detailActionMap = "Maps",
    detailActionSave = "Save",
    detailActionSaved = "Saved",
    settingsSyncStatus = "Status",
    coverageSpaces = "Venues",
    coverageMunicipalities = "Municipalities",
    coverageTypes = "Venue types",
    coverageWithPhone = "With phone",
    coverageByType = "By venue type",
    coverageByMunicipality = "By municipality",
    coverageContributions = "Local contributions",
    coverageNoData = "Nothing to show yet",

    syncIdle = "Not synced",
    syncSyncing = "Syncing…",
    syncUpToDate = { count -> "Up to date · $count on server" },
    syncOffline = "Offline · showing cached data",
    syncRejected = "The server rejected the request",
    syncServer = "The server is not responding",
    syncMalformed = "Unexpected response from the server",
    syncUnknown = "Could not sync",
    syncRetry = "Retry",
    syncNewSpace = { name -> "New venue: $name" },

    settingsAppearance = "Appearance",
    settingsTheme = "Theme",
    settingsThemeSystem = "System",
    settingsThemeDark = "Dark",
    settingsThemeLight = "Light",
    settingsLanguage = "Language",
    settingsData = "Data",
    settingsRefresh = "Sync now",
    settingsAbout = "About",
    settingsAboutBody = "Directory of public cultural venues in the state of Aguascalientes. " +
        "A Colectivo Vendaval project in collaboration with IAJU.",
    settingsPlatform = "Platform",
    settingsSourceCatalogue = { count -> "$count venues in the base catalogue" },
)

fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.ES -> Spanish
    AppLanguage.EN -> English
}

val LocalStrings: ProvidableCompositionLocal<Strings> = staticCompositionLocalOf { Spanish }
