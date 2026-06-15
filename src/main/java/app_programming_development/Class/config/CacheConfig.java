package app_programming_development.Class.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String LECTURES = "lectures";
    public static final String LECTURE_DETAIL = "lectureDetail";
    public static final String CERTIFICATES = "certificates";
    public static final String PROBLEMS = "problems";
    public static final String MOCK_EXAMS = "mockExams";
    public static final String REVIEWS = "reviews";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                LECTURES, LECTURE_DETAIL, CERTIFICATES, PROBLEMS, MOCK_EXAMS, REVIEWS
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }
}
