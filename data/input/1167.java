public class User extends SecurePerson implements Serializable, UserDetails {
    private static final long serialVersionUID = 3832626162173359411L;
    private Long id;
    private String username;
    private String confirmPassword;
    private String passwordHint;
    private String email;
    private String phoneNumber;
    private Integer version;
    private Set<Role> roles = new HashSet<Role>();
    private boolean enabled;
    public User() {
    }
    public User(final String username) {
        this.username = username;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }
    @Column(nullable = false, length = 50, unique = true)
    public String getUsername() {
        return username;
    }
    @Override
    @Column(nullable = false)
    public String getPassword() {
        return _password;
    }
    @Transient
    public String getConfirmPassword() {
        return confirmPassword;
    }
    @Column(name = "password_hint")
    public String getPasswordHint() {
        return passwordHint;
    }
    @Override
    @Column(name = "first_name", nullable = false, length = 50)
    public String getFirstName() {
        return _firstName;
    }
    @Override
    @Column(name = "last_name", nullable = false, length = 50)
    public String getLastName() {
        return _lastName;
    }
    @Override
    @Column(name = "patronymic", nullable = false, length = 50)
    public String getPatronymic() {
        return _patronymic;
    }
    @Column(name = "email")
    public String getEmail() {
        return email;
    }
    @Column(name = "phone_number")
    public String getPhoneNumber() {
        return phoneNumber;
    }
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = { @JoinColumn(name = "user_id") }, inverseJoinColumns = @JoinColumn(name = "role_id"))
    public Set<Role> getRoles() {
        return roles;
    }
    @Transient
    public List<LabelValue> getRoleList() {
        final List<LabelValue> userRoles = new ArrayList<LabelValue>();
        if (this.roles != null) {
            for (final Role role : roles) {
                userRoles.add(new LabelValue(role.getDescription(), role.getName()));
            }
        }
        return userRoles;
    }
    public void addRole(Role role) {
        getRoles().add(role);
    }
    @Transient
    public GrantedAuthority[] getAuthorities() {
        return roles.toArray(new GrantedAuthority[0]);
    }
    @Version
    public Integer getVersion() {
        return version;
    }
    @Column(name = "account_enabled")
    public boolean isEnabled() {
        return enabled;
    }
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public void setVersion(Integer version) {
        this.version = version;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        final User user = (User) o;
        return !(username != null ? !username.equals(user.getUsername()) : user.getUsername() != null);
    }
    @Override
    public int hashCode() {
        return (username != null ? username.hashCode() : 0);
    }
    @Override
    public String toString() {
        final ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).append("username", this.username).append("enabled", this.enabled);
        final GrantedAuthority[] auths = this.getAuthorities();
        if (auths != null) {
            sb.append("Granted Authorities: ");
            for (int i = 0; i < auths.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(auths[i].toString());
            }
        } else {
            sb.append("No Granted Authorities");
        }
        return sb.toString();
    }
}
