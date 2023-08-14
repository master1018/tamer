public class MathPerformanceTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public static final double sDouble1 = -2450.50;
    public static final double sDouble2 = -500;
    public static final float sFloat = 300.50f;
    public static final int sInt = 90;
    @Override
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        intermediates.setInternalIterations(ITERATIONS);
        return 0;
    }
    public void testDoubleAbs() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
            result = Math.abs(sDouble1);
        }
    }
    public void testFloatAbs() {
        float result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
            result = Math.abs(sFloat);
        }
    }
    public void testMathSin() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
            result = Math.sin(sDouble1);
        }
    }
    public void testMathCos() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
            result = Math.cos(sDouble1);
        }
    }
    public void testMathTan() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
            result = Math.tan(sDouble1);
        }
    }
    public void testMathASin() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
            result = Math.asin(sDouble1);
        }
    }
    public void testMathACos() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
            result = Math.acos(sDouble1);
        }
    }
    public void testMathATan() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
            result = Math.atan(sDouble1);
        }
    }
    public void testMathLog() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
            result = Math.log(sDouble1);
        }
    }
    public void testMathSqrt() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
            result = Math.sqrt(sDouble1);
        }
    }
    public void testMathCeil() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
            result = Math.ceil(sDouble1);
        }
    }
    public void testMathRound() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
            result = Math.round(sDouble1);
        }
    }
    public void testMathFloor() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
            result = Math.floor(sDouble1);
        }
    }
    public void testMathExp() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
            result = Math.exp(sDouble1);
        }
    }
    public void testMathPow() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
            result = Math.pow(sDouble1, sDouble2);
        }
    }
    public void testMathMax() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
            result = Math.max(sDouble1, sDouble2);
        }
    }
    public void testMathMin() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
            result = Math.min(sDouble1, sDouble2);
        }
    }
    public void testMathRandom() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
            result = Math.random();
        }
    }
    public void testMathIEEERemainder() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
            result = Math.IEEEremainder(sDouble1, sDouble2);
        }
    }
    public void testMathToDegrees() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
            result = Math.toDegrees(sDouble1);
        }
    }
    public void testMathToRadians() {
        double result;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
            result = Math.toRadians(sDouble1);
        }
    }
}
