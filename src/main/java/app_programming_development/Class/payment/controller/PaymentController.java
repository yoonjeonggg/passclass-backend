package app_programming_development.Class.payment.controller;

import app_programming_development.Class.dto.payment.request.PaymentCancelRequest;
import app_programming_development.Class.dto.payment.request.PaymentConfirmRequest;
import app_programming_development.Class.dto.payment.response.PaymentPrepareResponse;
import app_programming_development.Class.dto.payment.response.PaymentResponse;
import app_programming_development.Class.global.ApiResponse;
import app_programming_development.Class.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "결제 관련 API (NICE Payment)")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/prepare/{lectureId}")
    @Operation(summary = "결제 준비", description = "NICE 결제창 호출 전 주문 정보를 생성합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "강의를 찾을 수 없습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 수강 또는 결제된 강의입니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(@PathVariable Long lectureId) {
        PaymentPrepareResponse result = paymentService.preparePayment(lectureId);
        return ResponseEntity.ok(ApiResponse.ok(result, "결제 준비가 완료되었습니다."));
    }

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "NICE 결제창 완료 후 서버 결제 승인을 처리합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "결제 승인 실패", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request) {
        PaymentResponse result = paymentService.confirmPayment(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "결제가 완료되었습니다."));
    }

    @PostMapping("/cancel/{paymentId}")
    @Operation(summary = "결제 취소", description = "완료된 결제를 취소합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "취소 처리 실패", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인이 필요합니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없습니다.", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없습니다.", content = @Content)
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request) {
        PaymentResponse result = paymentService.cancelPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "결제가 취소되었습니다."));
    }

    @GetMapping("/me")
    @Operation(summary = "내 결제 내역 조회", description = "내 결제 내역을 페이지네이션으로 조회합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 성공")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PaymentResponse> result = paymentService.getMyPayments(page, size);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }
}
