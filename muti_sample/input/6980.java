public class Trim {
    private static Random generator = new Random();
    public static void main(String[] args) throws Exception {
        bash();
    }
    private static void bash() throws Exception {
        for (int i=0; i<1000; i++) {
            StringBuffer sb1 = generateTestBuffer(0, 100);
            StringBuffer sb2 = new StringBuffer(sb1);
            sb1.trimToSize();
            if (!sb1.toString().equals(sb2.toString()))
                throw new RuntimeException(
                    "trim mutated stringbuffer contents");
            StringBuffer sb3 = generateTestBuffer(0, 100);
            sb1.append(sb3);
            sb2.append(sb3);
            if (generator.nextInt(2) == 0)
                sb1.trimToSize();
            else
                sb2.trimToSize();
            if (!sb1.toString().equals(sb2.toString()))
                throw new RuntimeException(
                    "trim mutated stringbuffer contents");
            sb3 = new StringBuffer(100);
            sb3.append("a");
            sb1.append(sb3);
            sb2.append(sb3);
            if (generator.nextInt(2) == 0)
                sb1.trimToSize();
            else
                sb2.trimToSize();
            if (!sb1.toString().equals(sb2.toString()))
                throw new RuntimeException(
                    "trim mutated stringbuffer contents");
        }
    }
    private static void capacityCheck() {
        for (int i=0; i<100; i++) {
            int sizeNeeded = generator.nextInt(1000)+1;
            int sizeExtra = generator.nextInt(100) + 1;
            StringBuffer sb = new StringBuffer(sizeNeeded + sizeExtra);
            StringBuffer sb2 = generateTestBuffer(sizeNeeded, sizeNeeded);
            if (sb2.length() != sizeNeeded)
                throw new RuntimeException("sb generated incorrectly");
            sb.append(sb2);
            int oldCapacity = sb.capacity();
            sb.trimToSize();
            int newCapacity = sb.capacity();
            if (oldCapacity == newCapacity)
                throw new RuntimeException("trim failed");
        }
    }
    private static int getRandomIndex(int constraint1, int constraint2) {
        int range = constraint2 - constraint1;
        if (range <= 0)
            return constraint1;
        int x = generator.nextInt(range);
        return constraint1 + x;
    }
    private static StringBuffer generateTestBuffer(int min, int max) {
        StringBuffer aNewStringBuffer = new StringBuffer();
        int aNewLength = getRandomIndex(min, max);
        for(int y=0; y<aNewLength; y++) {
            int achar = generator.nextInt(30)+30;
            char test = (char)(achar);
            aNewStringBuffer.append(test);
        }
        return aNewStringBuffer;
    }
}
