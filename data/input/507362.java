public class TwilightCalculator {
    public static final int DAY = 0;
    public static final int NIGHT = 1;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0f);
    private static final float J0 = 0.0009f;
    private static final float ALTIDUTE_CORRECTION_CIVIL_TWILIGHT = -0.104719755f;
    private static final float C1 = 0.0334196f;
    private static final float C2 = 0.000349066f;
    private static final float C3 = 0.000005236f;
    private static final float OBLIQUITY = 0.40927971f;
    private static final long UTC_2000 = 946728000000L;
    public long mSunset;
    public long mSunrise;
    public int mState;
    public void calculateTwilight(long time, double latiude, double longitude) {
        final float daysSince2000 = (float) (time - UTC_2000) / DateUtils.DAY_IN_MILLIS;
        final float meanAnomaly = 6.240059968f + daysSince2000 * 0.01720197f;
        final float trueAnomaly = meanAnomaly + C1 * FloatMath.sin(meanAnomaly) + C2
                * FloatMath.sin(2 * meanAnomaly) + C3 * FloatMath.sin(3 * meanAnomaly);
        final float solarLng = trueAnomaly + 1.796593063f + (float) Math.PI;
        final double arcLongitude = -longitude / 360;
        float n = Math.round(daysSince2000 - J0 - arcLongitude);
        double solarTransitJ2000 = n + J0 + arcLongitude + 0.0053f * FloatMath.sin(meanAnomaly)
                + -0.0069f * FloatMath.sin(2 * solarLng);
        double solarDec = Math.asin(FloatMath.sin(solarLng) * FloatMath.sin(OBLIQUITY));
        final double latRad = latiude * DEGREES_TO_RADIANS;
        double cosHourAngle = (FloatMath.sin(ALTIDUTE_CORRECTION_CIVIL_TWILIGHT) - Math.sin(latRad)
                * Math.sin(solarDec)) / (Math.cos(latRad) * Math.cos(solarDec));
        if (cosHourAngle >= 1) {
            mState = NIGHT;
            mSunset = -1;
            mSunrise = -1;
            return;
        } else if (cosHourAngle <= -1) {
            mState = DAY;
            mSunset = -1;
            mSunrise = -1;
            return;
        }
        float hourAngle = (float) (Math.acos(cosHourAngle) / (2 * Math.PI));
        mSunset = Math.round((solarTransitJ2000 + hourAngle) * DateUtils.DAY_IN_MILLIS) + UTC_2000;
        mSunrise = Math.round((solarTransitJ2000 - hourAngle) * DateUtils.DAY_IN_MILLIS) + UTC_2000;
        if (mSunrise < time && mSunset > time) {
            mState = DAY;
        } else {
            mState = NIGHT;
        }
    }
}
