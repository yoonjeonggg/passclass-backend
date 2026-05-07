package app_programming_development.Class.dto.certificate.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateInfo {
    private Long id;
    private String name;
}
