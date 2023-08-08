public class UserManagerImpl implements UserManager {
    private DlpUserDao dlpUserDao;
    private final Log logger = LogFactory.getLog(getClass());
    private DlpUserDetailsManager dlpUserDetailsManager;
    public final void setDlpUserDao(final DlpUserDao dDao) {
        this.dlpUserDao = dDao;
    }
    public final void setDlpUserDetailsManager(DlpUserDetailsManager dlpUserDetailsManager) {
        this.dlpUserDetailsManager = dlpUserDetailsManager;
    }
    @Override
    public final List<DlpUser> findUsers() {
        return this.dlpUserDao.findUsers();
    }
    @Override
    public final List<Dataset> findReadOnlyDatasets(DlpUser user) {
        return this.dlpUserDao.findReadOnlyDatasets(user);
    }
    @Override
    public final List<Dataset> findAdminDatasets(DlpUser user) {
        return this.dlpUserDao.findAdminDatasets(user);
    }
    @Override
    public final List<Dataset> listDatasets() {
        return this.dlpUserDao.findDatasets();
    }
    @Override
    public final Set<Integer> findReadOnlyDatasetIds(String userId) {
        return this.dlpUserDao.findReadOnlyDatasetIds(userId);
    }
    @Override
    public final Set<Integer> findAdminDatasetIds(String userId) {
        return this.dlpUserDao.findAdminDatasetIds(userId);
    }
    @Override
    public final Set<Integer> findReadOnlyCategoryIds(String userId) {
        return this.dlpUserDao.findReadOnlyCategoryIds(userId);
    }
    @Override
    public final Set<Integer> findAdminCategoryIds(String userId) {
        return this.dlpUserDao.findAdminCategoryIds(userId);
    }
    @Override
    public void setUserEnabled(DlpUser user, boolean on) {
        dlpUserDetailsManager.setDlpUserEnabled(user, on);
    }
    @Override
    public void setUserAdmin(DlpUser user, boolean on) {
        dlpUserDetailsManager.setDlpUserAdmin(user, on);
    }
    @Override
    public void deleteUser(DlpUser user) {
        dlpUserDetailsManager.deleteUser(user);
    }
    @Override
    public boolean isLoggedIn() {
        return !(SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymous"));
    }
    @Override
    public boolean isUserAdmin() {
        for (GrantedAuthority g : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
            if (g.getAuthority().equals("ROLE_ADMINISTRATOR")) return true;
        }
        return false;
    }
    @Override
    public String getUserDisplayName() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymous".equals(username)) {
            return "anonymous";
        } else {
            return dlpUserDao.getUser(username).getName();
        }
    }
    @Override
    public UserCreationResponse createUser(String email, String password, String givenName, String lastName, String organisation, boolean consent) {
        final boolean DEFAULT_USERS_ENABLED = true;
        if (this.dlpUserDetailsManager.userExists(email)) {
            return new UserCreationResponse(false, false, true, null);
        }
        HashMap<String, String> fieldErrors = new HashMap<String, String>();
        if (email == null || email.indexOf("@") < 1) {
            fieldErrors.put("Email", "Invalid address (Could not find '@').");
        }
        if (givenName == null || givenName.length() == 0) {
            fieldErrors.put("Given Name", "Too short.");
        }
        if (lastName == null || lastName.length() == 0) {
            fieldErrors.put("Last Name", "Too short.");
        }
        if (password == null || password.length() == 0) {
            fieldErrors.put("Password", "Too short.");
        }
        if (organisation == null) {
            fieldErrors.put("Organisation", "Too short.");
        }
        if (consent == false) {
            fieldErrors.put("Consent", "Your consent is required to create an account.");
        }
        if (fieldErrors.size() > 0) {
            return new UserCreationResponse(false, false, false, fieldErrors);
        }
        DlpUser user = new DlpUser();
        user.setAdmin(false);
        user.setEnabled(DEFAULT_USERS_ENABLED);
        user.setSid(email);
        user.setName(givenName + " " + lastName + ", " + organisation);
        user.setProvidedConsent(consent);
        this.dlpUserDetailsManager.createUser(user, password);
        return new UserCreationResponse(true, !DEFAULT_USERS_ENABLED, false, null);
    }
    protected boolean isAnonymous(Authentication auth) {
        return auth.getName().equals("anonymous");
    }
    @Override
    public boolean getProvidedConsent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymous(auth)) {
            return false;
        }
        return dlpUserDao.getUser(auth.getName()).getProvidedConsent();
    }
    @Override
    public void setProvidedConsent(boolean b, HttpServletRequest request) throws IllegalAccessException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (isAnonymous(auth)) {
            throw new IllegalAccessException("Anonymous user can't consent");
        }
        DlpUser user = dlpUserDao.getUser(auth.getName());
        user.setProvidedConsent(b);
        dlpUserDetailsManager.updateDlpUserDetail(user);
        dlpUserDetailsManager.updateAafUserDetail(auth.getName(), request);
    }
}
