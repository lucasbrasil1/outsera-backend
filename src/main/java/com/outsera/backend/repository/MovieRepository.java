package com.outsera.backend.repository;

import com.outsera.backend.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByWinnerTrue();

    @Query("SELECT DISTINCT p.name, m.year FROM Movie m JOIN m.producersList p " +
           "WHERE m.winner = true AND m.year IS NOT NULL " +
           "ORDER BY p.name ASC, m.year ASC")
    List<Object[]> findWinnerProducerYears();
}
