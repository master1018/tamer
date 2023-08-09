public class AWTPermissionFactory
    implements PermissionFactory<AWTPermission>
{
    @Override
    public AWTPermission newPermission(String name) {
        return new AWTPermission(name);
    }
}
