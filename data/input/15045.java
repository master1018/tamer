public class SingleEntryRegistry extends RegistryImpl {
    SingleEntryRegistry(int port, String name, Remote object)
            throws RemoteException {
        super(port);
        this.name = name;
        this.object = object;
    }
    SingleEntryRegistry(int port,
                        RMIClientSocketFactory csf,
                        RMIServerSocketFactory ssf,
                        String name,
                        Remote object)
            throws RemoteException {
        super(port, csf, ssf);
        this.name = name;
        this.object = object;
    }
    public String[] list() {
        return new String[] {name};
    }
    public Remote lookup(String name) throws NotBoundException {
        if (name.equals(this.name))
            return object;
        throw new NotBoundException("Not bound: \"" + name + "\" (only " +
                                    "bound name is \"" + this.name + "\")");
    }
    public void bind(String name, Remote obj) throws AccessException {
        throw new AccessException("Cannot modify this registry");
    }
    public void rebind(String name, Remote obj) throws AccessException {
        throw new AccessException("Cannot modify this registry");
    }
    public void unbind(String name) throws AccessException {
        throw new AccessException("Cannot modify this registry");
    }
    private final String name;
    private final Remote object;
    private static final long serialVersionUID = -4897238949499730950L;
}
