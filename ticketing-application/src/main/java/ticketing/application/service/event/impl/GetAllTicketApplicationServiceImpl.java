package ticketing.application.service.event.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ticketing.application.service.event.GetAllTicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.service.GetAllTicketDomainService;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class GetAllTicketApplicationServiceImpl implements GetAllTicketApplicationService {
    private final GetAllTicketDomainService service;
    @Override
    public List<TicketDomain> getAll() {
        return service.getAllTickets();
    }
}
