package ticketing.exception;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ResponseError (
        Integer status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {}
