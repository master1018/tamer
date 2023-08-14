public class CheckError {
    public static void main(String[] args) throws Exception {
        boolean passTest1 = false;
        File file = new File(System.getProperty("test.dir", "."),
                          "junkie.out");
        FileWriter fw = new FileWriter(file);
        PrintWriter ppw  = new PrintWriter(
                           new PrintWriter(fw));
        fw.close();
        ppw.println("Hello World!");
        file.deleteOnExit();
        if (ppw.checkError()) {
            System.out.println("Correct: An error occured in the" +
                " underlying writer");
            passTest1 = true;
        }
        ppw.close();
        FileOutputStream fos = new FileOutputStream(file);
        PrintWriter pps  = new PrintWriter(
                            new PrintStream(fos));
        fos.close();
        pps.println("Hello World!");
        if (pps.checkError()) {
            System.out.println("Correct: An error occured in the" +
                " underlying Stream");
        } else {
            if (!passTest1) {
                throw new Exception("CheckError() returned an incorrect value" +
                    " when error occured in the underlying Stream" +
                        " and when error occured in the underlying writer");
            } else {
                throw new Exception("CheckError() returned an incorrect value" +
                    " when the error has occured in the underlying Stream");
            }
        }
        if (!passTest1) {
                throw new Exception("CheckError() returned an incorrect value" +
                    " when the error has occured in the underlying Writer");
        }
        pps.close();
    }
}
