package gobov.roma.russia.repository;

import gobov.roma.russia.entity.OrderBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderBookRepository extends JpaRepository<OrderBook, Long> {

    Optional<OrderBook> findTopBySymbolOrderByTimestampDesc(String symbol);

    List<OrderBook> findBySymbolAndTimestampBetween(
            String symbol,
            LocalDateTime start,
            LocalDateTime end
    );

    // Новый метод для поиска последних стаканов по списку символов
    @Query("SELECT ob FROM OrderBook ob WHERE ob.id IN (" +
            "SELECT MAX(o.id) FROM OrderBook o " +
            "WHERE o.symbol IN :symbols " +
            "GROUP BY o.symbol)")
    List<OrderBook> findLatestBySymbols(@Param("symbols") List<String> symbols);
}