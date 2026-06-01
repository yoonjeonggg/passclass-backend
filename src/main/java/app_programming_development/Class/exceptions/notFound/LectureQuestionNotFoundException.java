package app_programming_development.Class.exceptions.notFound;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class LectureQuestionNotFoundException extends DomainException {
    public LectureQuestionNotFoundException() {
        super(HttpStatus.NOT_FOUND, "질문을 찾을 수 없습니다.");
    }
}
