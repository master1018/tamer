public class BasicClientCookie implements SetCookie, ClientCookie, Cloneable {
    public BasicClientCookie(final String name, final String value) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        this.name = name;
        this.attribs = new HashMap<String, String>();
        this.value = value;
    }
    public String getName() {
        return this.name;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(final String value) {
        this.value = value;
    }
    public String getComment() {
        return cookieComment;
    }
    public void setComment(String comment) {
        cookieComment = comment;
    }
    public String getCommentURL() {
        return null;
    }
    public Date getExpiryDate() {
        return cookieExpiryDate;
    }
    public void setExpiryDate (Date expiryDate) {
        cookieExpiryDate = expiryDate;
    }
    public boolean isPersistent() {
        return (null != cookieExpiryDate);
    }
    public String getDomain() {
        return cookieDomain;
    }
    public void setDomain(String domain) {
        if (domain != null) {
            cookieDomain = domain.toLowerCase(Locale.ENGLISH);
        } else {
            cookieDomain = null;
        }
    }
    public String getPath() {
        return cookiePath;
    }
    public void setPath(String path) {
        cookiePath = path;
    }
    public boolean isSecure() {
        return isSecure;
    }
    public void setSecure (boolean secure) {
        isSecure = secure;
    }
    public int[] getPorts() {
        return null;
    }
    public int getVersion() {
        return cookieVersion;
    }
    public void setVersion(int version) {
        cookieVersion = version;
    }
    public boolean isExpired(final Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date may not be null");
        }
        return (cookieExpiryDate != null  
            && cookieExpiryDate.getTime() <= date.getTime());
    }
    public void setAttribute(final String name, final String value) {
        this.attribs.put(name, value);
    }
    public String getAttribute(final String name) {
        return this.attribs.get(name);
    }
    public boolean containsAttribute(final String name) {
        return this.attribs.get(name) != null;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        BasicClientCookie clone = (BasicClientCookie) super.clone();
        clone.attribs = new HashMap<String, String>(this.attribs);
        return clone;
    }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[version: ");
        buffer.append(Integer.toString(this.cookieVersion));
        buffer.append("]");
        buffer.append("[name: ");
        buffer.append(this.name);
        buffer.append("]");
        buffer.append("[value: ");
        buffer.append(this.value);
        buffer.append("]");
        buffer.append("[domain: ");
        buffer.append(this.cookieDomain);
        buffer.append("]");
        buffer.append("[path: ");
        buffer.append(this.cookiePath);
        buffer.append("]");
        buffer.append("[expiry: ");
        buffer.append(this.cookieExpiryDate);
        buffer.append("]");
        return buffer.toString();
    }
    private final String name;
    private Map<String, String> attribs;
    private String value;
    private String  cookieComment;
    private String  cookieDomain;
    private Date cookieExpiryDate;
    private String cookiePath;
    private boolean isSecure;
    private int cookieVersion;
}
