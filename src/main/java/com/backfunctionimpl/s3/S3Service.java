package com.backfunctionimpl.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    @Value("${application.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;  // 생성자 주입 방식으로 변경

    //가져온거
    public String uploadFile(MultipartFile file) {
        log.info("Uploading file: {}", file.getOriginalFilename());
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try (InputStream inputStream = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            log.info("Uploading to S3 bucket: {}, fileName: {}", bucketName, fileName);
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
            log.info("File uploaded to S3: {}", fileName);
        } catch (AmazonS3Exception e) {
            log.error("S3 upload failed with AmazonS3Exception: {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error uploading file to S3 due to IOException: {}", e.getMessage(), e);
            throw new RuntimeException("S3 업로드 실패: " + e.getMessage(), e);
        }
        String url = s3Client.getUrl(bucketName, fileName).toString();
        log.info("Generated S3 URL: {}", url);
        return url;
    }
//    public String uploadFile(MultipartFile file) {
//        File fileObj = convertMultiPartFileToFile(file);
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
//        fileObj.delete();
//        return s3Client.getUrl(bucketName, fileName).toString();
//    }

    public void deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
    }

    private File convertMultiPartFileToFile(MultipartFile file) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp", file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }
        } catch (IOException e) {
            log.error("Error converting multipartFile to file", e);
        }
        return tempFile;
    }
}
