public class CompileAllJasmin {
    public static void main(String[] args) throws Exception {
        System.out.println("reading from "+args[0]+" and writing to "+args[1]);
        Properties p = new Properties();
        p.load(new FileInputStream(args[0]));
        int i=0;
        for (Iterator<Object> it_keys = p.keySet().iterator(); it_keys.hasNext();) {
            String file = (String) it_keys.next();
            Main m = new jasmin.Main();
            if (i ==0) {
                m.main(new String[] {"-d" ,args[1], file });
            } else {
                m.main(new String[] {file });
            }
            i++;
        }
    }
}
