public class BitTwiddle {
    private static final int N = 1000; 
    public static void main(String args[]) {
        Random rnd = new Random();
        if (highestOneBit(0) != 0)
            throw new RuntimeException("a");
        if (highestOneBit(-1) != MIN_VALUE)
            throw new RuntimeException("b");
        if (highestOneBit(1) != 1)
            throw new RuntimeException("c");
        if (lowestOneBit(0) != 0)
            throw new RuntimeException("d");
        if (lowestOneBit(-1) != 1)
            throw new RuntimeException("e");
        if (lowestOneBit(MIN_VALUE) != MIN_VALUE)
            throw new RuntimeException("f");
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            if (highestOneBit(x) != reverse(lowestOneBit(reverse(x))))
                throw new RuntimeException("g: " + toHexString(x));
        }
        if (numberOfLeadingZeros(0) != SIZE)
            throw new RuntimeException("h");
        if (numberOfLeadingZeros(-1) != 0)
            throw new RuntimeException("i");
        if (numberOfLeadingZeros(1) != (SIZE - 1))
            throw new RuntimeException("j");
        if (numberOfTrailingZeros(0) != SIZE)
            throw new RuntimeException("k");
        if (numberOfTrailingZeros(1) != 0)
            throw new RuntimeException("l");
        if (numberOfTrailingZeros(MIN_VALUE) != (SIZE - 1))
            throw new RuntimeException("m");
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            if (numberOfLeadingZeros(x) != numberOfTrailingZeros(reverse(x)))
                throw new RuntimeException("n: " + toHexString(x));
        }
        if (bitCount(0) != 0)
                throw new RuntimeException("o");
        for (int i = 0; i < SIZE; i++) {
            int pow2 = 1 << i;
            if (bitCount(pow2) != 1)
                throw new RuntimeException("p: " + i);
            if (bitCount(pow2 -1) != i)
                throw new RuntimeException("q: " + i);
        }
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            if (bitCount(x) != bitCount(reverse(x)))
                throw new RuntimeException("r: " + toHexString(x));
        }
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            int dist = rnd.nextInt();
            if (bitCount(x) != bitCount(rotateRight(x, dist)))
                throw new RuntimeException("s: " + toHexString(x) +
                                           toHexString(dist));
            if (bitCount(x) != bitCount(rotateLeft(x, dist)))
                throw new RuntimeException("t: " + toHexString(x) +
                                           toHexString(dist));
            if (rotateRight(x, dist) != rotateLeft(x, -dist))
                throw new RuntimeException("u: " + toHexString(x) +
                                           toHexString(dist));
            if (rotateRight(x, -dist) != rotateLeft(x, dist))
                throw new RuntimeException("v: " + toHexString(x) +
                                           toHexString(dist));
        }
        if (signum(0) != 0 || signum(1) != 1 || signum(-1) != -1
            || signum(MIN_VALUE) != -1 || signum(MAX_VALUE) != 1)
            throw new RuntimeException("w");
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            int sign = (x < 0 ? -1 : (x == 0 ? 0 : 1));
            if (signum(x) != sign)
                throw new RuntimeException("x: " + toHexString(x));
        }
        if(reverseBytes(0xaabbccdd) != 0xddccbbaa)
            throw new RuntimeException("y");
        for (int i = 0; i < N; i++) {
            int x = rnd.nextInt();
            if (bitCount(x) != bitCount(reverseBytes(x)))
                throw new RuntimeException("z: " + toHexString(x));
        }
    }
}
