package ticketing.infrastructure.persistence.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.repository.TicketDomainRepository;
import ticketing.infrastructure.persistence.mapper.GetAllTicketInfraMapper;
import ticketing.infrastructure.persistence.model.entity.Ticket;
import ticketing.infrastructure.persistence.repository.TicketInfrastructureRepository;

@Repository
@RequiredArgsConstructor
public class TicketInfrastructureJpaRepositoryImpl implements TicketDomainRepository {

    private final TicketInfrastructureRepository ticketInfraRepository;
    private final GetAllTicketInfraMapper mapper;

    @Override
    public TicketDomain findTicketById(Long ticketId) {
        Ticket ticket = ticketInfraRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Resource not found"));
        return mapper.toDomain(ticket);
    }
}
