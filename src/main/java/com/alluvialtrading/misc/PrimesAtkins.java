package com.alluvialtrading.misc;

import java.util.ArrayList;

public class PrimesAtkins implements IPrimes {
        //private ArrayList<Long> _primes;
        private int _limit;
        private boolean _primes[];
        private ArrayList<Long> _primesList;

        public PrimesAtkins(int limit) throws Exception
        {
                _limit = 0;
                if (limit > (Integer.MAX_VALUE - 1))
                        return;
                _limit = limit;
        _primesList = new ArrayList<Long>();
                _primes = new boolean[_limit + 1];
                FindPrimes();
        }

        @Override
        public boolean isPrime(long val)
        {
                if (val > _limit)
                        return false;
                if (val < 2)
                        return false;
                return _primes[(int)val];
        }

        @Override
        public long generatePrime(long val) throws Exception
        {
                for (int n = (int)(val + 1); n <= _limit; ++n)
                {
                        if (_primes[n])
                                return n;
                }
                throw new Exception("out of bounds");
        }

        private void FindPrimes()
        {
                //boolean[] isPrime = new boolean[_limit + 1];
                int sqrt = (int)Math.sqrt(_limit);

                _primes[2] = true;
                _primes[3] = true;

                for (int x = 1; x <= sqrt; x++)
                {
                        int xx = x*x;

                        for (int y = 1; y <= sqrt; y++)
                        {
                                int yy = y*y;

                                int n = (int) (4 * xx + yy);
                                if (n <= _limit && (n % 12 == 1 || n % 12 == 5))
                                        _primes[n] ^= true;
                                n = (int) (3 * xx + yy);
                                if (n <= _limit && n % 12 == 7)
                                        _primes[n] ^= true;
                                n = (int) (3 * xx - yy);
                                if (x > y && n <= _limit && n % 12 == 11)
                                        _primes[n] ^= true;
                        }
                }
                for (int n = 5; n <= sqrt; n+=2)
                {
                        if (_primes[n])
                        {
                                int s = n * n;
                                for (int k = s; k <= _limit; k += s)
                                        _primes[k] = false;
                        }
                }

                for (int n = 2; n <= _limit; ++n)
                {
                        if (_primes[n])
                                _primesList.add((long)n);
                }
        }

        @Override
        public long generatePreviousPrime(long start) throws Exception {
                if (start > _limit)
                        throw new Exception("start value out of bounds");
                for (int n = (int)(start - 1); n >= 2; --n)
                        if (_primes[n])
                                return n;
                throw new Exception("no prime less than 2");
        }
}