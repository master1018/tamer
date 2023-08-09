class BitLevel {
    private BitLevel() {}
    static int bitLength(BigInteger val) {
        val.establishOldRepresentation("BitLevel.bitLength");
        if (val.sign == 0) {
            return 0;
        }
        int bLength = (val.numberLength << 5);
        int highDigit = val.digits[val.numberLength - 1];
        if (val.sign < 0) {
            int i = val.getFirstNonzeroDigit();
            if (i == val.numberLength - 1) {
                highDigit--;
            }
        }
        bLength -= Integer.numberOfLeadingZeros(highDigit);
        return bLength;
    }
    static int bitCount(BigInteger val) {
        val.establishOldRepresentation("BitLevel.bitCount");
        int bCount = 0;
        if (val.sign == 0) {
            return 0;
        }
        int i = val.getFirstNonzeroDigit();;
        if (val.sign > 0) {
            for ( ; i < val.numberLength; i++) {
                bCount += Integer.bitCount(val.digits[i]);
            }
        } else {
            bCount += Integer.bitCount(-val.digits[i]);
            for (i++; i < val.numberLength; i++) {
                bCount += Integer.bitCount(~val.digits[i]);
            }
            bCount = (val.numberLength << 5) - bCount;
        }
        return bCount;
    }
    static boolean testBit(BigInteger val, int n) {
        val.establishOldRepresentation("BitLevel.testBit");
        return ((val.digits[n >> 5] & (1 << (n & 31))) != 0);
    }
    static boolean nonZeroDroppedBits(int numberOfBits, int digits[]) {
        int intCount = numberOfBits >> 5;
        int bitCount = numberOfBits & 31;
        int i;
        for (i = 0; (i < intCount) && (digits[i] == 0); i++) {
            ;
        }
        return ((i != intCount) || (digits[i] << (32 - bitCount) != 0));
    }
    static void shiftLeftOneBit(int result[], int source[], int srcLen) {
        int carry = 0;
        for(int i = 0; i < srcLen; i++) {
            int val = source[i];
            result[i] = (val << 1) | carry;
            carry = val >>> 31;
        }
        if(carry != 0) {
            result[srcLen] = carry;
        }
    }
    static BigInteger shiftLeftOneBit(BigInteger source) {
        source.establishOldRepresentation("BitLevel.shiftLeftOneBit");
        int srcLen = source.numberLength;
        int resLen = srcLen + 1;
        int resDigits[] = new int[resLen];
        shiftLeftOneBit(resDigits, source.digits, srcLen);
        BigInteger result = new BigInteger(source.sign, resLen, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    static BigInteger shiftRight(BigInteger source, int count) {
        source.establishOldRepresentation("BitLevel.shiftRight");
        int intCount = count >> 5; 
        count &= 31; 
        if (intCount >= source.numberLength) {
            return ((source.sign < 0) ? BigInteger.MINUS_ONE : BigInteger.ZERO);
        }
        int i;
        int resLength = source.numberLength - intCount;
        int resDigits[] = new int[resLength + 1];
        shiftRight(resDigits, resLength, source.digits, intCount, count);
        if (source.sign < 0) {
            for (i = 0; (i < intCount) && (source.digits[i] == 0); i++) {
                ;
            }
            if ((i < intCount)
                    || ((count > 0) && ((source.digits[i] << (32 - count)) != 0))) {
                for (i = 0; (i < resLength) && (resDigits[i] == -1); i++) {
                    resDigits[i] = 0;
                }
                if (i == resLength) {
                    resLength++;
                }
                resDigits[i]++;
            }
        }
        BigInteger result = new BigInteger(source.sign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
    static void inplaceShiftRight(BigInteger val, int count) {
        val.establishOldRepresentation("BitLevel.inplaceShiftRight");
        int sign = val.signum();
        if (count == 0 || val.signum() == 0)
            return;
        int intCount = count >> 5; 
        val.numberLength -= intCount;
        if (!shiftRight(val.digits, val.numberLength, val.digits, intCount,
                count & 31)
                && sign < 0) {
            int i;
            for (i = 0; ( i < val.numberLength ) && ( val.digits[i] == -1 ); i++) {
                val.digits[i] = 0;
            }
            if (i == val.numberLength) {
                val.numberLength++;
            }
            val.digits[i]++;
        }
        val.cutOffLeadingZeroes();
        val.unCache();
    }
    static boolean shiftRight(int result[], int resultLen, int source[],
            int intCount, int count) {
        int i;
        boolean allZero = true;
        for (i = 0; i < intCount; i++)
            allZero &= source[i] == 0;
        if (count == 0) {
            System.arraycopy(source, intCount, result, 0, resultLen);
            i = resultLen;
        } else {
            int leftShiftCount = 32 - count;
            allZero &= ( source[i] << leftShiftCount ) == 0;
            for (i = 0; i < resultLen - 1; i++) {
                result[i] = ( source[i + intCount] >>> count )
                | ( source[i + intCount + 1] << leftShiftCount );
            }
            result[i] = ( source[i + intCount] >>> count );
            i++;
        }
        return allZero;
    }
    static BigInteger flipBit(BigInteger val, int n){
        val.establishOldRepresentation("BitLevel.flipBit");
        int resSign = (val.sign == 0) ? 1 : val.sign;
        int intCount = n >> 5;
        int bitN = n & 31;
        int resLength = Math.max(intCount + 1, val.numberLength) + 1;
        int resDigits[] = new int[resLength];
        int i;
        int bitNumber = 1 << bitN;
        System.arraycopy(val.digits, 0, resDigits, 0, val.numberLength);
        if (val.sign < 0) {
            if (intCount >= val.numberLength) {
                resDigits[intCount] = bitNumber;
            } else {
                int firstNonZeroDigit = val.getFirstNonzeroDigit();
                if (intCount > firstNonZeroDigit) {
                    resDigits[intCount] ^= bitNumber;
                } else if (intCount < firstNonZeroDigit) {
                    resDigits[intCount] = -bitNumber;
                    for (i=intCount + 1; i < firstNonZeroDigit; i++) {
                        resDigits[i]=-1;
                    }
                    resDigits[i] = resDigits[i]--;
                } else {
                    i = intCount;
                    resDigits[i] = -((-resDigits[intCount]) ^ bitNumber);
                    if (resDigits[i] == 0) {
                        for (i++; resDigits[i] == -1 ; i++) {
                            resDigits[i] = 0;
                        }
                        resDigits[i]++;
                    }
                }
            }
        } else {
            resDigits[intCount] ^= bitNumber;
        }
        BigInteger result = new BigInteger(resSign, resLength, resDigits);
        result.cutOffLeadingZeroes();
        return result;
    }
}
