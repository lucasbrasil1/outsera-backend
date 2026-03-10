package com.outsera.backend.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.dto.ProducerIntervalDTO;
import com.outsera.backend.model.Movie;
import com.outsera.backend.repository.MovieRepository;

@Service
public class ProducerService {

    private final MovieRepository movieRepository;

    public ProducerService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Busca todos os filmes vencedores, agrupa por produtor, calcula intervalos
     * entre prêmios consecutivos e retorna os produtores com menor e maior intervalo.
     */
    public AwardIntervalsResponseDTO getAwardIntervals() {
        List<Movie> winningMovies = movieRepository.findByWinnerTrue();

        // producer name -> lista de anos em que ganhou (ordenada)
        Map<String, List<Integer>> producerWins = new LinkedHashMap<>();

        for (Movie movie : winningMovies) {
            Integer year = movie.getYear();
            if (year == null) continue;

            List<String> producerNames = parseProducerNames(movie.getProducers());
            for (String name : producerNames) {
                producerWins
                        .computeIfAbsent(name, k -> new ArrayList<>())
                        .add(year);
            }
        }

        // Ordenar anos de cada produtor
        producerWins.forEach((name, years) -> years.sort(Integer::compareTo));

        // Calcular todos os intervalos consecutivos
        List<ProducerIntervalDTO> allIntervals = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : producerWins.entrySet()) {
            String producer = entry.getKey();
            List<Integer> years = entry.getValue();
            if (years.size() < 2) continue;

            for (int i = 1; i < years.size(); i++) {
                int previousWin = years.get(i - 1);
                int followingWin = years.get(i);
                int interval = followingWin - previousWin;
                allIntervals.add(new ProducerIntervalDTO(producer, interval, previousWin, followingWin));
            }
        }

        if (allIntervals.isEmpty()) {
            return new AwardIntervalsResponseDTO(new ArrayList<>(), new ArrayList<>());
        }

        int minInterval = allIntervals.stream()
                .map(ProducerIntervalDTO::getInterval)
                .min(Comparator.naturalOrder())
                .orElse(0);

        int maxInterval = allIntervals.stream()
                .map(ProducerIntervalDTO::getInterval)
                .max(Comparator.naturalOrder())
                .orElse(0);

        List<ProducerIntervalDTO> minList = allIntervals.stream()
                .filter(dto -> dto.getInterval().equals(minInterval))
                .collect(Collectors.toList());

        List<ProducerIntervalDTO> maxList = allIntervals.stream()
                .filter(dto -> dto.getInterval().equals(maxInterval))
                .collect(Collectors.toList());

        return new AwardIntervalsResponseDTO(minList, maxList);
    }

    /**
     * Separa os nomes de produtores: divide por " and " e por vírgula.
     */
    private List<String> parseProducerNames(String producersStr) {
        if (producersStr == null || producersStr.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(producersStr.split(" and "))
                .flatMap(part -> Arrays.stream(part.split(",")))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toList());
    }
}
