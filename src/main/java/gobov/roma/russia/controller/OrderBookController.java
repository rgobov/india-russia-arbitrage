package gobov.roma.russia.controller;

import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.repository.OrderBookRepository;
import gobov.roma.russia.service.OutOrderBookServise;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orderbooks")
public class OrderBookController {

    private final OrderBookRepository orderBookRepository;
    public final OutOrderBookServise outputOrderBookService;

    @Autowired
    public OrderBookController(OrderBookRepository orderBookRepository, OutOrderBookServise outputOrderBookService) {
        this.orderBookRepository = orderBookRepository;
        this.outputOrderBookService = outputOrderBookService;
    }

    @GetMapping("/latest")
    public ResponseEntity<List<OrderBookDTO>> getLatestOrderBooks(@RequestParam List<String> symbols) {

    return ResponseEntity.ok(outputOrderBookService.convertOrderBookToDTO(symbols));

    }

    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<OrderBook>> getHistoricalOrderBooks(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<OrderBook> orderBooks = orderBookRepository.findBySymbolAndTimestampBetween(symbol, start, end);
        return ResponseEntity.ok(orderBooks);
    }
}