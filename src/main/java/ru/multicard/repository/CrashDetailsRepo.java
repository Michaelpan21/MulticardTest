package ru.multicard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.multicard.entity.CrashDetails;

public interface CrashDetailsRepo extends JpaRepository<CrashDetails, Long> {

}
