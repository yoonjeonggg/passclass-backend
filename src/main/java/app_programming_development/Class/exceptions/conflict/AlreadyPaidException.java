package app_programming_development.Class.exceptions.conflict;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class AlreadyPaidException extends DomainException {
    public AlreadyPaidException() {
        super(HttpStatus.CONFLICT, "이미 결제된 강의입니다.");
    }
}
