package com.outsera.backend.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.dto.ProducerIntervalDTO;
import com.outsera.backend.model.Movie;
import com.outsera.backend.repository.MovieRepository;

class ProducerServiceTest {

    private MovieRepository movieRepository;
    private ProducerService producerService;

    @BeforeEach
    void setUp() {
        movieRepository = Mockito.mock(MovieRepository.class);
        producerService = new ProducerService(movieRepository);
    }

    @Test
    @DisplayName("When there are no winning movies, min and max lists are empty")
    void getAwardIntervals_noWinningMovies_returnsEmptyLists() {
        when(movieRepository.findByWinnerTrue()).thenReturn(Collections.emptyList());

        AwardIntervalsResponseDTO response = producerService.getAwardIntervals();

        assertThat(response).isNotNull();
        assertThat(response.getMin()).isEmpty();
        assertThat(response.getMax()).isEmpty();
    }

    @Test
    @DisplayName("Calculates min and max intervals correctly for multiple producers and wins")
    void getAwardIntervals_calculatesMinAndMaxIntervalsCorrectly() {
        Movie m1 = createMovie(2000, "Prod A and Prod B");
        Movie m2 = createMovie(2002, "Prod A");
        Movie m3 = createMovie(2006, "Prod A, Prod C");
        Movie m4 = createMovie(2003, "Prod B and Prod C");

        when(movieRepository.findByWinnerTrue()).thenReturn(Arrays.asList(m1, m2, m3, m4));

        AwardIntervalsResponseDTO response = producerService.getAwardIntervals();

        assertThat(response).isNotNull();

        List<ProducerIntervalDTO> minList = response.getMin();
        List<ProducerIntervalDTO> maxList = response.getMax();

        assertThat(minList).hasSize(1);
        ProducerIntervalDTO min = minList.getFirst();
        assertThat(min.getProducer()).isEqualTo("Prod A");
        assertThat(min.getInterval()).isEqualTo(2);
        assertThat(min.getPreviousWin()).isEqualTo(2000);
        assertThat(min.getFollowingWin()).isEqualTo(2002);

        assertThat(maxList).hasSize(1);
        ProducerIntervalDTO max = maxList.getFirst();
        assertThat(max.getProducer()).isEqualTo("Prod A");
        assertThat(max.getInterval()).isEqualTo(4);
        assertThat(max.getPreviousWin()).isEqualTo(2002);
        assertThat(max.getFollowingWin()).isEqualTo(2006);
    }

    @Test
    @DisplayName("Handles multiple producers with same min and max interval values")
    void getAwardIntervals_multipleProducersWithSameIntervals() {
        Movie m1 = createMovie(2000, "Prod A");
        Movie m2 = createMovie(2002, "Prod A and Prod B");
        Movie m3 = createMovie(2004, "Prod B");

        when(movieRepository.findByWinnerTrue()).thenReturn(Arrays.asList(m1, m2, m3));

        AwardIntervalsResponseDTO response = producerService.getAwardIntervals();

        assertThat(response).isNotNull();

        List<ProducerIntervalDTO> minList = response.getMin();
        List<ProducerIntervalDTO> maxList = response.getMax();

        assertThat(minList).hasSize(2);
        assertThat(maxList).hasSize(2);

        assertThat(minList)
                .extracting(ProducerIntervalDTO::getInterval)
                .containsOnly(2);

        assertThat(maxList)
                .extracting(ProducerIntervalDTO::getInterval)
                .containsOnly(2);
    }

    @Test
    @DisplayName("Ignores movies without year or producers and handles blanks safely")
    void getAwardIntervals_ignoresInvalidMovies() {
        Movie valid1 = createMovie(2000, "Prod A, Prod B");
        Movie valid2 = createMovie(2005, "Prod A");

        Movie noYear = createMovie(null, "Prod A");
        Movie noProducers = createMovie(2010, null);
        Movie blankProducers = createMovie(2012, "   ");

        when(movieRepository.findByWinnerTrue())
                .thenReturn(Arrays.asList(valid1, valid2, noYear, noProducers, blankProducers));

        AwardIntervalsResponseDTO response = producerService.getAwardIntervals();

        assertThat(response).isNotNull();
        assertThat(response.getMin()).hasSize(1);
        assertThat(response.getMax()).hasSize(1);

        ProducerIntervalDTO interval = response.getMin().getFirst();
        assertThat(interval.getProducer()).isEqualTo("Prod A");
        assertThat(interval.getInterval()).isEqualTo(5);
        assertThat(interval.getPreviousWin()).isEqualTo(2000);
        assertThat(interval.getFollowingWin()).isEqualTo(2005);
    }

    private Movie createMovie(Integer year, String producers) {
        Movie movie = new Movie();
        movie.setYear(year);
        movie.setProducers(producers);
        movie.setWinner(true);
        return movie;
    }
}
