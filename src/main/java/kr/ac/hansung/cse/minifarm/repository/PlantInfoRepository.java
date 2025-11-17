package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.PlantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantInfoRepository extends JpaRepository<PlantInfo, Long> {
}
