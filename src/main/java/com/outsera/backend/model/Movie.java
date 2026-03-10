package com.outsera.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie")
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_year")
    private Integer year;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String studios;

    @Column(length = 1000)
    private String producers;

    @Column(nullable = false)
    private Boolean winner = false;

    @ManyToMany
    @JoinTable(
            name = "movie_producer",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "producer_id")
    )
    private List<Producer> producersList = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getStudios() {
        return studios;
    }
    
    
    public void setStudios(String studios) {
        this.studios = studios;
    }

    public String getProducers() {
        return producers;
    }
    
    
    public void setProducers(String producers) {
        this.producers = producers;
    }

    public Boolean getWinner() {
        return winner;
    }
    
    public void setWinner(Boolean winner) {
        this.winner = winner;
    }

    public List<Producer> getProducersList() {
        return producersList;
    }
    
    
    public void setProducersList(List<Producer> producersList) {
        this.producersList = producersList;
    }
}
