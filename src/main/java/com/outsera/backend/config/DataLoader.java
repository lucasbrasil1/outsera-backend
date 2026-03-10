package com.outsera.backend.config;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.outsera.backend.model.Movie;
import com.outsera.backend.model.Producer;
import com.outsera.backend.repository.MovieRepository;
import com.outsera.backend.repository.ProducerRepository;

@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Value("${csv.file:Movielist.csv}")
    private String csvFile;

    private final MovieRepository movieRepository;
    private final ProducerRepository producerRepository;

    public DataLoader(MovieRepository movieRepository, ProducerRepository producerRepository) {
        this.movieRepository = movieRepository;
        this.producerRepository = producerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Iniciando carregamento do arquivo {}", csvFile);

        var resource = new ClassPathResource(csvFile);

        if (!resource.exists()) {
            log.error("Arquivo CSV '{}' nao encontrado. Encerrando aplicacao.", csvFile);
            System.exit(1);
            return;
        }

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8); var csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build()) {
            load(csvReader);
        }
    }

    /**
     * Método extraído para facilitar testes unitários e permitir melhorias de
     * performance sem depender do ciclo de vida do Spring Boot.
     */
    void load(CSVReader csvReader) throws Exception {
        String[] line = csvReader.readNext(); // header
        if (line == null || line.length < 5) {
            log.warn("Arquivo CSV vazio ou formato invalido");
            return;
        }

        Map<String, Producer> producerCache = new HashMap<>();
        int count = 0;
        while ((line = csvReader.readNext()) != null) {
            try {
                Movie movie = parseMovie(line, producerCache);
                movieRepository.save(movie);
                count++;
            } catch (Exception e) {
                log.warn("Erro ao processar linha: {} - {}", Arrays.toString(line), e.getMessage());
            }
        }

        log.info("Carregamento concluido: {} filmes importados", count);
    }

    Movie parseMovie(String[] line, Map<String, Producer> producerCache) {
        Integer year = parseYear(line[0]);
        String title = line[1].trim();
        String studios = line.length > 2 ? line[2].trim() : "";
        String producers = line.length > 3 ? line[3].trim() : "";
        boolean winner = line.length > 4 && "yes".equalsIgnoreCase(line[4].trim());

        Movie movie = new Movie();
        movie.setYear(year);
        movie.setTitle(title);
        movie.setStudios(studios);
        movie.setProducers(producers);
        movie.setWinner(winner);

        movie.setProducersList(parseProducers(producers, producerCache));
        return movie;
    }

    Integer parseYear(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    List<Producer> parseProducers(String producersStr, Map<String, Producer> producerCache) {
        if (producersStr == null || producersStr.isBlank()) {
            return new ArrayList<>();
        }

        List<String> names = Arrays.stream(producersStr.split(" and "))
                .flatMap(part -> Arrays.stream(part.split(",")))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());

        List<Producer> producers = new ArrayList<>();
        for (String name : names) {
            Producer producer = producerCache.get(name);
            if (producer == null) {
                producer = producerRepository.findByName(name)
                        .orElseGet(() -> {
                            Producer p = new Producer(name);
                            return producerRepository.save(p);
                        });
                producerCache.put(name, producer);
            }
            producers.add(producer);
        }
        return producers;
    }
}
