package com.rashid.helpdesk.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class PageRequestsTest {

    private static final Set<String> ALLOWED = Set.of("createdAt", "updatedAt");

    @Test
    void of_validInput_buildsPageableWithRequestedSortAndIdTieBreaker() {
        Pageable pageable = PageRequests.of(2, 25, "updatedAt", "asc", ALLOWED);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(25);
        assertThat(pageable.getSort().toList())
                .extracting(Sort.Order::getProperty, Sort.Order::getDirection)
                .containsExactly(
                        tuple("updatedAt", Sort.Direction.ASC),
                        tuple("id", Sort.Direction.ASC));
    }

    @Test
    void of_directionIsCaseInsensitive() {
        Pageable pageable = PageRequests.of(0, 20, "createdAt", "DESC", ALLOWED);

        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void of_sizeAboveMax_isCappedAtMax() {
        Pageable pageable = PageRequests.of(0, 1000, "createdAt", "desc", ALLOWED);

        assertThat(pageable.getPageSize()).isEqualTo(PageRequests.MAX_SIZE);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void of_sizeBelowOne_becomesOne(int size) {
        Pageable pageable = PageRequests.of(0, size, "createdAt", "desc", ALLOWED);

        assertThat(pageable.getPageSize()).isEqualTo(1);
    }

    @Test
    void of_negativePage_becomesZero() {
        Pageable pageable = PageRequests.of(-3, 20, "createdAt", "desc", ALLOWED);

        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void of_sortFieldNotAllowed_throwsBadRequestListingAllowedFields() {
        assertThatThrownBy(() -> PageRequests.of(0, 20, "passwordHash", "desc", ALLOWED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("createdAt")
                .hasMessageContaining("updatedAt");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"up", "descending"})
    void of_invalidDirection_throwsBadRequest(String direction) {
        assertThatThrownBy(() -> PageRequests.of(0, 20, "createdAt", direction, ALLOWED))
                .isInstanceOf(BadRequestException.class);
    }
}