package ticketing.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ticketing.infrastructure.persistence.model.entity.Ticket;

public interface GetAllTicketInfraRepository extends JpaRepository<Ticket, Long> {
}
