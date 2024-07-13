package lol.zanspace.unloadedactivity;

import net.minecraft.util.math.random.Random;

import static java.lang.Math.*;
import static net.minecraft.util.math.MathHelper.sign;


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
        if (UnloadedActivity.config.debugLogs)
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

    public static long sampleNegativeBinomial(int successes, double odds,  Random random) {
        return samplePoisson(sampleGamma(successes, (1.0-odds)/odds, random), random);
    }

    public static long sampleNegativeBinomialWithMax(long cycles, int successes, double odds, Random random) {
        long failedTrials = Long.MAX_VALUE;
        long attempts = 0;
        while (failedTrials > cycles && attempts < UnloadedActivity.config.maxNegativeBinomialAttempts) {

            failedTrials = sampleNegativeBinomial(successes, odds, random);
            attempts++;
        }

        if (failedTrials > cycles) {
            //we have attempted this many times and the probability of this happening is probably very low.
            //So we'll just pretend this was the output even though its not accurate at all
            failedTrials = (long) (random.nextDouble() * cycles);
        }
        return failedTrials;
    }

    public static long sampleNegativeBinomialWithMinMax(long minCycles, long maxCycles, int successes, double odds, Random random) {
        long failedTrials = Long.MAX_VALUE;
        long attempts = 0;
        while ((failedTrials > maxCycles || failedTrials < minCycles) && attempts < 100) {

            failedTrials = sampleNegativeBinomial(successes, odds, random);
            attempts++;
        }

        if (failedTrials > maxCycles || failedTrials < minCycles) {
            //we have attempted this 100 times and the probability of this happening is probably very low.
            //So we'll just pretend this was the output even though its not accurate at all
            failedTrials = ((long) (random.nextDouble() * (maxCycles-minCycles)))+minCycles;
        }
        return failedTrials;
    }

    public static final double[] logFactorials = new double[]{
            0.0,
            0.0,
            0.6931471805599453,
            1.791759469228055,
            3.1780538303479458,
            4.787491742782046,
            6.579251212010101,
            8.525161361065415,
            10.60460290274525,
            12.801827480081469
    };

    // lambda >= 10 uses algorithm PTRD found here: https://research.wu.ac.at/ws/portalfiles/portal/18953249/document.pdf
    // lambda < 10 uses the algorithm found here: https://math.stackexchange.com/questions/785188/simple-algorithm-for-generating-poisson-distribution
    public static long samplePoisson(double lambda, Random random) {

        if (lambda < 10) {
            long k = 0;
            double p = random.nextDouble();
            while (p > exp(-lambda)) {
                p *= random.nextDouble();
                k += 1;
            }
            return k;
        }

        double smu = sqrt(lambda);
        double b = 0.931 + 2.53*smu;
        double a = 0.059 + 0.02483*b;
        double vr = 0.9277 - 3.6224/(b-2.0);
        double oneDivAlpha = 1.1239 + 1.1328/(b-3.4);

        double logSqrtPi2 = 0.9189385332046727; //result from log(sqrt(PI*2))

        while (true) {

            double v = random.nextDouble();
            double u;

            if (v <= 0.86*vr) {
                u = v/vr-0.43;
                return (long)floor((2.0*a/(0.5-abs(u))+b)*u+lambda+0.445);
            }

            if (v >= vr) {
                u = random.nextDouble()-0.5;
            } else {
                u = v/vr-0.93;
                u = sign(u)*0.5 - u;
                v = random.nextDouble()*vr;
            }

            double us = 0.5-abs(u);

            if (us < 0.013 && v > us)
                continue;

            int k = (int)((2.0*a/us+b)*u+lambda+0.445);
            v = v*oneDivAlpha/(a/(us*us)+b);

            if (k >= 10 && log(v*smu) <= (k + 0.5) * log(lambda/k)-lambda-logSqrtPi2+k-(1.0/12.0 - 1.0/(360.0*k*k))/k)
                return k;

            if (0 <= k && k <= 9 && log(v) <= k*log(lambda) - lambda - logFactorials[k])
                return k;

        }
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

    public static OccurrencesAndLeftover getOccurrencesAndLeftoverTicksFastOld(long cycles, double normalOdds, int randomTickSpeed, int maxOccurrences, Random random) {
        if (UnloadedActivity.config.debugLogs)
            UnloadedActivity.LOGGER.info("Ran getOccurrencesAndLeftoverTicksFast. cycles: "+cycles+" normalOdds: "+normalOdds+" maxOccurrences: "+maxOccurrences);

        double multiplier = getRandomPickOdds(randomTickSpeed);

        double newCycles = cycles*multiplier;

        long randRoundCycles = randomRound(newCycles, random);

        OccurrencesAndLeftover oal = getOccurrencesAndLeftoverTicksBruteForce(randRoundCycles, normalOdds, maxOccurrences, random);
        if (oal.occurrences == maxOccurrences) {

            double differenceRatio = newCycles/randRoundCycles;

            oal.leftover = (long) ((oal.leftover-random.nextFloat())/multiplier*differenceRatio);
        }
        return oal;
    }

    public static OccurrencesAndLeftover getOccurrencesAndLeftoverTicks(long cycles, double odds, int maxOccurrences, Random random) {

        if (odds <= 0)
            return new OccurrencesAndLeftover(0,0);

        if (maxOccurrences <= 0)
            return new OccurrencesAndLeftover(0,cycles);

        int successes = getOccurrencesBinomial(cycles, odds, maxOccurrences, random);

        long leftover;

        if (successes == maxOccurrences) {
            long failedTrials = sampleNegativeBinomialWithMax(cycles, successes, odds, random);
            leftover = cycles - failedTrials;
        } else {
            leftover = 0;
        }

        return new OccurrencesAndLeftover(successes, leftover);
    }

    //595ms, 200 chunks
    public static OccurrencesAndLeftover getOccurrencesAndLeftoverTicksBruteForce(long cycles, double odds, int maxOccurrences, Random random) {

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

