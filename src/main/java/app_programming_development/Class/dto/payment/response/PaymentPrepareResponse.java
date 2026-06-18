package app_programming_development.Class.dto.payment.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentPrepareResponse {
    private String orderId;
    private String clientKey;
    private int amount;
    private String goodsName;
    private String buyerName;
    private String buyerEmail;
}
