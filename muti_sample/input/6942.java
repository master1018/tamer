public class VMVersionMismatchException extends RuntimeException {
    public VMVersionMismatchException(String supported, String target) {
        super();
        supportedVersions = supported;
        targetVersion = target;
    }
    public String getMessage() {
        StringBuffer msg = new StringBuffer();
        msg.append("Supported versions are ");
        msg.append(supportedVersions);
        msg.append(". Target VM is ");
        msg.append(targetVersion);
        return msg.toString();
    }
    public String getSupportedVersions() {
        return supportedVersions;
    }
    public String getTargetVersion() {
        return targetVersion;
    }
    private String supportedVersions;
    private String targetVersion;
}
