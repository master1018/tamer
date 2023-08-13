public class StatisticsAccumulator {
    protected double max = Double.MIN_VALUE;
    protected double min = Double.MAX_VALUE;
    private double sampleSum;
    private double sampleSquareSum;
    private long sampleCount;
    protected String unit;
    public void sample(double value) {
        sampleCount++;
        if( value < min )  min = value;
        if( value > max) max = value;
        sampleSum += value;
        sampleSquareSum += (value * value);
    } 
    public String getValue( ) {
        return toString();
    }
    public String toString( ) {
        return "Minimum Value = " + min + " " + unit + " " +
            "Maximum Value = " + max + " " + unit + " " +
            "Average Value = " + computeAverage() + " " +  unit + " " +
            "Standard Deviation = " + computeStandardDeviation() + " " + unit +
            " " + "Samples Collected = " + sampleCount;
    }
    protected double computeAverage( ) {
        return (sampleSum / sampleCount);
    }
    protected double computeStandardDeviation( ) {
        double sampleSumSquare = sampleSum * sampleSum;
        return Math.sqrt(
            (sampleSquareSum-((sampleSumSquare)/sampleCount))/(sampleCount-1));
    }
    public StatisticsAccumulator( String unit ) {
        this.unit = unit;
        sampleCount = 0;
        sampleSum = 0;
        sampleSquareSum = 0;
    }
    void clearState( ) {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sampleCount = 0;
        sampleSum = 0;
        sampleSquareSum = 0;
    }
    public void unitTestValidate( String expectedUnit, double expectedMin,
        double expectedMax, long expectedSampleCount, double expectedAverage,
        double expectedStandardDeviation )
    {
        if( !expectedUnit.equals( unit ) ){
            throw new RuntimeException(
                "Unit is not same as expected Unit" +
                "\nUnit = " + unit + "ExpectedUnit = " + expectedUnit );
        }
        if( min != expectedMin ) {
            throw new RuntimeException(
                "Minimum value is not same as expected minimum value" +
                "\nMin Value = " + min + "Expected Min Value = " + expectedMin);
        }
        if( max != expectedMax ) {
            throw new RuntimeException(
                "Maximum value is not same as expected maximum value" +
                "\nMax Value = " + max + "Expected Max Value = " + expectedMax);
        }
        if( sampleCount != expectedSampleCount ) {
            throw new RuntimeException(
                "Sample count is not same as expected Sample Count" +
                "\nSampleCount = " + sampleCount + "Expected Sample Count = " +
                expectedSampleCount);
        }
        if( computeAverage() != expectedAverage ) {
            throw new RuntimeException(
                "Average is not same as expected Average" +
                "\nAverage = " + computeAverage() + "Expected Average = " +
                expectedAverage);
        }
        double difference = Math.abs(
            computeStandardDeviation() - expectedStandardDeviation);
        if( difference > 1 ) {
            throw new RuntimeException(
                "Standard Deviation is not same as expected Std Deviation" +
                "\nStandard Dev = " + computeStandardDeviation() +
                "Expected Standard Dev = " + expectedStandardDeviation);
        }
    }
} 
