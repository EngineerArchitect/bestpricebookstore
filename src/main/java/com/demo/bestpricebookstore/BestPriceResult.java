package com.demo.bestpricebookstore;

import java.util.List;

public record BestPriceResult(RestCallStatistics callStatistics, Book bestPriceDeal, List<Book> allDeals) {

}
