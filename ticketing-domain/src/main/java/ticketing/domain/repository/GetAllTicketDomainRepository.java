package ticketing.domain.repository;

import ticketing.domain.model.entity.TicketDomain;
import java.util.List;

public interface GetAllTicketDomainRepository {
    List<TicketDomain> findAll();
}
