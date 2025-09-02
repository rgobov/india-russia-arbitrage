package gobov.roma.india.shoonya.controllers;

import com.fasterxml.jackson.annotation.JsonProperty;
import gobov.roma.india.shoonya.service.QuoteService; // Исправлен импорт
import gobov.roma.reserch.TimeSliceDTO;
import gobov.roma.reserch.TimeSliceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/")
public class QuoteController {

    private final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    private final QuoteService quoteService;

    private final TimeSliceService timeSliceService;

    public QuoteController(QuoteService quoteService, TimeSliceDTO timeSliceDTO, TimeSliceService timeSliceService) {
        this.quoteService = quoteService;
        this.timeSliceService = timeSliceService;
    }

    @PostMapping("/shoonya")
    public ResponseEntity<String> receiveQuotes(@RequestBody List<Quote> quotes) {
        Instant start = Instant.now();
        quoteService.saveToMap(quotes);
        int savedCount = quoteService.saveQuotes(quotes);
        timeSliceService.createSnapshot();
        Instant end = Instant.now();

        long durationMs = end.toEpochMilli() - start.toEpochMilli();
        logger.info("Обработано {} котировок за {} мс", quotes.size(), durationMs);

        return ResponseEntity.ok("Received and saved " + savedCount + " quotes");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Server is running");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    // DTO для передачи данных
    public static class Quote {
        private String exchange;
        private String symbol;
        private String expDate;
        private BigDecimal strikePrice;
        @JsonProperty("OpType")
        private String opType;
        private BigDecimal bidPrice;
        @JsonProperty("BidQty")
        private Long bidQty;
        private BigDecimal askPrice;
        @JsonProperty("AskQty")
        private Long askQty;

        // Геттеры и сеттеры
        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getExpDate() {
            return expDate;
        }

        public void setExpDate(String expDate) {
            this.expDate = expDate;
        }

        public BigDecimal getStrikePrice() {
            return strikePrice;
        }

        public void setStrikePrice(BigDecimal strikePrice) {
            this.strikePrice = strikePrice;
        }

        public String getOpType() {
            return opType;
        }

        public void setOpType(String opType) {
            this.opType = opType;
        }

        public BigDecimal getBidPrice() {
            return bidPrice;
        }

        public void setBidPrice(BigDecimal bidPrice) {
            this.bidPrice = bidPrice;
        }

        public Long getBidQty() {
            return bidQty;
        }

        public void setBidQty(Long bidQty) {
            this.bidQty = bidQty;
        }

        public BigDecimal getAskPrice() {
            return askPrice;
        }

        public void setAskPrice(BigDecimal askPrice) {
            this.askPrice = askPrice;
        }

        public Long getAskQty() {
            return askQty;
        }

        public void setAskQty(Long askQty) {
            this.askQty = askQty;
        }

        @Override
        public String toString() {
            return "Quote{" +
                    "exchange='" + exchange + '\'' +
                    ", symbol='" + symbol + '\'' +
                    '}';
        }
    }
}