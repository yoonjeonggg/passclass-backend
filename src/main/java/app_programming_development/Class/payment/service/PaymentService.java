package app_programming_development.Class.payment.service;

import app_programming_development.Class.dto.payment.request.PaymentCancelRequest;
import app_programming_development.Class.dto.payment.request.PaymentConfirmRequest;
import app_programming_development.Class.dto.payment.response.PaymentPrepareResponse;
import app_programming_development.Class.dto.payment.response.PaymentResponse;
import app_programming_development.Class.enrollment.entity.Enrollments;
import app_programming_development.Class.enrollment.repository.EnrollmentRepository;
import app_programming_development.Class.enums.PaymentStatus;
import app_programming_development.Class.exceptions.badRequest.PaymentFailedException;
import app_programming_development.Class.exceptions.conflict.AlreadyEnrolledException;
import app_programming_development.Class.exceptions.conflict.AlreadyPaidException;
import app_programming_development.Class.exceptions.forbidden.NotPaymentOwnerException;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.PaymentNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.logging.AuditLog;
import app_programming_development.Class.payment.entity.Payments;
import app_programming_development.Class.payment.repository.PaymentRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SecurityUtils securityUtils;
    private final RestTemplate restTemplate;

    @Value("${nicepay.client-key}")
    private String clientKey;

    @Value("${nicepay.secret-key}")
    private String secretKey;

    @Value("${nicepay.api-url}")
    private String apiUrl;

    @Transactional
    public PaymentPrepareResponse preparePayment(Long lectureId) {
        Users currentUser = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId)
                .orElseThrow(LectureNotFoundException::new);

        if (enrollmentRepository.existsByUserIdAndLecturesId(currentUser.getId(), lectureId)) {
            throw new AlreadyEnrolledException();
        }

        if (paymentRepository.existsByUserIdAndLectureIdAndStatusIn(
                currentUser.getId(), lectureId, List.of(PaymentStatus.COMPLETED))) {
            throw new AlreadyPaidException();
        }

        // PENDING이 이미 있으면 기존 orderId 재사용 (결제창 재오픈)
        Payments payment = paymentRepository
                .findByUserIdAndLectureIdAndStatus(currentUser.getId(), lectureId, PaymentStatus.PENDING)
                .orElseGet(() -> {
                    String newOrderId = "ORDER-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
                    return paymentRepository.save(Payments.builder()
                            .user(currentUser)
                            .lecture(lecture)
                            .orderId(newOrderId)
                            .amount(lecture.getPrice())
                            .build());
                });

        return PaymentPrepareResponse.builder()
                .orderId(payment.getOrderId())
                .clientKey(clientKey)
                .amount(lecture.getPrice())
                .goodsName(lecture.getTitle())
                .buyerName(currentUser.getNickname())
                .buyerEmail(currentUser.getEmail())
                .build();
    }

    @AuditLog(action = "PAYMENT_CONFIRM")
    @Transactional
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        Payments payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(PaymentNotFoundException::new);

        if (payment.getAmount() != request.getAmount()) {
            payment.fail();
            throw new PaymentFailedException("결제 금액이 일치하지 않습니다.");
        }

        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString((clientKey + ":" + secretKey).getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        Map<String, Object> body = Map.of("amount", request.getAmount());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/v1/payments/" + request.getTid(),
                    HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            if (responseBody == null || !"0000".equals(responseBody.get("resultCode"))) {
                payment.fail();
                String msg = responseBody != null ? (String) responseBody.get("resultMsg") : "알 수 없는 오류";
                throw new PaymentFailedException("NICE 결제 승인 실패: " + msg);
            }

            payment.complete(request.getTid());

            if (!enrollmentRepository.existsByUserIdAndLecturesId(
                    payment.getUser().getId(), payment.getLecture().getId())) {
                Enrollments enrollment = Enrollments.builder()
                        .user(payment.getUser())
                        .lectures(payment.getLecture())
                        .build();
                enrollmentRepository.save(enrollment);
            }

            log.info("Payment confirmed: orderId={}, tid={}", request.getOrderId(), request.getTid());
            return PaymentResponse.from(payment);

        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            payment.fail();
            log.error("Payment confirm failed: orderId={}", request.getOrderId(), e);
            throw new PaymentFailedException("결제 처리 중 오류가 발생하였습니다.");
        }
    }

    @AuditLog(action = "PAYMENT_CANCEL")
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, PaymentCancelRequest request) {
        Users currentUser = securityUtils.getCurrentUser();
        Payments payment = paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);

        if (!payment.getUser().getId().equals(currentUser.getId())) {
            throw new NotPaymentOwnerException();
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentFailedException("취소 가능한 결제가 아닙니다.");
        }

        String authorization = "Basic " + Base64.getEncoder()
                .encodeToString((clientKey + ":" + secretKey).getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        Map<String, Object> body = Map.of(
                "amount", payment.getAmount(),
                "reason", request.getReason()
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/v1/payments/" + payment.getTid() + "/cancel",
                    HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            if (responseBody == null || !"0000".equals(responseBody.get("resultCode"))) {
                String msg = responseBody != null ? (String) responseBody.get("resultMsg") : "알 수 없는 오류";
                throw new PaymentFailedException("NICE 결제 취소 실패: " + msg);
            }

            payment.cancel(request.getReason());
            enrollmentRepository.findByUserIdAndLecturesId(
                    payment.getUser().getId(), payment.getLecture().getId())
                    .ifPresent(enrollmentRepository::delete);

            log.info("Payment cancelled: paymentId={}", paymentId);
            return PaymentResponse.from(payment);

        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Payment cancel failed: paymentId={}", paymentId, e);
            throw new PaymentFailedException("결제 취소 처리 중 오류가 발생하였습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getMyPayments(int page, int size) {
        Users currentUser = securityUtils.getCurrentUser();
        return paymentRepository.findByUserId(currentUser.getId(), PageRequest.of(page, size))
                .map(PaymentResponse::from);
    }
}
