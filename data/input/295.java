public class Users {
    private Long user_id;
    private Date age;
    private Integer availible;
    private String firstname;
    private Date lastlogin;
    private String lastname;
    private Long lasttrans;
    private Long level_id;
    private String login;
    private String password;
    private Date regdate;
    private Integer status;
    private Integer title_id;
    private Date starttime;
    private Date updatetime;
    private String pictureuri;
    private String deleted;
    private Long language_id;
    private Adresses adresses;
    private String resethash;
    private String activatehash;
    private Userlevel userlevel;
    private Userdata rechnungsaddressen;
    private Userdata lieferadressen;
    private Usergroups[] usergroups;
    private Set organisation_users;
    private UserSipData userSipData;
    private Long externalUserId;
    private String externalUserType;
    private Sessiondata sessionData;
    public Users() {
        super();
    }
    public Long getUser_id() {
        return user_id;
    }
    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
    public Adresses getAdresses() {
        return adresses;
    }
    public void setAdresses(Adresses adresses) {
        this.adresses = adresses;
    }
    public Date getAge() {
        return age;
    }
    public void setAge(Date age) {
        if (age == null) age = new Date();
        this.age = age;
    }
    public Integer getAvailible() {
        return availible;
    }
    public void setAvailible(Integer availible) {
        this.availible = availible;
    }
    public String getFirstname() {
        return firstname;
    }
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    public Date getLastlogin() {
        return lastlogin;
    }
    public void setLastlogin(Date lastlogin) {
        this.lastlogin = lastlogin;
    }
    public String getLastname() {
        return lastname;
    }
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
    public Long getLasttrans() {
        return lasttrans;
    }
    public void setLasttrans(Long lasttrans) {
        this.lasttrans = lasttrans;
    }
    public Long getLevel_id() {
        return level_id;
    }
    public void setLevel_id(Long level_id) {
        this.level_id = level_id;
    }
    public Userdata getLieferadressen() {
        return lieferadressen;
    }
    public void setLieferadressen(Userdata lieferadressen) {
        this.lieferadressen = lieferadressen;
    }
    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Userdata getRechnungsaddressen() {
        return rechnungsaddressen;
    }
    public void setRechnungsaddressen(Userdata rechnungsaddressen) {
        this.rechnungsaddressen = rechnungsaddressen;
    }
    public Date getRegdate() {
        return regdate;
    }
    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getTitle_id() {
        return title_id;
    }
    public void setTitle_id(Integer title_id) {
        this.title_id = title_id;
    }
    public Usergroups[] getUsergroups() {
        return usergroups;
    }
    public void setUsergroups(Usergroups[] usergroups) {
        this.usergroups = usergroups;
    }
    public Userlevel getUserlevel() {
        return userlevel;
    }
    public void setUserlevel(Userlevel userlevel) {
        this.userlevel = userlevel;
    }
    public Date getStarttime() {
        return starttime;
    }
    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }
    public Date getUpdatetime() {
        return updatetime;
    }
    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
    public String getDeleted() {
        return deleted;
    }
    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
    public String getPictureuri() {
        return pictureuri;
    }
    public void setPictureuri(String pictureuri) {
        this.pictureuri = pictureuri;
    }
    public Long getLanguage_id() {
        return language_id;
    }
    public void setLanguage_id(Long language_id) {
        this.language_id = language_id;
    }
    public Set getOrganisation_users() {
        return organisation_users;
    }
    public void setOrganisation_users(Set organisation_users) {
        this.organisation_users = organisation_users;
    }
    public String getResethash() {
        return resethash;
    }
    public void setResethash(String resethash) {
        this.resethash = resethash;
    }
    public String getActivatehash() {
        return activatehash;
    }
    public void setActivatehash(String activatehash) {
        this.activatehash = activatehash;
    }
    public Long getExternalUserId() {
        return externalUserId;
    }
    public void setExternalUserId(Long externalUserId) {
        this.externalUserId = externalUserId;
    }
    public String getExternalUserType() {
        return externalUserType;
    }
    public void setExternalUserType(String externalUserType) {
        this.externalUserType = externalUserType;
    }
    public Sessiondata getSessionData() {
        return sessionData;
    }
    public void setSessionData(Sessiondata sessionData) {
        this.sessionData = sessionData;
    }
    public UserSipData getUserSipData() {
        return userSipData;
    }
    public void setUserSipData(UserSipData userSipData) {
        this.userSipData = userSipData;
    }
}
