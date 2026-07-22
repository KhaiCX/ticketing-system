package ticketing.domain.model.entity;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record TicketDomain (
     Long id,
     String name,
     String description,
     int stockInitial,
     int stockAvailable,
     boolean isStockPrepared,
     Long priceOriginal,
     Long priceFlash,
     LocalDateTime saleStartTime,
     LocalDateTime saleEndTime,
     int status,
     Long activityId,
     LocalDateTime updatedAt,
     LocalDateTime createdAt) {}
