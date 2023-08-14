public class SerialVersion {
    public static void main(String[] args) throws Exception {
        String dir = System.getProperty("test.src");
        File  sFile =  new File (dir,"SerialVersion.1.2.1");
        ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(sFile));
        PermissionCollection pc = (PermissionCollection)ois.readObject();
        System.out.println("1.2.1 collection = " + pc);
        sFile =  new File (dir,"SerialVersion.1.3.1");
        ois = new ObjectInputStream
                (new FileInputStream(sFile));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("1.3.1 collection = " + pc);
        sFile =  new File (dir,"SerialVersion.1.4");
        ois = new ObjectInputStream
                (new FileInputStream(sFile));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("1.4 collection = " + pc);
        MyPermission mp = new MyPermission("SerialVersionTest");
        PermissionCollection bpc = mp.newPermissionCollection();
        sFile =  new File (dir,"SerialVersion.current");
        ObjectOutputStream oos = new ObjectOutputStream
                (new FileOutputStream("SerialVersion.current"));
        oos.writeObject(bpc);
        oos.close();
        ois = new ObjectInputStream
                (new FileInputStream("SerialVersion.current"));
        pc = (PermissionCollection)ois.readObject();
        System.out.println("current collection = " + pc);
    }
}
class MyPermission extends BasicPermission {
    public MyPermission(String name) {
        super(name);
    }
}
