package app_programming_development.Class.certificate.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.dto.certificate.request.CertificateRequest;
import app_programming_development.Class.dto.certificate.response.CertificateResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public CertificateResponse createCertificate(CertificateRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = Certificates.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        certificateRepository.save(certificate);

        return CertificateResponse.from(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getCertificates() {
        return certificateRepository.findAll()
                .stream()
                .map(CertificateResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> searchCertificates(String keyword) {
        return certificateRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(CertificateResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificateResponse updateCertificate(Long id, CertificateRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificate.setName(request.getName());
        certificate.setDescription(request.getDescription());

        return CertificateResponse.from(certificate);
    }

    @Transactional
    public void deleteCertificate(Long id) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificateRepository.delete(certificate);
    }
}
