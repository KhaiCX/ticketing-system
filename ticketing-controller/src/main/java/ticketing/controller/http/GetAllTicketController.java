package ticketing.controller.http;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ticketing.application.service.event.GetAllTicketApplicationService;
import ticketing.domain.model.entity.TicketDomain;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class GetAllTicketController {
    private final GetAllTicketApplicationService eventAppService;
    private final RestTemplate restTemplate;

    @GetMapping
    @RateLimiter(name = "backendA", fallbackMethod = "fallbackHello")
    public ResponseEntity<List<TicketDomain>> getAllTickets() {
        return ResponseEntity.ok(eventAppService.getAll());
    }

    public ResponseEntity<?> fallbackHello(Throwable throwable) {
        return ResponseEntity.status(429).body("Too many request");
    }

    public ResponseEntity<?> fallbackCircuit(Throwable throwable) {
        return ResponseEntity.status(429).body("Too many request");
    }

    @GetMapping("/hi/v1")
    @CircuitBreaker(name = "backendB", fallbackMethod = "fallbackCircuit")
    public String hiV1() {
        String url = "https://fakestoreapi.com/products/1";
        return restTemplate.getForObject(url, String.class);
        // return eventAppService.sayHi("KhaiCX v1");
    }

}
