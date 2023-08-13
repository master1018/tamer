class UnixPath
    extends AbstractPath
{
    private static ThreadLocal<SoftReference<CharsetEncoder>> encoder =
        new ThreadLocal<SoftReference<CharsetEncoder>>();
    private final UnixFileSystem fs;
    private final byte[] path;
    private volatile String stringValue;
    private int hash;
    private volatile int[] offsets;
    UnixPath(UnixFileSystem fs, byte[] path) {
        this.fs = fs;
        this.path = path;
    }
    UnixPath(UnixFileSystem fs, String input) {
        this(fs, encode(normalizeAndCheck(input)));
    }
    static String normalizeAndCheck(String input) {
        int n = input.length();
        char prevChar = 0;
        for (int i=0; i < n; i++) {
            char c = input.charAt(i);
            if ((c == '/') && (prevChar == '/'))
                return normalize(input, n, i - 1);
            checkNotNul(input, c);
            prevChar = c;
        }
        if (prevChar == '/')
            return normalize(input, n, n - 1);
        return input;
    }
    private static void checkNotNul(String input, char c) {
        if (c == '\u0000')
            throw new InvalidPathException(input, "Nul character not allowed");
    }
    private static String normalize(String input, int len, int off) {
        if (len == 0)
            return input;
        int n = len;
        while ((n > 0) && (input.charAt(n - 1) == '/')) n--;
        if (n == 0)
            return "/";
        StringBuilder sb = new StringBuilder(input.length());
        if (off > 0)
            sb.append(input.substring(0, off));
        char prevChar = 0;
        for (int i=off; i < n; i++) {
            char c = input.charAt(i);
            if ((c == '/') && (prevChar == '/'))
                continue;
            checkNotNul(input, c);
            sb.append(c);
            prevChar = c;
        }
        return sb.toString();
    }
    private static byte[] encode(String input) {
        SoftReference<CharsetEncoder> ref = encoder.get();
        CharsetEncoder ce = (ref != null) ? ref.get() : null;
        if (ce == null) {
            ce = Charset.defaultCharset().newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
            encoder.set(new SoftReference<CharsetEncoder>(ce));
        }
        char[] ca = input.toCharArray();
        byte[] ba = new byte[(int)(ca.length * (double)ce.maxBytesPerChar())];
        ByteBuffer bb = ByteBuffer.wrap(ba);
        CharBuffer cb = CharBuffer.wrap(ca);
        ce.reset();
        CoderResult cr = ce.encode(cb, bb, true);
        boolean error;
        if (!cr.isUnderflow()) {
            error = true;
        } else {
            cr = ce.flush(bb);
            error = !cr.isUnderflow();
        }
        if (error) {
            throw new InvalidPathException(input,
                "Malformed input or input contains unmappable chacraters");
        }
        int len = bb.position();
        if (len != ba.length)
            ba = Arrays.copyOf(ba, len);
        return ba;
    }
    byte[] asByteArray() {
        return path;
    }
    byte[] getByteArrayForSysCalls() {
        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
            return resolve(getFileSystem().defaultDirectory(), path);
        } else {
            if (!isEmpty()) {
                return path;
            } else {
                byte[] here = { '.' };
                return here;
            }
        }
    }
    String getPathForExecptionMessage() {
        return toString();
    }
    String getPathForPermissionCheck() {
        if (getFileSystem().needToResolveAgainstDefaultDirectory()) {
            return new String(getByteArrayForSysCalls());
        } else {
            return toString();
        }
    }
    static UnixPath toUnixPath(Path obj) {
        if (obj == null)
            throw new NullPointerException();
        if (!(obj instanceof UnixPath))
            throw new ProviderMismatchException();
        return (UnixPath)obj;
    }
    private void initOffsets() {
        if (offsets == null) {
            int count, index;
            count = 0;
            index = 0;
            if (isEmpty()) {
                count = 1;
            } else {
                while (index < path.length) {
                    byte c = path[index++];
                    if (c != '/') {
                        count++;
                        while (index < path.length && path[index] != '/')
                            index++;
                    }
                }
            }
            int[] result = new int[count];
            count = 0;
            index = 0;
            while (index < path.length) {
                byte c = path[index];
                if (c == '/') {
                    index++;
                } else {
                    result[count++] = index++;
                    while (index < path.length && path[index] != '/')
                        index++;
                }
            }
            synchronized (this) {
                if (offsets == null)
                    offsets = result;
            }
        }
    }
    private boolean isEmpty() {
        return path.length == 0;
    }
    private UnixPath emptyPath() {
        return new UnixPath(getFileSystem(), new byte[0]);
    }
    @Override
    public UnixFileSystem getFileSystem() {
        return fs;
    }
    @Override
    public UnixPath getRoot() {
        if (path.length > 0 && path[0] == '/') {
            return getFileSystem().rootDirectory();
        } else {
            return null;
        }
    }
    @Override
    public UnixPath getFileName() {
        initOffsets();
        int count = offsets.length;
        if (count == 0)
            return null;
        if (count == 1 && path.length > 0 && path[0] != '/')
            return this;
        int lastOffset = offsets[count-1];
        int len = path.length - lastOffset;
        byte[] result = new byte[len];
        System.arraycopy(path, lastOffset, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }
    @Override
    public UnixPath getParent() {
        initOffsets();
        int count = offsets.length;
        if (count == 0) {
            return null;
        }
        int len = offsets[count-1] - 1;
        if (len <= 0) {
            return getRoot();
        }
        byte[] result = new byte[len];
        System.arraycopy(path, 0, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }
    @Override
    public int getNameCount() {
        initOffsets();
        return offsets.length;
    }
    @Override
    public UnixPath getName(int index) {
        initOffsets();
        if (index < 0)
            throw new IllegalArgumentException();
        if (index >= offsets.length)
            throw new IllegalArgumentException();
        int begin = offsets[index];
        int len;
        if (index == (offsets.length-1)) {
            len = path.length - begin;
        } else {
            len = offsets[index+1] - begin - 1;
        }
        byte[] result = new byte[len];
        System.arraycopy(path, begin, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }
    @Override
    public UnixPath subpath(int beginIndex, int endIndex) {
        initOffsets();
        if (beginIndex < 0)
            throw new IllegalArgumentException();
        if (beginIndex >= offsets.length)
            throw new IllegalArgumentException();
        if (endIndex > offsets.length)
            throw new IllegalArgumentException();
        if (beginIndex >= endIndex) {
            throw new IllegalArgumentException();
        }
        int begin = offsets[beginIndex];
        int len;
        if (endIndex == offsets.length) {
            len = path.length - begin;
        } else {
            len = offsets[endIndex] - begin - 1;
        }
        byte[] result = new byte[len];
        System.arraycopy(path, begin, result, 0, len);
        return new UnixPath(getFileSystem(), result);
    }
    @Override
    public boolean isAbsolute() {
        return (path.length > 0 && path[0] == '/');
    }
    private static byte[] resolve(byte[] base, byte[] child) {
        int baseLength = base.length;
        int childLength = child.length;
        if (childLength == 0)
            return base;
        if (baseLength == 0 || child[0] == '/')
            return child;
        byte[] result;
        if (baseLength == 1 && base[0] == '/') {
            result = new byte[childLength + 1];
            result[0] = '/';
            System.arraycopy(child, 0, result, 1, childLength);
        } else {
            result = new byte[baseLength + 1 + childLength];
            System.arraycopy(base, 0, result, 0, baseLength);
            result[base.length] = '/';
            System.arraycopy(child, 0, result, baseLength+1, childLength);
        }
        return result;
    }
    @Override
    public UnixPath resolve(Path obj) {
        byte[] other = toUnixPath(obj).path;
        if (other.length > 0 && other[0] == '/')
            return ((UnixPath)obj);
        byte[] result = resolve(path, other);
        return new UnixPath(getFileSystem(), result);
    }
    UnixPath resolve(byte[] other) {
        return resolve(new UnixPath(getFileSystem(), other));
    }
    @Override
    public UnixPath relativize(Path obj) {
        UnixPath other = toUnixPath(obj);
        if (other.equals(this))
            return emptyPath();
        if (this.isAbsolute() != other.isAbsolute())
            throw new IllegalArgumentException("'other' is different type of Path");
        if (this.isEmpty())
            return other;
        int bn = this.getNameCount();
        int cn = other.getNameCount();
        int n = (bn > cn) ? cn : bn;
        int i = 0;
        while (i < n) {
            if (!this.getName(i).equals(other.getName(i)))
                break;
            i++;
        }
        int dotdots = bn - i;
        if (i < cn) {
            UnixPath remainder = other.subpath(i, cn);
            if (dotdots == 0)
                return remainder;
            boolean isOtherEmpty = other.isEmpty();
            int len = dotdots*3 + remainder.path.length;
            if (isOtherEmpty) {
                assert remainder.isEmpty();
                len--;
            }
            byte[] result = new byte[len];
            int pos = 0;
            while (dotdots > 0) {
                result[pos++] = (byte)'.';
                result[pos++] = (byte)'.';
                if (isOtherEmpty) {
                    if (dotdots > 1) result[pos++] = (byte)'/';
                } else {
                    result[pos++] = (byte)'/';
                }
                dotdots--;
            }
            System.arraycopy(remainder.path, 0, result, pos, remainder.path.length);
            return new UnixPath(getFileSystem(), result);
        } else {
            byte[] result = new byte[dotdots*3 - 1];
            int pos = 0;
            while (dotdots > 0) {
                result[pos++] = (byte)'.';
                result[pos++] = (byte)'.';
                if (dotdots > 1)
                    result[pos++] = (byte)'/';
                dotdots--;
            }
            return new UnixPath(getFileSystem(), result);
        }
    }
    @Override
    public Path normalize() {
        final int count = getNameCount();
        if (count == 0)
            return this;
        boolean[] ignore = new boolean[count];      
        int[] size = new int[count];                
        int remaining = count;                      
        boolean hasDotDot = false;                  
        boolean isAbsolute = isAbsolute();
        for (int i=0; i<count; i++) {
            int begin = offsets[i];
            int len;
            if (i == (offsets.length-1)) {
                len = path.length - begin;
            } else {
                len = offsets[i+1] - begin - 1;
            }
            size[i] = len;
            if (path[begin] == '.') {
                if (len == 1) {
                    ignore[i] = true;  
                    remaining--;
                }
                else {
                    if (path[begin+1] == '.')   
                        hasDotDot = true;
                }
            }
        }
        if (hasDotDot) {
            int prevRemaining;
            do {
                prevRemaining = remaining;
                int prevName = -1;
                for (int i=0; i<count; i++) {
                    if (ignore[i])
                        continue;
                    if (size[i] != 2) {
                        prevName = i;
                        continue;
                    }
                    int begin = offsets[i];
                    if (path[begin] != '.' || path[begin+1] != '.') {
                        prevName = i;
                        continue;
                    }
                    if (prevName >= 0) {
                        ignore[prevName] = true;
                        ignore[i] = true;
                        remaining = remaining - 2;
                        prevName = -1;
                    } else {
                        if (isAbsolute) {
                            boolean hasPrevious = false;
                            for (int j=0; j<i; j++) {
                                if (!ignore[j]) {
                                    hasPrevious = true;
                                    break;
                                }
                            }
                            if (!hasPrevious) {
                                ignore[i] = true;
                                remaining--;
                            }
                        }
                    }
                }
            } while (prevRemaining > remaining);
        }
        if (remaining == count)
            return this;
        if (remaining == 0) {
            return isAbsolute ? getFileSystem().rootDirectory() : emptyPath();
        }
        int len = remaining - 1;
        if (isAbsolute)
            len++;
        for (int i=0; i<count; i++) {
            if (!ignore[i])
                len += size[i];
        }
        byte[] result = new byte[len];
        int pos = 0;
        if (isAbsolute)
            result[pos++] = '/';
        for (int i=0; i<count; i++) {
            if (!ignore[i]) {
                System.arraycopy(path, offsets[i], result, pos, size[i]);
                pos += size[i];
                if (--remaining > 0) {
                    result[pos++] = '/';
                }
            }
        }
        return new UnixPath(getFileSystem(), result);
    }
    @Override
    public boolean startsWith(Path other) {
        if (!(Objects.requireNonNull(other) instanceof UnixPath))
            return false;
        UnixPath that = (UnixPath)other;
        if (that.path.length > path.length)
            return false;
        int thisOffsetCount = getNameCount();
        int thatOffsetCount = that.getNameCount();
        if (thatOffsetCount == 0 && this.isAbsolute()) {
            return that.isEmpty() ? false : true;
        }
        if (thatOffsetCount > thisOffsetCount)
            return false;
        if ((thatOffsetCount == thisOffsetCount) &&
            (path.length != that.path.length)) {
            return false;
        }
        for (int i=0; i<thatOffsetCount; i++) {
            Integer o1 = offsets[i];
            Integer o2 = that.offsets[i];
            if (!o1.equals(o2))
                return false;
        }
        int i=0;
        while (i < that.path.length) {
            if (this.path[i] != that.path[i])
                return false;
            i++;
        }
        if (i < path.length && this.path[i] != '/')
            return false;
        return true;
    }
    @Override
    public boolean endsWith(Path other) {
        if (!(Objects.requireNonNull(other) instanceof UnixPath))
            return false;
        UnixPath that = (UnixPath)other;
        int thisLen = path.length;
        int thatLen = that.path.length;
        if (thatLen > thisLen)
            return false;
        if (thisLen > 0 && thatLen == 0)
            return false;
        if (that.isAbsolute() && !this.isAbsolute())
            return false;
        int thisOffsetCount = getNameCount();
        int thatOffsetCount = that.getNameCount();
        if (thatOffsetCount > thisOffsetCount) {
            return false;
        } else {
            if (thatOffsetCount == thisOffsetCount) {
                if (thisOffsetCount == 0)
                    return true;
                int expectedLen = thisLen;
                if (this.isAbsolute() && !that.isAbsolute())
                    expectedLen--;
                if (thatLen != expectedLen)
                    return false;
            } else {
                if (that.isAbsolute())
                    return false;
            }
        }
        int thisPos = offsets[thisOffsetCount - thatOffsetCount];
        int thatPos = that.offsets[0];
        if ((thatLen - thatPos) != (thisLen - thisPos))
            return false;
        while (thatPos < thatLen) {
            if (this.path[thisPos++] != that.path[thatPos++])
                return false;
        }
        return true;
    }
    @Override
    public int compareTo(Path other) {
        int len1 = path.length;
        int len2 = ((UnixPath) other).path.length;
        int n = Math.min(len1, len2);
        byte v1[] = path;
        byte v2[] = ((UnixPath) other).path;
        int k = 0;
        while (k < n) {
            int c1 = v1[k] & 0xff;
            int c2 = v2[k] & 0xff;
            if (c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }
    @Override
    public boolean equals(Object ob) {
        if ((ob != null) && (ob instanceof UnixPath)) {
            return compareTo((Path)ob) == 0;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            for (int i = 0; i< path.length; i++) {
                h = 31*h + (path[i] & 0xff);
            }
            hash = h;
        }
        return h;
    }
    @Override
    public String toString() {
        if (stringValue == null)
            stringValue = new String(path);     
        return stringValue;
    }
    int openForAttributeAccess(boolean followLinks) throws IOException {
        int flags = O_RDONLY;
        if (!followLinks)
            flags |= O_NOFOLLOW;
        try {
            return open(this, flags, 0);
        } catch (UnixException x) {
            if (getFileSystem().isSolaris() && x.errno() == EINVAL)
                x.setError(ELOOP);
            if (x.errno() == ELOOP)
                throw new FileSystemException(getPathForExecptionMessage(), null,
                    x.getMessage() + " or unable to access attributes of symbolic link");
            x.rethrowAsIOException(this);
            return -1; 
        }
    }
    void checkRead() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkRead(getPathForPermissionCheck());
    }
    void checkWrite() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkWrite(getPathForPermissionCheck());
    }
    void checkDelete() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkDelete(getPathForPermissionCheck());
    }
    @Override
    public UnixPath toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPropertyAccess("user.dir");
        }
        return new UnixPath(getFileSystem(),
            resolve(getFileSystem().defaultDirectory(), path));
    }
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        checkRead();
        UnixPath absolute = toAbsolutePath();
        if (Util.followLinks(options)) {
            try {
                byte[] rp = realpath(absolute);
                return new UnixPath(getFileSystem(), rp);
            } catch (UnixException x) {
                x.rethrowAsIOException(this);
            }
        }
        UnixPath result = fs.rootDirectory();
        for (int i=0; i<absolute.getNameCount(); i++) {
            UnixPath element = absolute.getName(i);
            if ((element.asByteArray().length == 1) && (element.asByteArray()[0] == '.'))
                continue;
            if ((element.asByteArray().length == 2) && (element.asByteArray()[0] == '.') &&
                (element.asByteArray()[1] == '.'))
            {
                UnixFileAttributes attrs = null;
                try {
                    attrs = UnixFileAttributes.get(result, false);
                } catch (UnixException x) {
                    x.rethrowAsIOException(result);
                }
                if (!attrs.isSymbolicLink()) {
                    result = result.getParent();
                    if (result == null) {
                        result = fs.rootDirectory();
                    }
                    continue;
                }
            }
            result = result.resolve(element);
        }
        try {
            UnixFileAttributes.get(result, false);
        } catch (UnixException x) {
            x.rethrowAsIOException(result);
        }
        return result;
    }
    @Override
    public URI toUri() {
        return UnixUriUtils.toUri(this);
    }
    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>[] events,
                             WatchEvent.Modifier... modifiers)
        throws IOException
    {
        if (watcher == null)
            throw new NullPointerException();
        if (!(watcher instanceof AbstractWatchService))
            throw new ProviderMismatchException();
        checkRead();
        return ((AbstractWatchService)watcher).register(this, events, modifiers);
    }
}
