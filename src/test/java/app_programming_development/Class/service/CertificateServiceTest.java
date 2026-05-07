package app_programming_development.Class.service;

import app_programming_development.Class.dto.certificate.request.CertificateRequest;
import app_programming_development.Class.dto.certificate.response.CertificateResponse;
import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.user.entity.Users;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.AdminRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.certificate.service.CertificateService;
import app_programming_development.Class.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private CertificateService certificateService;

    private Users admin;
    private Users user;
    private Certificates certificate;
    private CertificateRequest request;

    @BeforeEach
    void setUp() {
        admin = Users.builder()
                .email("admin@test.com")
                .password("password")
                .nickname("관리자")
                .role(UserRole.ADMIN)
                .build();

        user = Users.builder()
                .email("user@test.com")
                .password("password")
                .nickname("사용자")
                .role(UserRole.USER)
                .build();

        certificate = Certificates.builder()
                .name("정보처리기사")
                .description("국가기술자격증")
                .build();

        request = new CertificateRequest("정보처리기사", "국가기술자격증");
    }

    @Test
    @DisplayName("자격증 등록 - 성공")
    void createCertificate_성공() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.save(any())).willReturn(certificate);

        CertificateResponse result = certificateService.createCertificate(request);

        assertThat(result.getName()).isEqualTo("정보처리기사");
        then(certificateRepository).should().save(any());
    }

    @Test
    @DisplayName("자격증 등록 - 관리자가 아닌 경우 예외")
    void createCertificate_관리자아님_예외() {
        given(securityUtils.getCurrentUser()).willReturn(user);

        assertThatThrownBy(() -> certificateService.createCertificate(request))
                .isInstanceOf(AdminRoleRequiredException.class);
    }

    @Test
    @DisplayName("자격증 목록 조회 - 성공")
    void getCertificates_성공() {
        given(certificateRepository.findAll()).willReturn(List.of(certificate));

        List<CertificateResponse> result = certificateService.getCertificates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("정보처리기사");
    }

    @Test
    @DisplayName("자격증 검색 - 키워드로 검색 성공")
    void searchCertificates_성공() {
        given(certificateRepository.findByNameContainingIgnoreCase("정보"))
                .willReturn(List.of(certificate));

        List<CertificateResponse> result = certificateService.searchCertificates("정보");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("정보");
    }

    @Test
    @DisplayName("자격증 검색 - 결과 없을 때 빈 리스트")
    void searchCertificates_결과없음_빈리스트() {
        given(certificateRepository.findByNameContainingIgnoreCase("없는자격증"))
                .willReturn(List.of());

        List<CertificateResponse> result = certificateService.searchCertificates("없는자격증");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("자격증 수정 - 성공")
    void updateCertificate_성공() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));

        CertificateResponse result = certificateService.updateCertificate(1L, request);

        assertThat(result.getName()).isEqualTo("정보처리기사");
    }

    @Test
    @DisplayName("자격증 수정 - 존재하지 않는 자격증 예외")
    void updateCertificate_자격증없음_예외() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.updateCertificate(999L, request))
                .isInstanceOf(CertificateNotFoundException.class);
    }

    @Test
    @DisplayName("자격증 삭제 - 성공")
    void deleteCertificate_성공() {
        given(securityUtils.getCurrentUser()).willReturn(admin);
        given(certificateRepository.findById(1L)).willReturn(Optional.of(certificate));

        assertThatNoException().isThrownBy(() -> certificateService.deleteCertificate(1L));
        then(certificateRepository).should().delete(certificate);
    }
}
