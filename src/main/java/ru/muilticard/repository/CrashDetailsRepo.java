package ru.muilticard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.muilticard.entity.CrashDetails;

public interface CrashDetailsRepo extends JpaRepository<CrashDetails, Long> {

}
