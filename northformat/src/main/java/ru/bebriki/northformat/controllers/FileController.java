package ru.bebriki.northformat.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.bebriki.northformat.entities.File;
import ru.bebriki.northformat.errors.FileNotFoundException;
import ru.bebriki.northformat.repositories.FileRepository;

import java.io.IOException;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileRepository fileRepository;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            File fileEntity = new File();
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setData(file.getBytes());
            fileRepository.save(fileEntity);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
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

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws FileNotFoundException {

        File fileEntity = fileRepository.findById(id).orElseThrow(
                () -> new FileNotFoundException("There is no file with id: " + id)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileEntity.getFileName()).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(fileEntity.getData()));
    }

}