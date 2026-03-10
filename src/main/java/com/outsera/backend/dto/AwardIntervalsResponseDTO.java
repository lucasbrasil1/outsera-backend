package com.outsera.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class AwardIntervalsResponseDTO {

    private List<ProducerIntervalDTO> min = new ArrayList<>();
    private List<ProducerIntervalDTO> max = new ArrayList<>();

    public AwardIntervalsResponseDTO() {
    }

    public AwardIntervalsResponseDTO(List<ProducerIntervalDTO> min, List<ProducerIntervalDTO> max) {
        this.min = min != null ? min : new ArrayList<>();
        this.max = max != null ? max : new ArrayList<>();
    }

    public List<ProducerIntervalDTO> getMin() {
        return min;
    }

    public void setMin(List<ProducerIntervalDTO> min) {
        this.min = min != null ? min : new ArrayList<>();
    }

    public List<ProducerIntervalDTO> getMax() {
        return max;
    }

    public void setMax(List<ProducerIntervalDTO> max) {
        this.max = max != null ? max : new ArrayList<>();
    }
}
