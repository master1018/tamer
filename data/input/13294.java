public class SetLength {
    public static void main(String[] argv) throws Exception {
        StringBuffer active = new StringBuffer();
        active.append("first one");
        String a = active.toString();
        active.setLength(0);
        active.append("second");
        String b = active.toString();
        active.setLength(0);
        System.out.println("first: " + a);
        System.out.println("second: " + b);
        if (!a.equals("first one")) {
            throw new Exception("StringBuffer.setLength() overwrote string");
        }
    }
}
