package com.walkmates.lab3;

import com.walkmates.model.Listing;
import com.walkmates.model.ListingType;
import com.walkmates.model.Seeker;
import com.walkmates.service.ai.LlmClient;
import com.walkmates.service.ai.MatchExplanationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MetamorphicRelationsTest {

    private Seeker seeker() {
        return new Seeker(
                "pat@example.com",
                "Pat",
                "0701112233");
    }

    private MatchExplanationService service() {
        // No live LLM is needed for recommendBestMatch.
        return new MatchExplanationService(mock(LlmClient.class));
    }

    @Test
    @DisplayName("MR-1: irrelevant description change does not change the best match")
    void irrelevantDescriptionDoesNotChangeBestMatch() {
        MatchExplanationService service = service();

        Listing dogWalk = new Listing(
                "provider-1",
                "Walk Rex",
                "Friendly dog",
                ListingType.DOG_WALK);

        Listing dayVisit = new Listing(
                "provider-2",
                "Visit Luna",
                "Feed and check the pet",
                ListingType.DAY_VISIT);

        List<Listing> candidates = List.of(dogWalk, dayVisit);

        // Record the selected listing before changing irrelevant free text.
        Listing before = service.recommendBestMatch(seeker(), candidates);

        // MR-1: changing only the description should not affect the ranking.
        dogWalk.setDescription(
                "Friendly dog. The provider likes watching movies.");

        Listing after = service.recommendBestMatch(seeker(), candidates);

        // The same listing should still be selected.
        assertThat(after.getId()).isEqualTo(before.getId());
    }

    @Test
    @DisplayName("MR-2: changing candidate order does not change the best match")
    void candidateOrderDoesNotChangeBestMatch() {
        MatchExplanationService service = service();

        Listing dogWalk = new Listing(
                "provider-1",
                "Walk Rex",
                "Friendly dog",
                ListingType.DOG_WALK);

        Listing dayVisit = new Listing(
                "provider-2",
                "Visit Luna",
                "Short visit",
                ListingType.DAY_VISIT);

        Listing petSitting = new Listing(
                "provider-3",
                "Pet sitting",
                "Stay with the pet",
                ListingType.PET_SITTING);

        Listing firstResult = service.recommendBestMatch(
                seeker(),
                List.of(dogWalk, dayVisit, petSitting));

        // MR-2: reorder the same candidates without changing their data.
        Listing secondResult = service.recommendBestMatch(
                seeker(),
                List.of(petSitting, dogWalk, dayVisit));

        // The selected listing must be independent of input order.
        assertThat(secondResult.getId()).isEqualTo(firstResult.getId());
    }
}