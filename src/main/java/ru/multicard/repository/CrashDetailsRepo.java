package ru.multicard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.multicard.entity.CrashDetails;

import java.util.List;

public interface CrashDetailsRepo extends JpaRepository<CrashDetails, Long> {

    @Modifying
    @Query("delete from CrashDetails c where c.id in ?1")
    Integer deleteAllByIds(List<Long> ids);
}
