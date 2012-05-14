package com.alluvialtrading.misc;

public class PrimeSieve {
    public PrimeSieve() {
         System.out.println(this.getClass().getName());
        System.exit(0);
    }
    public static void main(String[] args) {
        new PrimeSieve();
        int N = 100000;

        // initially assume all integers are prime
        boolean[] isPrime = new boolean[N + 1];
        for (int i = 2; i <= N; i++) {
            isPrime[i] = true;
        }

        // mark non-primes <= N using Sieve of Eratosthenes
        for (int i = 2; i*i <= N; i++) {

            // if i is prime, then mark multiples of i as nonprime
            // suffices to consider mutiples i, i+1, ..., N/i
            if (isPrime[i]) {
                for (int j = i; i*j <= N; j++) {
                    isPrime[i*j] = false;
                }
            }
        }

        // count primes
       // int primes = 0;
        for (int i = 2; i <= N; i++) {
            if (isPrime[i]) System.out.println(i);
        }
    // char bkag = '';   //System.out.println("The number of primes <= " + N + " is " + primes);
    }
}