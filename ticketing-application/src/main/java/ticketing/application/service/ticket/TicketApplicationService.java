package ticketing.application.service.ticket;

import ticketing.domain.model.entity.TicketDomain;

public interface TicketApplicationService {
    TicketDomain getTicketById(Long ticketId);
}
