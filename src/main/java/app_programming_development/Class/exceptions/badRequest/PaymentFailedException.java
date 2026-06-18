package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class PaymentFailedException extends DomainException {
    public PaymentFailedException() {
        super(HttpStatus.BAD_REQUEST, "결제에 실패하였습니다.");
    }
    public PaymentFailedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
