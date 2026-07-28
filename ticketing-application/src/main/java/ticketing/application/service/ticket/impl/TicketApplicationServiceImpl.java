package ticketing.application.service.ticket.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ticketing.application.service.ticket.TicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.service.TicketDomainService;

@Service
@RequiredArgsConstructor
public class TicketApplicationServiceImpl implements TicketApplicationService {

    private final TicketDomainService ticketDomainService;

    @Override
    public TicketDomain getTicketById(Long ticketId) {
        return ticketDomainService.getTicketById(ticketId);
    }
}
