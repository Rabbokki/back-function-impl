package com.backfunctionimpl.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;  // 생성자 주입 방식 사용

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadFile(@RequestPart(value = "file", required = false) MultipartFile image,
                                             @RequestPart(value = "content") String content) {
        System.out.println(content);
        if (image == null) {
            return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(s3Service.uploadFile(image), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        s3Service.deleteFile(fileName);
        return ResponseEntity.ok("File deleted successfully");
    }
}
