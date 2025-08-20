package by.antohakon.visitsanalitics.service;

import by.antohakon.visitsanalitics.dto.VisitStatusDto;
import by.antohakon.visitsanalitics.entity.VisitStatus;
import by.antohakon.visitsanalitics.exceptions.VisitStatusNotFoundException;
import by.antohakon.visitsanalitics.repository.VisitStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitStatusServiceImpl implements VisitStatusService {

    private final VisitStatusRepository visitStatusRepository;

    @Override
    public VisitStatusDto getStatusByUuid(UUID visitId) {

        log.info("method getHistoryByUuid ");
        log.info("try find visit id {}", visitId);

        VisitStatus findVisitStatus = visitStatusRepository.findByVisitId(visitId);
        if (findVisitStatus == null) {
            throw new VisitStatusNotFoundException("Visit not found with id " + visitId);
        }

        log.info("successfully visit to DB : {}", findVisitStatus);

        VisitStatusDto visitHistoryDto = VisitStatusDto.builder()
                .visitId(findVisitStatus.getVisitId())
                .historyDate(findVisitStatus.getHistoryDate())
                .status(findVisitStatus.getStatus())
                .comment(findVisitStatus.getComment())
                .build();

        return visitHistoryDto;

    }

    @Override
    public Page<VisitStatusDto> getAllHistory(Pageable pageable) {

        log.info("method getHistory ");
        return visitStatusRepository.findAll(pageable)
                .map(visitStatus -> VisitStatusDto.builder()
                        .visitId(visitStatus.getVisitId())
                        .historyDate(visitStatus.getHistoryDate())
                        .status(visitStatus.getStatus())
                        .comment(visitStatus.getComment())
                        .build());
    }
}
