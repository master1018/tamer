public class RdnMisc {
    public static void main(String args[])
                throws Exception {
        Attributes attrs = new BasicAttributes();
        attrs.put("a", "Mango");
        attrs.put("l", "Yellow<right");
        attrs.put("b", "Juicy, Fruit");
        attrs.put("k", "Orange>ripe");
        attrs.put("c", "Favourite+Fruit");
        attrs.put("j", "Green#raw");
        attrs.put("d", "Tropical\\Fruit");
        attrs.put("i", "Alfanso;expensive");
        attrs.put("e", "Seasonal\"Fruit");
        attrs.put("h", "Summer");
        attrs.put("f", "Smell=Great");
        attrs.put("g", "Taste==Yummy");
        byte[] mangoJuice = new byte[6];
        for (int i = 0; i < mangoJuice.length; i++) {
            mangoJuice[i] = (byte) i;
        }
        attrs.put("m", mangoJuice);
        Rdn rdn = new Rdn(attrs);
        System.out.println();
        System.out.println("size:" + rdn.size());
        System.out.println("toString():" + rdn.toString());
        System.out.println("getType(): " + rdn.getType());
        System.out.println("getValue(): " + rdn.getValue());
        System.out.println();
        System.out.println("toAttributes(): " + rdn.toAttributes());
        Rdn rdn2 = new Rdn("cn=Juicy\\, Fruit");
        System.out.println("Serializing rdn:" + rdn2);
        ObjectOutputStream out = new ObjectOutputStream(
                                    new FileOutputStream("rdn.ser"));
        out.writeObject(rdn2);
        out.close();
        ObjectInputStream in = new ObjectInputStream(
                                    new FileInputStream("rdn.ser"));
        System.out.println();
        System.out.println("Deserialized RDN:" + in.readObject());
        in.close();
    }
}
