public class ReadLineSync {
    public static int lineCount = 0;
    public static void main( String[] args ) throws Exception {
        String dir = System.getProperty(".", ".");
        File f = new File(dir, "test.txt");
        createFile(f);
        f.deleteOnExit();
        BufferedReader reader = new BufferedReader(
                                new FileReader(f));
        try {
            int threadCount = 2;
            ExecutorService es = Executors.newFixedThreadPool(threadCount);
            for (int i=0; i < threadCount; i++)
                es.execute(new BufferedReaderConsumer(reader));
            es.shutdown();
            while (!es.awaitTermination(60, TimeUnit.SECONDS));
        } finally {
            reader.close();
        }
    }
    static class BufferedReaderConsumer extends Thread {
        BufferedReader reader;
        public BufferedReaderConsumer( BufferedReader reader ) {
            this.reader = reader;
        }
        public void run() {
            try {
                String record = reader.readLine();
                if ( record == null ) {
                    System.out.println( "File already finished" );
                    return;
                }
                if ( record.length() == 0 ) {
                    System.out.println("Empty string on first read." +
                                Thread.currentThread().getName() );
                }
                while ( record != null ) {
                    lineCount++;
                    if ( record.length() == 0 ) {
                        throw new Exception( "Invalid tokens with string '" +
                                record + "' on line " + lineCount );
                    }
                    record = reader.readLine();
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }
    private static void createFile(File f) throws IOException {
        BufferedWriter w = new BufferedWriter(
                           new FileWriter(f));
        int count = 10000;
        while (count > 0) {
            w.write("abcd \r\n");
            w.write("efg \r\n");
            w.write("hijk \r\n");
            w.write("lmnop \r\n");
            w.write("qrstuv \r\n");
            w.write("wxy and z \r\n");
            w.write("now you \r\n");
            w.write("know your \r\n");
            w.write("abc \r\n");
            w.write("next time \r\n");
            w.write("want you \r\n");
            w.write("sing with me \r\n");
            count--;
        }
        w.close();
    }
}
