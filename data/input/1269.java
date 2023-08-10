public class AuthorityTypeDaoImpl extends AbstractDao<AuthorityType> implements AuthorityTypeDao {
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(AuthorityTypeDaoImpl.class);
    private static final String INSTANCE_NAME = AuthorityType.class.getName();
    public AuthorityTypeDaoImpl() {
    }
    public AuthorityTypeDaoImpl(SessionManager sessionManager) {
        super(sessionManager);
    }
    @Override
    protected String getInstanceClassName() {
        return INSTANCE_NAME;
    }
}
