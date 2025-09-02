package gobov.roma.russia.controller;

import gobov.roma.russia.dto.OrderBookDTO;
import gobov.roma.russia.entity.OrderBook;
import gobov.roma.russia.repository.OrderBookRepository;
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
//    public final OutOrderBookServise outputOrderBookService;

    @Autowired
    public OrderBookController(OrderBookRepository orderBookRepository) {
        this.orderBookRepository = orderBookRepository;
//        this.outputOrderBookService = outputOrderBookService;
    }



    }

