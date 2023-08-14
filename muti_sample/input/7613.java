public class PermClass {
    public static void main(String[] args) throws Exception {
        String dir = System.getProperty("test.src");
        if (dir == null) {
            dir = ".";
        }
        MyPermission mp = new MyPermission("PermClass");
        if (args != null && args.length == 1 && args[0] != null) {
            PermissionCollection bpc = mp.newPermissionCollection();
            bpc.add(mp);
            File sFile = new File(dir, "PermClass." + args[0]);
            ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream("PermClass." + args[0]));
            oos.writeObject(bpc);
            oos.close();
            System.exit(0);
        }
        File sFile = new File(dir, "PermClass.1.2.1");
        ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(sFile));
        PermissionCollection pc = (PermissionCollection)ois.readObject();
        System.out.println("1.2.1 collection = " + pc);
        if (pc.implies(mp)) {
            System.out.println("JDK 1.2.1 test passed");
        } else {
            throw new Exception("JDK 1.2.1 test failed");
        }
        sFile = new File(dir, "PermClass.1.3.1");
        ois = new ObjectInputStream(new FileInputStream(sFile));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("1.3.1 collection = " + pc);
        if (pc.implies(mp)) {
            System.out.println("JDK 1.3.1 test passed");
        } else {
            throw new Exception("JDK 1.3.1 test failed");
        }
        sFile = new File(dir, "PermClass.1.4");
        ois = new ObjectInputStream(new FileInputStream(sFile));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("1.4 collection = " + pc);
        if (pc.implies(mp)) {
            System.out.println("JDK 1.4 test 1 passed");
        } else {
            throw new Exception("JDK 1.4 test 1 failed");
        }
        PermissionCollection bpc = mp.newPermissionCollection();
        bpc.add(mp);
        sFile = new File(dir, "PermClass.current");
        ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream("PermClass.current"));
        oos.writeObject(bpc);
        oos.close();
        ois = new ObjectInputStream(new FileInputStream("PermClass.current"));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("current collection = " + pc);
        if (pc.implies(mp)) {
            System.out.println("JDK 1.4 test 2 passed");
        } else {
            throw new Exception("JDK 1.4 test 2 failed");
        }
    }
}
class MyPermission extends BasicPermission {
    public MyPermission(String name) {
        super(name);
    }
}
