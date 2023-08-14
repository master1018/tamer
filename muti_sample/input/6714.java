public class SerialDatalink implements Serializable, Cloneable {
    private URL url;
    private int baseType;
    private String baseTypeName;
    public SerialDatalink(URL url) throws SerialException {
        if (url == null) {
            throw new SerialException("Cannot serialize empty URL instance");
        }
        this.url = url;
    }
    public URL getDatalink() throws SerialException {
        URL aURL = null;
        try {
            aURL = new URL((this.url).toString());
        } catch (java.net.MalformedURLException e) {
            throw new SerialException("MalformedURLException: " + e.getMessage());
        }
        return aURL;
    }
    static final long serialVersionUID = 2826907821828733626L;
}
