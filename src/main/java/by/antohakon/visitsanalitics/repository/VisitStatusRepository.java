package by.antohakon.visitsanalitics.repository;

import by.antohakon.visitsanalitics.entity.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VisitStatusRepository extends JpaRepository<VisitStatus, Long> {

    VisitStatus findByVisitId(UUID visitId);

}
