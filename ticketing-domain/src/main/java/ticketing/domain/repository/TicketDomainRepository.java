package ticketing.domain.repository;

import ticketing.domain.model.entity.TicketDomain;

public interface TicketDomainRepository {
    TicketDomain findTicketById(Long ticketId);
}
