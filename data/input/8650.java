public class Ext_AllPolicy {
        public static void main (String[] args) {
                FilePermission mine = new FilePermission("/tmp/bar", "read");
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                        sm.checkPermission(mine);
                }
        }
}
