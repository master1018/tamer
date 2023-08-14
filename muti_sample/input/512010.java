class SunCalculator {
    static final double ZENITH_ASTRONOMICAL = 108;
    static final double ZENITH_NAUTICAL = 102;
    static final double ZENITH_CIVIL = 96;
    static final double ZENITH_OFFICIAL = 90.8333;
    private Location mLocation;
    private TimeZone mTimeZone;
    SunCalculator(Location location, String timeZoneIdentifier) {
        mLocation = location;
        mTimeZone = TimeZone.getTimeZone(timeZoneIdentifier);
    }
    public void setLocation(Location location) {
        mLocation = location;
    }
    public double computeSunriseTime(double solarZenith, Calendar date) {
        return computeSolarEventTime(solarZenith, date, true);
    }
    public double computeSunsetTime(double solarZenith, Calendar date) {
        return computeSolarEventTime(solarZenith, date, false);
    }
    public static int timeToHours(double time) {
        int hour = (int) Math.floor(time);
        int minute = (int) Math.round((time - hour) * 60);
        if (minute == 60) {
            hour++;
        }
        return hour;
    }
    public static int timeToMinutes(double time) {
        int hour = (int) Math.floor(time);
        int minute = (int) Math.round((time - hour) * 60);
        if (minute == 60) {
            minute = 0;
        }
        return minute;
    }
    public static float timeToDayFraction(double time) {
        int hour = (int) Math.floor(time);
        int minute = (int) Math.round((time - hour) * 60);
        if (minute == 60) {
            minute = 0;
            hour++;
        }
        return (hour * 60 + minute) / 1440.0f;
    }
    public static String timeToString(double time) {
        StringBuffer buffer = new StringBuffer();
        int hour = (int) Math.floor(time);
        int minute = (int) Math.round((time - hour) * 60);
        if (minute == 60) {
            minute = 0;
            hour++;
        }
        buffer.append(hour).append(':').append(minute < 10 ? "0" + minute : minute);
        return buffer.toString();
    }
    private double computeSolarEventTime(double solarZenith, Calendar date, boolean isSunrise) {
        date.setTimeZone(mTimeZone);
        double longitudeHour = getLongitudeHour(date, isSunrise);
        double meanAnomaly = getMeanAnomaly(longitudeHour);
        double sunTrueLong = getSunTrueLongitude(meanAnomaly);
        double cosineSunLocalHour = getCosineSunLocalHour(sunTrueLong, solarZenith);
        if ((cosineSunLocalHour < -1.0) || (cosineSunLocalHour > 1.0)) {
            return 0;
        }
        double sunLocalHour = getSunLocalHour(cosineSunLocalHour, isSunrise);
        double localMeanTime = getLocalMeanTime(sunTrueLong, longitudeHour, sunLocalHour);
        return getLocalTime(localMeanTime, date);
    }
    private double getBaseLongitudeHour() {
        return mLocation.getLongitude() / 15.0;
    }
    private double getLongitudeHour(Calendar date, Boolean isSunrise) {
        int offset = 18;
        if (isSunrise) {
            offset = 6;
        }
        double dividend = offset - getBaseLongitudeHour();
        double addend = dividend / 24.0;
        return getDayOfYear(date) + addend;
    }
    private static double getMeanAnomaly(double longitudeHour) {
        return 0.9856 * longitudeHour - 3.289;
    }
    private static double getSunTrueLongitude(double meanAnomaly) {
        final double meanRadians = Math.toRadians(meanAnomaly);
        double sinMeanAnomaly = Math.sin(meanRadians);
        double sinDoubleMeanAnomaly = Math.sin((meanRadians * 2.0));
        double firstPart = meanAnomaly + sinMeanAnomaly * 1.916;
        double secondPart = sinDoubleMeanAnomaly * 0.020 + 282.634;
        double trueLongitude = firstPart + secondPart;
        if (trueLongitude > 360) {
            trueLongitude = trueLongitude - 360.0;
        }
        return trueLongitude;
    }
    private static double getRightAscension(double sunTrueLong) {
        double tanL = Math.tan(Math.toRadians(sunTrueLong));
        double innerParens = Math.toDegrees(tanL) * 0.91764;
        double rightAscension = Math.atan(Math.toRadians(innerParens));
        rightAscension = Math.toDegrees(rightAscension);
        if (rightAscension < 0.0) {
            rightAscension = rightAscension + 360.0;
        } else if (rightAscension > 360.0) {
            rightAscension = rightAscension - 360.0;
        }
        double ninety = 90.0;
        double longitudeQuadrant = (int) (sunTrueLong / ninety);
        longitudeQuadrant = longitudeQuadrant * ninety;
        double rightAscensionQuadrant = (int) (rightAscension / ninety);
        rightAscensionQuadrant = rightAscensionQuadrant * ninety;
        double augend = longitudeQuadrant - rightAscensionQuadrant;
        return (rightAscension + augend) / 15.0;
    }
    private double getCosineSunLocalHour(double sunTrueLong, double zenith) {
        double sinSunDeclination = getSinOfSunDeclination(sunTrueLong);
        double cosineSunDeclination = getCosineOfSunDeclination(sinSunDeclination);
        final double zenithInRads = Math.toRadians(zenith);
        final double latitude = Math.toRadians(mLocation.getLatitude());
        double cosineZenith = Math.cos(zenithInRads);
        double sinLatitude = Math.sin(latitude);
        double cosLatitude = Math.cos(latitude);
        double sinDeclinationTimesSinLat = sinSunDeclination * sinLatitude;
        double dividend = cosineZenith - sinDeclinationTimesSinLat;
        double divisor = cosineSunDeclination * cosLatitude;
        return dividend / divisor;
    }
    private static double getSinOfSunDeclination(double sunTrueLong) {
        double sinTrueLongitude = Math.sin(Math.toRadians(sunTrueLong));
        return sinTrueLongitude * 0.39782;
    }
    private static double getCosineOfSunDeclination(double sinSunDeclination) {
        double arcSinOfSinDeclination = Math.asin(sinSunDeclination);
        return Math.cos(arcSinOfSinDeclination);
    }
    private static double getSunLocalHour(double cosineSunLocalHour, Boolean isSunrise) {
        double arcCosineOfCosineHourAngle = Math.acos(cosineSunLocalHour);
        double localHour = Math.toDegrees(arcCosineOfCosineHourAngle);
        if (isSunrise) {
            localHour = 360.0 - localHour;
        }
        return localHour / 15.0;
    }
    private static double getLocalMeanTime(double sunTrueLong, double longitudeHour,
            double sunLocalHour) {
        double rightAscension = getRightAscension(sunTrueLong);
        double innerParens = longitudeHour * 0.06571;
        double localMeanTime = sunLocalHour + rightAscension - innerParens;
        localMeanTime = localMeanTime - 6.622;
        if (localMeanTime < 0.0) {
            localMeanTime = localMeanTime + 24.0;
        } else if (localMeanTime > 24.0) {
            localMeanTime = localMeanTime - 24.0;
        }
        return localMeanTime;
    }
    private double getLocalTime(double localMeanTime, Calendar date) {
        double utcTime = localMeanTime - getBaseLongitudeHour();
        double utcOffSet = getUTCOffSet(date);
        double utcOffSetTime = utcTime + utcOffSet;
        return adjustForDST(utcOffSetTime, date);
    }
    private double adjustForDST(double localMeanTime, Calendar date) {
        double localTime = localMeanTime;
        if (mTimeZone.inDaylightTime(date.getTime())) {
            localTime++;
        }
        if (localTime > 24.0) {
            localTime = localTime - 24.0;
        }
        return localTime;
    }
    private static double getDayOfYear(Calendar date) {
        return date.get(Calendar.DAY_OF_YEAR);
    }
    private static double getUTCOffSet(Calendar date) {
        int offSetInMillis = date.get(Calendar.ZONE_OFFSET);
        return offSetInMillis / 3600000;
    }
}