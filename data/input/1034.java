public class test {
                public void run() {
                    try {
                        int ch;
                        while ((ch = sockIS.read()) != -1) System.out.write(ch);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    System.err.println("End of Stream");
                }
}
