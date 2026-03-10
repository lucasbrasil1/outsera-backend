package com.outsera.backend.controller;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        // Pelo menos um elemento em cada lista de acordo com o fixture
        assertThat(minNode.size()).isGreaterThanOrEqualTo(1);
        assertThat(maxNode.size()).isGreaterThanOrEqualTo(1);

        // Verifica formato dos elementos (campos esperados)
        JsonNode firstMin = minNode.get(0);
        assertThat(firstMin.has("producer")).isTrue();
        assertThat(firstMin.has("interval")).isTrue();
        assertThat(firstMin.has("previousWin")).isTrue();
        assertThat(firstMin.has("followingWin")).isTrue();
    }

    @Test
    @DisplayName("GET /api/v1/producers/award-intervals deve calcular min e max exatamente como esperado para o CSV de fixture")
    void getAwardIntervals_shouldReturnExactMinAndMaxFromFixture() {
        ResponseEntity<AwardIntervalsResponseDTO> response =
                restTemplate.getForEntity("/api/v1/producers/award-intervals", AwardIntervalsResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AwardIntervalsResponseDTO body = response.getBody();
        assertThat(body).as("Corpo da resposta nao deve ser nulo").isNotNull();

        List<ProducerIntervalDTO> minList = body != null ? body.getMin() : List.of();
        List<ProducerIntervalDTO> maxList = body != null ? body.getMax() : List.of();

        assertThat(minList)
                .extracting(ProducerIntervalDTO::getProducer,
                            ProducerIntervalDTO::getInterval,
                            ProducerIntervalDTO::getPreviousWin,
                            ProducerIntervalDTO::getFollowingWin)
                .containsExactlyInAnyOrder(
                        tuple("Prod A", 3, 2000, 2003),
                        tuple("Prod B", 3, 2001, 2004)
                );

        assertThat(maxList)
                .extracting(ProducerIntervalDTO::getProducer,
                            ProducerIntervalDTO::getInterval,
                            ProducerIntervalDTO::getPreviousWin,
                            ProducerIntervalDTO::getFollowingWin)
                .containsExactlyInAnyOrder(
                        tuple("Prod C", 10, 1995, 2005)
                );
    } 
}
