public class LongString {
    public static void main(String[] args) throws Exception {
        Random rand = new Random(System.currentTimeMillis());
        ByteArrayOutputStream bout;
        ByteArrayInputStream bin;
        ObjectOutputStream oout;
        ObjectInputStream oin;
        FileInputStream fin;
        File mesgf;
        StringBuffer sbuf = new StringBuffer();
        for (int i = 0; i < 100000; i++)
            sbuf.append((char) rand.nextInt(Character.MAX_VALUE + 1));
        String str = sbuf.toString();
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(str);
        oout.flush();
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new ObjectInputStream(bin);
        String strcopy = (String) oin.readObject();
        if (! str.equals(strcopy))
            throw new Error("deserialized long string not equal to original");
        String mesg = "Message in golden file";
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(mesg);
        oout.flush();
        byte[] buf1 = bout.toByteArray();
        mesgf = new File(System.getProperty("test.src", "."), "mesg.ser");
        fin = new FileInputStream(mesgf);
        bout = new ByteArrayOutputStream();
        try {
            while (fin.available() > 0)
                bout.write(fin.read());
        } finally {
            fin.close();
        }
        byte[] buf2 = bout.toByteArray();
        if (! Arrays.equals(buf1, buf2))
            throw new Error("incompatible string format (write)");
        fin = new FileInputStream(mesgf);
        try {
            oin = new ObjectInputStream(fin);
            String mesgcopy = (String) oin.readObject();
            if (! mesg.equals(mesgcopy))
                throw new Error("incompatible string format (read)");
        } finally {
            fin.close();
        }
    }
}
