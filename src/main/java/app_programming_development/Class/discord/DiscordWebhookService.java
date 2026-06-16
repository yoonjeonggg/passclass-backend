package app_programming_development.Class.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordWebhookService {

    private final DiscordWebhookProperties webhookProperties;
    private final RestTemplateBuilder restTemplateBuilder;

    // ──────────────────────────────────────────────
    // Public API - 각 채널별 전송 메서드
    // ──────────────────────────────────────────────

    /** #new-lectures: 강의 등록 알림 */
    @Async
    public void sendNewLecture(String title, String instructorNickname, String category) {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("📚 새 강의가 등록되었습니다")
                .color(DiscordEmbed.COLOR_GREEN)
                .fields(List.of(
                        DiscordEmbed.Field.builder().name("강의명").value(title).inline(false).build(),
                        DiscordEmbed.Field.builder().name("강사").value(instructorNickname).inline(true).build(),
                        DiscordEmbed.Field.builder().name("카테고리").value(category != null ? category : "미분류").inline(true).build()
                ))
                .timestamp(DiscordEmbed.nowTimestamp())
                .build();
        send(webhookProperties.getNewLectures(), embed);
    }

    /** #new-users: 회원가입 알림 */
    @Async
    public void sendNewUser(String nickname, String role) {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("👤 새 사용자가 가입했습니다")
                .color(DiscordEmbed.COLOR_BLUE)
                .fields(List.of(
                        DiscordEmbed.Field.builder().name("닉네임").value(nickname).inline(true).build(),
                        DiscordEmbed.Field.builder().name("역할").value(role).inline(true).build()
                ))
                .timestamp(DiscordEmbed.nowTimestamp())
                .build();
        send(webhookProperties.getNewUsers(), embed);
    }

    /** #errors: 서버 에러 알림 */
    @Async
    public void sendError(String errorType, String message, String requestUri) {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("🚨 서버 에러 발생")
                .description("즉시 확인이 필요합니다.")
                .color(DiscordEmbed.COLOR_RED)
                .fields(List.of(
                        DiscordEmbed.Field.builder().name("에러 유형").value(errorType).inline(true).build(),
                        DiscordEmbed.Field.builder().name("요청 URI").value(requestUri != null ? requestUri : "unknown").inline(true).build(),
                        DiscordEmbed.Field.builder().name("메시지").value(truncate(message, 500)).inline(false).build()
                ))
                .timestamp(DiscordEmbed.nowTimestamp())
                .build();
        send(webhookProperties.getErrors(), embed);
    }

    /** #events: 수강신청 알림 */
    @Async
    public void sendEnrollment(String nickname, String lectureTitle) {
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("🎓 수강신청")
                .color(DiscordEmbed.COLOR_GOLD)
                .fields(List.of(
                        DiscordEmbed.Field.builder().name("수강생").value(nickname).inline(true).build(),
                        DiscordEmbed.Field.builder().name("강의").value(lectureTitle).inline(true).build()
                ))
                .timestamp(DiscordEmbed.nowTimestamp())
                .build();
        send(webhookProperties.getEvents(), embed);
    }

    /** #events: 리뷰 등록 알림 */
    @Async
    public void sendNewReview(String nickname, String lectureTitle, int rating) {
        String stars = "⭐".repeat(rating);
        DiscordEmbed embed = DiscordEmbed.builder()
                .title("✍️ 새 리뷰가 등록되었습니다")
                .color(DiscordEmbed.COLOR_GOLD)
                .fields(List.of(
                        DiscordEmbed.Field.builder().name("작성자").value(nickname).inline(true).build(),
                        DiscordEmbed.Field.builder().name("강의").value(lectureTitle).inline(true).build(),
                        DiscordEmbed.Field.builder().name("평점").value(stars + " (" + rating + "/5)").inline(false).build()
                ))
                .timestamp(DiscordEmbed.nowTimestamp())
                .build();
        send(webhookProperties.getEvents(), embed);
    }

    // ──────────────────────────────────────────────
    // Internal
    // ──────────────────────────────────────────────

    private void send(String webhookUrl, DiscordEmbed embed) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return; // 웹훅 URL 미설정 시 무시
        }
        try {
            RestTemplate restTemplate = restTemplateBuilder.build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            DiscordEmbed.Payload payload = DiscordEmbed.Payload.of(embed);
            HttpEntity<DiscordEmbed.Payload> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[Discord] 전송 실패: status={}", response.getStatusCode());
            }
        } catch (Exception e) {
            // Discord 장애가 서비스에 영향 주지 않도록 예외 흡수
            log.warn("[Discord] 웹훅 전송 중 오류: {}", e.getMessage());
        }
    }

    private String truncate(String text, int max) {
        if (text == null) return "null";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
