abstract class AbstractAclFileAttributeView
    implements AclFileAttributeView, DynamicFileAttributeView
{
    private static final String OWNER_NAME = "owner";
    private static final String ACL_NAME = "acl";
    @Override
    public final String name() {
        return "acl";
    }
    @Override
    @SuppressWarnings("unchecked")
    public final void setAttribute(String attribute, Object value)
        throws IOException
    {
        if (attribute.equals(OWNER_NAME)) {
            setOwner((UserPrincipal)value);
            return;
        }
        if (attribute.equals(ACL_NAME)) {
            setAcl((List<AclEntry>)value);
            return;
        }
        throw new IllegalArgumentException("'" + name() + ":" +
            attribute + "' not recognized");
    }
    @Override
    public final Map<String,Object> readAttributes(String[] attributes)
        throws IOException
    {
        boolean acl = false;
        boolean owner = false;
        for (String attribute: attributes) {
            if (attribute.equals("*")) {
                owner = true;
                acl = true;
                continue;
            }
            if (attribute.equals(ACL_NAME)) {
                acl = true;
                continue;
            }
            if (attribute.equals(OWNER_NAME)) {
                owner = true;
                continue;
            }
            throw new IllegalArgumentException("'" + name() + ":" +
                attribute + "' not recognized");
        }
        Map<String,Object> result = new HashMap<>(2);
        if (acl)
            result.put(ACL_NAME, getAcl());
        if (owner)
            result.put(OWNER_NAME, getOwner());
        return Collections.unmodifiableMap(result);
    }
}
