package mx.vendaval.core.domain.usecase

import mx.vendaval.core.domain.model.CoverageStats
import mx.vendaval.core.domain.model.CulturalSpace
import mx.vendaval.core.domain.model.Municipality
import mx.vendaval.core.domain.model.SpaceId
import mx.vendaval.core.domain.model.SpaceOrigin
import mx.vendaval.core.domain.model.VenueType

/**
 * Derives the figures behind the coverage screen.
 *
 * Counts are computed from the live catalogue rather than hard-coded, which is
 * what the original site got wrong: its hero advertised 20 venues, 7
 * municipalities and 5 venue types while the backing array already held 27
 * records. Numbers that can drift from their source are numbers that will.
 */
object ComputeCoverageStats {

    operator fun invoke(
        spaces: List<CulturalSpace>,
        favourites: Set<SpaceId>,
    ): CoverageStats {
        if (spaces.isEmpty()) return CoverageStats.EMPTY

        // Seed every bucket so the chart keeps a stable row order and renders
        // genuine zeros instead of omitting the category.
        val byType = VenueType.entries.associateWith { type ->
            spaces.count { it.type == type }
        }.filterValues { it > 0 }

        val byMunicipality = Municipality.entries.associateWith { municipality ->
            spaces.count { it.municipality == municipality }
        }

        return CoverageStats(
            totalSpaces = spaces.size,
            byType = byType,
            byMunicipality = byMunicipality,
            municipalitiesCovered = byMunicipality.count { it.value > 0 },
            municipalitiesInState = Municipality.entries.size,
            withPhone = spaces.count { it.phone != null },
            favourites = spaces.count { it.id in favourites },
            localContributions = spaces.count { it.origin == SpaceOrigin.LOCAL },
        )
    }
}
