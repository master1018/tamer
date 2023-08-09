class TickScaler {
    private double mMinVal; 
    private double mMaxVal; 
    private double mRangeVal;
    private int mNumPixels; 
    private int mPixelsPerTick; 
    private double mPixelsPerRange;
    private double mTickIncrement;
    private double mMinMajorTick;
    TickScaler(double minVal, double maxVal, int numPixels, int pixelsPerTick) {
        mMinVal = minVal;
        mMaxVal = maxVal;
        mNumPixels = numPixels;
        mPixelsPerTick = pixelsPerTick;
    }
    public void setMinVal(double minVal) {
        mMinVal = minVal;
    }
    public double getMinVal() {
        return mMinVal;
    }
    public void setMaxVal(double maxVal) {
        mMaxVal = maxVal;
    }
    public double getMaxVal() {
        return mMaxVal;
    }
    public void setNumPixels(int numPixels) {
        mNumPixels = numPixels;
    }
    public int getNumPixels() {
        return mNumPixels;
    }
    public void setPixelsPerTick(int pixelsPerTick) {
        mPixelsPerTick = pixelsPerTick;
    }
    public int getPixelsPerTick() {
        return mPixelsPerTick;
    }
    public void setPixelsPerRange(double pixelsPerRange) {
        mPixelsPerRange = pixelsPerRange;
    }
    public double getPixelsPerRange() {
        return mPixelsPerRange;
    }
    public void setTickIncrement(double tickIncrement) {
        mTickIncrement = tickIncrement;
    }
    public double getTickIncrement() {
        return mTickIncrement;
    }
    public void setMinMajorTick(double minMajorTick) {
        mMinMajorTick = minMajorTick;
    }
    public double getMinMajorTick() {
        return mMinMajorTick;
    }
    public int valueToPixel(double value) {
        return (int) Math.ceil(mPixelsPerRange * (value - mMinVal) - 0.5);
    }
    public double valueToPixelFraction(double value) {
        return mPixelsPerRange * (value - mMinVal);
    }
    public double pixelToValue(int pixel) {
        return mMinVal + (pixel / mPixelsPerRange);
    }
    public void computeTicks(boolean useGivenEndPoints) {
        int numTicks = mNumPixels / mPixelsPerTick;
        mRangeVal = mMaxVal - mMinVal;
        mTickIncrement = mRangeVal / numTicks;
        double dlogTickIncrement = Math.log10(mTickIncrement);
        int logTickIncrement = (int) Math.floor(dlogTickIncrement);
        double scale = Math.pow(10, logTickIncrement);
        double scaledTickIncr = mTickIncrement / scale;
        if (scaledTickIncr > 5.0)
            scaledTickIncr = 10;
        else if (scaledTickIncr > 2)
            scaledTickIncr = 5;
        else if (scaledTickIncr > 1)
            scaledTickIncr = 2;
        else
            scaledTickIncr = 1;
        mTickIncrement = scaledTickIncr * scale;
        if (!useGivenEndPoints) {
            double minorTickIncrement = mTickIncrement / 5;
            double dval = mMaxVal / minorTickIncrement;
            int ival = (int) dval;
            if (ival != dval)
                mMaxVal = (ival + 1) * minorTickIncrement;
            ival = (int) (mMinVal / mTickIncrement);
            mMinVal = ival * mTickIncrement;
            mMinMajorTick = mMinVal;
        } else {
            int ival = (int) (mMinVal / mTickIncrement);
            mMinMajorTick = ival * mTickIncrement;
            if (mMinMajorTick < mMinVal)
                mMinMajorTick = mMinMajorTick + mTickIncrement;
        }
        mRangeVal = mMaxVal - mMinVal;
        mPixelsPerRange = (double) mNumPixels / mRangeVal;
    }
}
