package app_programming_development.Class.problem.service;

import app_programming_development.Class.dto.wrongnote.request.UpdateWrongNoteMemoRequest;
import app_programming_development.Class.dto.wrongnote.response.WrongNoteResponse;
import app_programming_development.Class.exceptions.forbidden.NotWrongNoteOwnerException;
import app_programming_development.Class.exceptions.notFound.WrongNoteNotFoundException;
import app_programming_development.Class.problem.entity.WrongNotes;
import app_programming_development.Class.problem.repository.WrongNotesRepository;
import app_programming_development.Class.security.SecurityUtils;
import app_programming_development.Class.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongNoteService {

    private final WrongNotesRepository wrongNotesRepository;
    private final SecurityUtils securityUtils;

    public List<WrongNoteResponse> getMyWrongNotes() {
        Users user = securityUtils.getCurrentUser();
        return wrongNotesRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(WrongNoteResponse::from)
                .toList();
    }

    @Transactional
    public void deleteWrongNote(Long wrongNoteId) {
        Users user = securityUtils.getCurrentUser();

        WrongNotes note = wrongNotesRepository.findById(wrongNoteId)
                .orElseThrow(WrongNoteNotFoundException::new);

        if (!note.getUser().getId().equals(user.getId())) {
            throw new NotWrongNoteOwnerException();
        }

        wrongNotesRepository.delete(note);
        log.info("WrongNote deleted: wrongNoteId={}, userId={}", wrongNoteId, user.getId());
    }

    @Transactional
    public WrongNoteResponse toggleFavorite(Long wrongNoteId) {
        Users user = securityUtils.getCurrentUser();
        WrongNotes note = wrongNotesRepository.findById(wrongNoteId)
                .orElseThrow(WrongNoteNotFoundException::new);
        if (!note.getUser().getId().equals(user.getId())) {
            throw new NotWrongNoteOwnerException();
        }
        note.setFavorite(!note.isFavorite());
        wrongNotesRepository.save(note);
        log.info("WrongNote favorite toggled: wrongNoteId={}, isFavorite={}", wrongNoteId, note.isFavorite());
        return WrongNoteResponse.from(note);
    }

    public List<WrongNoteResponse> getMyFavoriteWrongNotes() {
        Users user = securityUtils.getCurrentUser();
        return wrongNotesRepository.findByUser_IdAndIsFavoriteOrderByCreatedAtDesc(user.getId(), true)
                .stream()
                .map(WrongNoteResponse::from)
                .toList();
    }

    @Transactional
    public WrongNoteResponse updateMemo(Long wrongNoteId, UpdateWrongNoteMemoRequest request) {
        Users user = securityUtils.getCurrentUser();
        WrongNotes note = wrongNotesRepository.findById(wrongNoteId)
                .orElseThrow(WrongNoteNotFoundException::new);
        if (!note.getUser().getId().equals(user.getId())) {
            throw new NotWrongNoteOwnerException();
        }
        note.setMemo(request.getMemo());
        wrongNotesRepository.save(note);
        log.info("WrongNote memo updated: wrongNoteId={}", wrongNoteId);
        return WrongNoteResponse.from(note);
    }
}
