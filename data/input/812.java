public class ShibbolethAuthenticationProcessingFilter extends AbstractProcessingFilter {
    protected String shibbolethUsernameHeaderKey = "Shib-TargetedID";
    protected String shibbolethFirstNameHeaderKey = "Shib-InetOrgPerson-givenName";
    protected String shibbolethLastNameHeaderKey = "Shib-Person-surname";
    protected String shibbolethEmailHeaderKey = "Shib-InetOrgPerson-mail";
    ;
    protected String key = null;
    protected ShibbolethUserDetails shibbolethUserDetails;
    protected String defaultRolePrefix = "ROLE_";
    protected String defaultRole = "ROLE_SHIBUSER";
    protected String defaultDomainName = null;
    protected Long defaultDomainId;
    protected boolean migrationEnabled = false;
    protected String migrationTargetUrl = null;
    protected boolean migrationNecessary = false;
    protected boolean redirectOnAuthenticationSuccessEnabled = true;
    protected boolean redirectOnAuthenticationFailureEnabled = true;
    protected boolean processEachUrlEnabled = false;
    protected boolean onlyProcessFilterProcessesUrlEnabled = true;
    protected boolean returnAfterUnsuccessfulAuthentication = false;
    protected boolean returnAfterSuccessfulAuthentication = false;
    public ShibbolethAuthenticationProcessingFilter() {
        super();
        super.setAuthenticationDetailsSource(new ShibbolethAuthenticationDetailsSource());
    }
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        Assert.hasLength(shibbolethUsernameHeaderKey, "shibbolethUsernameHeaderKey must be specified");
        Assert.hasLength(shibbolethFirstNameHeaderKey, "shibbolethFirstNameHeaderKey must be specified");
        Assert.hasLength(shibbolethLastNameHeaderKey, "shibbolethLastNameHeaderKey must be specified");
        Assert.hasLength(shibbolethEmailHeaderKey, "shibbolethEmailHeaderKey must be specified");
        Assert.hasLength(key, "key must be specified");
        Assert.hasLength(defaultRole, "defaultRole must be specified");
        doAfterPropertiesSet();
    }
    protected void doAfterPropertiesSet() throws Exception {
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            throw new ServletException("Can only process HttpServletRequest");
        }
        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("Can only process HttpServletResponse");
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String usernameHeader = httpRequest.getHeader(shibbolethUsernameHeaderKey);
        if (requiresAuthentication(httpRequest, httpResponse) && (usernameHeader != null) && !("".equals(usernameHeader))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request is to process authentication");
            }
            Authentication authResult;
            try {
                onPreAuthentication(httpRequest, httpResponse);
                authResult = attemptAuthentication(httpRequest);
                if (isContinueChainBeforeSuccessfulAuthentication()) {
                    chain.doFilter(request, response);
                }
                successfulAuthentication(httpRequest, httpResponse, authResult);
                if (isReturnAfterSuccessfulAuthentication()) {
                    return;
                }
            } catch (AuthenticationException failed) {
                unsuccessfulAuthentication(httpRequest, httpResponse, failed);
                if (isReturnAfterUnsuccessfulAuthentication()) {
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request) throws AuthenticationException {
        String username = (String) request.getHeader(shibbolethUsernameHeaderKey);
        String password = "";
        GrantedAuthority[] grantedAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl(getDefaultRole()) };
        PrincipalAcegiUserToken authRequest = new PrincipalAcegiUserToken(getKey(), username, password, grantedAuthorities, username);
        setDetails(request, authRequest);
        try {
            request.getSession().setAttribute(AuthenticationProcessingFilter.ACEGI_SECURITY_LAST_USERNAME_KEY, username);
        } catch (IllegalStateException ignored) {
        }
        Authentication authentication = this.getAuthenticationManager().authenticate(authRequest);
        if (isMigrationEnabled() && authentication.equals(authRequest)) {
            setMigrationNecessary(true);
        }
        return authentication;
    }
    @Override
    protected String determineTargetUrl(HttpServletRequest request) {
        String targetUrl = null;
        if (isMigrationEnabled() && isMigrationNecessary()) {
            targetUrl = getMigrationTargetUrl();
            setMigrationNecessary(false);
        } else {
            targetUrl = isAlwaysUseDefaultTargetUrl() ? null : obtainFullRequestUrl(request);
        }
        if (targetUrl == null) {
            targetUrl = getDefaultTargetUrl();
        }
        return targetUrl;
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication success: " + authResult.toString());
        }
        SecurityContextHolder.getContext().setAuthentication(authResult);
        if (logger.isDebugEnabled()) {
            logger.debug("Updated SecurityContextHolder to contain the following Authentication: '" + authResult + "'");
        }
        getRememberMeServices().loginSuccess(request, response, authResult);
        if (this.eventPublisher != null) {
            eventPublisher.publishEvent(new AuthenticationSuccessEvent(authResult));
        }
        onSuccessfulAuthentication(request, response, authResult);
        if (isRedirectOnAuthenticationSuccessEnabled()) {
            String targetUrl = determineTargetUrl(request);
            sendRedirect(request, response, targetUrl);
        }
    }
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(null);
        if (logger.isDebugEnabled()) {
            logger.debug("Updated SecurityContextHolder to contain null Authentication");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication request failed: " + failed.toString());
        }
        try {
            request.getSession().setAttribute(AbstractProcessingFilter.ACEGI_SECURITY_LAST_EXCEPTION_KEY, failed);
        } catch (IllegalStateException ignored) {
        }
        getRememberMeServices().loginFail(request, response);
        onUnsuccessfulAuthentication(request, response, failed);
        if (isRedirectOnAuthenticationFailureEnabled()) {
            String failureUrl = determineFailureUrl(request, failed);
            sendRedirect(request, response, failureUrl);
        }
    }
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticationRequired = ((super.requiresAuthentication(request, response) || isProcessEachUrlEnabled())) && (((authentication == null) || (authentication instanceof AnonymousAuthenticationToken) || (super.requiresAuthentication(request, response) && isOnlyProcessFilterProcessesUrlEnabled())));
        return authenticationRequired;
    }
    public String getDefaultFilterProcessesUrl() {
        return "/j_acegi_security_check";
    }
    protected void setDetails(HttpServletRequest request, PrincipalAcegiUserToken authRequest) {
        authRequest.setDetails(getAuthenticationDetailsSource().buildDetails(request));
    }
    public class ShibbolethAuthenticationDetailsSource implements AuthenticationDetailsSource {
        public Object buildDetails(HttpServletRequest request) {
            shibbolethUserDetails = new ShibbolethUserDetailsImpl();
            shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.USERNAME_KEY, request.getHeader(shibbolethUsernameHeaderKey));
            if (request.getHeader(shibbolethEmailHeaderKey) != null) {
                shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.EMAIL_KEY, ((String) request.getHeader(shibbolethEmailHeaderKey)).toLowerCase(Locale.ENGLISH));
            }
            if (request.getHeader(shibbolethFirstNameHeaderKey) != null) {
                shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.FIRSTNAME_KEY, request.getHeader(shibbolethFirstNameHeaderKey));
            }
            if (request.getHeader(shibbolethLastNameHeaderKey) != null) {
                shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.LASTNAME_KEY, request.getHeader(shibbolethLastNameHeaderKey));
            }
            if (getDefaultDomainName() != null) {
                shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.AUTHENTICATIONDOMAINNAME_KEY, getDefaultDomainName());
            }
            if (getDefaultDomainId() != null) {
                shibbolethUserDetails.getAttributes().put(ShibbolethUserDetailsImpl.AUTHENTICATIONDOMAINID_KEY, getDefaultDomainId());
            }
            return shibbolethUserDetails;
        }
    }
    public String getDefaultRole() {
        return defaultRole;
    }
    public void setDefaultRole(String defaultRole) {
        if (defaultRole.toLowerCase(Locale.ENGLISH).startsWith(defaultRolePrefix.toLowerCase(Locale.ENGLISH))) {
            this.defaultRole = defaultRole;
        } else {
            this.defaultRole = getDefaultRolePrefix() + defaultRole;
        }
    }
    public String getShibbolethUsernameHeaderKey() {
        return shibbolethUsernameHeaderKey;
    }
    public void setShibbolethUsernameHeaderKey(String shibbolethUsernameHeaderKey) {
        this.shibbolethUsernameHeaderKey = shibbolethUsernameHeaderKey;
    }
    public String getShibbolethFirstNameHeaderKey() {
        return shibbolethFirstNameHeaderKey;
    }
    public void setShibbolethFirstNameHeaderKey(String shibbolethFirstNameHeaderKey) {
        this.shibbolethFirstNameHeaderKey = shibbolethFirstNameHeaderKey;
    }
    public String getShibbolethLastNameHeaderKey() {
        return shibbolethLastNameHeaderKey;
    }
    public void setShibbolethLastNameHeaderKey(String shibbolethLastNameHeaderKey) {
        this.shibbolethLastNameHeaderKey = shibbolethLastNameHeaderKey;
    }
    public String getShibbolethEmailHeaderKey() {
        return shibbolethEmailHeaderKey;
    }
    public void setShibbolethEmailHeaderKey(String shibbolethEmailHeaderKey) {
        this.shibbolethEmailHeaderKey = shibbolethEmailHeaderKey;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getDefaultDomainName() {
        return defaultDomainName;
    }
    public void setDefaultDomainName(String defaultDomainName) {
        this.defaultDomainName = defaultDomainName;
    }
    public String getDefaultRolePrefix() {
        return defaultRolePrefix;
    }
    public void setDefaultRolePrefix(String defaultRolePrefix) {
        this.defaultRolePrefix = defaultRolePrefix;
    }
    public Long getDefaultDomainId() {
        return defaultDomainId;
    }
    public void setDefaultDomainId(Long defaultDomainId) {
        this.defaultDomainId = defaultDomainId;
    }
    public boolean isReturnAfterUnsuccessfulAuthentication() {
        return returnAfterUnsuccessfulAuthentication;
    }
    public void setReturnAfterUnsuccessfulAuthentication(boolean returnAfterUnsuccessfulAuthentication) {
        this.returnAfterUnsuccessfulAuthentication = returnAfterUnsuccessfulAuthentication;
    }
    public boolean isReturnAfterSuccessfulAuthentication() {
        return returnAfterSuccessfulAuthentication;
    }
    public void setReturnAfterSuccessfulAuthentication(boolean returnAfterSuccessfulAuthentication) {
        this.returnAfterSuccessfulAuthentication = returnAfterSuccessfulAuthentication;
    }
    public boolean isMigrationEnabled() {
        return migrationEnabled;
    }
    protected void setMigrationEnabled(boolean migrationEnabled) {
        this.migrationEnabled = migrationEnabled;
    }
    public String getMigrationTargetUrl() {
        return migrationTargetUrl;
    }
    public void setMigrationTargetUrl(String migrationTargetUrl) {
        this.migrationTargetUrl = migrationTargetUrl;
        setMigrationEnabled(migrationTargetUrl == null ? false : true);
    }
    protected boolean isMigrationNecessary() {
        return migrationNecessary;
    }
    protected void setMigrationNecessary(boolean migrationNecessary) {
        this.migrationNecessary = migrationNecessary;
    }
    public boolean isProcessEachUrlEnabled() {
        return processEachUrlEnabled;
    }
    public void setProcessEachUrlEnabled(boolean processEachUrlEnabled) {
        this.processEachUrlEnabled = processEachUrlEnabled;
    }
    public boolean isRedirectOnAuthenticationSuccessEnabled() {
        return redirectOnAuthenticationSuccessEnabled;
    }
    public void setRedirectOnAuthenticationSuccessEnabled(boolean redirectOnAuthenticationSuccessEnabled) {
        this.redirectOnAuthenticationSuccessEnabled = redirectOnAuthenticationSuccessEnabled;
    }
    public boolean isRedirectOnAuthenticationFailureEnabled() {
        return redirectOnAuthenticationFailureEnabled;
    }
    public void setRedirectOnAuthenticationFailureEnabled(boolean redirectOnAuthenticationFailureEnabled) {
        this.redirectOnAuthenticationFailureEnabled = redirectOnAuthenticationFailureEnabled;
    }
    public boolean isOnlyProcessFilterProcessesUrlEnabled() {
        return onlyProcessFilterProcessesUrlEnabled;
    }
    public void setOnlyProcessFilterProcessesUrlEnabled(boolean onlyProcessFilterProcessesUrlEnabled) {
        this.onlyProcessFilterProcessesUrlEnabled = onlyProcessFilterProcessesUrlEnabled;
    }
}
