public class DnsUrl extends Uri {
    private String domain;      
    public static DnsUrl[] fromList(String urlList)
            throws MalformedURLException {
        DnsUrl[] urls = new DnsUrl[(urlList.length() + 1) / 2];
        int i = 0;              
        StringTokenizer st = new StringTokenizer(urlList, " ");
        while (st.hasMoreTokens()) {
            urls[i++] = new DnsUrl(st.nextToken());
        }
        DnsUrl[] trimmed = new DnsUrl[i];
        System.arraycopy(urls, 0, trimmed, 0, i);
        return trimmed;
    }
    public DnsUrl(String url) throws MalformedURLException {
        super(url);
        if (!scheme.equals("dns")) {
            throw new MalformedURLException(
                    url + " is not a valid DNS pseudo-URL");
        }
        domain = path.startsWith("/")
            ? path.substring(1)
            : path;
        domain = domain.equals("")
            ? "."
            : UrlUtil.decode(domain);
    }
    public String getDomain() {
        return domain;
    }
}
