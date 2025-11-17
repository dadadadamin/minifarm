package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 특정 식물 + 날짜의 다이어리 1건
    Optional<Diary> findByUserPlantIdAndDiaryDate(Long userPlantId, LocalDate diaryDate);

    // 월별 달력 조회용 (기간 내 다이어리들)
    List<Diary> findAllByUserPlantIdAndDiaryDateBetweenOrderByDiaryDateAsc(
            Long userPlantId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 사진이 있는 다이어리 개수
    long countByUserPlantIdAndImageUrlIsNotNull(Long userPlantId);

    // 타임라인(전체) 조회용
    List<Diary> findAllByUserPlantIdOrderByDiaryDateAsc(Long userPlantId);
}
