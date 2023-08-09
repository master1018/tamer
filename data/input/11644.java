public class MinMax {
    static void go(String what, float result, float correctResult) {
        String v = what + ": got " + result + ", expected " + correctResult;
        if (!(Float.toString(result).equals(Float.toString(correctResult))))
            throw new RuntimeException(v);
        System.err.println(v);
    }
    static void go(String what, double result, double correctResult) {
        String v = what + ": got " + result + ", expected " + correctResult;
        if (!(Double.toString(result).equals(Double.toString(correctResult))))
            throw new RuntimeException(v);
        System.err.println(v);
    }
    public static void main(String[] args) {
        float fnz = -0.0f;
        float fpz = +0.0f;
        go("Math.min(fnz, fnz)", Math.min(fnz, fnz), fnz);
        go("Math.min(fnz, fpz)", Math.min(fnz, fpz), fnz);
        go("Math.min(fpz, fnz)", Math.min(fpz, fnz), fnz);
        go("Math.min(fpz, fpz)", Math.min(fpz, fpz), fpz);
        go("Math.min(-1.0f, fnz)", Math.min(-1.0f, fnz), -1.0f);
        go("Math.min(-1.0f, fpz)", Math.min(-1.0f, fpz), -1.0f);
        go("Math.min(+1.0f, fnz)", Math.min(+1.0f, fnz), fnz);
        go("Math.min(+1.0f, fpz)", Math.min(+1.0f, fpz), fpz);
        go("Math.min(-1.0f, +1.0f)", Math.min(-1.0f, +1.0f), -1.0f);
        go("Math.min(fnz, -1.0f)", Math.min(fnz, -1.0f), -1.0f);
        go("Math.min(fpz, -1.0f)", Math.min(fpz, -1.0f), -1.0f);
        go("Math.min(fnz, +1.0f)", Math.min(fnz, +1.0f), fnz);
        go("Math.min(fpz, +1.0f)", Math.min(fpz, +1.0f), fpz);
        go("Math.min(+1.0f, -1.0f)", Math.min(+1.0f, -1.0f), -1.0f);
        go("Math.max(fnz, fnz)", Math.max(fnz, fnz), fnz);
        go("Math.max(fnz, fpz)", Math.max(fnz, fpz), fpz);
        go("Math.max(fpz, fnz)", Math.max(fpz, fnz), fpz);
        go("Math.max(fpz, fpz)", Math.max(fpz, fpz), fpz);
        go("Math.max(-1.0f, fnz)", Math.max(-1.0f, fnz), fnz);
        go("Math.max(-1.0f, fpz)", Math.max(-1.0f, fpz), fpz);
        go("Math.max(+1.0f, fnz)", Math.max(+1.0f, fnz), +1.0f);
        go("Math.max(+1.0f, fpz)", Math.max(+1.0f, fpz), +1.0f);
        go("Math.max(-1.0f, +1.0f)", Math.max(-1.0f, +1.0f), +1.0f);
        go("Math.max(fnz, -1.0f)", Math.max(fnz, -1.0f), fnz);
        go("Math.max(fpz, -1.0f)", Math.max(fpz, -1.0f), fpz);
        go("Math.max(fnz, +1.0f)", Math.max(fnz, +1.0f), +1.0f);
        go("Math.max(fpz, +1.0f)", Math.max(fpz, +1.0f), +1.0f);
        go("Math.max(+1.0f, -1.0f)", Math.max(+1.0f, -1.0f), +1.0f);
        double dnz = -0.0d;
        double dpz = +0.0d;
        go("Math.min(dnz, dnz)", Math.min(dnz, dnz), dnz);
        go("Math.min(dnz, dpz)", Math.min(dnz, dpz), dnz);
        go("Math.min(dpz, dnz)", Math.min(dpz, dnz), dnz);
        go("Math.min(dpz, dpz)", Math.min(dpz, dpz), dpz);
        go("Math.min(-1.0d, dnz)", Math.min(-1.0d, dnz), -1.0d);
        go("Math.min(-1.0d, dpz)", Math.min(-1.0d, dpz), -1.0d);
        go("Math.min(+1.0d, dnz)", Math.min(+1.0d, dnz), dnz);
        go("Math.min(+1.0d, dpz)", Math.min(+1.0d, dpz), dpz);
        go("Math.min(-1.0d, +1.0d)", Math.min(-1.0d, +1.0d), -1.0d);
        go("Math.min(dnz, -1.0d)", Math.min(dnz, -1.0d), -1.0d);
        go("Math.min(dpz, -1.0d)", Math.min(dpz, -1.0d), -1.0d);
        go("Math.min(dnz, +1.0d)", Math.min(dnz, +1.0d), dnz);
        go("Math.min(dpz, +1.0d)", Math.min(dpz, +1.0d), dpz);
        go("Math.min(+1.0d, -1.0d)", Math.min(+1.0d, -1.0d), -1.0d);
        go("Math.max(dnz, dnz)", Math.max(dnz, dnz), dnz);
        go("Math.max(dnz, dpz)", Math.max(dnz, dpz), dpz);
        go("Math.max(dpz, dnz)", Math.max(dpz, dnz), dpz);
        go("Math.max(dpz, dpz)", Math.max(dpz, dpz), dpz);
        go("Math.max(-1.0d, dnz)", Math.max(-1.0d, dnz), dnz);
        go("Math.max(-1.0d, dpz)", Math.max(-1.0d, dpz), dpz);
        go("Math.max(+1.0d, dnz)", Math.max(+1.0d, dnz), +1.0d);
        go("Math.max(+1.0d, dpz)", Math.max(+1.0d, dpz), +1.0d);
        go("Math.max(-1.0d, +1.0d)", Math.max(-1.0d, +1.0d), +1.0d);
        go("Math.max(dnz, -1.0d)", Math.max(dnz, -1.0d), dnz);
        go("Math.max(dpz, -1.0d)", Math.max(dpz, -1.0d), dpz);
        go("Math.max(dnz, +1.0d)", Math.max(dnz, +1.0d), +1.0d);
        go("Math.max(dpz, +1.0d)", Math.max(dpz, +1.0d), +1.0d);
        go("Math.max(+1.0d, -1.0d)", Math.max(+1.0d, -1.0d), +1.0d);
    }
}
