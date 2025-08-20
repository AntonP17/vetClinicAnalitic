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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

        try {

            log.info("take message : {}", message);

            VisitStatusEventDto visitStatusEventDto = objectMapper.readValue(message, VisitStatusEventDto.class);
//
//            VisitStatus newVisitStatus = VisitStatus.builder()
//                    .visitId(visitStatusEventDto.visitId())
//                    .historyDate(LocalDateTime.now())
//                    .status(visitStatusEventDto.status())
//                    .comment(visitStatusEventDto.comment())
//                    .build();
//            visitStatusRepository.save(newVisitStatus);

            log.info("try find visitStatus : {}", visitStatusEventDto.visitId());
            VisitStatus findVisitStatus = visitStatusRepository.findByVisitId(visitStatusEventDto.visitId());

            if (findVisitStatus == null) {

                log.info("visitStatus not found, save to DB");

                visitStatusRepository.save(VisitStatus.builder()
                        .visitId(visitStatusEventDto.visitId())
                        .historyDate(LocalDateTime.now())
                        .status(visitStatusEventDto.status())
                        .comment(visitStatusEventDto.comment())
                        .build()
                );

            } else {

                log.info("visitStatus found");
                log.info("try save new status : {}", visitStatusEventDto);
                findVisitStatus.setHistoryDate(LocalDateTime.now());
                findVisitStatus.setStatus(visitStatusEventDto.status());
                findVisitStatus.setComment(visitStatusEventDto.comment());
                visitStatusRepository.save(findVisitStatus);

            }


        } catch (JsonProcessingException e) {
            log.error("failed to parse message", e);
        }

    }

}
