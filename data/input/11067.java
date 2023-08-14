public class AddToReadOnlyPermissionCollection {
    public static void main(String args[]) throws Exception {
        try {
            if (args.length == 0) {
                tryAllPC();
                tryBasicPC();
                tryFilePC();
                tryPropPC();
                trySockPC();
            } else {
                for (int i=0; i <args.length; i++) {
                    switch (args[i].toLowerCase().charAt(1)) {
                    case 'a':
                        tryAllPC();
                        break;
                    case 'b':
                        tryBasicPC();
                        break;
                    case 'f':
                        tryFilePC();
                        break;
                    case 'p':
                        tryPropPC();
                        break;
                    case 's':
                        trySockPC();
                        break;
                    default:
                        throw new Exception("usage: AddToReadOnlyPermissonCollection [-a -b -f -p -s]");
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
        System.out.println("Passed. OKAY");
    }
    static void tryPropPC() throws Exception {
        try {
            PropertyPermission p0 = new PropertyPermission("user.home","read");
            PermissionCollection pc = p0.newPermissionCollection();
            pc.setReadOnly();   
            PropertyPermission p1 = new PropertyPermission("java.home","read");
            pc.add(p1);
            throw new
                Exception("Failed...PropertyPermission added to readonly PropertyPermissionCollection.");
        } catch (SecurityException se) {
            System.out.println("PropertyPermissionCollection passed");
        }
    }
    static void trySockPC() throws Exception {
        try {
            SocketPermission p0= new SocketPermission("example.com","connect");
            PermissionCollection pc = p0.newPermissionCollection();
            pc.setReadOnly();   
            SocketPermission p1= new SocketPermission("example.net","connect");
            pc.add(p1);
            throw new
                Exception("Failed...SocketPermission added to readonly SocketPermissionCollection.");
        } catch (SecurityException se) {
            System.out.println("SocketPermissionCollection passed");
        }
    }
    static void tryFilePC() throws Exception {
        try {
            FilePermission p0 = new FilePermission("/home/foobar","read");
            PermissionCollection pc = p0.newPermissionCollection();
            pc.setReadOnly();   
            FilePermission p1 = new FilePermission("/home/quux","read");
            pc.add(p1);
            throw new
                Exception("Failed...FilePermission added to readonly FilePermissionCollection.");
        } catch (SecurityException se) {
            System.out.println("FilePermissionCollection passed");
        }
    }
    static void tryBasicPC() throws Exception {
        try {
            MyBasicPermission p0 = new MyBasicPermission("BasicPermision");
            PermissionCollection pc = p0.newPermissionCollection();
            pc.setReadOnly();   
            MyBasicPermission p1 = new MyBasicPermission("EvenMoreBasic");
            pc.add(p1);
            throw new
                Exception("Failed...BasicPermission added to readonly BasicPermissionCollection.");
        } catch (SecurityException se) {
            System.out.println("BasicPermissionCollection passed");
        }
    }
    static void tryAllPC() throws Exception {
        try {
            AllPermission p0 = new AllPermission("AllOfIt","read");
            PermissionCollection pc = p0.newPermissionCollection();
            pc.setReadOnly();   
            AllPermission p1 = new AllPermission("SomeOfIt","read");
            pc.add(p1);
            throw new
                Exception("Failed...AllPermission added to readonly AllPermissionCollection.");
        } catch (SecurityException se) {
            System.out.println("AllPermissionCollection passed");
        }
    }
}
class MyBasicPermission extends BasicPermission {
    public MyBasicPermission(String name)  {
        super(name);
    }
}
