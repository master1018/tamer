public class ToString {
    private static Random generator = new Random();
    public static void main(String args[]) throws Exception {
        boolean b = false;
        Boolean B = new Boolean(b);
        if (!B.toString().equals(Boolean.toString(b)))
            throw new RuntimeException("Boolean wrapper toString() failure.");
        b = true;
        B = new Boolean(b);
        if (!B.toString().equals(Boolean.toString(b)))
            throw new RuntimeException("Boolean wrapper toString() failure.");
        for(int x=0; x<100; x++) {
            char c = (char)generator.nextInt();
            Character C = new Character(c);
            if (!C.toString().equals(Character.toString(c)))
                throw new RuntimeException("Character wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            byte y = (byte)generator.nextInt();
            Byte Y = new Byte(y);
            if (!Y.toString().equals(Byte.toString(y)))
                throw new RuntimeException("Byte wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            short s = (short)generator.nextInt();
            Short S = new Short(s);
            if (!S.toString().equals(Short.toString(s)))
                throw new RuntimeException("Short wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            int i = generator.nextInt();
            Integer I = new Integer(i);
            if (!I.toString().equals(Integer.toString(i)))
                throw new RuntimeException("Integer wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            long l = generator.nextLong();
            Long L = new Long(l);
            if (!L.toString().equals(Long.toString(l)))
                throw new RuntimeException("Long wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            float f = generator.nextFloat();
            Float F = new Float(f);
            if (!F.toString().equals(Float.toString(f)))
                throw new RuntimeException("Float wrapper toString() failure.");
        }
        for(int x=0; x<100; x++) {
            double d = generator.nextDouble();
            Double D = new Double(d);
            if (!D.toString().equals(Double.toString(d)))
                throw new RuntimeException("Double wrapper toString() failure.");
        }
    }
}
