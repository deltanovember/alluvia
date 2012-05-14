package com.alluvialtrading.misc;



public class Primes implements IPrimes {

        IPrimes _primes;

        public Primes(long limit) throws Exception
        {
                if (limit < Integer.MAX_VALUE - 1)
                        _primes = new PrimesAtkins((int)limit);
                else
                        _primes = new PrimesSieve(limit);
        }

        public Primes()
        {
                _primes = new PrimesSieve();
        }

        @Override
        public long generatePreviousPrime(long start) throws Exception {
                return _primes.generatePreviousPrime(start);
        }

        @Override
        public long generatePrime(long start) throws Exception {
                return _primes.generatePrime(start);
        }

        @Override
        public boolean isPrime(long n) {
                return _primes.isPrime(n);
        }

}