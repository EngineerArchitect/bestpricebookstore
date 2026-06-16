package com.demo.bestpricebookstore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BookRetrievalService {
    @Value("#{${book.store.baseurls}}")
    private Map<String, String> storeUrlMap;

    private final RestClient restClient = RestClient.create();

    public List<Book> getBookFromAllStores(String bookName) throws InterruptedException {

        ThreadFactory factory = Thread.ofVirtual().name("book-store-thr-", 0).factory();

        // FIX: Added explicit generic type <Book> to Joiner.awaitAll()
        try (var scope = StructuredTaskScope.open(Joiner.<Book>awaitAll(), cf -> cf.withThreadFactory(factory))) {

            List<Subtask<Book>> bookTasks = new ArrayList<>();
            storeUrlMap.forEach((name, url) -> {
                bookTasks.add(scope.fork(() -> getBookFromStore(name, url, bookName)));
            });

            scope.join();

            // Dump stacktrace of all failures
            bookTasks.stream()
                    .filter(t -> t.state() == Subtask.State.FAILED)
                    .map(Subtask::exception)
                    .forEach(Throwable::printStackTrace);

            // Collect only successful outcomes
            return bookTasks.stream()
                    .filter(t -> t.state() == Subtask.State.SUCCESS)
                    .map(Subtask::get)
                    .toList();
        }
    }

    private Book getBookFromStore(String storeName, String url, String bookName) {
        long start = System.currentTimeMillis();
        Book book = restClient.get()
                .uri(url + "/store/book", t -> t.queryParam("name", bookName).build())
                .retrieve()
                .body(Book.class);
        long end = System.currentTimeMillis();

        // This smoothly reads the ScopedValue bound in BestPriceBookController
        RestCallStatistics timeObj = BestPriceBookController.TIMEMAP.get();
        timeObj.addTiming(storeName, end - start);

        return book;
    }
}