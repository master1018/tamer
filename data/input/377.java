public class UserServiceImpl implements UserService {
    private String usersFile = "";
    public UserServiceImpl(String usersFile) {
        this.usersFile = usersFile;
    }
    @SuppressWarnings({ "AccessStaticViaInstance" })
    public Userinfo getUsers() {
        ConfigFileConstants m_config = new ConfigFileConstants();
        String onmsHome = m_config.getHome();
        Userinfo userinfo = new Userinfo();
        try {
            IBindingFactory bfact = BindingDirectory.getFactory(Userinfo.class);
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            userinfo = (Userinfo) uctx.unmarshalDocument(new FileInputStream(onmsHome + "/etc/" + "users.xml"), null);
        } catch (JiBXException e) {
            System.out.println("JiBX Error: " + e);
        } catch (FileNotFoundException e) {
            System.out.println("File not found!!!");
        }
        return (userinfo);
    }
}
