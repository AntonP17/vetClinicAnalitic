package by.antohakon.visitsanalitics.controller;

import by.antohakon.visitsanalitics.dto.VisitStatusDto;
import by.antohakon.visitsanalitics.service.VisitStatusServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/status")
public class VisitStatusController {

    private final VisitStatusServiceImpl visitStatusService;

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public Page<VisitStatusDto> getVisitStatuses(@PageableDefault(size = 5) Pageable pageable) {
        return visitStatusService.getAllHistory(pageable);
    }

    @GetMapping("/{visitId}")
    @ResponseStatus(value = HttpStatus.OK)
    public VisitStatusDto getVisitStatusByUuid(@PathVariable UUID visitId) {

      return visitStatusService.getStatusByUuid(visitId);

    }
}
