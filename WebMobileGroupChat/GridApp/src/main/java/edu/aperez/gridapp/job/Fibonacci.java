package edu.aperez.gridapp.job;

import java.util.HashMap;

/**
 * Created by alexperez on 12/01/2016.
 */
public class Fibonacci {

    HashMap<Integer, Long> cache;

    public Fibonacci() {
        cache = new HashMap<>();
        cache.put(0, 0l);
        cache.put(1, 1l);
    }


    public Long calculate(Integer n) {
        Long cachedResult = cache.get(n);
        if (cachedResult != null)
            return cachedResult;
        else {
            Long result = calculate (n - 1) + calculate(n - 2);
            cache.put(n,result);
            return result;
        }
    }


}
