package ticketing.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ticketing.infrastructure.persistence.model.entity.Ticket;

@Repository
public interface GetAllTicketInfraRepository extends JpaRepository<Ticket, Long> {
}
