public class SBBasher {
    public static void main(String[] args) throws Exception {
        SBBasher basher = new SBBasher();
        for (int iterations=0; iterations<100; iterations++) {
            String testString = basher.generateTestString();
            boolean result = basher.Test1(testString);
            if (result == false)
                throw new RuntimeException("Substring or replace failure.");
        }
        for (int iterations=0; iterations<100; iterations++) {
            String testString = basher.generateTestString();
            boolean result = basher.Test2(testString);
            if (result == false)
                throw new RuntimeException("Insert or delete failure.");
        }
        for (int iterations=0; iterations<100; iterations++) {
            String testString = basher.generateTestString();
            boolean result = basher.Test3(testString);
            if (result == false)
              throw new RuntimeException("Insert, delete or replace failure.");
        }
    }
    private int getRandomIndex(int constraint1, int constraint2) {
        int range = constraint2 - constraint1;
        int x = generator.nextInt();
        return constraint1 + Math.abs(x % range);
    }
    static Random generator = new Random();
    private String generateTestString() {
        StringBuffer aNewString = new StringBuffer(120);
        int aNewLength = getRandomIndex(1,100);
        for(int y=0; y<aNewLength; y++) {
            int achar = generator.nextInt();
            char test = (char)(achar);
            aNewString.append(test);
        }
        return aNewString.toString();
    }
    private boolean Test1(String before) {
        StringBuffer bashed = new StringBuffer(before);
        String slice;
        for (int i=0; i<100; i++) {
            int startIndex = getRandomIndex(0, before.length());
            int endIndex = getRandomIndex(startIndex, before.length());
            if (endIndex < bashed.length()) {
                slice = bashed.substring(startIndex, endIndex);
            }
            else {
                slice = bashed.substring(startIndex);
            }
            bashed.replace(startIndex, endIndex, slice);
        }
        String after = bashed.toString();
        if (!before.equals(after))
            return false;
        else
            return true;
    }
    private boolean Test2(String before) {
        StringBuffer bashed = new StringBuffer(before);
        String slice;
        for (int i=0; i<100; i++) {
            int startIndex = getRandomIndex(0, before.length());
            int endIndex = getRandomIndex(startIndex, before.length());
            if (endIndex < bashed.length())
                slice = bashed.substring(startIndex, endIndex);
            else
                slice = bashed.substring(startIndex);
            if (slice.length() == 1)
                bashed.deleteCharAt(startIndex);
            else
                bashed.delete(startIndex, endIndex);
            bashed.insert(startIndex, slice.toCharArray(), 0, slice.length());
        }
        String after = bashed.toString();
        if (!before.equals(after))
            return false;
        else
            return true;
    }
    private boolean Test3(String before) {
        StringBuffer bashed1 = new StringBuffer(before);
        StringBuffer bashed2 = new StringBuffer(before);
        int startIndex = getRandomIndex(0, bashed1.length());
        int endIndex = getRandomIndex(startIndex, bashed2.length());
        String insertString = generateTestString();
        bashed1.delete(startIndex, endIndex);
        bashed1.insert(startIndex, insertString);
        bashed2.replace(startIndex, endIndex, insertString);
        String result1 = bashed1.toString();
        String result2 = bashed2.toString();
        if (!result1.equals(result2))
            return false;
        else
            return true;
    }
}
