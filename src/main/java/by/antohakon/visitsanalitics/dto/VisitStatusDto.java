package by.antohakon.visitsanalitics.dto;

import by.antohakon.visitsanalitics.entity.Status;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record VisitStatusDto(UUID visitId,
                             LocalDateTime historyDate,
                             Status status,
                             String comment) {
}
