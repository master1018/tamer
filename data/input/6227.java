public class ExitVM {
    public static void main(String[]args) throws Exception {
        RuntimePermission newWildcard = new RuntimePermission("exitVM.*");
        RuntimePermission oldWildcard = new RuntimePermission("exitVM");
        RuntimePermission other = new RuntimePermission("exitVM.23");
        System.out.println("Testing RuntimePermission(\"exitVM.*\")");
        System.out.println("    testing getName()");
        if (!newWildcard.getName().equals("exitVM.*")) {
            throw new Exception
                ("expected: exitVM.* received:" + newWildcard.getName());
        }
        System.out.println
            ("    testing equals(new RuntimePermission(\"exitVM.*\"))");
        if (!newWildcard.equals(new RuntimePermission("exitVM.*"))) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing equals(new RuntimePermission(\"exitVM.23\"))");
        if (newWildcard.equals(other)) {
            throw new Exception("expected false, received true");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.23\"))");
        if (!newWildcard.implies(other)) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.*\"))");
        if (!newWildcard.implies(new RuntimePermission("exitVM.*"))) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM\"))");
        if (!newWildcard.implies(oldWildcard)) {
            throw new Exception("expected true, received false");
        }
        System.out.println("Testing RuntimePermission(\"exitVM\")");
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.*\"))");
        if (!oldWildcard.implies(newWildcard)) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM\"))");
        if (!oldWildcard.implies(new RuntimePermission("exitVM"))) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.23\"))");
        if (!oldWildcard.implies(other)) {
            throw new Exception("expected true, received false");
        }
        System.out.println("Testing PermissionCollection containing " +
                           "RuntimePermission(\"exitVM.*\")");
        PermissionCollection newPC = newWildcard.newPermissionCollection();
        newPC.add(newWildcard);
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.23\"))");
        if (!newPC.implies(other)) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.*\"))");
        if (!newPC.implies(new RuntimePermission("exitVM.*"))) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM\"))");
        if (!newPC.implies(oldWildcard)) {
            throw new Exception("expected true, received false");
        }
        System.out.println("Testing PermissionCollection containing " +
                           "RuntimePermission(\"exitVM\")");
        PermissionCollection oldPC = oldWildcard.newPermissionCollection();
        oldPC.add(oldWildcard);
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.23\"))");
        if (!oldPC.implies(other)) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM.*\"))");
        if (!oldPC.implies(new RuntimePermission("exitVM.*"))) {
            throw new Exception("expected true, received false");
        }
        System.out.println
            ("    testing implies(new RuntimePermission(\"exitVM\"))");
        if (!oldPC.implies(oldWildcard)) {
            throw new Exception("expected true, received false");
        }
    }
}
