public class RemoveProvider {
    public static void main(String[] args) throws Exception {
        Provider p1 = new MyProvider("name1",1,"");
        Security.addProvider(p1);
        Provider p2 = new MyProvider("name2",1,"");
        Security.addProvider(p2);
        System.out.println("
        Provider[] provs = Security.getProviders();
        for (int i=0; i<provs.length; i++)
            System.out.println(provs[i].toString());
        System.out.println("");
        System.out.println("
        Security.removeProvider("name1");
        provs = Security.getProviders();
        for (int i=0; i<provs.length; i++)
            System.out.println(provs[i].toString());
        System.out.println("");
        System.out.println("
        Map.Entry me = null;
        Set es = p1.entrySet();
        Iterator i = es.iterator();
        while (i.hasNext()) {
            me = (Map.Entry)i.next();
            System.out.println("Key: " + (String)me.getKey());
            System.out.println("Value: " + (String)me.getValue());
        }
        try {
            me.setValue("name1.mac");
            throw new Exception("Expected exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            System.out.println("Expected exception caught");
        }
        System.out.println("");
        System.out.println("
        Object o = null;
        Set ks = p1.keySet();
        i = ks.iterator();
        while (i.hasNext()) {
            o = i.next();
            System.out.println((String)o);
        }
        try {
            ks.remove(o);
            throw new Exception("Expected exception not thrown");
        } catch (UnsupportedOperationException uoe) {
        }
        System.out.println("");
        System.out.println("
        Collection c = p1.values();
        i = c.iterator();
        while (i.hasNext()) {
            System.out.println((String)i.next());
        }
        System.out.println("");
        System.out.println("
        p1.put("Cipher", "name1.des");
        i = es.iterator();
        boolean found = false;
        while (i.hasNext()) {
            me = (Map.Entry)i.next();
            System.out.println("Key: " + (String)me.getKey());
            System.out.println("Value: " + (String)me.getValue());
            if (((String)me.getKey()).equals("Cipher"))
                found = true;
        }
        if (!found)
            throw new Exception("EntrySet not live");
        i = ks.iterator();
        while (i.hasNext()) {
            o = i.next();
            System.out.println((String)o);
        }
        i = c.iterator();
        while (i.hasNext()) {
            System.out.println((String)i.next());
        }
        System.out.println("");
        System.out.println("
        p1.remove("Digest");
        i = es.iterator();
        while (i.hasNext()) {
            me = (Map.Entry)i.next();
            System.out.println("Key: " + (String)me.getKey());
            System.out.println("Value: " + (String)me.getValue());
        }
        i = ks.iterator();
        while (i.hasNext()) {
            o = i.next();
            System.out.println((String)o);
        }
        i = c.iterator();
        while (i.hasNext()) {
            System.out.println((String)i.next());
        }
        es = p1.entrySet();
        i = es.iterator();
        while (i.hasNext()) {
            me = (Map.Entry)i.next();
            System.out.println("Key: " + (String)me.getKey());
            System.out.println("Value: " + (String)me.getValue());
        }
        try {
            me.setValue("name1.mac");
            throw new Exception("Expected exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            System.out.println("Expected exception caught");
        }
    }
}
class MyProvider extends Provider {
    public MyProvider(String name, double version, String info) {
        super(name, version, info);
        put("Signature", name+".signature");
        put("Digest", name+".digest");
    }
}
