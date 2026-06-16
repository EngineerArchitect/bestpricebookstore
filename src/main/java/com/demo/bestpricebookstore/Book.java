package com.demo.bestpricebookstore;

/**
 * Represents a book
 */
public record Book(String bookStore, String bookName, String author, int cost, int numPages, String link) {

}
