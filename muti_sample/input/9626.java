public class DerValueConstructor {
    public static void main(String[] args) throws Exception {
        String name = "CN=anne test";
        DerOutputStream debugDER;
        byte[] ba;
        X500Name dn = new X500Name(name);
        System.err.println("DEBUG: dn: " + dn.toString());
        debugDER = new DerOutputStream();
        dn.encode(debugDER);
        ba = debugDER.toByteArray();
        System.err.print("DEBUG: encoded X500Name bytes: ");
        System.out.println(toHexString(ba));
        System.err.println();
        System.out.println("DEBUG: decoding into X500Name ...");
        X500Name dn1 = new X500Name(new DerValue(ba));
        System.err.println("DEBUG: dn1: " + dn1.toString());
        System.err.println();
        dn1 = new X500Name(ba);
        System.err.println("DEBUG: dn1: " + dn1.toString());
        System.err.println();
        dn1 = new X500Name(new DerInputStream(ba));
        System.err.println("DEBUG: dn1: " + dn1.toString());
        System.err.println();
        GeneralName gn = new GeneralName(dn);
        System.err.println("DEBUG: gn: " + gn.toString());
        debugDER = new DerOutputStream();
        gn.encode(debugDER);
        ba = debugDER.toByteArray();
        System.err.print("DEBUG: encoded GeneralName bytes: ");
        System.out.println(toHexString(ba));
        System.err.println();
        System.out.println("DEBUG: decoding into GeneralName ...");
        GeneralName gn1 = new GeneralName(new DerValue(ba));
        System.err.println("DEBUG: gn1: " + gn1.toString());
        System.err.println();
        GeneralSubtree subTree = new GeneralSubtree(gn, 0, -1);
        System.err.println("DEBUG: subTree: " + subTree.toString());
        debugDER = new DerOutputStream();
        subTree.encode(debugDER);
        ba = debugDER.toByteArray();
        System.err.print("DEBUG: encoded GeneralSubtree bytes: ");
        System.out.println(toHexString(ba));
        System.err.println();
        GeneralSubtree debugSubtree = new GeneralSubtree(new DerValue(ba));
    }
    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                            '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}
