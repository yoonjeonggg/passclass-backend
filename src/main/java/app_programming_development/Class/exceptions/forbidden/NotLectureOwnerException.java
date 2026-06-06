package app_programming_development.Class.exceptions.forbidden;

import app_programming_development.Class.exceptions.DomainException;
import org.springframework.http.HttpStatus;

public class NotLectureOwnerException extends DomainException {
    public NotLectureOwnerException() {
        super(HttpStatus.FORBIDDEN, "해당 강의의 강사만 수정/삭제할 수 있습니다.");
    }
}
