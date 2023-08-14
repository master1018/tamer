public class PasswordCallback implements Callback, Serializable {
    private static final long serialVersionUID = 2267422647454909926L;
    private String prompt;
    boolean echoOn;
    private char[] inputPassword;
    private void setPrompt(String prompt) throws IllegalArgumentException {
        if (prompt == null || prompt.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("auth.14")); 
        }
        this.prompt = prompt;
    }
    public PasswordCallback(String prompt, boolean echoOn) {
        super();
        setPrompt(prompt);
        this.echoOn = echoOn;
    }
    public String getPrompt() {
        return prompt;
    }
    public boolean isEchoOn() {
        return echoOn;
    }
    public void setPassword(char[] password) {
        if (password == null) {
            this.inputPassword = password;
        } else {
            inputPassword = new char[password.length];
            System.arraycopy(password, 0, inputPassword, 0, inputPassword.length);
        }
    }
    public char[] getPassword() {
        if (inputPassword != null) {
            char[] tmp = new char[inputPassword.length];
            System.arraycopy(inputPassword, 0, tmp, 0, tmp.length);
            return tmp;
        }
        return null;
    }
    public void clearPassword() {
        if (inputPassword != null) {
            Arrays.fill(inputPassword, '\u0000');
        }
    }
}
