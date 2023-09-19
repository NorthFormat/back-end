package ru.bebriki.northformat.controllers;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bebriki.northformat.entities.File;
import ru.bebriki.northformat.errors.FileNotFoundException;
import ru.bebriki.northformat.repositories.FileRepository;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;


@EnableScheduling
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@CrossOrigin
public class FileController {

    private final FileRepository fileRepository;

    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<byte[]> uploadFile(@RequestParam("file") MultipartFile file) {
        try {

            File fileEntity = File.builder().fileName(file.getOriginalFilename())
                    .dateCreation(LocalDateTime.now(Clock.systemUTC()))
                    .data(file.getBytes())
                    .build();

            fileRepository.save(fileEntity);

            return readFile(fileEntity);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new byte[]{});
        }
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<byte[]> readFile(@PathVariable Long id) throws FileNotFoundException {

        File fileEntity = fileRepository.findById(id).orElseThrow(
                () -> new FileNotFoundException("There is no file with id: " + id)
        );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileEntity.getFileName())
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileEntity.getData());

    }

    private ResponseEntity<byte[]> readFile(File fileEntity) throws FileNotFoundException {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileEntity.getFileName())
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(fileEntity.getData());

    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws FileNotFoundException {

        File fileEntity = fileRepository.findById(id).orElseThrow(
                () -> new FileNotFoundException("There is no file with id: " + id)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                .filename(fileEntity.getFileName())
                .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(fileEntity.getData()));
    }

    @GetMapping("/getAll")
    public List<File> downloadFile() throws FileNotFoundException {

        List<File> files = fileRepository.findAll();
        for (File f : files) {
            System.out.println(f);
        }
        return files;
    }

    @Transactional
    @PostMapping("/overwriting/{id}")
    public ResponseEntity<String> overwriting(@RequestParam("text") String text, @PathVariable Long id) throws FileNotFoundException {
        try {
            File fileEntity = fileRepository.findById(id).orElseThrow(
                    () -> new FileNotFoundException("There is no file with id:" + id)
            );
            byte[] newText = text.getBytes();
            fileEntity.setData(newText);
            fileRepository.save(fileEntity);
            return ResponseEntity.ok("File overwriting successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to overwriting file");
        }
    }

    @Transactional
    @PutMapping("/work/{id}")
    public ResponseEntity<byte[]> work(@RequestParam("text") String text, @PathVariable Long id) throws FileNotFoundException {
        overwriting(text, id);
        return readFile(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) throws FileNotFoundException {
        try {
            File fileEntity = fileRepository.findById(id).orElseThrow(
                    () -> new FileNotFoundException("There is no file with id:" + id)
            );
            fileRepository.delete(fileEntity);
            return ResponseEntity.ok("File delete successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void deleteFilesByTime() throws FileNotFoundException {
        Clock clock = Clock.systemUTC();
        LocalDateTime time = LocalDateTime.now(clock);
        List<File> files = fileRepository.findAll();
        for (File f : files) {
            if (time.getHour() - f.getDateCreation().getHour() >= 1) delete(f.getId());
        }
    }
}