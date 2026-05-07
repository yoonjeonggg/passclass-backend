package app_programming_development.Class.file.repository;

import app_programming_development.Class.file.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Files, Long> {
}
