package app_programming_development.Class.discord;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "discord.webhooks")
public class DiscordWebhookProperties {

    /** #new-lectures 채널 웹훅 URL */
    private String newLectures = "";

    /** #new-users 채널 웹훅 URL */
    private String newUsers = "";

    /** #errors 채널 웹훅 URL */
    private String errors = "";

    /** #events 채널 웹훅 URL (수강신청, 리뷰 등) */
    private String events = "";
}
