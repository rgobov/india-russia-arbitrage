package gobov.roma.russia.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderBookDTO {
    private String symbol;
    private String exchange;
    private LocalDateTime timestamp;
    private List<LevelDTO> bids;
    private List<LevelDTO> asks;
}