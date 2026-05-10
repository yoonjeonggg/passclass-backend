package app_programming_development.Class.mockexam.service;

import app_programming_development.Class.certificate.entity.Certificates;
import app_programming_development.Class.certificate.repository.CertificateRepository;
import app_programming_development.Class.dto.mockexam.request.MockExamAddQuestionRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamCreateRequest;
import app_programming_development.Class.dto.mockexam.request.MockExamSubmitRequest;
import app_programming_development.Class.dto.mockexam.response.*;
import app_programming_development.Class.enums.UserRole;
import app_programming_development.Class.exceptions.forbidden.TeacherRoleRequiredException;
import app_programming_development.Class.exceptions.notFound.CertificateNotFoundException;
import app_programming_development.Class.exceptions.notFound.MockExamNotFoundException;
import app_programming_development.Class.exceptions.notFound.ProblemNotFoundException;
import app_programming_development.Class.mockexam.entity.MockExamQuestions;
import app_programming_development.Class.mockexam.entity.MockExamResults;
import app_programming_development.Class.mockexam.entity.MockExams;
import app_programming_development.Class.mockexam.repository.MockExamQuestionsRepository;
import app_programming_development.Class.mockexam.repository.MockExamRepository;
import app_programming_development.Class.mockexam.repository.MockExamResultsRepository;
import app_programming_development.Class.problem.entity.Problems;
import app_programming_development.Class.problem.repository.ProblemRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MockExamService {

    private final MockExamRepository mockExamRepository;
    private final MockExamQuestionsRepository mockExamQuestionsRepository;
    private final MockExamResultsRepository mockExamResultsRepository;
    private final CertificateRepository certificateRepository;
    private final ProblemRepository problemRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public MockExamCreateResponse createMockExam(MockExamCreateRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        Certificates certificate = certificateRepository.findById(request.getCertificateId())
                .orElseThrow(CertificateNotFoundException::new);

        MockExams exam = MockExams.builder()
                .certificates(certificate)
                .creator(user)
                .title(request.getTitle())
                .timeLimit(0)
                .build();

        mockExamRepository.save(exam);
        log.info("MockExam created: examId={}, creatorId={}", exam.getId(), user.getId());
        return MockExamCreateResponse.from(exam);
    }

    public List<MockExamListResponse> getMockExams(Long certificateId) {
        return mockExamRepository.findByCertificates_IdOrderByCreatedAtDesc(certificateId)
                .stream()
                .map(MockExamListResponse::from)
                .toList();
    }

    public MockExamQuestionsResponse getMockExamQuestions(Long mockExamId) {
        MockExams exam = mockExamRepository.findById(mockExamId)
                .orElseThrow(MockExamNotFoundException::new);

        List<MockExamQuestionDto> questions = mockExamQuestionsRepository.findByMockExams_Id(mockExamId)
                .stream()
                .map(q -> MockExamQuestionDto.from(q.getProblems()))
                .toList();

        return MockExamQuestionsResponse.builder()
                .mockExamId(exam.getId())
                .title(exam.getTitle())
                .questions(questions)
                .build();
    }

    @Transactional
    public MockExamSubmitResponse submitMockExam(Long mockExamId, MockExamSubmitRequest request) {
        Users user = securityUtils.getCurrentUser();

        MockExams exam = mockExamRepository.findById(mockExamId)
                .orElseThrow(MockExamNotFoundException::new);

        Map<Long, Problems> examProblems = mockExamQuestionsRepository.findByMockExams_Id(mockExamId)
                .stream()
                .collect(Collectors.toMap(q -> q.getProblems().getId(), MockExamQuestions::getProblems));

        mockExamResultsRepository.deleteByUser_IdAndMockExams_Id(user.getId(), mockExamId);

        List<MockExamResultItemDto> resultItems = request.getAnswers().stream().map(answer -> {
            Problems problem = examProblems.get(answer.getProblemId());
            if (problem == null) throw new ProblemNotFoundException();

            boolean correct = problem.getCorrectAnswer() == answer.getSelectedAnswer();

            mockExamResultsRepository.save(MockExamResults.builder()
                    .user(user)
                    .mockExams(exam)
                    .problems(problem)
                    .selectedAnswer(answer.getSelectedAnswer())
                    .isCorrect(correct)
                    .build());

            return MockExamResultItemDto.builder()
                    .problemId(problem.getId())
                    .correct(correct)
                    .build();
        }).toList();

        int correct = (int) resultItems.stream().filter(MockExamResultItemDto::isCorrect).count();
        int score = resultItems.isEmpty() ? 0 : (int) Math.round((double) correct / resultItems.size() * 100);

        log.info("MockExam submitted: examId={}, userId={}, score={}", mockExamId, user.getId(), score);
        return MockExamSubmitResponse.builder()
                .score(score)
                .results(resultItems)
                .build();
    }

    public MockExamSubmitResponse getMockExamResults(Long mockExamId) {
        Users user = securityUtils.getCurrentUser();

        mockExamRepository.findById(mockExamId).orElseThrow(MockExamNotFoundException::new);

        List<MockExamResults> results = mockExamResultsRepository.findByUser_IdAndMockExams_Id(user.getId(), mockExamId);

        List<MockExamResultItemDto> resultItems = results.stream()
                .map(r -> MockExamResultItemDto.builder()
                        .problemId(r.getProblems().getId())
                        .correct(r.isCorrect())
                        .build())
                .toList();

        int correct = (int) resultItems.stream().filter(MockExamResultItemDto::isCorrect).count();
        int score = resultItems.isEmpty() ? 0 : (int) Math.round((double) correct / resultItems.size() * 100);

        return MockExamSubmitResponse.builder()
                .score(score)
                .results(resultItems)
                .build();
    }

    @Transactional
    public void deleteMockExam(Long mockExamId) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        MockExams exam = mockExamRepository.findById(mockExamId)
                .orElseThrow(MockExamNotFoundException::new);

        mockExamRepository.delete(exam);
        log.info("MockExam deleted: examId={}, deletedBy={}", mockExamId, user.getId());
    }

    @Transactional
    public void addQuestion(Long mockExamId, MockExamAddQuestionRequest request) {
        Users user = securityUtils.getCurrentUser();
        if (user.getRole() == UserRole.USER) {
            throw new TeacherRoleRequiredException();
        }

        MockExams exam = mockExamRepository.findById(mockExamId)
                .orElseThrow(MockExamNotFoundException::new);

        Problems problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(ProblemNotFoundException::new);

        if (mockExamQuestionsRepository.existsByMockExams_IdAndProblems_Id(mockExamId, request.getProblemId())) {
            return;
        }

        mockExamQuestionsRepository.save(MockExamQuestions.builder()
                .mockExams(exam)
                .problems(problem)
                .build());

        log.info("MockExam question added: examId={}, problemId={}", mockExamId, request.getProblemId());
    }
}
