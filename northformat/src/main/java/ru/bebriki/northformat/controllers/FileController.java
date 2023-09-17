package ru.bebriki.northformat.controllers;

import jakarta.transaction.Transactional;
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

    @Transactional
    @PostMapping("/overwriting/{id}")
    public ResponseEntity<String> overwriting(@RequestParam("text") String text, @PathVariable Long id) throws FileNotFoundException{
        try{
            File fileEntity = fileRepository.findById(id).orElseThrow(
                    ()-> new FileNotFoundException("There is no file with id:" + id)
            );
            byte[] newText = text.getBytes();
            fileEntity.setData(newText);
            fileRepository.save(fileEntity);
            return ResponseEntity.ok("File overwriting successfully");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to overwriting file");
        }
    }

    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> work(@RequestParam("text") String text, @PathVariable Long id) throws FileNotFoundException{
        try{
            overwriting(text,id);
            readFile(id);
            Long time = (System.currentTimeMillis()/1000)/60;
            while((System.currentTimeMillis()/1000)/60-time<60) continue;
            File fileEntity = fileRepository.findById(id).orElseThrow(
                    ()-> new FileNotFoundException("There is no file with id:" + id)
            );
            fileRepository.delete(fileEntity);
            return ResponseEntity.ok("File delete successfully");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete file");
        }
    }
}