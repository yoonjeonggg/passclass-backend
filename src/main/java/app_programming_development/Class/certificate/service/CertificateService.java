package app_programming_development.Class.certificate.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.config.CacheConfig;
import app_programming_development.Class.dto.certificate.request.CertificateRequest;
import app_programming_development.Class.dto.certificate.response.CertificateResponse;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    @CacheEvict(value = CacheConfig.CERTIFICATES, allEntries = true)
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

        log.info("[ADMIN] Certificate created: certificateId={}, name={}, adminId={}",
                certificate.getId(), certificate.getName(), currentUser.getId());
        return CertificateResponse.from(certificate);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.CERTIFICATES, key = "'all'")
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
    @CacheEvict(value = CacheConfig.CERTIFICATES, allEntries = true)
    public CertificateResponse updateCertificate(Long id, CertificateRequest request) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificate.setName(request.getName());
        certificate.setDescription(request.getDescription());

        log.info("[ADMIN] Certificate updated: certificateId={}, adminId={}", id, currentUser.getId());
        return CertificateResponse.from(certificate);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.CERTIFICATES, allEntries = true)
    public void deleteCertificate(Long id) {
        Users currentUser = securityUtils.getCurrentUser();

        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            throw new AdminRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(id)
                .orElseThrow(CertificateNotFoundException::new);

        certificateRepository.delete(certificate);
        log.info("[ADMIN] Certificate deleted: certificateId={}, adminId={}", id, currentUser.getId());
    }
}
