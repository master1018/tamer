public abstract class Uri implements Parcelable, Comparable<Uri> {
    private static final String LOG = Uri.class.getSimpleName();
    @SuppressWarnings("RedundantStringConstructorCall")
    private static final String NOT_CACHED = new String("NOT CACHED");
    public static final Uri EMPTY = new HierarchicalUri(null, Part.NULL,
            PathPart.EMPTY, Part.NULL, Part.NULL);
    private Uri() {}
    public abstract boolean isHierarchical();
    public boolean isOpaque() {
        return !isHierarchical();
    }
    public abstract boolean isRelative();
    public boolean isAbsolute() {
        return !isRelative();
    }
    public abstract String getScheme();
    public abstract String getSchemeSpecificPart();
    public abstract String getEncodedSchemeSpecificPart();
    public abstract String getAuthority();
    public abstract String getEncodedAuthority();
    public abstract String getUserInfo();
    public abstract String getEncodedUserInfo();
    public abstract String getHost();
    public abstract int getPort();
    public abstract String getPath();
    public abstract String getEncodedPath();
    public abstract String getQuery();
    public abstract String getEncodedQuery();
    public abstract String getFragment();
    public abstract String getEncodedFragment();
    public abstract List<String> getPathSegments();
    public abstract String getLastPathSegment();
    public boolean equals(Object o) {
        if (!(o instanceof Uri)) {
            return false;
        }
        Uri other = (Uri) o;
        return toString().equals(other.toString());
    }
    public int hashCode() {
        return toString().hashCode();
    }
    public int compareTo(Uri other) {
        return toString().compareTo(other.toString());
    }
    public abstract String toString();
    public abstract Builder buildUpon();
    private final static int NOT_FOUND = -1;
    private final static int NOT_CALCULATED = -2;
    private static final String NOT_HIERARCHICAL
            = "This isn't a hierarchical URI.";
    private static final String DEFAULT_ENCODING = "UTF-8";
    public static Uri parse(String uriString) {
        return new StringUri(uriString);
    }
    public static Uri fromFile(File file) {
        if (file == null) {
            throw new NullPointerException("file");
        }
        PathPart path = PathPart.fromDecoded(file.getAbsolutePath());
        return new HierarchicalUri(
                "file", Part.EMPTY, path, Part.NULL, Part.NULL);
    }
    private static class StringUri extends AbstractHierarchicalUri {
        static final int TYPE_ID = 1;
        private final String uriString;
        private StringUri(String uriString) {
            if (uriString == null) {
                throw new NullPointerException("uriString");
            }
            this.uriString = uriString;
        }
        static Uri readFrom(Parcel parcel) {
            return new StringUri(parcel.readString());
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(TYPE_ID);
            parcel.writeString(uriString);
        }
        private volatile int cachedSsi = NOT_CALCULATED;
        private int findSchemeSeparator() {
            return cachedSsi == NOT_CALCULATED
                    ? cachedSsi = uriString.indexOf(':')
                    : cachedSsi;
        }
        private volatile int cachedFsi = NOT_CALCULATED;
        private int findFragmentSeparator() {
            return cachedFsi == NOT_CALCULATED
                    ? cachedFsi = uriString.indexOf('#', findSchemeSeparator())
                    : cachedFsi;
        }
        public boolean isHierarchical() {
            int ssi = findSchemeSeparator();
            if (ssi == NOT_FOUND) {
                return true;
            }
            if (uriString.length() == ssi + 1) {
                return false;
            }
            return uriString.charAt(ssi + 1) == '/';
        }
        public boolean isRelative() {
            return findSchemeSeparator() == NOT_FOUND;
        }
        private volatile String scheme = NOT_CACHED;
        public String getScheme() {
            @SuppressWarnings("StringEquality")
            boolean cached = (scheme != NOT_CACHED);
            return cached ? scheme : (scheme = parseScheme());
        }
        private String parseScheme() {
            int ssi = findSchemeSeparator();
            return ssi == NOT_FOUND ? null : uriString.substring(0, ssi);
        }
        private Part ssp;
        private Part getSsp() {
            return ssp == null ? ssp = Part.fromEncoded(parseSsp()) : ssp;
        }
        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }
        public String getSchemeSpecificPart() {
            return getSsp().getDecoded();
        }
        private String parseSsp() {
            int ssi = findSchemeSeparator();
            int fsi = findFragmentSeparator();
            return fsi == NOT_FOUND
                    ? uriString.substring(ssi + 1)
                    : uriString.substring(ssi + 1, fsi);
        }
        private Part authority;
        private Part getAuthorityPart() {
            if (authority == null) {
                String encodedAuthority
                        = parseAuthority(this.uriString, findSchemeSeparator());
                return authority = Part.fromEncoded(encodedAuthority);
            }
            return authority;
        }
        public String getEncodedAuthority() {
            return getAuthorityPart().getEncoded();
        }
        public String getAuthority() {
            return getAuthorityPart().getDecoded();
        }
        private PathPart path;
        private PathPart getPathPart() {
            return path == null
                    ? path = PathPart.fromEncoded(parsePath())
                    : path;
        }
        public String getPath() {
            return getPathPart().getDecoded();
        }
        public String getEncodedPath() {
            return getPathPart().getEncoded();
        }
        public List<String> getPathSegments() {
            return getPathPart().getPathSegments();
        }
        private String parsePath() {
            String uriString = this.uriString;
            int ssi = findSchemeSeparator();
            if (ssi > -1) {
                boolean schemeOnly = ssi + 1 == uriString.length();
                if (schemeOnly) {
                    return null;
                }
                if (uriString.charAt(ssi + 1) != '/') {
                    return null;
                }
            } else {
            }
            return parsePath(uriString, ssi);
        }
        private Part query;
        private Part getQueryPart() {
            return query == null
                    ? query = Part.fromEncoded(parseQuery()) : query;
        }
        public String getEncodedQuery() {
            return getQueryPart().getEncoded();
        }
        private String parseQuery() {
            int qsi = uriString.indexOf('?', findSchemeSeparator());
            if (qsi == NOT_FOUND) {
                return null;
            }
            int fsi = findFragmentSeparator();
            if (fsi == NOT_FOUND) {
                return uriString.substring(qsi + 1);
            }
            if (fsi < qsi) {
                return null;
            }
            return uriString.substring(qsi + 1, fsi);
        }
        public String getQuery() {
            return getQueryPart().getDecoded();
        }
        private Part fragment;
        private Part getFragmentPart() {
            return fragment == null
                    ? fragment = Part.fromEncoded(parseFragment()) : fragment;
        }
        public String getEncodedFragment() {
            return getFragmentPart().getEncoded();
        }
        private String parseFragment() {
            int fsi = findFragmentSeparator();
            return fsi == NOT_FOUND ? null : uriString.substring(fsi + 1);
        }
        public String getFragment() {
            return getFragmentPart().getDecoded();
        }
        public String toString() {
            return uriString;
        }
        static String parseAuthority(String uriString, int ssi) {
            int length = uriString.length();
            if (length > ssi + 2
                    && uriString.charAt(ssi + 1) == '/'
                    && uriString.charAt(ssi + 2) == '/') {
                int end = ssi + 3;
                LOOP: while (end < length) {
                    switch (uriString.charAt(end)) {
                        case '/': 
                        case '?': 
                        case '#': 
                            break LOOP;
                    }
                    end++;
                }
                return uriString.substring(ssi + 3, end);
            } else {
                return null;
            }
        }
        static String parsePath(String uriString, int ssi) {
            int length = uriString.length();
            int pathStart;
            if (length > ssi + 2
                    && uriString.charAt(ssi + 1) == '/'
                    && uriString.charAt(ssi + 2) == '/') {
                pathStart = ssi + 3;
                LOOP: while (pathStart < length) {
                    switch (uriString.charAt(pathStart)) {
                        case '?': 
                        case '#': 
                            return ""; 
                        case '/': 
                            break LOOP;
                    }
                    pathStart++;
                }
            } else {
                pathStart = ssi + 1;
            }
            int pathEnd = pathStart;
            LOOP: while (pathEnd < length) {
                switch (uriString.charAt(pathEnd)) {
                    case '?': 
                    case '#': 
                        break LOOP;
                }
                pathEnd++;
            }
            return uriString.substring(pathStart, pathEnd);
        }
        public Builder buildUpon() {
            if (isHierarchical()) {
                return new Builder()
                        .scheme(getScheme())
                        .authority(getAuthorityPart())
                        .path(getPathPart())
                        .query(getQueryPart())
                        .fragment(getFragmentPart());
            } else {
                return new Builder()
                        .scheme(getScheme())
                        .opaquePart(getSsp())
                        .fragment(getFragmentPart());
            }
        }
    }
    public static Uri fromParts(String scheme, String ssp,
            String fragment) {
        if (scheme == null) {
            throw new NullPointerException("scheme");
        }
        if (ssp == null) {
            throw new NullPointerException("ssp");
        }
        return new OpaqueUri(scheme, Part.fromDecoded(ssp),
                Part.fromDecoded(fragment));
    }
    private static class OpaqueUri extends Uri {
        static final int TYPE_ID = 2;
        private final String scheme;
        private final Part ssp;
        private final Part fragment;
        private OpaqueUri(String scheme, Part ssp, Part fragment) {
            this.scheme = scheme;
            this.ssp = ssp;
            this.fragment = fragment == null ? Part.NULL : fragment;
        }
        static Uri readFrom(Parcel parcel) {
            return new OpaqueUri(
                parcel.readString(),
                Part.readFrom(parcel),
                Part.readFrom(parcel)
            );
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(TYPE_ID);
            parcel.writeString(scheme);
            ssp.writeTo(parcel);
            fragment.writeTo(parcel);
        }
        public boolean isHierarchical() {
            return false;
        }
        public boolean isRelative() {
            return scheme == null;
        }
        public String getScheme() {
            return this.scheme;
        }
        public String getEncodedSchemeSpecificPart() {
            return ssp.getEncoded();
        }
        public String getSchemeSpecificPart() {
            return ssp.getDecoded();
        }
        public String getAuthority() {
            return null;
        }
        public String getEncodedAuthority() {
            return null;
        }
        public String getPath() {
            return null;
        }
        public String getEncodedPath() {
            return null;
        }
        public String getQuery() {
            return null;
        }
        public String getEncodedQuery() {
            return null;
        }
        public String getFragment() {
            return fragment.getDecoded();
        }
        public String getEncodedFragment() {
            return fragment.getEncoded();
        }
        public List<String> getPathSegments() {
            return Collections.emptyList();
        }
        public String getLastPathSegment() {
            return null;
        }
        public String getUserInfo() {
            return null;
        }
        public String getEncodedUserInfo() {
            return null;
        }
        public String getHost() {
            return null;
        }
        public int getPort() {
            return -1;
        }
        private volatile String cachedString = NOT_CACHED;
        public String toString() {
            @SuppressWarnings("StringEquality")
            boolean cached = cachedString != NOT_CACHED;
            if (cached) {
                return cachedString;
            }
            StringBuilder sb = new StringBuilder();
            sb.append(scheme).append(':');
            sb.append(getEncodedSchemeSpecificPart());
            if (!fragment.isEmpty()) {
                sb.append('#').append(fragment.getEncoded());
            }
            return cachedString = sb.toString();
        }
        public Builder buildUpon() {
            return new Builder()
                    .scheme(this.scheme)
                    .opaquePart(this.ssp)
                    .fragment(this.fragment);
        }
    }
    static class PathSegments extends AbstractList<String>
            implements RandomAccess {
        static final PathSegments EMPTY = new PathSegments(null, 0);
        final String[] segments;
        final int size;
        PathSegments(String[] segments, int size) {
            this.segments = segments;
            this.size = size;
        }
        public String get(int index) {
            if (index >= size) {
                throw new IndexOutOfBoundsException();
            }
            return segments[index];
        }
        public int size() {
            return this.size;
        }
    }
    static class PathSegmentsBuilder {
        String[] segments;
        int size = 0;
        void add(String segment) {
            if (segments == null) {
                segments = new String[4];
            } else if (size + 1 == segments.length) {
                String[] expanded = new String[segments.length * 2];
                System.arraycopy(segments, 0, expanded, 0, segments.length);
                segments = expanded;
            }
            segments[size++] = segment;
        }
        PathSegments build() {
            if (segments == null) {
                return PathSegments.EMPTY;
            }
            try {
                return new PathSegments(segments, size);
            } finally {
                segments = null;
            }
        }
    }
    private abstract static class AbstractHierarchicalUri extends Uri {
        public String getLastPathSegment() {
            List<String> segments = getPathSegments();
            int size = segments.size();
            if (size == 0) {
                return null;
            }
            return segments.get(size - 1);
        }
        private Part userInfo;
        private Part getUserInfoPart() {
            return userInfo == null
                    ? userInfo = Part.fromEncoded(parseUserInfo()) : userInfo;
        }
        public final String getEncodedUserInfo() {
            return getUserInfoPart().getEncoded();
        }
        private String parseUserInfo() {
            String authority = getEncodedAuthority();
            if (authority == null) {
                return null;
            }
            int end = authority.indexOf('@');
            return end == NOT_FOUND ? null : authority.substring(0, end);
        }
        public String getUserInfo() {
            return getUserInfoPart().getDecoded();
        }
        private volatile String host = NOT_CACHED;
        public String getHost() {
            @SuppressWarnings("StringEquality")
            boolean cached = (host != NOT_CACHED);
            return cached ? host
                    : (host = parseHost());
        }
        private String parseHost() {
            String authority = getEncodedAuthority();
            if (authority == null) {
                return null;
            }
            int userInfoSeparator = authority.indexOf('@');
            int portSeparator = authority.indexOf(':', userInfoSeparator);
            String encodedHost = portSeparator == NOT_FOUND
                    ? authority.substring(userInfoSeparator + 1)
                    : authority.substring(userInfoSeparator + 1, portSeparator);
            return decode(encodedHost);
        }
        private volatile int port = NOT_CALCULATED;
        public int getPort() {
            return port == NOT_CALCULATED
                    ? port = parsePort()
                    : port;
        }
        private int parsePort() {
            String authority = getEncodedAuthority();
            if (authority == null) {
                return -1;
            }
            int userInfoSeparator = authority.indexOf('@');
            int portSeparator = authority.indexOf(':', userInfoSeparator);
            if (portSeparator == NOT_FOUND) {
                return -1;
            }
            String portString = decode(authority.substring(portSeparator + 1));
            try {
                return Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.w(LOG, "Error parsing port string.", e);
                return -1;
            }
        }
    }
    private static class HierarchicalUri extends AbstractHierarchicalUri {
        static final int TYPE_ID = 3;
        private final String scheme; 
        private final Part authority;
        private final PathPart path;
        private final Part query;
        private final Part fragment;
        private HierarchicalUri(String scheme, Part authority, PathPart path,
                Part query, Part fragment) {
            this.scheme = scheme;
            this.authority = Part.nonNull(authority);
            this.path = path == null ? PathPart.NULL : path;
            this.query = Part.nonNull(query);
            this.fragment = Part.nonNull(fragment);
        }
        static Uri readFrom(Parcel parcel) {
            return new HierarchicalUri(
                parcel.readString(),
                Part.readFrom(parcel),
                PathPart.readFrom(parcel),
                Part.readFrom(parcel),
                Part.readFrom(parcel)
            );
        }
        public int describeContents() {
            return 0;
        }
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeInt(TYPE_ID);
            parcel.writeString(scheme);
            authority.writeTo(parcel);
            path.writeTo(parcel);
            query.writeTo(parcel);
            fragment.writeTo(parcel);
        }
        public boolean isHierarchical() {
            return true;
        }
        public boolean isRelative() {
            return scheme == null;
        }
        public String getScheme() {
            return scheme;
        }
        private Part ssp;
        private Part getSsp() {
            return ssp == null
                    ? ssp = Part.fromEncoded(makeSchemeSpecificPart()) : ssp;
        }
        public String getEncodedSchemeSpecificPart() {
            return getSsp().getEncoded();
        }
        public String getSchemeSpecificPart() {
            return getSsp().getDecoded();
        }
        private String makeSchemeSpecificPart() {
            StringBuilder builder = new StringBuilder();
            appendSspTo(builder);
            return builder.toString();
        }
        private void appendSspTo(StringBuilder builder) {
            String encodedAuthority = authority.getEncoded();
            if (encodedAuthority != null) {
                builder.append("
            }
            String encodedPath = path.getEncoded();
            if (encodedPath != null) {
                builder.append(encodedPath);
            }
            if (!query.isEmpty()) {
                builder.append('?').append(query.getEncoded());
            }
        }
        public String getAuthority() {
            return this.authority.getDecoded();
        }
        public String getEncodedAuthority() {
            return this.authority.getEncoded();
        }
        public String getEncodedPath() {
            return this.path.getEncoded();
        }
        public String getPath() {
            return this.path.getDecoded();
        }
        public String getQuery() {
            return this.query.getDecoded();
        }
        public String getEncodedQuery() {
            return this.query.getEncoded();
        }
        public String getFragment() {
            return this.fragment.getDecoded();
        }
        public String getEncodedFragment() {
            return this.fragment.getEncoded();
        }
        public List<String> getPathSegments() {
            return this.path.getPathSegments();
        }
        private volatile String uriString = NOT_CACHED;
        @Override
        public String toString() {
            @SuppressWarnings("StringEquality")
            boolean cached = (uriString != NOT_CACHED);
            return cached ? uriString
                    : (uriString = makeUriString());
        }
        private String makeUriString() {
            StringBuilder builder = new StringBuilder();
            if (scheme != null) {
                builder.append(scheme).append(':');
            }
            appendSspTo(builder);
            if (!fragment.isEmpty()) {
                builder.append('#').append(fragment.getEncoded());
            }
            return builder.toString();
        }
        public Builder buildUpon() {
            return new Builder()
                    .scheme(scheme)
                    .authority(authority)
                    .path(path)
                    .query(query)
                    .fragment(fragment);
        }
    }
    public static final class Builder {
        private String scheme;
        private Part opaquePart;
        private Part authority;
        private PathPart path;
        private Part query;
        private Part fragment;
        public Builder() {}
        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }
        Builder opaquePart(Part opaquePart) {
            this.opaquePart = opaquePart;
            return this;
        }
        public Builder opaquePart(String opaquePart) {
            return opaquePart(Part.fromDecoded(opaquePart));
        }
        public Builder encodedOpaquePart(String opaquePart) {
            return opaquePart(Part.fromEncoded(opaquePart));
        }
        Builder authority(Part authority) {
            this.opaquePart = null;
            this.authority = authority;
            return this;
        }
        public Builder authority(String authority) {
            return authority(Part.fromDecoded(authority));
        }
        public Builder encodedAuthority(String authority) {
            return authority(Part.fromEncoded(authority));
        }
        Builder path(PathPart path) {
            this.opaquePart = null;
            this.path = path;
            return this;
        }
        public Builder path(String path) {
            return path(PathPart.fromDecoded(path));
        }
        public Builder encodedPath(String path) {
            return path(PathPart.fromEncoded(path));
        }
        public Builder appendPath(String newSegment) {
            return path(PathPart.appendDecodedSegment(path, newSegment));
        }
        public Builder appendEncodedPath(String newSegment) {
            return path(PathPart.appendEncodedSegment(path, newSegment));
        }
        Builder query(Part query) {
            this.opaquePart = null;
            this.query = query;
            return this;
        }
        public Builder query(String query) {
            return query(Part.fromDecoded(query));
        }
        public Builder encodedQuery(String query) {
            return query(Part.fromEncoded(query));
        }
        Builder fragment(Part fragment) {
            this.fragment = fragment;
            return this;
        }
        public Builder fragment(String fragment) {
            return fragment(Part.fromDecoded(fragment));
        }
        public Builder encodedFragment(String fragment) {
            return fragment(Part.fromEncoded(fragment));
        }
        public Builder appendQueryParameter(String key, String value) {
            this.opaquePart = null;
            String encodedParameter = encode(key, null) + "="
                    + encode(value, null);
            if (query == null) {
                query = Part.fromEncoded(encodedParameter);
                return this;
            }
            String oldQuery = query.getEncoded();
            if (oldQuery == null || oldQuery.length() == 0) {
                query = Part.fromEncoded(encodedParameter);
            } else {
                query = Part.fromEncoded(oldQuery + "&" + encodedParameter);
            }
            return this;
        }
        public Uri build() {
            if (opaquePart != null) {
                if (this.scheme == null) {
                    throw new UnsupportedOperationException(
                            "An opaque URI must have a scheme.");
                }
                return new OpaqueUri(scheme, opaquePart, fragment);
            } else {
                PathPart path = this.path;
                if (path == null || path == PathPart.NULL) {
                    path = PathPart.EMPTY;
                } else {
                    if (hasSchemeOrAuthority()) {
                        path = PathPart.makeAbsolute(path);
                    }
                }
                return new HierarchicalUri(
                        scheme, authority, path, query, fragment);
            }
        }
        private boolean hasSchemeOrAuthority() {
            return scheme != null
                    || (authority != null && authority != Part.NULL);
        }
        @Override
        public String toString() {
            return build().toString();
        }
    }
    public List<String> getQueryParameters(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        }
        String query = getEncodedQuery();
        if (query == null) {
            return Collections.emptyList();
        }
        String encodedKey;
        try {
            encodedKey = URLEncoder.encode(key, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        query = "&" + query;
        String prefix = "&" + encodedKey + "=";
        ArrayList<String> values = new ArrayList<String>();
        int start = 0;
        int length = query.length();
        while (start < length) {
            start = query.indexOf(prefix, start);
            if (start == -1) {
                break;
            }
            start += prefix.length();
            int end = query.indexOf('&', start);
            if (end == -1) {
                end = query.length();
            }
            String value = query.substring(start, end);
            values.add(decode(value));
            start = end;
        }
        return Collections.unmodifiableList(values);
    }
    public String getQueryParameter(String key) {
        if (isOpaque()) {
            throw new UnsupportedOperationException(NOT_HIERARCHICAL);
        }
        if (key == null) {
          throw new NullPointerException("key");
        }
        final String query = getEncodedQuery();
        if (query == null) {
            return null;
        }
        final String encodedKey = encode(key, null);
        final int encodedKeyLength = encodedKey.length();
        int encodedKeySearchIndex = 0;
        final int encodedKeySearchEnd = query.length() - (encodedKeyLength + 1);
        while (encodedKeySearchIndex <= encodedKeySearchEnd) {
            int keyIndex = query.indexOf(encodedKey, encodedKeySearchIndex);
            if (keyIndex == -1) {
                break;
            }
            final int equalsIndex = keyIndex + encodedKeyLength;
            if (equalsIndex >= query.length()) {
                break;
            }
            if (query.charAt(equalsIndex) != '=') {
                encodedKeySearchIndex = equalsIndex + 1;
                continue;
            }
            if (keyIndex == 0 || query.charAt(keyIndex - 1) == '&') {
                int end = query.indexOf('&', equalsIndex);
                if (end == -1) {
                    end = query.length();
                }
                return decode(query.substring(equalsIndex + 1, end));
            } else {
                encodedKeySearchIndex = equalsIndex + 1;
            }
        }
        return null;
    }
    private static final int NULL_TYPE_ID = 0;
    public static final Parcelable.Creator<Uri> CREATOR
            = new Parcelable.Creator<Uri>() {
        public Uri createFromParcel(Parcel in) {
            int type = in.readInt();
            switch (type) {
                case NULL_TYPE_ID: return null;
                case StringUri.TYPE_ID: return StringUri.readFrom(in);
                case OpaqueUri.TYPE_ID: return OpaqueUri.readFrom(in);
                case HierarchicalUri.TYPE_ID:
                    return HierarchicalUri.readFrom(in);
            }
            throw new AssertionError("Unknown URI type: " + type);
        }
        public Uri[] newArray(int size) {
            return new Uri[size];
        }
    };
    public static void writeToParcel(Parcel out, Uri uri) {
        if (uri == null) {
            out.writeInt(NULL_TYPE_ID);
        } else {
            uri.writeToParcel(out, 0);
        }
    }
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    public static String encode(String s) {
        return encode(s, null);
    }
    public static String encode(String s, String allow) {
        if (s == null) {
            return null;
        }
        StringBuilder encoded = null;
        int oldLength = s.length();
        int current = 0;
        while (current < oldLength) {
            int nextToEncode = current;
            while (nextToEncode < oldLength
                    && isAllowed(s.charAt(nextToEncode), allow)) {
                nextToEncode++;
            }
            if (nextToEncode == oldLength) {
                if (current == 0) {
                    return s;
                } else {
                    encoded.append(s, current, oldLength);
                    return encoded.toString();
                }
            }
            if (encoded == null) {
                encoded = new StringBuilder();
            }
            if (nextToEncode > current) {
                encoded.append(s, current, nextToEncode);
            } else {
            }
            current = nextToEncode;
            int nextAllowed = current + 1;
            while (nextAllowed < oldLength
                    && !isAllowed(s.charAt(nextAllowed), allow)) {
                nextAllowed++;
            }
            String toEncode = s.substring(current, nextAllowed);
            try {
                byte[] bytes = toEncode.getBytes(DEFAULT_ENCODING);
                int bytesLength = bytes.length;
                for (int i = 0; i < bytesLength; i++) {
                    encoded.append('%');
                    encoded.append(HEX_DIGITS[(bytes[i] & 0xf0) >> 4]);
                    encoded.append(HEX_DIGITS[bytes[i] & 0xf]);
                }
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
            current = nextAllowed;
        }
        return encoded == null ? s : encoded.toString();
    }
    private static boolean isAllowed(char c, String allow) {
        return (c >= 'A' && c <= 'Z')
                || (c >= 'a' && c <= 'z')
                || (c >= '0' && c <= '9')
                || "_-!.~'()*".indexOf(c) != NOT_FOUND
                || (allow != null && allow.indexOf(c) != NOT_FOUND);
    }
    private static final byte[] REPLACEMENT = { (byte) 0xFF, (byte) 0xFD };
    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder decoded = null;
        ByteArrayOutputStream out = null;
        int oldLength = s.length();
        int current = 0;
        while (current < oldLength) {
            int nextEscape = s.indexOf('%', current);
            if (nextEscape == NOT_FOUND) {
                if (decoded == null) {
                    return s;
                } else {
                    decoded.append(s, current, oldLength);
                    return decoded.toString();
                }
            }
            if (decoded == null) {
                decoded = new StringBuilder(oldLength);
                out = new ByteArrayOutputStream(4);
            } else {
                out.reset();
            }
            if (nextEscape > current) {
                decoded.append(s, current, nextEscape);
                current = nextEscape;
            } else {
            }
            try {
                do {
                    if (current + 2 >= oldLength) {
                        out.write(REPLACEMENT);
                    } else {
                        int a = Character.digit(s.charAt(current + 1), 16);
                        int b = Character.digit(s.charAt(current + 2), 16);
                        if (a == -1 || b == -1) {
                            out.write(REPLACEMENT);
                        } else {
                            out.write((a << 4) + b);
                        }
                    }
                    current += 3;
                } while (current < oldLength && s.charAt(current) == '%');
                decoded.append(out.toString(DEFAULT_ENCODING));
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return decoded == null ? s : decoded.toString();
    }
    static abstract class AbstractPart {
        static class Representation {
            static final int BOTH = 0;
            static final int ENCODED = 1;
            static final int DECODED = 2;
        }
        volatile String encoded;
        volatile String decoded;
        AbstractPart(String encoded, String decoded) {
            this.encoded = encoded;
            this.decoded = decoded;
        }
        abstract String getEncoded();
        final String getDecoded() {
            @SuppressWarnings("StringEquality")
            boolean hasDecoded = decoded != NOT_CACHED;
            return hasDecoded ? decoded : (decoded = decode(encoded));
        }
        final void writeTo(Parcel parcel) {
            @SuppressWarnings("StringEquality")
            boolean hasEncoded = encoded != NOT_CACHED;
            @SuppressWarnings("StringEquality")
            boolean hasDecoded = decoded != NOT_CACHED;
            if (hasEncoded && hasDecoded) {
                parcel.writeInt(Representation.BOTH);
                parcel.writeString(encoded);
                parcel.writeString(decoded);
            } else if (hasEncoded) {
                parcel.writeInt(Representation.ENCODED);
                parcel.writeString(encoded);
            } else if (hasDecoded) {
                parcel.writeInt(Representation.DECODED);
                parcel.writeString(decoded);
            } else {
                throw new AssertionError();
            }
        }
    }
    static class Part extends AbstractPart {
        static final Part NULL = new EmptyPart(null);
        static final Part EMPTY = new EmptyPart("");
        private Part(String encoded, String decoded) {
            super(encoded, decoded);
        }
        boolean isEmpty() {
            return false;
        }
        String getEncoded() {
            @SuppressWarnings("StringEquality")
            boolean hasEncoded = encoded != NOT_CACHED;
            return hasEncoded ? encoded : (encoded = encode(decoded));
        }
        static Part readFrom(Parcel parcel) {
            int representation = parcel.readInt();
            switch (representation) {
                case Representation.BOTH:
                    return from(parcel.readString(), parcel.readString());
                case Representation.ENCODED:
                    return fromEncoded(parcel.readString());
                case Representation.DECODED:
                    return fromDecoded(parcel.readString());
                default:
                    throw new AssertionError();
            }
        }
        static Part nonNull(Part part) {
            return part == null ? NULL : part;
        }
        static Part fromEncoded(String encoded) {
            return from(encoded, NOT_CACHED);
        }
        static Part fromDecoded(String decoded) {
            return from(NOT_CACHED, decoded);
        }
        static Part from(String encoded, String decoded) {
            if (encoded == null) {
                return NULL;
            }
            if (encoded.length() == 0) {
                return EMPTY;
            }
            if (decoded == null) {
                return NULL;
            }
            if (decoded .length() == 0) {
                return EMPTY;
            }
            return new Part(encoded, decoded);
        }
        private static class EmptyPart extends Part {
            public EmptyPart(String value) {
                super(value, value);
            }
            @Override
            boolean isEmpty() {
                return true;
            }
        }
    }
    static class PathPart extends AbstractPart {
        static final PathPart NULL = new PathPart(null, null);
        static final PathPart EMPTY = new PathPart("", "");
        private PathPart(String encoded, String decoded) {
            super(encoded, decoded);
        }
        String getEncoded() {
            @SuppressWarnings("StringEquality")
            boolean hasEncoded = encoded != NOT_CACHED;
            return hasEncoded ? encoded : (encoded = encode(decoded, "/"));
        }
        private PathSegments pathSegments;
        PathSegments getPathSegments() {
            if (pathSegments != null) {
                return pathSegments;
            }
            String path = getEncoded();
            if (path == null) {
                return pathSegments = PathSegments.EMPTY;
            }
            PathSegmentsBuilder segmentBuilder = new PathSegmentsBuilder();
            int previous = 0;
            int current;
            while ((current = path.indexOf('/', previous)) > -1) {
                if (previous < current) {
                    String decodedSegment
                            = decode(path.substring(previous, current));
                    segmentBuilder.add(decodedSegment);
                }
                previous = current + 1;
            }
            if (previous < path.length()) {
                segmentBuilder.add(decode(path.substring(previous)));
            }
            return pathSegments = segmentBuilder.build();
        }
        static PathPart appendEncodedSegment(PathPart oldPart,
                String newSegment) {
            if (oldPart == null) {
                return fromEncoded("/" + newSegment);
            }
            String oldPath = oldPart.getEncoded();
            if (oldPath == null) {
                oldPath = "";
            }
            int oldPathLength = oldPath.length();
            String newPath;
            if (oldPathLength == 0) {
                newPath = "/" + newSegment;
            } else if (oldPath.charAt(oldPathLength - 1) == '/') {
                newPath = oldPath + newSegment;
            } else {
                newPath = oldPath + "/" + newSegment;
            }
            return fromEncoded(newPath);
        }
        static PathPart appendDecodedSegment(PathPart oldPart, String decoded) {
            String encoded = encode(decoded);
            return appendEncodedSegment(oldPart, encoded);
        }
        static PathPart readFrom(Parcel parcel) {
            int representation = parcel.readInt();
            switch (representation) {
                case Representation.BOTH:
                    return from(parcel.readString(), parcel.readString());
                case Representation.ENCODED:
                    return fromEncoded(parcel.readString());
                case Representation.DECODED:
                    return fromDecoded(parcel.readString());
                default:
                    throw new AssertionError();
            }
        }
        static PathPart fromEncoded(String encoded) {
            return from(encoded, NOT_CACHED);
        }
        static PathPart fromDecoded(String decoded) {
            return from(NOT_CACHED, decoded);
        }
        static PathPart from(String encoded, String decoded) {
            if (encoded == null) {
                return NULL;
            }
            if (encoded.length() == 0) {
                return EMPTY;
            }
            return new PathPart(encoded, decoded);
        }
        static PathPart makeAbsolute(PathPart oldPart) {
            @SuppressWarnings("StringEquality")
            boolean encodedCached = oldPart.encoded != NOT_CACHED;
            String oldPath = encodedCached ? oldPart.encoded : oldPart.decoded;
            if (oldPath == null || oldPath.length() == 0
                    || oldPath.startsWith("/")) {
                return oldPart;
            }
            String newEncoded = encodedCached
                    ? "/" + oldPart.encoded : NOT_CACHED;
            @SuppressWarnings("StringEquality")
            boolean decodedCached = oldPart.decoded != NOT_CACHED;
            String newDecoded = decodedCached
                    ? "/" + oldPart.decoded
                    : NOT_CACHED;
            return new PathPart(newEncoded, newDecoded);
        }
    }
    public static Uri withAppendedPath(Uri baseUri, String pathSegment) {
        Builder builder = baseUri.buildUpon();
        builder = builder.appendEncodedPath(pathSegment);
        return builder.build();
    }
}
