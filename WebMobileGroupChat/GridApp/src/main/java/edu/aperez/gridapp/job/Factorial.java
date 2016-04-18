package edu.aperez.gridapp.job;

/**
 * Created by alexperez on 12/01/2016.
 */
public class Factorial {

    public static Long calculate(long number) {
        if (number <= 1)
            return 1l;
        else
            return number * calculate(number - 1);
    }
}
