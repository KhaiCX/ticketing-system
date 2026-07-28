package ticketing.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.repository.TicketDomainRepository;
import ticketing.domain.service.TicketDomainService;

@Service
@RequiredArgsConstructor
public class TicketDomainServiceImpl implements TicketDomainService {

    private final TicketDomainRepository ticketDomainRepository;

    @Override
    public TicketDomain getTicketById(Long ticketId) {
        return ticketDomainRepository.findTicketById(ticketId);
    }
}
