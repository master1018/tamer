public class ServiceTag {
    private String instanceURN;
    private String productName;
    private String productVersion;
    private String productURN;
    private String productParent;
    private String productParentURN;
    private String productDefinedInstanceID;
    private String productVendor;
    private String platformArch;
    private String container;
    private String source;
    private int installerUID;
    private Date timestamp;
    private final int MAX_URN_LEN             = 256 - 1;
    private final int MAX_PRODUCT_NAME_LEN    = 256 - 1;
    private final int MAX_PRODUCT_VERSION_LEN = 64 - 1;
    private final int MAX_PRODUCT_PARENT_LEN  = 256 - 1;
    private final int MAX_PRODUCT_VENDOR_LEN  = 64 - 1;
    private final int MAX_PLATFORM_ARCH_LEN   = 64 - 1;
    private final int MAX_CONTAINER_LEN       = 64 - 1;
    private final int MAX_SOURCE_LEN          = 64 - 1;
    private ServiceTag() {
    }
    ServiceTag(String instanceURN,
               String productName,
               String productVersion,
               String productURN,
               String productParent,
               String productParentURN,
               String productDefinedInstanceID,
               String productVendor,
               String platformArch,
               String container,
               String source,
               int installerUID,
               Date timestamp) {
        setInstanceURN(instanceURN);
        setProductName(productName);
        setProductVersion(productVersion);
        setProductURN(productURN);
        setProductParentURN(productParentURN);
        setProductParent(productParent);
        setProductDefinedInstanceID(productDefinedInstanceID);
        setProductVendor(productVendor);
        setPlatformArch(platformArch);
        setContainer(container);
        setSource(source);
        setInstallerUID(installerUID);
        setTimestamp(timestamp);
    }
    public static ServiceTag newInstance(String productName,
                                         String productVersion,
                                         String productURN,
                                         String productParent,
                                         String productParentURN,
                                         String productDefinedInstanceID,
                                         String productVendor,
                                         String platformArch,
                                         String container,
                                         String source) {
          return new ServiceTag("", 
                                productName,
                                productVersion,
                                productURN,
                                productParent,
                                productParentURN,
                                productDefinedInstanceID,
                                productVendor,
                                platformArch,
                                container,
                                source,
                                -1,
                                null);
    }
    public static ServiceTag newInstance(String instanceURN,
                                         String productName,
                                         String productVersion,
                                         String productURN,
                                         String productParent,
                                         String productParentURN,
                                         String productDefinedInstanceID,
                                         String productVendor,
                                         String platformArch,
                                         String container,
                                         String source) {
          return new ServiceTag(instanceURN,
                                productName,
                                productVersion,
                                productURN,
                                productParent,
                                productParentURN,
                                productDefinedInstanceID,
                                productVendor,
                                platformArch,
                                container,
                                source,
                                -1,
                                null);
    }
    static ServiceTag newInstanceWithUrnTimestamp(ServiceTag st) {
        String instanceURN =
            (st.getInstanceURN().length() == 0 ? Util.generateURN() :
                                                 st.getInstanceURN());
        ServiceTag svcTag = new ServiceTag(instanceURN,
                                           st.getProductName(),
                                           st.getProductVersion(),
                                           st.getProductURN(),
                                           st.getProductParent(),
                                           st.getProductParentURN(),
                                           st.getProductDefinedInstanceID(),
                                           st.getProductVendor(),
                                           st.getPlatformArch(),
                                           st.getContainer(),
                                           st.getSource(),
                                           st.getInstallerUID(),
                                           new Date());
        return svcTag;
    }
    public static String generateInstanceURN() {
        return Util.generateURN();
    }
    public String getInstanceURN() {
        return instanceURN;
    }
    public String getProductName() {
        return productName;
    }
    public String getProductVersion() {
        return productVersion;
    }
    public String getProductURN() {
        return productURN;
    }
    public String getProductParentURN() {
        return productParentURN;
    }
    public String getProductParent() {
        return productParent;
    }
    public String getProductDefinedInstanceID() {
        return productDefinedInstanceID;
    }
    public String getProductVendor() {
        return productVendor;
    }
    public String getPlatformArch() {
        return platformArch;
    }
    public Date getTimestamp() {
        if (timestamp != null) {
            return (Date) timestamp.clone();
        } else {
            return null;
        }
    }
    public String getContainer() {
        return container;
    }
    public String getSource() {
        return source;
    }
    public int getInstallerUID() {
        return installerUID;
    }
    private void setInstanceURN(String instanceURN) {
        if (instanceURN == null) {
            throw new NullPointerException("Parameter instanceURN cannot be null");
        }
        if (instanceURN.length() > MAX_URN_LEN) {
            throw new IllegalArgumentException("instanceURN \"" + instanceURN +
                "\" exceeds maximum length " + MAX_URN_LEN);
        }
        this.instanceURN = instanceURN;
    }
    private void setProductName(String productName) {
        if (productName == null) {
            throw new NullPointerException("Parameter productName cannot be null");
        }
        if (productName.length() == 0) {
            throw new IllegalArgumentException("product name cannot be empty");
        }
        if (productName.length() > MAX_PRODUCT_NAME_LEN) {
            throw new IllegalArgumentException("productName \"" + productName +
                "\" exceeds maximum length " + MAX_PRODUCT_NAME_LEN);
        }
        this.productName = productName;
    }
    private void setProductVersion(String productVersion) {
        if (productVersion == null) {
            throw new NullPointerException("Parameter productVersion cannot be null");
        }
        if (productVersion.length() == 0) {
            throw new IllegalArgumentException("product version cannot be empty");
        }
        if (productVersion.length() > MAX_PRODUCT_VERSION_LEN) {
            throw new IllegalArgumentException("productVersion \"" +
                productVersion + "\" exceeds maximum length " +
                MAX_PRODUCT_VERSION_LEN);
        }
        this.productVersion = productVersion;
    }
    private void setProductURN(String productURN) {
        if (productURN == null) {
            throw new NullPointerException("Parameter productURN cannot be null");
        }
        if (productURN.length() == 0) {
            throw new IllegalArgumentException("product URN cannot be empty");
        }
        if (productURN.length() > MAX_URN_LEN) {
            throw new IllegalArgumentException("productURN \"" + productURN +
                "\" exceeds maximum length " + MAX_URN_LEN);
        }
        this.productURN = productURN;
    }
    private void setProductParentURN(String productParentURN) {
        if (productParentURN == null) {
            throw new NullPointerException("Parameter productParentURN cannot be null");
        }
        if (productParentURN.length() > MAX_URN_LEN) {
            throw new IllegalArgumentException("productParentURN \"" +
                productParentURN + "\" exceeds maximum length " +
                MAX_URN_LEN);
        }
        this.productParentURN = productParentURN;
    }
    private void setProductParent(String productParent) {
        if (productParent == null) {
            throw new NullPointerException("Parameter productParent cannot be null");
        }
        if (productParent.length() == 0) {
            throw new IllegalArgumentException("product parent cannot be empty");
        }
        if (productParent.length() > MAX_PRODUCT_PARENT_LEN) {
            throw new IllegalArgumentException("productParent \"" +
                productParent + "\" exceeds maximum length " +
                MAX_PRODUCT_PARENT_LEN);
        }
        this.productParent = productParent;
    }
    void setProductDefinedInstanceID(String productDefinedInstanceID) {
        if (productDefinedInstanceID == null) {
            throw new NullPointerException("Parameter productDefinedInstanceID cannot be null");
        }
        if (productDefinedInstanceID.length() > MAX_URN_LEN) {
            throw new IllegalArgumentException("productDefinedInstanceID \"" +
                productDefinedInstanceID + "\" exceeds maximum length " +
                MAX_URN_LEN);
        }
        this.productDefinedInstanceID = productDefinedInstanceID;
    }
    private void setProductVendor(String productVendor) {
        if (productVendor == null) {
            throw new NullPointerException("Parameter productVendor cannot be null");
        }
        if (productVendor.length() == 0) {
            throw new IllegalArgumentException("product vendor cannot be empty");
        }
        if (productVendor.length() > MAX_PRODUCT_VENDOR_LEN) {
            throw new IllegalArgumentException("productVendor \"" +
                productVendor + "\" exceeds maximum length " +
                MAX_PRODUCT_VENDOR_LEN);
        }
        this.productVendor = productVendor;
    }
    private void setPlatformArch(String platformArch) {
        if (platformArch == null) {
            throw new NullPointerException("Parameter platformArch cannot be null");
        }
        if (platformArch.length() == 0) {
            throw new IllegalArgumentException("platform architecture cannot be empty");
        }
        if (platformArch.length() > MAX_PLATFORM_ARCH_LEN) {
            throw new IllegalArgumentException("platformArch \"" +
                platformArch + "\" exceeds maximum length " +
                MAX_PLATFORM_ARCH_LEN);
        }
        this.platformArch = platformArch;
    }
    private void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    private void setContainer(String container) {
        if (container == null) {
            throw new NullPointerException("Parameter container cannot be null");
        }
        if (container.length() == 0) {
            throw new IllegalArgumentException("container cannot be empty");
        }
        if (container.length() > MAX_CONTAINER_LEN) {
            throw new IllegalArgumentException("container \"" +
                container + "\" exceeds maximum length " +
                MAX_CONTAINER_LEN);
        }
        this.container = container;
    }
    private void setSource(String source) {
        if (source == null) {
            throw new NullPointerException("Parameter source cannot be null");
        }
        if (source.length() == 0) {
            throw new IllegalArgumentException("source cannot be empty");
        }
        if (source.length() > MAX_SOURCE_LEN) {
            throw new IllegalArgumentException("source \"" + source +
                "\" exceeds maximum length " + MAX_SOURCE_LEN);
        }
        this.source = source;
    }
    private void setInstallerUID(int installerUID) {
        this.installerUID = installerUID;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ServiceTag)) {
            return false;
        }
        ServiceTag st = (ServiceTag) obj;
        if (st == this) {
            return true;
        }
        return st.getInstanceURN().equals(getInstanceURN());
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.instanceURN != null ? this.instanceURN.hashCode() : 0);
        return hash;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ST_NODE_INSTANCE_URN).append("=").append(instanceURN).append("\n");
        sb.append(ST_NODE_PRODUCT_NAME).append("=").append(productName).append("\n");
        sb.append(ST_NODE_PRODUCT_VERSION).append("=").append(productVersion).append("\n");
        sb.append(ST_NODE_PRODUCT_URN).append("=").append(productURN).append("\n");
        sb.append(ST_NODE_PRODUCT_PARENT_URN).append("=").append(productParentURN).append("\n");
        sb.append(ST_NODE_PRODUCT_PARENT).append("=").append(productParent).append("\n");
        sb.append(ST_NODE_PRODUCT_DEFINED_INST_ID).append("=").append(productDefinedInstanceID).append("\n");
        sb.append(ST_NODE_PRODUCT_VENDOR).append("=").append(productVendor).append("\n");
        sb.append(ST_NODE_PLATFORM_ARCH).append("=").append(platformArch).append("\n");
        sb.append(ST_NODE_TIMESTAMP).append("=").append(Util.formatTimestamp(timestamp)).append("\n");
        sb.append(ST_NODE_CONTAINER).append("=").append(container).append("\n");
        sb.append(ST_NODE_SOURCE).append("=").append(source).append("\n");
        sb.append(ST_NODE_INSTALLER_UID).append("=").append(String.valueOf(installerUID)).append("\n");
        return sb.toString();
    }
    public static ServiceTag getJavaServiceTag(String source) throws IOException {
        return Installer.getJavaServiceTag(source);
    }
}
