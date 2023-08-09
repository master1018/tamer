public class SnmpEngineParameters implements Serializable {
    private static final long serialVersionUID = 3720556613478400808L;
    private UserAcl uacl = null;
    private String securityFile = null;
    private boolean encrypt = false;
    private SnmpEngineId engineId = null;
    public void setSecurityFile(String securityFile) {
        this.securityFile = securityFile;
    }
    public String getSecurityFile() {
        return securityFile;
    }
    public void setUserAcl(UserAcl uacl) {
        this.uacl = uacl;
    }
    public UserAcl getUserAcl() {
        return uacl;
    }
    public void activateEncryption() {
        this.encrypt = true;
    }
    public void deactivateEncryption() {
        this.encrypt = false;
    }
    public boolean isEncryptionEnabled() {
        return encrypt;
    }
    public void setEngineId(SnmpEngineId engineId) {
        this.engineId = engineId;
    }
    public SnmpEngineId getEngineId() {
        return engineId;
    }
}
