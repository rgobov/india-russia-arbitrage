package gobov.roma.russia.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LevelDTO {
    private BigDecimal price;
    private long volume;
}