public class ConvertSingle {
    public static void main(String args[]) throws Exception {
        try{
            String s = "\n";
            byte ss[] = null;
            String sstring = "x";
            ss = s.getBytes();
            ByteArrayInputStream BAIS = new ByteArrayInputStream(ss);
            InputStreamReader ISR = new InputStreamReader(BAIS, "Unicode");
            BufferedReader BR = new BufferedReader(ISR);
            sstring = BR.readLine();
            BR.close();
            System.out.println(sstring);
        } catch (MalformedInputException e){
            return;
        } catch (java.lang.InternalError e) {
            throw new Exception("ByteToCharUnicode is failing incorrectly for "
                                + " single byte input");
        }
    }
}
