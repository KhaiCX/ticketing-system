package ticketing.domain.service;

import ticketing.domain.model.entity.TicketDomain;

import java.util.List;

public interface GetAllTicketDomainService {
    List<TicketDomain> getAllTickets();
}
