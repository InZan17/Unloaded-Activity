package com.github.inzan123;

import net.minecraft.util.math.random.Random;

import static java.lang.Math.*;


public class Utils {
    public static double getChoose(long x, long y) {
        double choose = 1;
        for (int i = 0; i < x; i++) {
            choose *= (double) (y - i) / (i+1);
        }
        return choose;
    }
    public static double getRandomPickOdds(int randomTickSpeed) {
        return 1.0-pow(1.0 - 1.0 / 4096.0, randomTickSpeed);
    }
    public static int getOccurrences(long cycles, double odds, int maxOccurrences,  Random random) {
        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Ran getOccurrences. cycles: "+cycles+" odds: "+odds+" maxOccurrences: "+maxOccurrences);
        return getOccurrencesBinomial(cycles, odds, maxOccurrences, random);
    }

    //good for very low odds and when maxOccurrences are very high or unrestricted
    public static int newBinomialFunction(long cycles, double odds, int maxOccurrences,  Random random) {
        double log_q = log(1.0 - odds);
        int x = 0;
        double sum = 0;
        for(;;) {
            if (x >= maxOccurrences) {
                return maxOccurrences;
            }
            sum += log(random.nextDouble()) / (cycles - x);
            if(sum < log_q) {
                return x;
            }
            x++;
        }
    }

    //41ms, 200 chunks
    public static int getOccurrencesBinomial(long cycles, double odds, int maxOccurrences,  Random random) {

        if (odds <= 0)
            return 0;

        if (maxOccurrences <= 0)
            return 0;

        double choose = 1;

        double invertedOdds = 1-odds;

        double totalProbability = 0;

        double randomDouble = random.nextDouble();

        for (int i = 0; i<maxOccurrences;i++) {

            if (i == cycles) return i;

            if (i != 0) {
                choose *= (cycles - (i - 1))/i;
            }

            double finalProbability = choose * pow(odds, i) * pow(invertedOdds, cycles-i); //Probability of it happening "i" times

            totalProbability += finalProbability;

            if (randomDouble < totalProbability) {
                return i;
            }
        }
        return maxOccurrences;
    }

    public static long sampleNegativeBinomial(int successes, double odds, long timePassed,  Random random) {
        return samplePoisson(sampleGamma(successes, (1.0-odds)/odds, random), timePassed, random);
    }

    // From my observations when playing around in desmos, I found that at very low probabilities a binomial distribution is kinda a poisson distribution. I will implement an actual Poisson sampling algorithm later once I find one.
    public static long samplePoisson(double lambda, long maxValue, Random random) {
        return newBinomialFunction((long)lambda*10000, 0.0001, (int)max(maxValue, (long)Integer.MAX_VALUE), random);
    }


    // Algorithm from https://dl.acm.org/doi/pdf/10.1145/358407.358414
    // Since we only accept integers anyway we don't need to have a separate algorithm for 0 < shape < 1
    public static double sampleGamma(int shape, double scale, Random random) {

        if (shape <= 0)
            return 0;

        double d = shape-1.0/3.0;
        double c = 1.0/sqrt(d*9.0);

        while (true) {

            double x = random.nextGaussian();
            double v = pow(1.0+c*x, 3.0);

            if (v > 0) {

                double u = random.nextDouble();

                if (u < 1.0-0.0331*x*x*x*x)
                    return d*v*scale;

                if (log(u) < 0.5*x*x+d*(1.0-v+log(v)))
                    return d*v*scale;
            }
        }

    }

    public static long randomRound(double number, Random random) {
        return (long) floor(number+random.nextDouble());
    }

    public static OccurrencesAndLeftover getOccurrencesAndLeftoverTicksFast(long cycles, double normalOdds, int randomTickSpeed, int maxOccurrences, Random random) {
        if (UnloadedActivity.instance.config.debugLogs)
            UnloadedActivity.LOGGER.info("Ran getOccurrencesAndLeftoverTicksFast. cycles: "+cycles+" normalOdds: "+normalOdds+" maxOccurrences: "+maxOccurrences);

        double multiplier = getRandomPickOdds(randomTickSpeed);

        double newCycles = cycles*multiplier;

        long randRoundCycles = randomRound(newCycles, random);

        OccurrencesAndLeftover oal = getOccurrencesAndLeftoverTicks(randRoundCycles, normalOdds, maxOccurrences, random);
        if (oal.occurrences == maxOccurrences) {

            double differenceRatio = newCycles/randRoundCycles;

            oal.leftover = (long) ((oal.leftover-random.nextFloat())/multiplier*differenceRatio);
        }
        return oal;
    }

    //595ms, 200 chunks
    public static OccurrencesAndLeftover getOccurrencesAndLeftoverTicks(long cycles, double odds, int maxOccurrences, Random random) {

        if (odds <= 0)
            return new OccurrencesAndLeftover(0,0);

        if (maxOccurrences <= 0)
            return new OccurrencesAndLeftover(0,cycles);

        int successes = 0;
        long leftover = 0;

        for (int i = 0; i<cycles;i++) {

            if (successes >= maxOccurrences) {
                leftover = cycles-i;
                break;
            }

            if (random.nextDouble() < odds) {
                ++successes;
            }
        }
        return new OccurrencesAndLeftover(successes, leftover);
    }

    public static long getTicksSinceTime(long currentTime, long timePassed, int startTime, int stopTime) {

        long dayLength = 24000;

        long window = floorMod(stopTime-startTime-1, dayLength)+1; //we + and - 1 because we want dayLength to still be dayLength and not 0

        //the amount of ticks we calculated from the amount of days passed.
        long usefulTicks = window * (timePassed / dayLength);

        long previousTime = currentTime-timePassed;

        long currentIncompleteTime = floorMod(currentTime-startTime, dayLength);
        long previousIncompleteTime = floorMod(previousTime-startTime, dayLength);

        //the amount of ticks we calculated from the incomplete day.
        long restOfDayTicks = min(currentIncompleteTime, window) - min(previousIncompleteTime, window);

        if (currentIncompleteTime < previousIncompleteTime)
            restOfDayTicks+=window;

        if (restOfDayTicks < 0)
            restOfDayTicks = floorMod(restOfDayTicks, window);

        return restOfDayTicks + usefulTicks;
    }
}

