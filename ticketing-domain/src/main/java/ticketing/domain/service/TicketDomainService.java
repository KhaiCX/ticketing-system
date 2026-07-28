package ticketing.domain.service;

import ticketing.domain.model.entity.TicketDomain;

public interface TicketDomainService {
    TicketDomain getTicketById(Long ticketId);
}
