package app_programming_development.Class.dto.payment.response;

import app_programming_development.Class.enums.PaymentStatus;
import app_programming_development.Class.payment.entity.Payments;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long paymentId;
    private Long lectureId;
    private String lectureTitle;
    private String orderId;
    private String tid;
    private int amount;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public static PaymentResponse from(Payments payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .lectureId(payment.getLecture().getId())
                .lectureTitle(payment.getLecture().getTitle())
                .orderId(payment.getOrderId())
                .tid(payment.getTid())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
