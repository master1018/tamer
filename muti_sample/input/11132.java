public class Combine {
    private static HashMap map = new HashMap(10007);
    private static void appendFile(String fileName, boolean keep) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
        lineLoop:
            while (true) {
                String line = br.readLine();
                if (line == null)
                    break;
                if (keep || !map.containsKey(line)) {
                    System.out.println(line);
                    map.put(line,line);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage:  java Combine  file1  file2  ...");
            System.exit(2);
        }
        for (int i = 0; i < args.length; ++i)
            appendFile(args[i], i == 0);
    }
}
