public class AppendSB {
    private static Random generator = new Random();
    public static void main(String[] args) throws Exception {
        for (int i=0; i<1000; i++) {
            StringBuffer sb1 = generateTestBuffer(10, 100);
            StringBuffer sb2 = generateTestBuffer(10, 100);
            StringBuffer sb3 = generateTestBuffer(10, 100);
            String s1 = sb1.toString();
            String s2 = sb2.toString();
            String s3 = sb3.toString();
            String concatResult = new String(s1+s2+s3);
            StringBuffer test = new StringBuffer();
            test.append(sb1);
            test.append(sb2);
            test.append(sb3);
            if (!test.toString().equals(concatResult))
                throw new RuntimeException("StringBuffer.append failure");
        }
    }
    private static int getRandomIndex(int constraint1, int constraint2) {
        int range = constraint2 - constraint1;
        int x = generator.nextInt(range);
        return constraint1 + x;
    }
    private static StringBuffer generateTestBuffer(int min, int max) {
        StringBuffer aNewStringBuffer = new StringBuffer(120);
        int aNewLength = getRandomIndex(min, max);
        for(int y=0; y<aNewLength; y++) {
            int achar = generator.nextInt(30)+30;
            char test = (char)(achar);
            aNewStringBuffer.append(test);
        }
        return aNewStringBuffer;
    }
}
