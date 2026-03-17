package com.outsera.backend.controller;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.outsera.backend.dto.AwardIntervalsResponseDTO;
import com.outsera.backend.dto.ProducerIntervalDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProducerControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/producers/award-intervals deve retornar 200 e JSON com campos min e max")
    void getAwardIntervals_shouldReturn200AndValidJsonStructure() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/producers/award-intervals", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotBlank();

        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.isObject()).isTrue();
        assertThat(root.has("min")).isTrue();
        assertThat(root.has("max")).isTrue();

        JsonNode minNode = root.get("min");
        JsonNode maxNode = root.get("max");

        assertThat(minNode.isArray()).isTrue();
        assertThat(maxNode.isArray()).isTrue();

        assertThat(minNode.size()).isGreaterThanOrEqualTo(1);
        assertThat(maxNode.size()).isGreaterThanOrEqualTo(1);

        JsonNode firstMin = minNode.get(0);
        assertThat(firstMin.has("producer")).isTrue();
        assertThat(firstMin.has("interval")).isTrue();
        assertThat(firstMin.has("previousWin")).isTrue();
        assertThat(firstMin.has("followingWin")).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/producers/award-intervals deve calcular min e max exatamente conforme o CSV de fixture")
    void getAwardIntervals_shouldMatchCsvFixtureExactly() throws Exception {
        AwardIntervalsResponseDTO expected = computeExpectedFromCsv("Movielist.csv");

        ResponseEntity<AwardIntervalsResponseDTO> response =
                restTemplate.getForEntity("/api/v1/producers/award-intervals", AwardIntervalsResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AwardIntervalsResponseDTO body = response.getBody();
        assertThat(body).as("Corpo da resposta nao deve ser nulo").isNotNull();

        assertThat(body.getMin())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected.getMin());

        assertThat(body.getMax())
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrderElementsOf(expected.getMax());
    }

    /**
     * Lê o CSV e calcula independentemente os intervalos min/max esperados.
     * Qualquer alteração no arquivo reflete imediatamente nos valores esperados
     * do teste, tornando a verificação sempre derivada da fonte de dados.
     */
    private AwardIntervalsResponseDTO computeExpectedFromCsv(String filename) throws Exception {
        var resource = new ClassPathResource(filename);
        Map<String, List<Integer>> producerWins = new TreeMap<>();

        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
             var csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                     .build()) {

            csvReader.readNext(); // header
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 5 || !"yes".equalsIgnoreCase(line[4].trim())) continue;
                int year;
                try {
                    year = Integer.parseInt(line[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                String producersStr = line.length > 3 ? line[3].trim() : "";
                List<String> names = Arrays.stream(producersStr.split(" and "))
                        .flatMap(part -> Arrays.stream(part.split(",")))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();
                for (String name : names) {
                    producerWins.computeIfAbsent(name, k -> new ArrayList<>()).add(year);
                }
            }
        }

        producerWins.forEach((k, v) -> Collections.sort(v));

        List<ProducerIntervalDTO> minList = new ArrayList<>();
        List<ProducerIntervalDTO> maxList = new ArrayList<>();
        int minInterval = Integer.MAX_VALUE;
        int maxInterval = Integer.MIN_VALUE;

        for (Map.Entry<String, List<Integer>> entry : producerWins.entrySet()) {
            List<Integer> years = entry.getValue();
            for (int i = 1; i < years.size(); i++) {
                int prev = years.get(i - 1);
                int foll = years.get(i);
                int interval = foll - prev;
                ProducerIntervalDTO dto = new ProducerIntervalDTO(entry.getKey(), interval, prev, foll);

                if (interval < minInterval) { minInterval = interval; minList = new ArrayList<>(List.of(dto)); }
                else if (interval == minInterval) { minList.add(dto); }

                if (interval > maxInterval) { maxInterval = interval; maxList = new ArrayList<>(List.of(dto)); }
                else if (interval == maxInterval) { maxList.add(dto); }
            }
        }

        return new AwardIntervalsResponseDTO(minList, maxList);
    }
}
