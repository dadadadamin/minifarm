package kr.ac.hansung.cse.minifarm.repository;

import kr.ac.hansung.cse.minifarm.entity.UserPlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPlantRepository extends JpaRepository<UserPlant, Long> {
    //List<UserPlant> findByUserId(Long id, Long userId);
    List<UserPlant> findByUserId(@Param("userId") Long userId);
}
