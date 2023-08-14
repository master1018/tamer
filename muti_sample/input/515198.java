public abstract class DatatypeFactory {
    public static final String DATATYPEFACTORY_PROPERTY = "javax.xml.datatype.DatatypeFactory";
    public static final String DATATYPEFACTORY_IMPLEMENTATION_CLASS = new String("org.apache.xerces.jaxp.datatype.DatatypeFactoryImpl");
    protected DatatypeFactory() {}
    public static DatatypeFactory newInstance()
        throws DatatypeConfigurationException {
        try {
            return (DatatypeFactory) FactoryFinder.find(
                    DATATYPEFACTORY_PROPERTY,
                    DATATYPEFACTORY_IMPLEMENTATION_CLASS);
        } 
        catch (FactoryFinder.ConfigurationError e) {
            throw new DatatypeConfigurationException(e.getMessage(), e.getException());
        }
    }
    public abstract Duration newDuration(final String lexicalRepresentation);
    public abstract Duration newDuration(final long durationInMilliSeconds);
    public abstract Duration newDuration(
            final boolean isPositive,
            final BigInteger years,
            final BigInteger months,
            final BigInteger days,
            final BigInteger hours,
            final BigInteger minutes,
            final BigDecimal seconds);
    public Duration newDuration(
            final boolean isPositive,
            final int years,
            final int months,
            final int days,
            final int hours,
            final int minutes,
            final int seconds) {
        BigInteger realYears = (years != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) years) : null;
        BigInteger realMonths = (months != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) months) : null;
        BigInteger realDays = (days != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) days) : null;
        BigInteger realHours = (hours != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) hours) : null;
        BigInteger realMinutes = (minutes != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) minutes) : null;
        BigDecimal realSeconds = (seconds != DatatypeConstants.FIELD_UNDEFINED) ? BigDecimal.valueOf((long) seconds) : null;
        return newDuration(
                isPositive,
                realYears,
                realMonths,
                realDays,
                realHours,
                realMinutes,
                realSeconds
        );
    }
    public Duration newDurationDayTime(final String lexicalRepresentation) {
        if (lexicalRepresentation == null) {
            throw new NullPointerException("The lexical representation cannot be null.");
        }
        int pos = lexicalRepresentation.indexOf('T');
        int length = (pos >= 0) ? pos : lexicalRepresentation.length();
        for (int i = 0; i < length; ++i) {
            char c = lexicalRepresentation.charAt(i);
            if (c == 'Y' || c == 'M') {
                throw new IllegalArgumentException("Invalid dayTimeDuration value: " + lexicalRepresentation);
            }
        }
        return newDuration(lexicalRepresentation);
    }
    public Duration newDurationDayTime(final long durationInMilliseconds) {
        long _durationInMilliseconds = durationInMilliseconds;
        if (_durationInMilliseconds == 0) {
            return newDuration(true, DatatypeConstants.FIELD_UNDEFINED, 
                    DatatypeConstants.FIELD_UNDEFINED, 0, 0, 0, 0);
        }
        boolean tooLong = false;
        final boolean isPositive;
        if (_durationInMilliseconds < 0) {
            isPositive = false;
            if (_durationInMilliseconds == Long.MIN_VALUE) {
                _durationInMilliseconds++;
                tooLong = true;
            }
            _durationInMilliseconds *= -1;
        }
        else {
            isPositive = true;
        }
        long val = _durationInMilliseconds;
        int milliseconds = (int) (val % 60000L); 
        if (tooLong) {
            ++milliseconds;
        }
        if (milliseconds % 1000 == 0) {
            int seconds = milliseconds / 1000;
            val = val / 60000L;
            int minutes = (int) (val % 60L); 
            val = val / 60L;
            int hours = (int) (val % 24L); 
            long days = val / 24L;
            if (days <= ((long) Integer.MAX_VALUE)) {
                return newDuration(isPositive, DatatypeConstants.FIELD_UNDEFINED,
                        DatatypeConstants.FIELD_UNDEFINED, (int) days, hours, minutes, seconds);
            }
            else {
                return newDuration(isPositive, null, null,
                        BigInteger.valueOf(days), BigInteger.valueOf(hours), 
                        BigInteger.valueOf(minutes), BigDecimal.valueOf(milliseconds, 3));
            }   
        }
        BigDecimal seconds = BigDecimal.valueOf(milliseconds, 3);
        val = val / 60000L;
        BigInteger minutes = BigInteger.valueOf(val % 60L); 
        val = val / 60L;
        BigInteger hours = BigInteger.valueOf(val % 24L); 
        val = val / 24L;
        BigInteger days = BigInteger.valueOf(val);
        return newDuration(isPositive, null, null, days, hours, minutes, seconds);
    }
    public Duration newDurationDayTime(
            final boolean isPositive,
            final BigInteger day,
            final BigInteger hour,
            final BigInteger minute,
            final BigInteger second) {
        return newDuration(
                isPositive,
                null,  
                null, 
                day,
                hour,
                minute,
                (second != null)? new BigDecimal(second):null
        );
    }
    public Duration newDurationDayTime(
            final boolean isPositive,
            final int day,
            final int hour,
            final int minute,
            final int second) {
        return newDuration(isPositive, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                day, hour, minute, second);
    }
    public Duration newDurationYearMonth(final String lexicalRepresentation) {
        if (lexicalRepresentation == null) {
            throw new NullPointerException("The lexical representation cannot be null.");
        }
        int length = lexicalRepresentation.length();
        for (int i = 0; i < length; ++i) {
            char c = lexicalRepresentation.charAt(i);
            if (c == 'D' || c == 'T') {
                throw new IllegalArgumentException("Invalid yearMonthDuration value: " + lexicalRepresentation);
            }
        }
        return newDuration(lexicalRepresentation);
    }
    public Duration newDurationYearMonth(final long durationInMilliseconds) {
        return newDuration(durationInMilliseconds);
    }
    public Duration newDurationYearMonth(
            final boolean isPositive,
            final BigInteger year,
            final BigInteger month) {
        return newDuration(
                isPositive,
                year,
                month,
                null, 
                null, 
                null, 
                null  
        );
    }
    public Duration newDurationYearMonth(
            final boolean isPositive,
            final int year,
            final int month) {
        return newDuration(isPositive, year, month, 
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED,
                DatatypeConstants.FIELD_UNDEFINED, DatatypeConstants.FIELD_UNDEFINED);
    }
    public abstract XMLGregorianCalendar newXMLGregorianCalendar();
    public abstract XMLGregorianCalendar newXMLGregorianCalendar(final String lexicalRepresentation);
    public abstract XMLGregorianCalendar newXMLGregorianCalendar(final GregorianCalendar cal);
    public abstract XMLGregorianCalendar newXMLGregorianCalendar(
            final BigInteger year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final BigDecimal fractionalSecond,
            final int timezone);
    public XMLGregorianCalendar newXMLGregorianCalendar(
            final int year,
            final int month,
            final int day,
            final int hour,
            final int minute,
            final int second,
            final int millisecond,
            final int timezone) {
        BigInteger realYear = (year != DatatypeConstants.FIELD_UNDEFINED) ? BigInteger.valueOf((long) year) : null;
        BigDecimal realMillisecond = null; 
        if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
            if (millisecond < 0 || millisecond > 1000) {
                throw new IllegalArgumentException(
                        "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendar("
                        + "int year, int month, int day, int hour, int minute, int second, int millisecond, int timezone)"
                        + "with invalid millisecond: " + millisecond
                );
            }
            realMillisecond = BigDecimal.valueOf((long) millisecond, 3);
        }
        return newXMLGregorianCalendar(
                realYear,
                month,
                day,
                hour,
                minute,
                second,
                realMillisecond,
                timezone
        );
    }
    public XMLGregorianCalendar newXMLGregorianCalendarDate(
            final int year,
            final int month,
            final int day,
            final int timezone) {
        return newXMLGregorianCalendar(
                year,
                month,
                day,
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                timezone);
    }
    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final int timezone) {
        return newXMLGregorianCalendar(
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                hours,
                minutes,
                seconds,
                DatatypeConstants.FIELD_UNDEFINED, 
                timezone);
    }
    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final BigDecimal fractionalSecond,
            final int timezone) {
        return newXMLGregorianCalendar(
                null, 
                DatatypeConstants.FIELD_UNDEFINED, 
                DatatypeConstants.FIELD_UNDEFINED, 
                hours,
                minutes,
                seconds,
                fractionalSecond,
                timezone);
    }
    public XMLGregorianCalendar newXMLGregorianCalendarTime(
            final int hours,
            final int minutes,
            final int seconds,
            final int milliseconds,
            final int timezone) {
        BigDecimal realMilliseconds = null; 
        if (milliseconds != DatatypeConstants.FIELD_UNDEFINED) {
            if (milliseconds < 0 || milliseconds > 1000) {
                throw new IllegalArgumentException(
                        "javax.xml.datatype.DatatypeFactory#newXMLGregorianCalendarTime("
                        + "int hours, int minutes, int seconds, int milliseconds, int timezone)"
                        + "with invalid milliseconds: " + milliseconds
                );
            }
            realMilliseconds = BigDecimal.valueOf((long) milliseconds, 3);
        }
        return newXMLGregorianCalendarTime(
                hours,
                minutes,
                seconds,
                realMilliseconds,
                timezone
        );
    }
}
