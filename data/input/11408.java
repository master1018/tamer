public class X509AttributeName {
    private static final char SEPARATOR = '.';
    private String prefix = null;
    private String suffix = null;
    public X509AttributeName(String name) {
        int i = name.indexOf(SEPARATOR);
        if (i == (-1)) {
            prefix = name;
        } else {
            prefix = name.substring(0, i);
            suffix = name.substring(i + 1);
        }
    }
    public String getPrefix() {
      return (prefix);
    }
    public String getSuffix() {
      return (suffix);
    }
}
