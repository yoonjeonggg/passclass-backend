package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotPaymentOwnerException extends DomainException {
    public NotPaymentOwnerException() {
        super(HttpStatus.FORBIDDEN, "해당 결제에 접근 권한이 없습니다.");
    }
}
