public class Create {
    static final int length = 512;
    public static void main(String[] args) throws Exception {
        String fileName = createFileName(length);
        File file = new File(fileName);
        try {
            boolean result = file.createNewFile();
            if (result) {
                if (!file.exists())
                    throw new RuntimeException("Result is incorrect");
            } else {
                if (file.exists())
                    throw new RuntimeException("Result is incorrect");
            }
        } catch (IOException ioe) {
        }
    }
    public static String createFileName(int length){
        char[] array = new char[length];
        for(int i = 0 ; i < length ; i++)
            array[i] = (char)('0' + (i % 10));
        return new String(array);
    }
}
