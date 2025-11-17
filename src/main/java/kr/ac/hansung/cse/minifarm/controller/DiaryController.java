package kr.ac.hansung.cse.minifarm.controller;

import kr.ac.hansung.cse.minifarm.dto.diary.*;
import kr.ac.hansung.cse.minifarm.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


//모바일 앱 diary를 위한 부분 추후 코드 변경 필요할수도 있음
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    //달력 화면용 데이터 조회, ex): GET /api/diary/calendar?userPlantId=1&year=2025&month=11
    @GetMapping("/calendar")
    public ResponseEntity<DiaryCalendarResponse> getCalendar(
            @RequestParam Long userPlantId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        DiaryCalendarResponse response = diaryService.getCalendar(userPlantId, year, month);
        return ResponseEntity.ok(response);
    }

    //특정 날짜의 다이어리 조회  예: GET /api/diary?userPlantId=1&date=2025-11-05
    @GetMapping
    public ResponseEntity<DiaryResponse> getDiaryByDate(
            @RequestParam Long userPlantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DiaryResponse response = diaryService.getDiaryByDate(userPlantId, date);
        return ResponseEntity.ok(response);
    }

    // 다이어리 생성
    //multipart/form-data로 호출(우선은)
    //userPlantId, diaryDate, content (text)
    //image (file, optional)
    /*
    예시 (Postman):
    - form-data
    - key: userPlantId (text)
    - key: diaryDate   (text, 2025-11-05)
    - key: content     (text)
    - key: image       (file)
    */
    @PostMapping
    public ResponseEntity<DiaryResponse> createDiary(
            @RequestParam Long userPlantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate diaryDate,
            @RequestParam(required = false) String content,
            @RequestPart(required = false) MultipartFile image
    ) {
        DiaryResponse response = diaryService.createDiary(userPlantId, diaryDate, content, image);
        return ResponseEntity.ok(response);
    }

    //다이어리 수정
    //내용 또는 사진 둘 중 하나만수정 가능
    @PutMapping("/{diaryId}")
    public ResponseEntity<DiaryResponse> updateDiary(
            @PathVariable Long diaryId,
            @RequestParam(required = false) String content,
            @RequestPart(required = false) MultipartFile image
    ) {
        DiaryResponse response = diaryService.updateDiary(diaryId, content, image);
        return ResponseEntity.ok(response);
    }

    //다이어리 삭제
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.noContent().build();
    }

    //타임캡슐 / 타임라인 뷰  예: GET /api/diary/timeline?userPlantId=1
    //현재 ui구성 확정되지 않아 수정 필요 예상
    @GetMapping("/timeline")
    public ResponseEntity<DiaryTimelineResponse> getTimeline(
            @RequestParam Long userPlantId
    ) {
        DiaryTimelineResponse response = diaryService.getTimeline(userPlantId);
        return ResponseEntity.ok(response);
    }
}
