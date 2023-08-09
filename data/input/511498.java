public class MyPermissionCollection extends PermissionCollection {
    private static final long serialVersionUID = -8462474212761656528L;
     public MyPermissionCollection(boolean readOnly) {
         if (readOnly) {
             setReadOnly();
         }
     }
     public void add(Permission permission) {}
     public Enumeration<Permission> elements() {
         return new Enumeration<Permission>() {
             public boolean hasMoreElements() {
                 return false;
             }
             public Permission nextElement() {
                 throw new NoSuchElementException();
             }
         };
     }
     public boolean implies(Permission permission) {
         return false;
     }
 }
