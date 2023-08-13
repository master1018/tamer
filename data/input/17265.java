public class SystemInAvailable {
    public static void main(String[] args) throws Exception {
        byte[] b = new byte[1024];
        System.out.print("Press <enter>: ");
        System.out.flush();
        System.in.read(b);
        int a = System.in.available();
        if (a != 0) throw new Exception("System.in.available() ==> " + a);
    }
}
