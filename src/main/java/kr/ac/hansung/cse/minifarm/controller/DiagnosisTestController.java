package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.diagnosis.DiagnosisResponse;
import kr.ac.hansung.cse.minifarm.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;


//openai vision api사용한 workflow 위한 임시 테스트 파일 - api테스트 후 삭제 예정
@RestController
@RequiredArgsConstructor
public class DiagnosisTestController {

    private final DiagnosisService diagnosisService;

    /**
     * 로컬(static) 이미지로 진단 API 전체 플로우 테스트
     *
     * 예시 호출:
     * GET /api/diagnosis/test/local-image?userPlantId=1&filename=test-images/sample.jpg
     *
     * - filename 기준: src/main/resources/ 아래 경로
     *   → src/main/resources/static/test-images/sample.jpg 인 경우
     *      filename=test-images/sample.jpg (또는 static/test-images/...) 로 넘겨도 됨
     */
    @GetMapping("/api/diagnosis/test/local-image")
    public DiagnosisResponse testDiagnosisWithLocalImage(
            @RequestParam("userPlantId") Long userPlantId,
            @RequestParam(name = "filename", defaultValue = "static/test-images/sample.jpg") String filename
    ) throws IOException {

        // 1. 클래스패스에서 이미지 읽기
        Resource resource = new ClassPathResource(filename);

        if (!resource.exists()) {
            throw new IllegalArgumentException("해당 경로에 이미지가 없습니다: " + filename);
        }

        String originalFilename = resource.getFilename();
        String contentType = guessContentType(originalFilename);

        byte[] bytes;
        try (InputStream is = resource.getInputStream()) {
            bytes = is.readAllBytes();
        }

        // 2. byte[] -> MultipartFile 로 변환 (메모리 기반 구현)
        MultipartFile multipartFile =
                new InMemoryMultipartFile("image", originalFilename, contentType, bytes);

        // 3. 기존 모바일 진단 플로우 재사용
        return diagnosisService.analyzeFromMobile(
                userPlantId,
                multipartFile,
                "테스트용 로컬 이미지로 진단 요청"
        );
    }

    private String guessContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    /**
     * 간단한 메모리 기반 MultipartFile 구현
     * (spring-test 의 MockMultipartFile 대신 직접 구현)
     */
    private static class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String name,
                                     String originalFilename,
                                     String contentType,
                                     byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = (content != null ? content : new byte[0]);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public java.io.InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            java.nio.file.Files.write(dest.toPath(), content);
        }
    }


}
