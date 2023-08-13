public class GnomeFileTypeDetector
    extends AbstractFileTypeDetector
{
    private static final String GNOME_VFS_MIME_TYPE_UNKNOWN =
        "application/octet-stream";
    private final boolean gioAvailable;
    private final boolean gnomeVfsAvailable;
    public GnomeFileTypeDetector() {
        gioAvailable = initializeGio();
        if (gioAvailable) {
            gnomeVfsAvailable = false;
        } else {
            gnomeVfsAvailable = initializeGnomeVfs();
        }
    }
    @Override
    public String implProbeContentType(Path obj) throws IOException {
        if (!gioAvailable && !gnomeVfsAvailable)
            return null;
        if (!(obj instanceof UnixPath))
            return null;
        UnixPath path = (UnixPath)obj;
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path.getByteArrayForSysCalls());
        try {
            if (gioAvailable) {
                byte[] type = probeUsingGio(buffer.address());
                return (type == null) ? null : new String(type);
            } else {
                byte[] type = probeUsingGnomeVfs(buffer.address());
                if (type == null)
                    return null;
                String s = new String(type);
                return s.equals(GNOME_VFS_MIME_TYPE_UNKNOWN) ? null : s;
            }
        } finally {
            buffer.release();
        }
    }
    private static native boolean initializeGio();
    private static native byte[] probeUsingGio(long pathAddress);
    private static native boolean initializeGnomeVfs();
    private static native byte[] probeUsingGnomeVfs(long pathAddress);
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                System.loadLibrary("nio");
                return null;
        }});
    }
}
