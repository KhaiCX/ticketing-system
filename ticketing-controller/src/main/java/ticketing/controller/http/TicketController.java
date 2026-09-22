package ticketing.controller.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ticketing.application.service.ticket.TicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketController {

    private final TicketApplicationService ticketApplicationService;

    @GetMapping("/{ticketId}")
    @CircuitBreaker(name = "getTicketByIdRequest", fallbackMethod = "fallbackGetTicketById")
    public ResponseEntity<TicketDomain> getTicketById(@PathVariable("ticketId") Long ticketId) {
        return ResponseEntity.ok(ticketApplicationService.getTicketById(ticketId));
    }

    public ResponseEntity<TicketDomain> fallbackGetTicketById(Throwable exception) {

        //  Get ticket from cache
        return ResponseEntity.ok(TicketDomain.builder()
                .id(1L)
                .name("Ticket name cache")
                .description("Description cache")
                .priceOriginal(1L)
                .status(1)
                .activityId(1L)
                .saleEndTime(LocalDateTime.now())
                .stockInitial(1)
                .stockAvailable(1)
                .stockInitial(1)
                .saleStartTime(LocalDateTime.now())
                .isStockPrepared(true)
                .priceFlash(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }
}
