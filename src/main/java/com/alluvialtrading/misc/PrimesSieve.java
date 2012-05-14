package com.alluvialtrading.misc;

import java.util.TreeSet;

/**
 * @author wolfgang
 * @note checks and generates primes
 */
public class PrimesSieve implements IPrimes {

        // storage for ordered and optimized primes
        private TreeSet<Long> _primes;
        // storage for unoptimized access
        private TreeSet<Long> _unordered;
        // flag if optimization can be used
        private boolean _optimize;
        // upper limit for optimization (can be expanded)
        private long _upperLimit;

        /**
         * Ctor for unoptimized usage
         */
        public PrimesSieve()
        {
                _primes = new TreeSet<Long>();
                _unordered = new TreeSet<Long>();
                _primes.add(2L);
                _unordered.add(2L);
                _optimize = false;
        }

        /**
         * @param limit upper limit for prime pre-generation
         * @note takes some time to generate primes
         */
        public PrimesSieve(long limit)
        {
                _primes = new TreeSet<Long>();
                _unordered = new TreeSet<Long>();
                _primes.add(2L);
                optimize(limit);
        }

        /**
         * @param limit upper limit to generate primes
         */
        private void optimize(long limit)
        {
                try
                {
                        _upperLimit = 2L;
                        _optimize = true;
                        long prime = 2L;
                        while (prime <= limit)
                                prime = generatePrime(prime);
                }
                catch(Exception e)
                {
                        _optimize = false;
                }
        }

        /**
         * @param n number to test
         * @return upper boundary to check n for primeness
         */
        private long getUpperLimit(long n)
        {
                return (long)Math.ceil(Math.sqrt(n));
        }

        /**
         * @param n number to test
         * @param upper upper boundary
         * @return true if n is prime
         */
        private boolean checkModulo(long n, long upper)
        {
                // if n divides by 2 (or a multiple thereof) it is not prime (with the exception of 2 itself)
                if (n > 2 && n % 2 == 0)
                        return false;
                // since 2 is not a factor, start at three and leave all even numbers out
                for (long div = 3; div <= upper; div+=2)
                {
                        // if there is a number which divides n with no rest then we can safely assume n is not a prime
                        if (n % div == 0)
                                return false;
                }
                return true;
        }

        /**
         * @param tree collection to check
         * @param n number to test
         * @param upper upper boundary to test
         * @return false if number is definitely not prime
         */
        private boolean _checkFactor(TreeSet<Long> tree, long n, long upper)
        {
                // if entries in collection
                if (tree.size() > 0)
                {
                        // check all entries against n
                        for (long fac: tree)
                        {
                                if (fac > upper)
                                        break;
                                if ((n % fac) == 0)
                                        return false;
                        }
                }
                return true;
        }

        /**
         * @param n number to test
         * @param upper upper boundary to test
         * @return false if number is definitely not prime
         */
        private boolean checkFactor(long n, long upper)
        {
                // check the list of known primes first
                if (false == _checkFactor(_primes, n, upper))
                        return false;
                // check the other known primes also
                if (false == _checkFactor(_unordered, n, upper))
                        return false;
                return true;
        }

        /**
         * @param n prime number to add
         * @param start if known used to optimize, if not provide 0
         */
        private void addPrime(long n, long start)
        {
                // if n is the next prime from our optimized list it can be added there
                if (_optimize == true && start == _primes.last())
                {
                        _upperLimit = n;
                        _primes.add(n);
                }
                else
                {
                        // if it's not in any order just add the number to the unordered storage
                        _unordered.add(n);
                }
        }

        /**
         * @param start any number
         * @return the lowest prime > start
         * @throws Exception if no prime > start was found
         */
        public long generatePrime(long start) throws Exception
        {
                // any number smaller 2 is automatically 2
                if (start < 2)
                        return 2;

                // if we have optimization available look for the next highest prime in the list
                if (_optimize && start + 1 < _primes.last())
                        return _primes.ceiling(start + 1);

                // just check all numbers for primeness
                for (long n = start + 1; n < Long.MAX_VALUE; ++n)
                {
                        // as we increment n we need to update the upper limit too
                        long upper = getUpperLimit(n);
                        if (true == checkModulo(n, upper))
                        {
                                addPrime(n, start);
                                return n;
                        }
                }
                throw new Exception("No Prime found for value " + start);
        }

        /**
         * @param start any number
         * @return the highest prime < start
         * @throws Exception if no prime < start was found
         */
        public long generatePreviousPrime(long start) throws Exception
        {
                // >>!! could be optimized
                for (long n = start - 1; n > 1; --n)
                {
                        long upper = getUpperLimit(n);
                        if (true == checkModulo(n, upper))
                        {
                                addPrime(n, 0);
                                return n;
                        }
                }
                // <<!!
                throw new Exception("No Prime found for value " + start);
        }

        /**
         * @param n number to test
         * @return true if n is prime
         */
        public boolean isPrime(long n)
        {
                // check n in the prime storages
                if (_primes.contains(n))
                        return true;

                if (_unordered.contains(n))
                        return true;

                // if optimization is used and n < upper limit it is not possible that is is prime
                if (_optimize && n < _upperLimit)
                        return false;

                // any number < 2 is not prime either
                if (n < 2)
                        return false;

                // calculate the upper limit to check
                long upper = getUpperLimit(n);

                // first check all known prime factors
                if (false == checkFactor(n, upper))
                        return false;
                // then check all uneven numbers
                if (false == checkModulo(n, upper))
                        return false;

                try
                {
                        // if optimization is used and the prime just checked is the next prime after last known ordered prime
                        // just add the prime to the ordered storage
                        if (_optimize)
                        {
                                if (generatePrime(_primes.last() + 1) == n)
                                {
                                        addPrime(n, _primes.last());
                                }
                        }
                        else
                                addPrime(n, 0);
                }
                catch (Exception e)
                {
                        // if all fails just add the prime in the non optimized storage
                        addPrime(n, 0);
                }
                return true;
        }
}