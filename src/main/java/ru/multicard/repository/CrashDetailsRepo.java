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

    @Query("select c from CrashDetails c where c.id in ?1")
    List<CrashDetails> findAllByIds(List<Long> ids);

    /*@Query(nativeQuery = true, value = "select * from crash_details c where c.reason in " +
            "(select reason from (select count(c_2.reason) as v_count, c_2.reason" +
            " from crash_details c_2 where c_2.id in ?1 group by c_2.reason" +
            " order by v_count desc limit 3) as top_3) order by c.reason;")
    List<CrashDetails> findTop3ByReasonAndIdIn(List<Long> ids);

    @Query(nativeQuery = true, value = "select * from crash_details c_1 where c_1.id in ?1 " +
            "order by (c_1.end_date - c_1.begin_date) desc limit 3")
    List<CrashDetails> findTop3ByTimeAndIdIn(List<Long> ids);*/
}

