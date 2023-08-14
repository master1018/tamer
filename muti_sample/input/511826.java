class Division {
    static int divideArrayByInt(int dest[], int src[], final int srcLength,
            final int divisor) {
        long rem = 0;
        long bLong = divisor & 0xffffffffL;
        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (rem << 32) | (src[i] & 0xffffffffL);
            long quot;
            if (temp >= 0) {
                quot = (temp / bLong);
                rem = (temp % bLong);
            } else {
                long aPos = temp >>> 1;
                long bPos = divisor >>> 1;
                quot = aPos / bPos;
                rem = aPos % bPos;
                rem = (rem << 1) + (temp & 1);
                if ((divisor & 1) != 0) {
                    if (quot <= rem) {
                        rem -= quot;
                    } else {
                        if (quot - rem <= bLong) {
                            rem += bLong - quot;
                            quot -= 1;
                        } else {
                            rem += (bLong << 1) - quot;
                            quot -= 2;
                        }
                    }
                }
            }
            dest[i] = (int) (quot & 0xffffffffL);
        }
        return (int) rem;
    }
    static int remainderArrayByInt(int src[], final int srcLength,
            final int divisor) {
        long result = 0;
        for (int i = srcLength - 1; i >= 0; i--) {
            long temp = (result << 32) + (src[i] & 0xffffffffL);
            long res = divideLongByInt(temp, divisor);
            result = (int) (res >> 32);
        }
        return (int) result;
    }
    static int remainder(BigInteger dividend, int divisor) {
        dividend.establishOldRepresentation("Division.remainder");
        return remainderArrayByInt(dividend.digits, dividend.numberLength,
                divisor);
    }
    static long divideLongByInt(long a, int b) {
        long quot;
        long rem;
        long bLong = b & 0xffffffffL;
        if (a >= 0) {
            quot = (a / bLong);
            rem = (a % bLong);
        } else {
            long aPos = a >>> 1;
            long bPos = b >>> 1;
            quot = aPos / bPos;
            rem = aPos % bPos;
            rem = (rem << 1) + (a & 1);
            if ((b & 1) != 0) { 
                if (quot <= rem) {
                    rem -= quot;
                } else {
                    if (quot - rem <= bLong) {
                        rem += bLong - quot;
                        quot -= 1;
                    } else {
                        rem += (bLong << 1) - quot;
                        quot -= 2;
                    }
                }
            }
        }
        return (rem << 32) | (quot & 0xffffffffL);
    }
    static BigInteger[] divideAndRemainderByInteger(BigInteger val,
            int divisor, int divisorSign) {
        val.establishOldRepresentation("Division.divideAndRemainderByInteger");
        int[] valDigits = val.digits;
        int valLen = val.numberLength;
        int valSign = val.sign;
        if (valLen == 1) {
            long a = (valDigits[0] & 0xffffffffL);
            long b = (divisor & 0xffffffffL);
            long quo = a / b;
            long rem = a % b;
            if (valSign != divisorSign) {
                quo = -quo;
            }
            if (valSign < 0) {
                rem = -rem;
            }
            return new BigInteger[] { BigInteger.valueOf(quo),
                    BigInteger.valueOf(rem) };
        }
        int quotientLength = valLen;
        int quotientSign = ((valSign == divisorSign) ? 1 : -1);
        int quotientDigits[] = new int[quotientLength];
        int remainderDigits[];
        remainderDigits = new int[] { Division.divideArrayByInt(
                quotientDigits, valDigits, valLen, divisor) };
        BigInteger result0 = new BigInteger(quotientSign, quotientLength,
                quotientDigits);
        BigInteger result1 = new BigInteger(valSign, 1, remainderDigits);
        result0.cutOffLeadingZeroes();
        result1.cutOffLeadingZeroes();
        return new BigInteger[] { result0, result1 };
    }
}
