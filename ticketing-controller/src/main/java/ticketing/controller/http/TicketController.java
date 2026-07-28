package ticketing.controller.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ticketing.application.service.ticket.TicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {

    private final TicketApplicationService ticketApplicationService;

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDomain> getTicketById(@PathVariable("ticketId") Long ticketId) {
        return ResponseEntity.ok(ticketApplicationService.getTicketById(ticketId));
    }
}
