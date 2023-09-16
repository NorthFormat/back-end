package ru.bebriki.northformat.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bebriki.northformat.entities.File;

public interface FileRepository extends JpaRepository<File, Long> {
}
