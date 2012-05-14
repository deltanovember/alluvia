package com.alluvialtrading.misc;


/**
 * @author wolfgang
 * @note Find the lowest sum for a set of five primes for which any two primes concatenate to produce another prime.
 */
public class Problem60 {

    public static void main(String[] args) throws Exception {
        Problem60 p = new Problem60();
        System.out.println(p.getSolution());
    }
        // prime checker
        Primes _primes;
        // up to where to concatenate primes
        final long MAX_PRIME = 10000;

        /**
         * Ctor, pregenerate primes
         */
        public Problem60() throws Exception
        {
                _primes = new Primes(MAX_PRIME * 10);
        }

        /**
         * @param start seed for pregenerated primes (used for JUnit Tests)
         */
        public Problem60(int start) throws Exception
        {
                _primes = new Primes(start);
        }

        /**
         * @return the sum of the 5 primes that concatenated are also primes
         * @throws Exception if no solution could be found
         */
        public int getSolution() throws Exception {
                // calculate primes in 5 levels
                // from level 2 up check the concatenation of the previous levels
                for (long p1 = 3; p1 < MAX_PRIME; p1 = _primes.generatePrime(p1))
                {
                        for (long p2 = _primes.generatePrime(p1); p2 < MAX_PRIME; p2 = _primes.generatePrime(p2))
                        {
                                // only if the concatenation is successful go to the next level
                                if (isConcatable(new long[] {p1, p2}))
                                {
                                        for (long p3 = _primes.generatePrime(p2); p3 < MAX_PRIME; p3 = _primes.generatePrime(p3))
                                        {
                                                if (isConcatable(new long[] {p1, p2, p3}, 2))
                                                {
                                                        for (long p4 = _primes.generatePrime(p3); p4 < MAX_PRIME; p4 = _primes.generatePrime(p4))
                                                        {
                                                                if (isConcatable(new long[] {p1, p2, p3, p4}, 3))
                                                                {
                                                                        for (long p5 = _primes.generatePrime(p4); p5 < MAX_PRIME; p5 = _primes.generatePrime(p5))
                                                                        {
                                                                                if (isConcatable(new long[] {p1, p2, p3, p4, p5}, 4))
                                                                                {
                                                                                        // if all primes are concatable return the sum of the primes
                                                                                        return sumOf(new long[] {p1, p2, p3, p4, p5});
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
                throw new Exception("no solution found");
        }

        /**
         * @param primelist primes to check
         * @return true if each number is concatable with each other and still prime
         */
        boolean isConcatable(long[] primelist) {
                return isConcatable(primelist, 1);
        }

        /**
         * @param primelist primes to check
         * @param opt if parts are already checked specify the level still to check (< primelist.length)
         * @return true if each number is concatable with each other and still prime
         */
        boolean isConcatable(long[] primelist, int opt) {
                // use string builder for more performance
                StringBuilder sb = new StringBuilder();
                StringBuilder s1 = new StringBuilder();

                // check all primes in list
                for (int i = 0; i < primelist.length; ++i)
                {
                        // convert prime to string for concatenation
                        s1.setLength(0);
                        s1.append(Long.toString(primelist[i]));

                        // check optimization level
                        if (i + 1 > opt)
                                opt = i + 1;

                        // check against the other primes
                        for (int j = opt; j < primelist.length; ++j)
                        {
                                // build concatenated string
                                sb.setLength(0);
                                sb.append(Long.toString(primelist[j]));
                                sb.append(s1);
                                // reconvert to long and check for primes
                                long test = Long.parseLong(sb.toString());
                                if (false == _primes.isPrime(test))
                                        return false;

                                // again for the other direction
                                sb.setLength(0);
                                sb.append(s1);
                                sb.append(Long.toString(primelist[j]));
                                test = Long.parseLong(sb.toString());
                                if (false == _primes.isPrime(test))
                                        return false;
                        }
                }
                return true;
        }

        /**
         * @param primelist list of numbers
         * @return the sum of all the numbers in <primelist>
         */
        int sumOf(long[] primelist) {
                int result = 0;
                for (long l: primelist)
                        result += (int)l;
                return result;
        }
}