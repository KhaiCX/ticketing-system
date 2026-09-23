package ticketing.application.service.ticket.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import ticketing.application.service.ticket.TicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.service.TicketDomainService;

@Service
@RequiredArgsConstructor
public class TicketApplicationServiceImpl implements TicketApplicationService {

    private final TicketDomainService ticketDomainService;

    @Override
    @Cacheable(cacheNames = "tickets")
    public TicketDomain getTicketById(Long ticketId) {
        return ticketDomainService.getTicketById(ticketId);
    }
}
