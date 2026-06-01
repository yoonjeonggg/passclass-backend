package app_programming_development.Class.dto.lecture.response;

import app_programming_development.Class.dto.certificate.response.CertificateInfo;
import app_programming_development.Class.lecture.entity.Lectures;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class LectureListDto {
    private Long id;
    private String title;
    private String category;
    private String thumbnailUrl;
    private Double rating;
    private CertificateInfo certificate;
    private Long enrollmentCount;
    private Long likeCount;
    private String instructorNickname;

    public static LectureListDto from(Lectures lecture, Double rating) {
        return LectureListDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .category(lecture.getCategory())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .certificate(CertificateInfo.builder()
                        .id(lecture.getCertificates().getId())
                        .name(lecture.getCertificates().getName())
                        .build())
                .rating(rating)
                .enrollmentCount(0L)
                .likeCount(0L)
                .instructorNickname(lecture.getInstructor().getNickname())
                .build();
    }

    public static LectureListDto from(Lectures lecture, Double rating, Long enrollmentCount, Long likeCount) {
        return LectureListDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .category(lecture.getCategory())
                .thumbnailUrl(lecture.getThumbnailUrl())
                .certificate(CertificateInfo.builder()
                        .id(lecture.getCertificates().getId())
                        .name(lecture.getCertificates().getName())
                        .build())
                .rating(rating)
                .enrollmentCount(enrollmentCount)
                .likeCount(likeCount)
                .instructorNickname(lecture.getInstructor().getNickname())
                .build();
    }
}
