package ticketing.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.infrastructure.persistence.model.entity.Ticket;
import java.util.List;
@Component
public class GetAllTicketInfraMapper {
    public List<TicketDomain> toDomain(List<Ticket> tickets) {
        return tickets.stream().map(ticket -> TicketDomain.builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .description(ticket.getDescription())
                .priceOriginal(ticket.getPriceOriginal())
                .priceFlash(ticket.getPriceFlash())
                .stockInitial(ticket.getStockInitial())
                .stockAvailable(ticket.getStockAvailable())
                .saleStartTime(ticket.getSaleStartTime())
                .saleEndTime(ticket.getSaleEndTime())
                .status(ticket.getStatus())
                .activityId(ticket.getActivityId())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build()).toList();
    }

    public TicketDomain toDomain(Ticket ticket) {
        return TicketDomain.builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .description(ticket.getDescription())
                .priceOriginal(ticket.getPriceOriginal())
                .priceFlash(ticket.getPriceFlash())
                .stockInitial(ticket.getStockInitial())
                .stockAvailable(ticket.getStockAvailable())
                .saleStartTime(ticket.getSaleStartTime())
                .saleEndTime(ticket.getSaleEndTime())
                .status(ticket.getStatus())
                .activityId(ticket.getActivityId())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
