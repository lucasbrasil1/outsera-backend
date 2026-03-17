package com.outsera.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.dto.ProducerIntervalDTO;
import com.outsera.backend.repository.MovieRepository;

@Service
public class ProducerService {

    private final MovieRepository movieRepository;

    public ProducerService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public AwardIntervalsResponseDTO getAwardIntervals() {
        List<Object[]> rows = movieRepository.findWinnerProducerYears();

        List<ProducerIntervalDTO> minList = new ArrayList<>();
        List<ProducerIntervalDTO> maxList = new ArrayList<>();
        int minInterval = Integer.MAX_VALUE;
        int maxInterval = Integer.MIN_VALUE;

        String currentProducer = null;
        int prevYear = -1;

        for (Object[] row : rows) {
            String producer = (String) row[0];
            int year = (Integer) row[1];

            if (producer.equals(currentProducer)) {
                int interval = year - prevYear;
                ProducerIntervalDTO dto = new ProducerIntervalDTO(producer, interval, prevYear, year);

                if (interval < minInterval) {
                    minInterval = interval;
                    minList = new ArrayList<>();
                    minList.add(dto);
                } else if (interval == minInterval) {
                    minList.add(dto);
                }

                if (interval > maxInterval) {
                    maxInterval = interval;
                    maxList = new ArrayList<>();
                    maxList.add(dto);
                } else if (interval == maxInterval) {
                    maxList.add(dto);
                }
            }

            currentProducer = producer;
            prevYear = year;
        }

        return new AwardIntervalsResponseDTO(minList, maxList);
    }
}
