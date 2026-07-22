package ticketing.application.service.event;

import ticketing.domain.model.entity.TicketDomain;

import java.util.List;

public interface GetAllTicketApplicationService {
    List<TicketDomain> getAll();
}
