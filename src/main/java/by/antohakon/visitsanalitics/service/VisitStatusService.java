package by.antohakon.visitsanalitics.service;

import by.antohakon.visitsanalitics.dto.VisitStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VisitStatusService {

    VisitStatusDto getStatusByUuid(UUID visitId);
    Page<VisitStatusDto> getAllHistory(Pageable pageable);


}
