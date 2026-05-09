package se.hse.assistant_web_editor.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/// Service for uploading files to S3.
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${s3.endpoint}")
    private String endpoint;
    @Value("${s3.bucket}")
    private String bucket;
    @Value("${s3.access-key}")
    private String accessKey;
    @Value("${s3.secret-key}")
    private String secretKey;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("ru-central1"))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true) // <--- Добавлено для стабильной работы с Yandex
                .build();
    }

    public Map<String, String> storeToS3(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "unnamed_file";
        }

        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toUpperCase()
                : "FILE";

        String fileName = UUID.randomUUID().toString() + "_" + originalName;

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        double sizeKb = file.getSize() / 1024.0;
        String formattedSize = sizeKb > 1024
                ? String.format("%.1f Мб", sizeKb / 1024.0)
                : String.format("%.1f Кб", sizeKb);

        String cleanEndpoint = endpoint.replace("https://", "");
        String publicUrl = "https://" + bucket + "." + cleanEndpoint + "/" + fileName;

        return Map.of(
                "url", publicUrl,
                "name", originalName,
                "extension", extension,
                "size", formattedSize
        );
    }
}