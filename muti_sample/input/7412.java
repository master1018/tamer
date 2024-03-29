public class WritePrimitive {
    public static void main (String argv[]) throws IOException {
        System.err.println("\nRegression test for testing of " +
            "serialization/deserialization of primitives \n");
        FileInputStream istream = null;
        FileOutputStream ostream = null;
        try {
            int i = 123456;
            byte b = 12;
            short s = 45;
            char c = 'A';
            long l = 1234567890000L;
            float f = 3.14159f;
            double d = f*2;
            boolean z = true;
            String string = "The String";
            PrimitivesTest prim = new PrimitivesTest();
            ostream = new FileOutputStream("piotest1.tmp");
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            p.writeInt(i);
            p.writeByte(b);
            p.writeShort(s);
            p.writeChar(c);
            p.writeLong(l);
            p.writeFloat(f);
            p.writeDouble(d);
            p.writeBoolean(z);
            p.writeUTF(string);
            p.writeObject(string);
            p.writeObject(prim);
            p.flush();
            istream = new FileInputStream("piotest1.tmp");
            ObjectInputStream q = new ObjectInputStream(istream);
            int i_u = q.readInt();
            byte b_u = q.readByte();
            short s_u = q.readShort();
            char c_u = q.readChar();
            long l_u = q.readLong();
            float f_u = q.readFloat();
            double d_u = q.readDouble();
            boolean z_u = q.readBoolean();
            String string_utf = q.readUTF();
            String string_u = (String)q.readObject();
            if (i != i_u) {
                System.err.println("\nint:  expected " + i + " actual " +i_u);
                throw new Error();
            }
            if (b != b_u) {
                System.err.println("\nbyte:  expected " + b + " actual " +b_u);
                throw new Error();
            }
            if (s != s_u) {
                System.err.println("\nshort:  expected " + s + " actual " +
                                   s_u);
                throw new Error();
            }
            if (c != c_u) {
                System.err.println("\nchar:  expected " + c + " actual " +
                                   c_u);
                throw new Error();
            }
            if (l != l_u) {
                System.err.println("\nlong:  expected " + l + " actual " +
                                   l_u);
                throw new Error();
            }
            if (f != f_u) {
                System.err.println("\nfloat:  expected " + f + " actual " +
                                   f_u);
                throw new Error();
            }
            if (d != d_u) {
                System.err.println("\ndouble:  expected " + d + " actual " +
                                   d_u);
                throw new Error();
            }
            if (z != z_u) {
                System.err.println("\nboolean:  expected " + z + " actual " +
                                   z_u);
                throw new Error();
            }
            if (!string.equals(string_utf)) {
                System.err.println("\nString:  expected " + string +
                                   " actual " + string_utf);
                throw new Error();
            }
            if (!string.equals(string_u)) {
                System.err.println("\nString:  expected " + string +
                                   " actual " + string_u);
                throw new Error();
            }
            PrimitivesTest prim_u = (PrimitivesTest)q.readObject();
            if (!prim.equals(prim_u)) {
                System.err.println("\nTEST FAILED: Read primitive object " +
                    "correctly = " + false);
                System.err.println("\n " + prim);
                System.err.println("\n " + prim_u);
                throw new Error();
            }
            System.err.println("\nTEST PASSED");
        } catch (Exception e) {
            System.err.print("TEST FAILED: ");
            e.printStackTrace();
            System.err.println("\nInput remaining");
            int ch;
            try {
                while ((ch = istream.read()) != -1) {
                    System.err.print("\n " + Integer.toString(ch, 16)+ " ");
                }
                System.out.println("\n ");
            } catch (Exception f) {
                throw new Error();
            }
            throw new Error();
        } finally {
            if (istream != null) istream.close();
            if (ostream != null) ostream.close();
        }
    }
}
