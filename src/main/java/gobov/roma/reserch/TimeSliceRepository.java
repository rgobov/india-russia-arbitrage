package gobov.roma.reserch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TimeSliceRepository extends JpaRepository<TimeSlice, Long> {
    // Примеры запросов для извлечения
    List<TimeSlice> findByTimestampAfter(Instant startTime);  // Срезы после даты
    TimeSlice findFirstByOrderByTimestampDesc();  // Последний срез
}