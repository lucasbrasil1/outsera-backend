package com.outsera.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.outsera.backend.model.Movie;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByWinnerTrue();

    @Query("SELECT DISTINCT p.name as producer, m.year as year FROM Movie m JOIN m.producersList p " +
           "WHERE m.winner = true AND m.year IS NOT NULL " +
           "ORDER BY p.name ASC, m.year ASC")
    List<WinnerProducerYearView> findWinnerProducerYears();
}
