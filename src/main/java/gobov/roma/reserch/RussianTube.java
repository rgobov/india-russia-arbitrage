package gobov.roma.reserch;

import gobov.roma.india.shoonya.controllers.QuoteController;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Component
public class RussianTube {
    private final BlockingQueue<QuoteController.Quote> queue = new LinkedBlockingQueue<>(1000);



}
