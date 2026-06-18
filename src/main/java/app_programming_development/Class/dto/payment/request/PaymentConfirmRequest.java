package app_programming_development.Class.dto.payment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequest {
    @NotBlank
    private String orderId;
    @NotBlank
    private String tid;
    @NotNull
    private Integer amount;
}
