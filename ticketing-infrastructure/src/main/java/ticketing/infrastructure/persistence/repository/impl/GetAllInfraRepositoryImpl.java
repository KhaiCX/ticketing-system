package ticketing.infrastructure.persistence.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.repository.GetAllTicketDomainRepository;
import ticketing.infrastructure.persistence.mapper.GetAllTicketInfraMapper;
import ticketing.infrastructure.persistence.model.entity.Ticket;
import ticketing.infrastructure.persistence.repository.GetAllTicketInfraRepository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GetAllInfraRepositoryImpl implements GetAllTicketDomainRepository {
    private final GetAllTicketInfraRepository repository;
    private final GetAllTicketInfraMapper mapper;

    @Override
    public List<TicketDomain> findAll() {
        List<Ticket> tickets = repository.findAll();
        return mapper.toDomain(tickets);
    }
}
