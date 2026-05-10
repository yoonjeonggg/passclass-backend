package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class ProblemNotFoundException extends DomainException {
    public ProblemNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 문제를 찾을 수 없습니다.");
    }
}
