package ticketing.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.repository.GetAllTicketDomainRepository;
import ticketing.domain.service.GetAllTicketDomainService;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class GetAllTicketDomainServiceImpl implements GetAllTicketDomainService {
    private final GetAllTicketDomainRepository getAllTicketDomainRepository;
    @Override
    public List<TicketDomain> getAllTickets() {
        return getAllTicketDomainRepository.findAll();
    }
}
