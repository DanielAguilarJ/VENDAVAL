package mx.vendaval.core.data.local

/**
 * Typed wrappers around [JsonFileStore], one per persisted document.
 *
 * ## Why these exist
 * Registering three `JsonFileStore<T>` singletons in a DI container does not work:
 * generics are erased on the JVM and Kotlin/Native alike, so `JsonFileStore<A>` and
 * `JsonFileStore<B>` are the same key and the container hands back whichever was
 * registered last. That surfaces as a `ClassCastException` at first read — far from
 * the wiring that caused it.
 *
 * Qualifiers would paper over it, but they are strings checked at runtime. Distinct
 * types make the mistake unrepresentable and let the compiler enforce the wiring.
 *
 * As a bonus, the file name and serializer now live next to the data they describe
 * rather than inside a DI module.
 */
class CatalogueCache(paths: StoragePaths) {
    private val store = JsonFileStore(
        paths = paths,
        fileName = "catalogue.json",
        serializer = CatalogueSnapshot.serializer(),
        json = VendavalJson,
        defaultValue = { CatalogueSnapshot.EMPTY },
    )

    suspend fun read(): CatalogueSnapshot = store.read()
    suspend fun write(snapshot: CatalogueSnapshot): Boolean = store.write(snapshot)
}

class FavouritesCache(paths: StoragePaths) {
    private val store = JsonFileStore(
        paths = paths,
        fileName = "favourites.json",
        serializer = FavouritesSnapshot.serializer(),
        json = VendavalJson,
        defaultValue = { FavouritesSnapshot.EMPTY },
    )

    suspend fun read(): FavouritesSnapshot = store.read()
    suspend fun write(snapshot: FavouritesSnapshot): Boolean = store.write(snapshot)
}

class PreferencesCache(paths: StoragePaths) {
    private val store = JsonFileStore(
        paths = paths,
        fileName = "preferences.json",
        serializer = PreferencesSnapshot.serializer(),
        json = VendavalJson,
        defaultValue = { PreferencesSnapshot.DEFAULT },
    )

    suspend fun read(): PreferencesSnapshot = store.read()
    suspend fun write(snapshot: PreferencesSnapshot): Boolean = store.write(snapshot)
}
