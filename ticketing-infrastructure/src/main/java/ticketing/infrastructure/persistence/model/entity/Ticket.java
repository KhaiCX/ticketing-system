package ticketing.infrastructure.persistence.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
public class Ticket {
    @Id
    private Long id;
    private String name;
    private String description;
    private int stockInitial;
    private int stockAvailable;
    private boolean isStockPrepared;
    private Long priceOriginal;
    private Long priceFlash;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private int status;
    private Long activityId;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
}
