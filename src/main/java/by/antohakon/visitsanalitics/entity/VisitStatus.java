package by.antohakon.visitsanalitics.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visit_history", indexes = {
        @Index(columnList = "visitId", name = "visit_history_uuid_index")
})
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID visitId;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime historyDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    private String comment;

}
