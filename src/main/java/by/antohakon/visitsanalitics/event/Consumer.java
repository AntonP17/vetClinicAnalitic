package by.antohakon.visitsanalitics.event;

import by.antohakon.visitsanalitics.dto.VisitStatusEventDto;
import by.antohakon.visitsanalitics.entity.Status;
import by.antohakon.visitsanalitics.entity.VisitStatus;
import by.antohakon.visitsanalitics.exceptions.VisitStatusNotFoundException;
import by.antohakon.visitsanalitics.repository.VisitStatusRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class Consumer {

    private final VisitStatusRepository visitStatusRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topic.analytics}",
            groupId = "${kafka.group.analytics}",
            containerFactory = "analiticKafkaListenerContainerFactory"
    )
    @Transactional
    public void listen(String message) {
        try {
            log.info("Received analytics message: {}", message);
            VisitStatusEventDto eventDto = objectMapper.readValue(message, VisitStatusEventDto.class);
            processVisitStatusEvent(eventDto);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse analytics message", e);
        }
    }

    @Transactional
    public void processVisitStatusEvent(VisitStatusEventDto eventDto) {
        UUID visitId = eventDto.visitId();
        log.info("Processing status for visitId: {}, status: {}", visitId, eventDto.status());

        VisitStatus existingStatus = visitStatusRepository.findByVisitId(visitId);

        if (existingStatus == null) {

            createNewVisitStatus(eventDto);
            log.info("Created new status for visitId: {}", visitId);
        } else {
            handleStatusUpdate(existingStatus, eventDto);
        }
    }

    private void handleStatusUpdate(VisitStatus existingStatus, VisitStatusEventDto eventDto) {
        Status newStatus = eventDto.status();
        Status currentStatus = existingStatus.getStatus();

        // Правила обновления:
        // 1. FAILED всегда побеждает (перезаписывает любой другой статус)
        // 2. SUCCESS только если текущий статус не FAILED
        // 3. PROCESSING может быть обновлен на любой другой статус
        // 4. Если пытаемся обновить FAILED на SUCCESS - игнорируем

        if (newStatus == Status.FAILED) {
            // Правило 1: FAILED всегда перезаписывает
            updateVisitStatus(existingStatus, eventDto);
            log.info("Updated visit {} from {} to FAILED (rule 1)",
                    eventDto.visitId(), currentStatus);

        } else if (newStatus == Status.SUCCESS && currentStatus != Status.FAILED) {
            // Правило 2: SUCCESS только если не FAILED
            updateVisitStatus(existingStatus, eventDto);
            log.info("Updated visit {} from {} to SUCCESS (rule 2)",
                    eventDto.visitId(), currentStatus);

        } else if (newStatus == Status.PROCESSING) {
            // Правило 3: PROCESSING можно обновить
            updateVisitStatus(existingStatus, eventDto);
            log.info("Updated visit {} to PROCESSING (rule 3)", eventDto.visitId());

        } else {
            // Правило 4: Игнорируем невалидные переходы
            log.warn("Ignoring invalid status transition for visit {}: {} -> {}",
                    eventDto.visitId(), currentStatus, newStatus);
            log.warn("Message details: {}", eventDto.comment());
        }
    }

    private void createNewVisitStatus(VisitStatusEventDto eventDto) {
        VisitStatus newStatus = VisitStatus.builder()
                .visitId(eventDto.visitId())
                .historyDate(LocalDateTime.now())
                .status(eventDto.status())
                .comment(eventDto.comment())
                .build();
        visitStatusRepository.save(newStatus);
    }

    private void updateVisitStatus(VisitStatus visitStatus, VisitStatusEventDto eventDto) {
        visitStatus.setHistoryDate(LocalDateTime.now());
        visitStatus.setStatus(eventDto.status());
        visitStatus.setComment(eventDto.comment());
        visitStatusRepository.save(visitStatus);
    }

}
