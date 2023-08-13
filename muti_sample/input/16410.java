final class DefaultCallbackHandler implements CallbackHandler {
    private char[] passwd;
    private String authenticationID;
    private String authRealm;
    DefaultCallbackHandler(String principal, Object cred, String realm)
        throws IOException {
        authenticationID = principal;
        authRealm = realm;
        if (cred instanceof String) {
            passwd = ((String)cred).toCharArray();
        } else if (cred instanceof char[]) {
            passwd = (char[])((char[])cred).clone();
        } else if (cred != null) {
            String orig = new String((byte[])cred, "UTF8");
            passwd = orig.toCharArray();
        }
    }
    public void handle(Callback[] callbacks)
        throws IOException, UnsupportedCallbackException {
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof NameCallback) {
                    ((NameCallback)callbacks[i]).setName(authenticationID);
                } else if (callbacks[i] instanceof PasswordCallback) {
                    ((PasswordCallback)callbacks[i]).setPassword(passwd);
                } else if (callbacks[i] instanceof RealmChoiceCallback) {
                    String[] choices =
                        ((RealmChoiceCallback)callbacks[i]).getChoices();
                    int selected = 0;
                    if (authRealm != null && authRealm.length() > 0) {
                        selected = -1; 
                        for (int j = 0; j < choices.length; j++) {
                            if (choices[j].equals(authRealm)) {
                                selected = j;
                            }
                        }
                        if (selected == -1) {
                            StringBuffer allChoices = new StringBuffer();
                            for (int j = 0; j <  choices.length; j++) {
                                allChoices.append(choices[j] + ",");
                            }
                            throw new IOException("Cannot match " +
                                "'java.naming.security.sasl.realm' property value, '" +
                                authRealm + "' with choices " + allChoices +
                                "in RealmChoiceCallback");
                        }
                    }
                    ((RealmChoiceCallback)callbacks[i]).setSelectedIndex(selected);
                } else if (callbacks[i] instanceof RealmCallback) {
                    RealmCallback rcb = (RealmCallback) callbacks[i];
                    if (authRealm != null) {
                        rcb.setText(authRealm);  
                    } else {
                        String defaultRealm = rcb.getDefaultText();
                        if (defaultRealm != null) {
                            rcb.setText(defaultRealm); 
                        } else {
                            rcb.setText("");  
                        }
                    }
                } else {
                    throw new UnsupportedCallbackException(callbacks[i]);
                }
            }
    }
    void clearPassword() {
        if (passwd != null) {
            for (int i = 0; i < passwd.length; i++) {
                passwd[i] = '\0';
            }
            passwd = null;
        }
    }
    protected void finalize() throws Throwable {
        clearPassword();
    }
}
