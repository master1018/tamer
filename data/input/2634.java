public class ProcessAttachDebuggee {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        File f = new File(args[0]);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write( Integer.toString(port).getBytes("UTF-8") );
        fos.close();
        System.out.println("Debuggee bound to port: " + port);
        System.out.flush();
        Socket s = ss.accept();
        s.close();
        ss.close();
        System.out.println("Debuggee shutdown.");
    }
}
