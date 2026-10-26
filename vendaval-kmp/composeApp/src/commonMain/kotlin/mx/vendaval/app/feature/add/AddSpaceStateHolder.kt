package mx.vendaval.app.feature.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import mx.vendaval.app.presentation.StateHolder
import mx.vendaval.core.domain.model.DraftValidation
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SpaceDraft
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.SocialPlatform
import mx.vendaval.core.domain.model.VenueType
import mx.vendaval.core.domain.repository.SpaceRepository
import mx.vendaval.core.domain.service.LocationOutcome
import mx.vendaval.core.domain.service.LocationProvider
import mx.vendaval.core.domain.validation.SpaceDraftValidator

/** Outcome of a submission attempt. */
sealed interface SubmitState {
    data object Editing : SubmitState
    data object Submitting : SubmitState

    /** [published] distinguishes "on the server" from "queued on this device". */
    data class Saved(val published: Boolean) : SubmitState
    data class Rejected(val reason: String) : SubmitState
}

/**
 * Screen-scoped state for the submission form.
 *
 * Validation runs on every edit but errors are only *shown* after the first submit
 * attempt ([showErrors]). Flagging a field the user has not finished typing in is a
 * classic form-UX mistake: the message appears on the first keystroke and reads as a
 * failure rather than as guidance.
 */
class AddSpaceStateHolder(
    private val spaceRepository: SpaceRepository,
    private val locationProvider: LocationProvider,
) : StateHolder() {

    var draft: SpaceDraft by mutableStateOf(SpaceDraft())
        private set

    var validation: DraftValidation by mutableStateOf(DraftValidation())
        private set

    var showErrors: Boolean by mutableStateOf(false)
        private set

    var submitState: SubmitState by mutableStateOf(SubmitState.Editing)
        private set

    var isLocating: Boolean by mutableStateOf(false)
        private set

    val supportsLocation: Boolean get() = locationProvider.isSupported

    /** Images picked for this submission, held in memory until the space is saved. */
    var pickedPhotos: List<PickedImage> by mutableStateOf<List<PickedImage>>(emptyList())
        private set

    fun addPhoto(image: PickedImage) {
        pickedPhotos = pickedPhotos + image
    }

    fun removePhoto(index: Int) {
        pickedPhotos = pickedPhotos.filterIndexed { i, _ -> i != index }
    }

    /** Clears the form back to empty — used by the success screen's "add another". */
    fun reset() {
        draft = SpaceDraft()
        validation = DraftValidation()
        showErrors = false
        pickedPhotos = emptyList()
        submitState = SubmitState.Editing
    }

    fun onNameChange(value: String) = update { it.copy(name = value) }
    fun onAddressChange(value: String) = update { it.copy(address = value) }
    fun onPhoneChange(value: String) = update { it.copy(phone = value) }
    fun onMapQueryChange(value: String) = update { it.copy(mapQuery = value) }
    fun onTypeChange(value: VenueType) = update { it.copy(type = value) }
    fun onDescriptionChange(value: String) = update { it.copy(description = value) }
    fun onHoursChange(value: String) = update { it.copy(hours = value) }
    fun onWebsiteChange(value: String) = update { it.copy(website = value) }
    fun onSocialChange(platform: SocialPlatform, value: String) =
        update { it.copy(socialLinks = it.socialLinks + (platform to value)) }

    fun onMunicipalityChange(value: Municipality) = update { current ->
        // Pre-fill the coordinate with the municipal seat when the user has not
        // supplied one; it is a defensible starting point and can be edited.
        if (current.latitude.isBlank() && current.longitude.isBlank()) {
            current.copy(
                municipality = value,
                latitude = value.approximateCentre.latitude.toString(),
                longitude = value.approximateCentre.longitude.toString(),
            )
        } else {
            current.copy(municipality = value)
        }
    }

    fun onLatitudeChange(value: String) = update { it.copy(latitude = value.filterCoordinate()) }
    fun onLongitudeChange(value: String) = update { it.copy(longitude = value.filterCoordinate()) }

    fun useMunicipalityCentre() = update { current ->
        val centre = current.municipality?.approximateCentre ?: return@update current
        current.copy(
            latitude = centre.latitude.toString(),
            longitude = centre.longitude.toString(),
        )
    }

    fun useCurrentLocation() {
        scope.launch {
            isLocating = true
            val outcome = locationProvider.currentLocation()
            isLocating = false
            if (outcome is LocationOutcome.Available) {
                update { current ->
                    current.copy(
                        latitude = outcome.point.latitude.toString(),
                        longitude = outcome.point.longitude.toString(),
                    )
                }
            }
        }
    }

    fun submit() {
        showErrors = true
        if (!validation.isValid) return

        scope.launch {
            submitState = SubmitState.Submitting
            val result = spaceRepository.submit(draft)
            submitState = result.fold(
                onSuccess = { saved ->
                    // A record that is still LOCAL after submit means the network
                    // leg failed and it is sitting in the outbox — worth telling
                    // the user, because it is not on the shared map yet.
                    SubmitState.Saved(published = saved.origin != SpaceOrigin.LOCAL)
                },
                onFailure = { error ->
                    SubmitState.Rejected(error.message ?: "No se pudo guardar")
                },
            )
        }
    }

    private inline fun update(transform: (SpaceDraft) -> SpaceDraft) {
        draft = transform(draft)
        validation = SpaceDraftValidator(draft)
        if (submitState is SubmitState.Rejected) submitState = SubmitState.Editing
    }
}

/**
 * Keeps a coordinate field to characters that can form a decimal number, so the
 * validator only ever sees plausible input and the numeric keyboard on mobile
 * cannot be bypassed by a paste.
 */
private fun String.filterCoordinate(): String =
    filterIndexed { index, ch ->
        ch.isDigit() || ch == '.' || (ch == '-' && index == 0)
    }.take(20)
