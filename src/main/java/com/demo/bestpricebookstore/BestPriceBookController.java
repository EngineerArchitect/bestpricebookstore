package com.demo.bestpricebookstore;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/virtualstore")
public class BestPriceBookController {

    public static final ScopedValue<RestCallStatistics> TIMEMAP = ScopedValue.newInstance();

    @Autowired
    private BookRetrievalService retrievalService;

    @GetMapping("/book")
    public BestPriceResult getBestPriceForBook(@RequestParam String name) {

        long start = System.currentTimeMillis();

        RestCallStatistics timeObj = new RestCallStatistics();
        try {

            // fluent API is used here
            List<Book> books = ScopedValue.where(TIMEMAP, timeObj).call(() -> retrievalService.getBookFromAllStores(name));

            Book bestPriceBook = books.stream()
                    .min(Comparator.comparing(Book::cost))
                    .orElseThrow();

            return new BestPriceResult(timeObj, bestPriceBook, books);
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling getBestPrice",e);
        }
        finally {

            long end = System.currentTimeMillis();
            timeObj.addTiming("Best Price Store", end - start);

            timeObj.dumpTiming();
        }

    }

}
