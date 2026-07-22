package ticketing.application.service.event.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ticketing.application.service.event.GetAllTicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import ticketing.domain.service.GetAllTicketDomainService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllTicketApplicationServiceImpl implements GetAllTicketApplicationService {
    private final GetAllTicketDomainService service;

    @Override
    public List<TicketDomain> getAll() {
        return service.getAllTickets();
    }
}
