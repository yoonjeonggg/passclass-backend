package app_programming_development.Class.exceptions.badRequest;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends DomainException {
    public InvalidVerificationCodeException() {
        super(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 인증 코드입니다.");
    }
}
