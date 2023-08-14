public class DynamicPolicy extends Policy{
    static int refresher = 0;
    public DynamicPolicy() {
    }
    public PermissionCollection getPermissions(CodeSource cs) {
        Permissions perms = new Permissions();
        initStaticPolicy(perms);
        if (refresher == 1)
            perms.add(new PropertyPermission("user.name","read"));
        System.err.println("perms=[" + perms + "]");
        return perms;
    }
    public boolean implies(ProtectionDomain pd, Permission p) {
        return getPermissions(pd).implies(p);
    }
    public PermissionCollection getPermissions(ProtectionDomain pd) {
        Permissions perms = new Permissions();
        initStaticPolicy(perms);
        if (refresher == 1)
            perms.add(new PropertyPermission("user.name","read"));
        return perms;
    }
    public void refresh() {
        refresher++;
    }
    private void initStaticPolicy(PermissionCollection perms) {
        perms.add(new java.security.SecurityPermission("getPolicy"));
        perms.add(new java.security.SecurityPermission("setPolicy"));
        perms.add(new java.lang.RuntimePermission("stopThread"));
        perms.add(new java.net.SocketPermission("localhost:1024-", "listen"));
        perms.add(new PropertyPermission("java.version","read"));
        perms.add(new PropertyPermission("java.vendor","read"));
        perms.add(new PropertyPermission("java.vendor.url","read"));
        perms.add(new PropertyPermission("java.class.version","read"));
        perms.add(new PropertyPermission("os.name","read"));
        perms.add(new PropertyPermission("os.version","read"));
        perms.add(new PropertyPermission("os.arch","read"));
        perms.add(new PropertyPermission("file.separator","read"));
        perms.add(new PropertyPermission("path.separator","read"));
        perms.add(new PropertyPermission("line.separator","read"));
        perms.add(new PropertyPermission("java.specification.version", "read"));
        perms.add(new PropertyPermission("java.specification.vendor", "read"));
        perms.add(new PropertyPermission("java.specification.name", "read"));
        perms.add(new PropertyPermission("java.vm.specification.version", "read"));
        perms.add(new PropertyPermission("java.vm.specification.vendor", "read"));
        perms.add(new PropertyPermission("java.vm.specification.name", "read"));
        perms.add(new PropertyPermission("java.vm.version", "read"));
        perms.add(new PropertyPermission("java.vm.vendor", "read"));
        perms.add(new PropertyPermission("java.vm.name", "read"));
        return;
    }
}
