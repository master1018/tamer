public class AllPermissionsImpl extends PermissionImpl {
    public AllPermissionsImpl(String s) {
        super(s);
    }
    public boolean equals(Permission another) {
        return true;
    }
}
