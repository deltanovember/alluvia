package com.alluvialtrading.tools;


public class Benchmarker {
    long iterations = 1000000000L;
    String security = "BHP";
    double price = 40.0;

    public static void main(String[] args) {
        new Benchmarker();
    }

    Benchmarker() {
        long timer = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            process(security, price);
        }
        System.out.println("No objects took: " + (System.currentTimeMillis() - timer) + "ms");
        timer = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            process(new SimpleQuote(security, price));
        }
        System.out.println("Objects took: " + (System.currentTimeMillis() - timer) + "ms");

    }

    void process(SimpleQuote quote) {
    }

    void process(String security, double price) {

    }


}

class SimpleQuote {
    String security;
    double price;

    SimpleQuote(String security, double price) {
        this.security = security;
        this.price = price;
    }
}