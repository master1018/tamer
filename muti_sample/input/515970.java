public class CookieIdentityComparator implements Serializable, Comparator<Cookie> {
    private static final long serialVersionUID = 4466565437490631532L;
    public int compare(final Cookie c1, final Cookie c2) {
        int res = c1.getName().compareTo(c2.getName());
        if (res == 0) {
            String d1 = c1.getDomain();
            if (d1 == null) {
                d1 = "";
            }
            String d2 = c2.getDomain();
            if (d2 == null) {
                d2 = "";
            }
            res = d1.compareToIgnoreCase(d2);
        }
        return res;
    }
}
