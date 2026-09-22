package ticketing.application.service.cache;

import org.springframework.stereotype.Service;
import ticketing.domain.model.entity.TicketDomain;

@Service
public class TicketCacheService {
    public TicketDomain getTicketDetailCache(Long ticketId) {
        System.out.println("test call cache service with item id: " + ticketId);
        return null;
    }
}
