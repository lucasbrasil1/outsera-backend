package com.outsera.backend;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.model.Movie;
import com.outsera.backend.repository.MovieRepository;
import com.outsera.backend.service.ProducerService;

@SpringBootTest
public class ResourceIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ProducerService producerService;

    @Test
    void movielistCsvShouldLoadAndProduceAwardIntervals() {
        long totalMovies = movieRepository.count();
        assertTrue(totalMovies > 0, "Expected movies to be loaded from Movielist.csv");

        List<Movie> winningMovies = movieRepository.findByWinnerTrue();
        assertFalse(winningMovies.isEmpty(), "Expected at least one winning movie in Movielist.csv");

        AwardIntervalsResponseDTO response = producerService.getAwardIntervals();
        assertNotNull(response, "Expected a non-null award intervals response");
        assertNotNull(response.getMin(), "Expected non-null min list");
        assertNotNull(response.getMax(), "Expected non-null max list");
        assertFalse(response.getMin().isEmpty() || response.getMax().isEmpty(),
                "Expected Movielist.csv to produce at least one min and one max interval");
    }
}
