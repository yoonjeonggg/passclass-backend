package app_programming_development.Class.question.service;

import app_programming_development.Class.dto.question.request.LectureAnswerRequest;
import app_programming_development.Class.dto.question.request.LectureQuestionRequest;
import app_programming_development.Class.dto.question.response.LectureQuestionResponse;
import app_programming_development.Class.exceptions.notFound.LectureNotFoundException;
import app_programming_development.Class.exceptions.notFound.LectureQuestionNotFoundException;
import app_programming_development.Class.lecture.entity.Lectures;
import app_programming_development.Class.lecture.repository.LectureRepository;
import app_programming_development.Class.question.entity.LectureQuestions;
import app_programming_development.Class.question.repository.LectureQuestionRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureQuestionService {

    private final LectureQuestionRepository questionRepository;
    private final LectureRepository lectureRepository;
    private final SecurityUtils securityUtils;

    public List<LectureQuestionResponse> getQuestions(Long lectureId) {
        lectureRepository.findById(lectureId).orElseThrow(LectureNotFoundException::new);
        return questionRepository.findByLecture_IdOrderByCreatedAtDesc(lectureId)
                .stream()
                .map(LectureQuestionResponse::from)
                .toList();
    }

    @Transactional
    public LectureQuestionResponse askQuestion(Long lectureId, LectureQuestionRequest request) {
        Users user = securityUtils.getCurrentUser();
        Lectures lecture = lectureRepository.findById(lectureId).orElseThrow(LectureNotFoundException::new);

        LectureQuestions question = LectureQuestions.builder()
                .lecture(lecture)
                .user(user)
                .content(request.getContent())
                .build();

        questionRepository.save(question);
        log.info("Question asked: lectureId={}, userId={}", lectureId, user.getId());
        return LectureQuestionResponse.from(question);
    }

    @Transactional
    public LectureQuestionResponse answerQuestion(Long lectureId, Long questionId, LectureAnswerRequest request) {
        Users answerer = securityUtils.getCurrentUser();
        lectureRepository.findById(lectureId).orElseThrow(LectureNotFoundException::new);

        LectureQuestions question = questionRepository.findById(questionId)
                .orElseThrow(LectureQuestionNotFoundException::new);

        question.setAnswer(request.getAnswer());
        question.setAnswerer(answerer);
        question.setAnsweredAt(LocalDateTime.now());

        log.info("Question answered: questionId={}, answererId={}", questionId, answerer.getId());
        return LectureQuestionResponse.from(question);
    }
}
