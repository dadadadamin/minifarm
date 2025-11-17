package kr.ac.hansung.cse.minifarm.service;

import kr.ac.hansung.cse.minifarm.dto.diary.*;
import kr.ac.hansung.cse.minifarm.entity.Diary;
import kr.ac.hansung.cse.minifarm.entity.UserPlant;
import kr.ac.hansung.cse.minifarm.repository.DiaryRepository;
import kr.ac.hansung.cse.minifarm.repository.UserPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserPlantRepository userPlantRepository;

    // 간단한 로컬 저장 경로 (나중에 S3 등으로 교체 필요)
    private final String uploadDir = "uploads/diary";

    // ====== 달력 조회 ======

    @Transactional(readOnly = true)
    public DiaryCalendarResponse getCalendar(Long userPlantId, int year, int month) {
        UserPlant userPlant = getUserPlantOrThrow(userPlantId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        // 해당 월의 다이어리들
        List<Diary> diaries = diaryRepository
                .findAllByUserPlantIdAndDiaryDateBetweenOrderByDiaryDateAsc(userPlantId, start, end);

        Map<LocalDate, Diary> diaryByDate = diaries.stream()
                .collect(Collectors.toMap(Diary::getDiaryDate, d -> d));

        // 달력 day 리스트 생성
        List<DiaryCalendarDayDto> days = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            Diary diary = diaryByDate.get(cursor);

            days.add(DiaryCalendarDayDto.builder()
                    .date(cursor)
                    .hasDiary(diary != null)
                    .diaryId(diary != null ? diary.getId() : null)
                    .thumbnailUrl(diary != null ? diary.getImageUrl() : null)
                    .build());

            cursor = cursor.plusDays(1);
        }

        LocalDate plantedAt = userPlant.getStartedAt(); // 필드 이름은 실제 UserPlant에 맞게 수정
        long daysSincePlanted = 0;
        if (plantedAt != null) {
            // 첫날 포함할지 여부는 UI에서 맞게 조정
            daysSincePlanted = ChronoUnit.DAYS.between(plantedAt, LocalDate.now()) + 1;
        }

        long photoCount = diaryRepository.countByUserPlantIdAndImageUrlIsNotNull(userPlantId);

        return DiaryCalendarResponse.builder()
                .userPlantId(userPlantId)
                .plantNickname(userPlant.getNickname())
                .plantName(userPlant.getPlantInfo() != null ? userPlant.getPlantInfo().getName() : null)
                .firstPlantedDate(plantedAt)
                .daysSincePlanted(daysSincePlanted)
                .photoCount(photoCount)
                .year(year)
                .month(month)
                .days(days)
                .build();
    }

    // ====== 특정 날짜 다이어리 조회 ======

    @Transactional(readOnly = true)
    public DiaryResponse getDiaryByDate(Long userPlantId, LocalDate date) {
        Diary diary = diaryRepository.findByUserPlantIdAndDiaryDate(userPlantId, date)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "해당 날짜의 다이어리가 없습니다."));

        return toDiaryResponse(diary);
    }

    // ====== 다이어리 생성 ======

    public DiaryResponse createDiary(Long userPlantId,
                                     LocalDate diaryDate,
                                     String content,
                                     MultipartFile image) {

        UserPlant userPlant = getUserPlantOrThrow(userPlantId);

        // 이미 같은 날짜에 있으면 409
        diaryRepository.findByUserPlantIdAndDiaryDate(userPlantId, diaryDate)
                .ifPresent(d -> {
                    throw new ResponseStatusException(CONFLICT, "해당 날짜에는 이미 다이어리가 존재합니다.");
                });

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = storeImage(image);
        }

        Diary diary = Diary.builder()
                .userPlant(userPlant)
                .diaryDate(diaryDate)
                .content(content)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .build();

        Diary saved = diaryRepository.save(diary);
        return toDiaryResponse(saved);
    }

    // ====== 다이어리 수정 ======

    public DiaryResponse updateDiary(Long diaryId,
                                     String content,
                                     MultipartFile image) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "다이어리를 찾을 수 없습니다."));

        if (content != null) {
            diary.setContent(content);
        }

        if (image != null && !image.isEmpty()) {
            String imageUrl = storeImage(image);
            diary.setImageUrl(imageUrl);
        }

        diary.setUpdatedAt(LocalDateTime.now());

        return toDiaryResponse(diary);
    }

    // ====== 다이어리 삭제 ======

    public void deleteDiary(Long diaryId) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "다이어리를 찾을 수 없습니다."));
        diaryRepository.delete(diary);
    }

    // ====== 타임라인 조회 ======

    @Transactional(readOnly = true)
    public DiaryTimelineResponse getTimeline(Long userPlantId) {
        UserPlant userPlant = getUserPlantOrThrow(userPlantId);

        List<Diary> diaries = diaryRepository.findAllByUserPlantIdOrderByDiaryDateAsc(userPlantId);

        List<DiaryTimelineItemDto> items = diaries.stream()
                .filter(d -> d.getImageUrl() != null) // 타임라인은 사진 있는 것만 보고싶으면 유지, 아니면 제거
                .map(d -> DiaryTimelineItemDto.builder()
                        .id(d.getId())
                        .diaryDate(d.getDiaryDate())
                        .imageUrl(d.getImageUrl())
                        .content(d.getContent())
                        .build())
                .toList();

        return DiaryTimelineResponse.builder()
                .userPlantId(userPlantId)
                .plantNickname(userPlant.getNickname())
                .plantName(userPlant.getPlantInfo() != null ? userPlant.getPlantInfo().getName() : null)
                .items(items)
                .build();
    }

    // ====== 내부 유틸 ======

    private UserPlant getUserPlantOrThrow(Long userPlantId) {
        return userPlantRepository.findById(userPlantId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "userPlant를 찾을 수 없습니다."));
    }

    private DiaryResponse toDiaryResponse(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .userPlantId(diary.getUserPlant().getId())
                .diaryDate(diary.getDiaryDate())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    // 아주 단순한 로컬 파일 저장 (개발 단계용)- 추후 s3탑재시 변경
    private String storeImage(MultipartFile file) {
        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String originalFilename = Optional.ofNullable(file.getOriginalFilename()).orElse("diary");
            String ext = "";
            int dot = originalFilename.lastIndexOf('.');
            if (dot != -1) {
                ext = originalFilename.substring(dot);
            }

            String filename = UUID.randomUUID() + ext;
            Path filePath = dirPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            // 추후 정적 리소스 매핑 혹은 S3 URL 사용 예정
            return "/media/diary/" + filename;

        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "이미지 저장 중 오류가 발생했습니다.");
        }
    }
}
