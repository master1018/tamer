public abstract class URLStreamHandler {
    protected abstract URLConnection openConnection(URL u) throws IOException;
    protected URLConnection openConnection(URL u, Proxy proxy)
            throws IOException {
        throw new UnsupportedOperationException(Msg.getString("K034d")); 
    }
    protected void parseURL(URL u, String str, int start, int end) {
        if (str.startsWith("
                && str.indexOf('/', start + 2) == -1
                && end <= Integer.MIN_VALUE + 1) {
            throw new StringIndexOutOfBoundsException(end - 2 - start);
        }
        if (end < start) {
            if (this != u.strmHandler) {
                throw new SecurityException();
            }
            return;
        }
        String parseString = ""; 
        if (start < end) {
            parseString = str.substring(start, end);
        }
        end -= start;
        int fileIdx = 0;
        String host = u.getHost();
        int port = u.getPort();
        String ref = u.getRef();
        String file = u.getPath();
        String query = u.getQuery();
        String authority = u.getAuthority();
        String userInfo = u.getUserInfo();
        int refIdx = parseString.indexOf('#', 0);
        if (parseString.startsWith("
            int hostIdx = 2, portIdx = -1;
            port = -1;
            fileIdx = parseString.indexOf('/', hostIdx);
            int questionMarkIndex = parseString.indexOf('?', hostIdx);
            if ((questionMarkIndex != -1)
                    && ((fileIdx == -1) || (fileIdx > questionMarkIndex))) {
                fileIdx = questionMarkIndex;
            }
            if (fileIdx == -1) {
                fileIdx = end;
                file = ""; 
            }
            int hostEnd = fileIdx;
            if (refIdx != -1 && refIdx < fileIdx) {
                hostEnd = refIdx;
            }
            int userIdx = parseString.lastIndexOf('@', hostEnd);
            authority = parseString.substring(hostIdx, hostEnd);
            if (userIdx > -1) {
                userInfo = parseString.substring(hostIdx, userIdx);
                hostIdx = userIdx + 1;
            }
            portIdx = parseString.indexOf(':', userIdx == -1 ? hostIdx
                    : userIdx);
            int endOfIPv6Addr = parseString.indexOf(']');
            if (endOfIPv6Addr != -1) {
                try {
                    if (parseString.length() > endOfIPv6Addr + 1) {
                        char c = parseString.charAt(endOfIPv6Addr + 1);
                        if (c == ':') {
                            portIdx = endOfIPv6Addr + 1;
                        } else {
                            portIdx = -1;
                        }
                    } else {
                        portIdx = -1;
                    }
                } catch (Exception e) {
                }
            }
            if (portIdx == -1 || portIdx > fileIdx) {
                host = parseString.substring(hostIdx, hostEnd);
            } else {
                host = parseString.substring(hostIdx, portIdx);
                String portString = parseString.substring(portIdx + 1, hostEnd);
                if (portString.length() == 0) {
                    port = -1;
                } else {
                    port = Integer.parseInt(portString);
                }
            }
        }
        if (refIdx > -1) {
            ref = parseString.substring(refIdx + 1, end);
        }
        int fileEnd = (refIdx == -1 ? end : refIdx);
        int queryIdx = parseString.lastIndexOf('?', fileEnd);
        boolean canonicalize = false;
        if (queryIdx > -1) {
            query = parseString.substring(queryIdx + 1, fileEnd);
            if (queryIdx == 0 && file != null) {
                if (file.equals("")) { 
                    file = "/"; 
                } else if (file.startsWith("/")) { 
                    canonicalize = true;
                }
                int last = file.lastIndexOf('/') + 1;
                file = file.substring(0, last);
            }
            fileEnd = queryIdx;
        } else
        if (refIdx != 0) {
            query = null;
        }
        if (fileIdx > -1) {
            if (fileIdx < end && parseString.charAt(fileIdx) == '/') {
                file = parseString.substring(fileIdx, fileEnd);
            } else if (fileEnd > fileIdx) {
                if (file == null) {
                    file = ""; 
                } else if (file.equals("")) { 
                    file = "/"; 
                } else if (file.startsWith("/")) { 
                    canonicalize = true;
                }
                int last = file.lastIndexOf('/') + 1;
                if (last == 0) {
                    file = parseString.substring(fileIdx, fileEnd);
                } else {
                    file = file.substring(0, last)
                            + parseString.substring(fileIdx, fileEnd);
                }
            }
        }
        if (file == null) {
            file = ""; 
        }
        if (host == null) {
            host = ""; 
        }
        if (canonicalize) {
            file = URLUtil.canonicalizePath(file);
        }
        setURL(u, u.getProtocol(), host, port, authority, userInfo, file,
                query, ref);
    }
    @Deprecated
    protected void setURL(URL u, String protocol, String host, int port,
            String file, String ref) {
        if (this != u.strmHandler) {
            throw new SecurityException();
        }
        u.set(protocol, host, port, file, ref);
    }
    protected void setURL(URL u, String protocol, String host, int port,
            String authority, String userInfo, String file, String query,
            String ref) {
        if (this != u.strmHandler) {
            throw new SecurityException();
        }
        u.set(protocol, host, port, authority, userInfo, file, query, ref);
    }
    protected String toExternalForm(URL url) {
        StringBuilder answer = new StringBuilder();
        answer.append(url.getProtocol());
        answer.append(':');
        String authority = url.getAuthority();
        if (authority != null && authority.length() > 0) {
            answer.append("
            answer.append(url.getAuthority());
        }
        String file = url.getFile();
        String ref = url.getRef();
        if (file != null) {
            answer.append(file);
        }
        if (ref != null) {
            answer.append('#');
            answer.append(ref);
        }
        return answer.toString();
    }
    protected boolean equals(URL url1, URL url2) {
        if (!sameFile(url1, url2)) {
            return false;
        }
        String s1 = url1.getRef(), s2 = url2.getRef();
        if (s1 != s2 && (s1 == null || !s1.equals(s2))) {
            return false;
        }
        s1 = url1.getQuery();
        s2 = url2.getQuery();
        return s1 == s2 || (s1 != null && s1.equals(s2));
    }
    protected int getDefaultPort() {
        return -1;
    }
    protected InetAddress getHostAddress(URL url) {
        try {
            String host = url.getHost();
            if (host == null || host.length() == 0) {
                return null;
            }
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
    }
    protected int hashCode(URL url) {
        return toExternalForm(url).hashCode();
    }
    protected boolean hostsEqual(URL url1, URL url2) {
        String host1 = getHost(url1), host2 = getHost(url2);
        if (host1 != null && host1.equalsIgnoreCase(host2)) {
            return true;
        }
        InetAddress address1 = getHostAddress(url1);
        InetAddress address2 = getHostAddress(url2);
        if (address1 != null && address1.equals(address2)) {
            return true;
        }
        return false;
    }
    protected boolean sameFile(URL url1, URL url2) {
        String s1 = url1.getProtocol();
        String s2 = url2.getProtocol();
        if (s1 != s2 && (s1 == null || !s1.equals(s2))) {
            return false;
        }
        s1 = url1.getFile();
        s2 = url2.getFile();
        if (s1 != s2 && (s1 == null || !s1.equals(s2))) {
            return false;
        }
        if (!hostsEqual(url1, url2)) {
            return false;
        }
        int p1 = url1.getPort();
        if (p1 == -1) {
            p1 = getDefaultPort();
        }
        int p2 = url2.getPort();
        if (p2 == -1) {
            p2 = getDefaultPort();
        }
        return p1 == p2;
    }
    private static String getHost(URL url) {
        String host = url.getHost();
        if ("file".equals(url.getProtocol()) 
                && "".equals(host)) { 
            host = "localhost"; 
        }
        return host;
    }
}
