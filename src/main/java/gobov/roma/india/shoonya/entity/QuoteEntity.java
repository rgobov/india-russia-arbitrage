package gobov.roma.india.shoonya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "indian_quotes")
public class QuoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exchange", nullable = false)
    private String exchange;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "exp_date")
    private String expDate;

    @Column(name = "strike_price", precision = 19, scale = 10)
    private BigDecimal strikePrice;

    @Column(name = "op_type")
    private String opType;

    @Column(name = "bid_price", precision = 19, scale = 10)
    private BigDecimal bidPrice;

    @Column(name = "bid_qty")
    private Long bidQty;

    @Column(name = "ask_price", precision = 19, scale = 10)
    private BigDecimal askPrice;

    @Column(name = "ask_qty")
    private Long askQty;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

//    @PrePersist
//    protected void onPersist() {
//        this.timestamp = Instant.now();
//    }

    // Конструкторы
    public QuoteEntity() {}

    public QuoteEntity(String exchange, String symbol, String expDate, BigDecimal strikePrice,
                       String opType, BigDecimal bidPrice, Long bidQty, BigDecimal askPrice, Long askQty) {
        this.exchange = exchange;
        this.symbol = symbol;
        this.expDate = expDate;
        this.strikePrice = strikePrice;
        this.opType = opType;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.askPrice = askPrice;
        this.askQty = askQty;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuoteEntity that = (QuoteEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "QuoteEntity{" +
                "id=" + id +
                ", exchange='" + exchange + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}