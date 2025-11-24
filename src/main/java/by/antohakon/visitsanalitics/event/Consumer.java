package by.antohakon.visitsanalitics.event;

import by.antohakon.visitsanalitics.dto.VisitStatusEventDto;
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
            topics = "analytics",
            groupId = "analiticGroup",
            containerFactory = "analiticKafkaListenerContainerFactory"
    )
    public void listen(String message) {
        processKafkaMessage(message);
    }

    private void processKafkaMessage(String message) {
        try {
            log.info("Received message: {}", message);
            VisitStatusEventDto eventDto = parseMessage(message);
            processVisitStatusEvent(eventDto);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse message", e);
        }
    }

    private VisitStatusEventDto parseMessage(String message) throws JsonProcessingException {
        return objectMapper.readValue(message, VisitStatusEventDto.class);
    }

    @Transactional
    public void processVisitStatusEvent(VisitStatusEventDto eventDto) {
        UUID visitId = eventDto.visitId();
        log.info("Processing visit status for visitId: {}", visitId);

        VisitStatus existingStatus = findExistingVisitStatus(visitId);

        if (existingStatus == null) {
            createNewVisitStatus(eventDto);
        } else {
            updateExistingVisitStatus(existingStatus, eventDto);
        }
    }

    @Cacheable(value = "analitics_cache", key = "#visitId")
    public VisitStatus findExistingVisitStatus(UUID visitId) {
        log.info("Looking for existing visit status: {}", visitId);
        return visitStatusRepository.findByVisitId(visitId);
    }

    private void createNewVisitStatus(VisitStatusEventDto eventDto) {
        log.info("Creating new visit status for visitId: {}", eventDto.visitId());

        VisitStatus newStatus = buildVisitStatusFromEvent(eventDto);
        visitStatusRepository.save(newStatus);
    }

    private void updateExistingVisitStatus(VisitStatus existingStatus, VisitStatusEventDto eventDto) {
        log.info("Updating existing visit status: {}", eventDto.visitId());
        log.info("New status data: {}", eventDto);

        updateVisitStatusFields(existingStatus, eventDto);
        visitStatusRepository.save(existingStatus);
    }

    private VisitStatus buildVisitStatusFromEvent(VisitStatusEventDto eventDto) {
        return VisitStatus.builder()
                .visitId(eventDto.visitId())
                .historyDate(LocalDateTime.now())
                .status(eventDto.status())
                .comment(eventDto.comment())
                .build();
    }

    private void updateVisitStatusFields(VisitStatus visitStatus, VisitStatusEventDto eventDto) {
        visitStatus.setHistoryDate(LocalDateTime.now());
        visitStatus.setStatus(eventDto.status());
        visitStatus.setComment(eventDto.comment());
    }

}
