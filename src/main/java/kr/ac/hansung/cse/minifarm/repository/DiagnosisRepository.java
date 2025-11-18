package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
}
