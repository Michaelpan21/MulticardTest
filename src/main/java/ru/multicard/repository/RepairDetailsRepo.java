package ru.multicard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.multicard.entity.RepairDetails;

import java.util.List;

public interface RepairDetailsRepo extends JpaRepository<RepairDetails, Long> {

    @Modifying
    @Query("delete from RepairDetails r where r.id in ?1")
    Integer deleteAllByIds(List<Long> ids);

    @Query("select r from RepairDetails r where r.id in ?1")
    List<RepairDetails> findAllByIds(List<Long> ids);
}

