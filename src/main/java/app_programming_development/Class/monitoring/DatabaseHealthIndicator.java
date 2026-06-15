package app_programming_development.Class.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("database")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("status", "연결 정상")
                        .build();
            }
            return Health.down().withDetail("status", "쿼리 결과 이상").build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "연결 실패")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
