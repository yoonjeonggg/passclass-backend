package app_programming_development.Class.dto.notification.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UnreadCountResponse {
    private Long unreadCount;
}
