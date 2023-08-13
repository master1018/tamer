public class BigInteger extends Number implements Comparable<BigInteger>,
        Serializable {
    private static final long serialVersionUID = -8287574255936472291L;
    transient BigInt bigInt;
    transient private boolean bigIntIsValid = false;
    transient private boolean oldReprIsValid = false;
    void establishOldRepresentation(String caller) {
        if (!oldReprIsValid) {
            sign = bigInt.sign();
            if (sign != 0) digits = bigInt.littleEndianIntsMagnitude();
            else digits = new int[] { 0 };
            numberLength = digits.length;
            oldReprIsValid = true;
        }
    }
    BigInteger withNewRepresentation(String caller) {
        bigIntIsValid = false;
        return this;
    }
    void validate(String caller, String param) {
        if (bigIntIsValid) {
            if (bigInt == null)
                System.out.print("Claiming bigIntIsValid BUT bigInt == null, ");
            else if (bigInt.getNativeBIGNUM() == 0)
                System.out.print("Claiming bigIntIsValid BUT bigInt.bignum == 0, ");
        }
        else {
            if (oldReprIsValid) { 
                if (bigInt == null) bigInt = new BigInt();
                bigInt.putLittleEndianInts(digits, (sign < 0));
                bigIntIsValid = true;
            }
            else {
                throw new IllegalArgumentException(caller + ":" + param);
            }
        }
    }
    static void validate1(String caller, BigInteger a) {
        a.validate(caller, "1");
    }
    static void validate2(String caller, BigInteger a, BigInteger b) {
        a.validate(caller, "1");
        b.validate(caller, "2");
    }
    static void validate3(String caller, BigInteger a, BigInteger b, BigInteger c) {
        a.validate(caller, "1");
        b.validate(caller, "2");
        c.validate(caller, "3");
    }
    static void validate4(String caller, BigInteger a, BigInteger b, BigInteger c, BigInteger d) {
        a.validate(caller, "1");
        b.validate(caller, "2");
        c.validate(caller, "3");
        d.validate(caller, "4");
    }
    transient int digits[];
    transient int numberLength;
    transient int sign;
    public static final BigInteger ZERO = new BigInteger(0, 0);
    public static final BigInteger ONE = new BigInteger(1, 1);
    public static final BigInteger TEN = new BigInteger(1, 10);
    static final BigInteger MINUS_ONE = new BigInteger(-1, 1);
    static final int EQUALS = 0;
    static final int GREATER = 1;
    static final int LESS = -1;
    static final BigInteger[] SMALL_VALUES = { ZERO, ONE, new BigInteger(1, 2),
            new BigInteger(1, 3), new BigInteger(1, 4), new BigInteger(1, 5),
            new BigInteger(1, 6), new BigInteger(1, 7), new BigInteger(1, 8),
            new BigInteger(1, 9), TEN };
    private transient int firstNonzeroDigit = -2;
    private int signum;
    private byte[] magnitude;
    private transient int hashCode = 0;
    BigInteger(BigInt a) {
        bigInt = a;
        bigIntIsValid = true;
        validate("BigInteger(BigInt)", "this");
    }
    BigInteger(int sign, long value) {
        bigInt = new BigInt();
        bigInt.putULongInt(value, (sign < 0));
        bigIntIsValid = true;
    }
    BigInteger(int sign, int numberLength, int[] digits) {
        this.sign = sign;
        this.numberLength = numberLength;
        this.digits = digits;
        oldReprIsValid = true;
        withNewRepresentation("BigInteger(int sign, int numberLength, int[] digits)");
    }
    public BigInteger(int numBits, Random rnd) {
        if (numBits < 0) {
            throw new IllegalArgumentException(Messages.getString("math.1B")); 
        }
        if (numBits == 0) {
            sign = 0;
            numberLength = 1;
            digits = new int[] { 0 };
        } else {
            sign = 1;
            numberLength = (numBits + 31) >> 5;
            digits = new int[numberLength];
            for (int i = 0; i < numberLength; i++) {
                digits[i] = rnd.nextInt();
            }
            digits[numberLength - 1] >>>= (-numBits) & 31;
            cutOffLeadingZeroes();
        }
        oldReprIsValid = true;
        withNewRepresentation("BigInteger(int numBits, Random rnd)");
    }
    public BigInteger(int bitLength, int certainty, Random rnd) {
        if (bitLength < 2) {
            throw new ArithmeticException(Messages.getString("math.1C")); 
        }
        bigInt = BigInt.generatePrimeDefault(bitLength, rnd, null);
        bigIntIsValid = true;
    }
    public BigInteger(String val) {
        bigInt = new BigInt();
        bigInt.putDecString(val);
        bigIntIsValid = true;
    }
    public BigInteger(String val, int radix) {
        if (val == null) {
            throw new NullPointerException();
        }
        if (radix == 10) {
            bigInt = new BigInt();
            bigInt.putDecString(val);
            bigIntIsValid = true;
        } else if (radix == 16) {
            bigInt = new BigInt();
            bigInt.putHexString(val);
            bigIntIsValid = true;
        } else {
            if ((radix < Character.MIN_RADIX) || (radix > Character.MAX_RADIX)) {
                throw new NumberFormatException(Messages.getString("math.11")); 
            }
            if (val.length() == 0) {
                throw new NumberFormatException(Messages.getString("math.12")); 
            }
            BigInteger.setFromString(this, val, radix);
        }
    }
    public BigInteger(int signum, byte[] magnitude) {
        if (magnitude == null) {
            throw new NullPointerException();
        }
        if ((signum < -1) || (signum > 1)) {
            throw new NumberFormatException(Messages.getString("math.13")); 
        }
        if (signum == 0) {
            for (byte element : magnitude) {
                if (element != 0) {
                    throw new NumberFormatException(Messages.getString("math.14")); 
                }
            }
        }
        bigInt = new BigInt();
        bigInt.putBigEndian(magnitude, (signum < 0));
        bigIntIsValid = true;
    }
    public BigInteger(byte[] val) {
        if (val.length == 0) {
            throw new NumberFormatException(Messages.getString("math.12")); 
        }
        bigInt = new BigInt();
        bigInt.putBigEndianTwosComplement(val);
        bigIntIsValid = true;
    }
    public static BigInteger valueOf(long val) {
        if (val < 0) {
            if(val != -1) {
                return new BigInteger(-1, -val);
            }
            return MINUS_ONE;
        } else if (val <= 10) {
            return SMALL_VALUES[(int) val];
        } else {
            return new BigInteger(1, val);
        }
    }
    public byte[] toByteArray() {
        return twosComplement();
    }
    public BigInteger abs() {
        validate1("abs()", this);
        if (bigInt.sign() >= 0) return this;
        else {
            BigInt a = bigInt.copy();
            a.setSign(1);
            return new BigInteger(a);
        }
    }
    public BigInteger negate() {
        validate1("negate()", this);
        int sign = bigInt.sign();
        if (sign == 0) return this;
        else {
            BigInt a = bigInt.copy();
            a.setSign(-sign);
            return new BigInteger(a);
        }
    }
    public BigInteger add(BigInteger val) {
        validate2("add", this, val);
        if (val.bigInt.sign() == 0) return this;
        if (bigInt.sign() == 0) return val;
        return new BigInteger(BigInt.addition(bigInt, val.bigInt));
    }
    public BigInteger subtract(BigInteger val) {
        validate2("subtract", this, val);
        if (val.bigInt.sign() == 0) return this;
        else return new BigInteger(BigInt.subtraction(bigInt, val.bigInt));
    }
    public int signum() {
        if (oldReprIsValid) return sign;
        validate1("signum", this);
        return bigInt.sign();
    }
    public BigInteger shiftRight(int n) {
        return shiftLeft(-n);
    }
    public BigInteger shiftLeft(int n) {
        if (n == 0) return this;
        int sign = signum();
        if (sign == 0) return this;
        if ((sign > 0) || (n >= 0)) {
            validate1("shiftLeft", this);
            return new BigInteger(BigInt.shift(bigInt, n));
        }
        else {
            return BitLevel.shiftRight(this, -n);
        }
    }
    BigInteger shiftLeftOneBit() {
        return (signum() == 0) ? this : BitLevel.shiftLeftOneBit(this);
    }
    public int bitLength() {
        if (!bigIntIsValid && oldReprIsValid) return BitLevel.bitLength(this);
        validate1("bitLength", this);
        return bigInt.bitLength();
    }
    public boolean testBit(int n) {
        if (n < 0) {
            throw new ArithmeticException(Messages.getString("math.15")); 
        }
        int sign = signum();
        if ((sign > 0) && bigIntIsValid && !oldReprIsValid) {
            validate1("testBit", this);
            return bigInt.isBitSet(n);
        }
        else {
            establishOldRepresentation("testBit");
            if (n == 0) {
                return ((digits[0] & 1) != 0);
            }
            int intCount = n >> 5;
            if (intCount >= numberLength) {
                return (sign < 0);
            }
            int digit = digits[intCount];
            n = (1 << (n & 31)); 
            if (sign < 0) {
                int firstNonZeroDigit = getFirstNonzeroDigit();
                if (  intCount < firstNonZeroDigit  ){
                    return false;
                }else if( firstNonZeroDigit == intCount ){
                    digit = -digit;
                }else{
                    digit = ~digit;
                }
            }
            return ((digit & n) != 0);
        }
    }
    public BigInteger setBit(int n) {
        establishOldRepresentation("setBit");
        if( !testBit( n ) ){
            return BitLevel.flipBit(this, n);
        }else{
            return this;
        }
    }
    public BigInteger clearBit(int n) {
        establishOldRepresentation("clearBit");
        if (testBit(n)) {
            return BitLevel.flipBit(this, n);
        } else {
            return this;
        }
    }
    public BigInteger flipBit(int n) {
        establishOldRepresentation("flipBit");
        if (n < 0) {
            throw new ArithmeticException(Messages.getString("math.15")); 
        }
        return BitLevel.flipBit(this, n);
    }
    public int getLowestSetBit() {
        establishOldRepresentation("getLowestSetBit");
        if (sign == 0) {
            return -1;
        }
        int i = getFirstNonzeroDigit();
        return ((i << 5) + Integer.numberOfTrailingZeros(digits[i]));
    }
    public int bitCount() {
        establishOldRepresentation("bitCount");
        return BitLevel.bitCount(this);
    }
    public BigInteger not() {
        this.establishOldRepresentation("not");
        return Logical.not(this).withNewRepresentation("not");
    }
    public BigInteger and(BigInteger val) {
        this.establishOldRepresentation("and1");
        val.establishOldRepresentation("and2");
        return Logical.and(this, val).withNewRepresentation("and");
    }
    public BigInteger or(BigInteger val) {
        this.establishOldRepresentation("or1");
        val.establishOldRepresentation("or2");
        return Logical.or(this, val).withNewRepresentation("or");
    }
    public BigInteger xor(BigInteger val) {
        this.establishOldRepresentation("xor1");
        val.establishOldRepresentation("xor2");
        return Logical.xor(this, val).withNewRepresentation("xor");
    }
    public BigInteger andNot(BigInteger val) {
        this.establishOldRepresentation("andNot1");
        val.establishOldRepresentation("andNot2");
        return Logical.andNot(this, val).withNewRepresentation("andNot");
    }
    @Override
    public int intValue() {
        if (bigIntIsValid && (bigInt.twosCompFitsIntoBytes(4))) {
            return (int)bigInt.longInt();
        }
        else {
            this.establishOldRepresentation("intValue()");
            return (sign * digits[0]);
        }
    }
    @Override
    public long longValue() {
        if (bigIntIsValid && (bigInt.twosCompFitsIntoBytes(8))) {
            establishOldRepresentation("longValue()");
            return bigInt.longInt();
        }
        else {
            establishOldRepresentation("longValue()");
            long value = (numberLength > 1) ? (((long) digits[1]) << 32)
                    | (digits[0] & 0xFFFFFFFFL) : (digits[0] & 0xFFFFFFFFL);
            return (sign * value);
        }
    }
    @Override
    public float floatValue() {
        establishOldRepresentation("floatValue()");
        return (float) doubleValue();
    }
    @Override
    public double doubleValue() {
        establishOldRepresentation("doubleValue()");
        return Conversion.bigInteger2Double(this);
    }
    public int compareTo(BigInteger val) {
        validate2("compareTo", this, val);
        return BigInt.cmp(bigInt, val.bigInt);
    }
    public BigInteger min(BigInteger val) {
        return ((this.compareTo(val) == -1) ? this : val);
    }
    public BigInteger max(BigInteger val) {
        return ((this.compareTo(val) == 1) ? this : val);
    }
    @Override
    public int hashCode() {
        validate1("hashCode", this);
        if (hashCode != 0) {
            return hashCode;    
        }          
        establishOldRepresentation("hashCode");
        for (int i = 0; i < digits.length; i ++) {
            hashCode = (int)(hashCode * 33 + (digits[i] & 0xffffffff));            
        }  
        hashCode = hashCode * sign;
        return hashCode;
    }
    @Override
    public boolean equals(Object x) {
        if (this == x) {
            return true;
        }
        if (x instanceof BigInteger) {
            return this.compareTo((BigInteger)x) == 0;
        }
        return false;
    } 
    @Override
    public String toString() {
        validate1("toString()", this);
        return bigInt.decString();
    }
    public String toString(int radix) {
        validate1("toString(int radix)", this);
        if (radix == 10) {
            return bigInt.decString();
        } else {
            establishOldRepresentation("toString(int radix)");
            return Conversion.bigInteger2String(this, radix);
        }
   }
    public BigInteger gcd(BigInteger val) {
        validate2("gcd", this, val);
        return new BigInteger(BigInt.gcd(bigInt, val.bigInt, null));
    }
    public BigInteger multiply(BigInteger val) {
        validate2("multiply", this, val);
        return new BigInteger(BigInt.product(bigInt, val.bigInt, null));
    }
    public BigInteger pow(int exp) {
        if (exp < 0) {
            throw new ArithmeticException(Messages.getString("math.16")); 
        }
        validate1("pow", this);
        return new BigInteger(BigInt.exp(bigInt, exp, null));
    }
    public BigInteger[] divideAndRemainder(BigInteger divisor) {
        validate2("divideAndRemainder", this, divisor);
        BigInt quotient = new BigInt();
        BigInt remainder = new BigInt();
        BigInt.division(bigInt, divisor.bigInt, null, quotient, remainder);
        BigInteger[] a = new BigInteger[2];
        a[0] = new BigInteger(quotient);
        a[1] = new BigInteger(remainder);
        a[0].validate("divideAndRemainder", "quotient");
        a[1].validate("divideAndRemainder", "remainder");
        return a;
    }
    public BigInteger divide(BigInteger divisor) {
        validate2("divide", this, divisor);
        BigInt quotient = new BigInt();
        BigInt.division(bigInt, divisor.bigInt, null, quotient, null);
        return new BigInteger(quotient);
    }
    public BigInteger remainder(BigInteger divisor) {
        validate2("remainder", this, divisor);
        BigInt remainder = new BigInt();
        BigInt.division(bigInt, divisor.bigInt, null, null, remainder);
        return new BigInteger(remainder);
    }
    public BigInteger modInverse(BigInteger m) {
        if (m.signum() <= 0) {
            throw new ArithmeticException(Messages.getString("math.18")); 
        }
        validate2("modInverse", this, m);
        return new BigInteger(BigInt.modInverse(bigInt, m.bigInt, null));
    }
    public BigInteger modPow(BigInteger exponent, BigInteger m) {
        if (m.signum() <= 0) {
            throw new ArithmeticException(Messages.getString("math.18")); 
        }
        BigInteger base;
        if (exponent.signum() < 0) {
            base = modInverse(m);
        } else {
            base = this;
        }
        validate3("modPow", base, exponent, m);
        return new BigInteger(BigInt.modExp(base.bigInt, exponent.bigInt, m.bigInt, null));
    }
    public BigInteger mod(BigInteger m) {
        if (m.signum() <= 0) {
            throw new ArithmeticException(Messages.getString("math.18")); 
        }
        validate2("mod", this, m);
        return new BigInteger(BigInt.modulus(bigInt, m.bigInt, null));
    }
    public boolean isProbablePrime(int certainty) {
        validate1("isProbablePrime", this);
        return bigInt.isPrime(certainty, null, null);
    }
    public BigInteger nextProbablePrime() {
        if (sign < 0) {
            throw new ArithmeticException(Messages.getString("math.1A", this)); 
        }
        return Primality.nextProbablePrime(this);
    }
    public static BigInteger probablePrime(int bitLength, Random rnd) {
        return new BigInteger(bitLength, 100, rnd);
    }
    private byte[] twosComplement() {
        establishOldRepresentation("twosComplement()");
        if( this.sign == 0 ){
            return new byte[]{0};
        }
        BigInteger temp = this;
        int bitLen = bitLength();
        int iThis = getFirstNonzeroDigit();
        int bytesLen = (bitLen >> 3) + 1;
        byte[] bytes = new byte[bytesLen];
        int firstByteNumber = 0;
        int highBytes;
        int digitIndex = 0;
        int bytesInInteger = 4;
        int digit;
        int hB;
        if (bytesLen - (numberLength << 2) == 1) {
            bytes[0] = (byte) ((sign < 0) ? -1 : 0);
            highBytes = 4;
            firstByteNumber++;
        } else {
            hB = bytesLen & 3;
            highBytes = (hB == 0) ? 4 : hB;
        }
        digitIndex = iThis;
        bytesLen -= iThis << 2;
        if (sign < 0) {
            digit = -temp.digits[digitIndex];
            digitIndex++;
            if(digitIndex == numberLength){
                bytesInInteger = highBytes;
            }
            for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                bytes[--bytesLen] = (byte) digit;
            }
            while( bytesLen > firstByteNumber ){
                digit = ~temp.digits[digitIndex];
                digitIndex++;
                if(digitIndex == numberLength){
                    bytesInInteger = highBytes;
                }
                for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                    bytes[--bytesLen] = (byte) digit;
                }
            }
        } else {
            while (bytesLen > firstByteNumber) {
                digit = temp.digits[digitIndex];
                digitIndex++;
                if (digitIndex == numberLength) {
                    bytesInInteger = highBytes;
                }
                for (int i = 0; i < bytesInInteger; i++, digit >>= 8) {
                    bytes[--bytesLen] = (byte) digit;
                }
            }
        }
        return bytes;
    }
    static int multiplyByInt(int res[], int a[], final int aSize, final int factor) {
        long carry = 0;
        for (int i = 0; i < aSize; i++) {
            carry += (a[i] & 0xFFFFFFFFL) * (factor & 0xFFFFFFFFL);
            res[i] = (int)carry;
            carry >>>= 32;
        }
        return (int)carry;
    }
    static int inplaceAdd(int a[], final int aSize, final int addend) {
        long carry = addend & 0xFFFFFFFFL;
        for (int i = 0; (carry != 0) && (i < aSize); i++) {
            carry += a[i] & 0xFFFFFFFFL;
            a[i] = (int) carry;
            carry >>= 32;
        }
        return (int) carry;
    }
    private static void setFromString(BigInteger bi, String val, int radix) {
        int sign;
        int[] digits;
        int numberLength;
        int stringLength = val.length();
        int startChar;
        int endChar = stringLength;
        if (val.charAt(0) == '-') {
            sign = -1;
            startChar = 1;
            stringLength--;
        } else {
            sign = 1;
            startChar = 0;
        }
        int charsPerInt = Conversion.digitFitInInt[radix];
        int bigRadixDigitsLength = stringLength / charsPerInt;
        int topChars = stringLength % charsPerInt;
        if (topChars != 0) {
            bigRadixDigitsLength++;
        }
        digits = new int[bigRadixDigitsLength];
        int bigRadix = Conversion.bigRadices[radix - 2];
        int digitIndex = 0; 
        int substrEnd = startChar + ((topChars == 0) ? charsPerInt : topChars);
        int newDigit;
        for (int substrStart = startChar; substrStart < endChar; substrStart = substrEnd, substrEnd = substrStart
                + charsPerInt) {
            int bigRadixDigit = Integer.parseInt(val.substring(substrStart,
                    substrEnd), radix);
            newDigit = multiplyByInt(digits, digits, digitIndex, bigRadix);
            newDigit += inplaceAdd(digits, digitIndex, bigRadixDigit);
            digits[digitIndex++] = newDigit;
        }
        numberLength = digitIndex;
        bi.sign = sign;
        bi.numberLength = numberLength;
        bi.digits = digits;
        bi.cutOffLeadingZeroes();
        bi.oldReprIsValid = true;
        bi.withNewRepresentation("Cordoba-BigInteger: private static setFromString");
    }
    final void cutOffLeadingZeroes() {
        while ((numberLength > 0) && (digits[--numberLength] == 0)) {
            ;
        }
        if (digits[numberLength++] == 0) {
            sign = 0;
        }
    }
    boolean isOne() {
        return ((numberLength == 1) && (digits[0] == 1));
    }
    int getFirstNonzeroDigit(){
        if( firstNonzeroDigit == -2 ){
            int i;
            if( this.sign == 0  ){
                i = -1;
            } else{
                for(i=0; digits[i]==0; i++)
                    ;
            }
            firstNonzeroDigit = i;
        }
        return firstNonzeroDigit;
    }
    BigInteger copy() {
        establishOldRepresentation("copy()");
        int[] copyDigits = new int[numberLength];
        System.arraycopy(digits, 0, copyDigits, 0, numberLength);
        return new BigInteger(sign, numberLength, copyDigits);
    }
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        bigInt = new BigInt();
        bigInt.putBigEndian(magnitude, (signum < 0));
        bigIntIsValid = true;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        validate("writeObject", "this");
        signum = bigInt.sign();
            magnitude = bigInt.bigEndianMagnitude();
        out.defaultWriteObject();
    }
    void unCache(){
        firstNonzeroDigit = -2;
    }
}
