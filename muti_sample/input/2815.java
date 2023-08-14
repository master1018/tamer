public class NullX500Name {
    public static void main(String[] argv) throws Exception {
        X500Name subject;
        String name = "";
        subject = new X500Name(name);
        System.out.println("subject:" + subject.toString());
        System.out.println("getCN:" + subject.getCommonName());
        System.out.println("getC:" + subject.getCountry());
        System.out.println("getL:" + subject.getLocality());
        System.out.println("getST:" + subject.getState());
        System.out.println("getName:" + subject.getName());
        System.out.println("getO:" + subject.getOrganization());
        System.out.println("getOU:" + subject.getOrganizationalUnit());
        System.out.println("getType:" + subject.getType());
        DerOutputStream dos = new DerOutputStream();
        subject.encode(dos);
        byte[] out = dos.toByteArray();
        byte[] enc = subject.getEncoded();
        HexDumpEncoder e = new HexDumpEncoder();
        if (Arrays.equals(out, enc))
            System.out.println("Sucess: out:" + e.encodeBuffer(out));
        else {
            System.out.println("Failed: encode:" + e.encodeBuffer(out));
            System.out.println("getEncoded:" + e.encodeBuffer(enc));
        }
        X500Name x = new X500Name(enc);
        if (x.equals(subject))
            System.out.println("Sucess: X500Name(byte[]):" + x.toString());
        else
            System.out.println("Failed: X500Name(byte[]):" + x.toString());
    }
}
