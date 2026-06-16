package app_programming_development.Class.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class DiscordEmbed {

    // 색상 상수
    public static final int COLOR_GREEN = 5763719;   // 새 강의
    public static final int COLOR_BLUE  = 3447003;   // 새 사용자
    public static final int COLOR_RED   = 15158332;  // 에러
    public static final int COLOR_GOLD  = 16766720;  // 이벤트

    private String title;
    private String description;
    private int color;
    private List<Field> fields;
    private String timestamp;

    @Getter
    @Builder
    public static class Field {
        private String name;
        private String value;
        @JsonProperty("inline")
        private boolean inline;
    }

    public static String nowTimestamp() {
        return OffsetDateTime.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /** Discord 웹훅에 전송할 최상위 payload */
    @Getter
    @Builder
    public static class Payload {
        private List<DiscordEmbed> embeds;

        public static Payload of(DiscordEmbed embed) {
            return Payload.builder().embeds(List.of(embed)).build();
        }
    }
}
