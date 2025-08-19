package gobov.roma.india.shoonya.repository;

import gobov.roma.india.shoonya.entity.QuoteEntity; // Исправлен импорт
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {
}