public final class XAtom {
    private static Unsafe unsafe = XlibWrapper.unsafe;
    private static XAtom[] emptyList = new XAtom[0];
    public static final long XA_PRIMARY=1;
    public static final long XA_SECONDARY=2;
    public static final long XA_ARC=3;
    public static final long XA_ATOM=4;
    public static final long XA_BITMAP=5;
    public static final long XA_CARDINAL=6;
    public static final long XA_COLORMAP=7;
    public static final long XA_CURSOR=8;
    public static final long XA_CUT_BUFFER0=9;
    public static final long XA_CUT_BUFFER1=10;
    public static final long XA_CUT_BUFFER2=11;
    public static final long XA_CUT_BUFFER3=12;
    public static final long XA_CUT_BUFFER4=13;
    public static final long XA_CUT_BUFFER5=14;
    public static final long XA_CUT_BUFFER6=15;
    public static final long XA_CUT_BUFFER7=16;
    public static final long XA_DRAWABLE=17;
    public static final long XA_FONT=18;
    public static final long XA_INTEGER=19;
    public static final long XA_PIXMAP=20;
    public static final long XA_POINT=21;
    public static final long XA_RECTANGLE=22;
    public static final long XA_RESOURCE_MANAGER=23;
    public static final long XA_RGB_COLOR_MAP=24;
    public static final long XA_RGB_BEST_MAP=25;
    public static final long XA_RGB_BLUE_MAP=26;
    public static final long XA_RGB_DEFAULT_MAP=27;
    public static final long XA_RGB_GRAY_MAP=28;
    public static final long XA_RGB_GREEN_MAP=29;
    public static final long XA_RGB_RED_MAP=30;
    public static final long XA_STRING=31;
    public static final long XA_VISUALID=32;
    public static final long XA_WINDOW=33;
    public static final long XA_WM_COMMAND=34;
    public static final long XA_WM_HINTS=35;
    public static final long XA_WM_CLIENT_MACHINE=36;
    public static final long XA_WM_ICON_NAME=37;
    public static final long XA_WM_ICON_SIZE=38;
    public static final long XA_WM_NAME=39;
    public static final long XA_WM_NORMAL_HINTS=40;
    public static final long XA_WM_SIZE_HINTS=41;
    public static final long XA_WM_ZOOM_HINTS=42;
    public static final long XA_MIN_SPACE=43;
    public static final long XA_NORM_SPACE=44;
    public static final long XA_MAX_SPACE=45;
    public static final long XA_END_SPACE=46;
    public static final long XA_SUPERSCRIPT_X=47;
    public static final long XA_SUPERSCRIPT_Y=48;
    public static final long XA_SUBSCRIPT_X=49;
    public static final long XA_SUBSCRIPT_Y=50;
    public static final long XA_UNDERLINE_POSITION=51;
    public static final long XA_UNDERLINE_THICKNESS=52 ;
    public static final long XA_STRIKEOUT_ASCENT=53;
    public static final long XA_STRIKEOUT_DESCENT=54;
    public static final long XA_ITALIC_ANGLE=55;
    public static final long XA_X_HEIGHT=56;
    public static final long XA_QUAD_WIDTH=57;
    public static final long XA_WEIGHT=58;
    public static final long XA_POINT_SIZE=59;
    public static final long XA_RESOLUTION=60;
    public static final long XA_COPYRIGHT=61;
    public static final long XA_NOTICE=62;
    public static final long XA_FONT_NAME=63;
    public static final long XA_FAMILY_NAME=64;
    public static final long XA_FULL_NAME=65;
    public static final long XA_CAP_HEIGHT=66;
    public static final long XA_WM_CLASS=67;
    public static final long XA_WM_TRANSIENT_FOR=68;
    public static final long XA_LAST_PREDEFINED=68;
    static HashMap<Long, XAtom> atomToAtom = new HashMap<Long, XAtom>();
    static HashMap<String, XAtom> nameToAtom = new HashMap<String, XAtom>();
    static void register(XAtom at) {
        if (at == null) {
            return;
        }
        synchronized (XAtom.class) {
            if (at.atom != 0) {
                atomToAtom.put(Long.valueOf(at.atom), at);
            }
            if (at.name != null) {
                nameToAtom.put(at.name, at);
            }
        }
    }
    static XAtom lookup(long atom) {
        synchronized (XAtom.class) {
            return atomToAtom.get(Long.valueOf(atom));
        }
    }
    static XAtom lookup(String name) {
        synchronized (XAtom.class) {
            return nameToAtom.get(name);
        }
    }
    static XAtom get(long atom) {
        XAtom xatom = lookup(atom);
        if (xatom == null) {
            xatom = new XAtom(XToolkit.getDisplay(), atom);
        }
        return xatom;
    }
    public static XAtom get(String name) {
        XAtom xatom = lookup(name);
        if (xatom == null) {
            xatom = new XAtom(XToolkit.getDisplay(), name);
        }
        return xatom;
    }
    public final String getName() {
        if (name == null) {
            XToolkit.awtLock();
            try {
                this.name = XlibWrapper.XGetAtomName(display, atom);
            } finally {
                XToolkit.awtUnlock();
            }
            register();
        }
        return name;
    }
    static String asString(long atom) {
        XAtom at = lookup(atom);
        if (at == null) {
            return Long.toString(atom);
        } else {
            return at.toString();
        }
    }
    void register() {
        register(this);
    }
    public String toString() {
        if (name != null) {
            return name + ":" + atom;
        } else {
            return Long.toString(atom);
        }
    }
    long atom = 0;
    String name;
    long display;
    private XAtom(long display, String name) {
        this(display, name, true);
    }
    public XAtom(String name, boolean autoIntern) {
        this(XToolkit.getDisplay(), name, autoIntern);
    }
    public XAtom(long display, long atom) {
        this.atom = atom;
        this.display = display;
        register();
    }
    private XAtom(long display, String name, boolean autoIntern) {
        this.name = name;
        this.display = display;
        if (autoIntern) {
            XToolkit.awtLock();
            try {
                atom = XlibWrapper.InternAtom(display,name,0);
            } finally {
                XToolkit.awtUnlock();
            }
        }
        register();
    }
    public XAtom() {
    }
    public void setProperty(long window, String str) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            XlibWrapper.SetProperty(display,window,atom,str);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setPropertyUTF8(long window, String str) {
        XAtom XA_UTF8_STRING = XAtom.get("UTF8_STRING");   
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        byte[] bdata = null;
        try {
            bdata = str.getBytes("UTF-8");
        } catch (java.io.UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        if (bdata != null) {
            setAtomData(window, XA_UTF8_STRING.atom, bdata);
        }
    }
    public void setProperty8(long window, String str) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        byte[] bdata = null;
        try {
            bdata = str.getBytes("ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        if (bdata != null) {
            setAtomData(window, XA_STRING, bdata);
        }
    }
    public String getProperty(long window) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            return XlibWrapper.GetProperty(display,window,atom);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public long get32Property(long window, long property_type) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, 1,
                                     false, property_type);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return 0;
            }
            if (getter.getActualType() != property_type || getter.getActualFormat() != 32) {
                return 0;
            }
            return Native.getCard32(getter.getData());
        } finally {
            getter.dispose();
        }
    }
    public long getCard32Property(XBaseWindow window) {
        return get32Property(window.getWindow(), XA_CARDINAL);
    }
    public void setCard32Property(long window, long value) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            Native.putCard32(XlibWrapper.larg1, value);
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                atom, XA_CARDINAL, 32, XConstants.PropModeReplace,
                XlibWrapper.larg1, 1);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setCard32Property(XBaseWindow window, long value) {
        setCard32Property(window.getWindow(), value);
    }
    public boolean getAtomData(long window, long data_ptr, int length) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, (long)length,
                                     false, this);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return false;
            }
            if (getter.getActualType() != atom
                || getter.getActualFormat() != 32
                || getter.getNumberOfItems() != length
                )
                {
                    return false;
                }
            XlibWrapper.memcpy(data_ptr, getter.getData(), length*getAtomSize());
            return true;
        } finally {
            getter.dispose();
        }
    }
    public boolean getAtomData(long window, long type, long data_ptr, int length) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, (long)length,
                                     false, type);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return false;
            }
            if (getter.getActualType() != type
                || getter.getActualFormat() != 32
                || getter.getNumberOfItems() != length
                )
                {
                    return false;
                }
            XlibWrapper.memcpy(data_ptr, getter.getData(), length*getAtomSize());
            return true;
        } finally {
            getter.dispose();
        }
    }
    public void setAtomData(long window, long data_ptr, int length) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                atom, atom, 32, XConstants.PropModeReplace,
                data_ptr, length);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setAtomData(long window, long type, long data_ptr, int length) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                atom, type, 32, XConstants.PropModeReplace,
                data_ptr, length);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setAtomData8(long window, long type, long data_ptr, int length) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                atom, type, 8, XConstants.PropModeReplace,
                data_ptr, length);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void DeleteProperty(long window) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
             XlibWrapper.XDeleteProperty(XToolkit.getDisplay(), window, atom);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void DeleteProperty(XBaseWindow window) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window.getWindow());
        XToolkit.awtLock();
        try {
            XlibWrapper.XDeleteProperty(XToolkit.getDisplay(),
                window.getWindow(), atom);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setAtomData(long window, long property_type, byte[] data) {
        long bdata = Native.toData(data);
        try {
            setAtomData8(window, property_type, bdata, data.length);
        } finally {
            unsafe.freeMemory(bdata);
        }
    }
    public byte[] getByteArrayProperty(long window, long property_type) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, 0xFFFF,
                                     false, property_type);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return null;
            }
            if (getter.getActualType() != property_type || getter.getActualFormat() != 8) {
                return null;
            }
            byte[] res = XlibWrapper.getStringBytes(getter.getData());
            return res;
        } finally {
            getter.dispose();
        }
    }
    public void intern(boolean onlyIfExists) {
        XToolkit.awtLock();
        try {
            atom = XlibWrapper.InternAtom(display,name, onlyIfExists?1:0);
        } finally {
            XToolkit.awtUnlock();
        }
        register();
    }
    public boolean isInterned() {
        if (atom == 0) {
            XToolkit.awtLock();
            try {
                atom = XlibWrapper.InternAtom(display, name, 1);
            } finally {
                XToolkit.awtUnlock();
            }
            if (atom == 0) {
                return false;
            } else {
                register();
                return true;
            }
        } else {
            return true;
        }
    }
    public void setValues(long display, String name, long atom) {
        this.display = display;
        this.atom = atom;
        this.name = name;
        register();
    }
    static int getAtomSize() {
        return Native.getLongSize();
    }
    XAtom[] getAtomListProperty(long window) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, 0xFFFF,
                                     false, XA_ATOM);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return emptyList;
            }
            if (getter.getActualType() != XA_ATOM || getter.getActualFormat() != 32) {
                return emptyList;
            }
            int count = (int)getter.getNumberOfItems();
            if (count == 0) {
                return emptyList;
            }
            long list_atoms = getter.getData();
            XAtom[] res = new XAtom[count];
            for (int index = 0; index < count; index++) {
                res[index] = XAtom.get(XAtom.getAtom(list_atoms+index*getAtomSize()));
            }
            return res;
        } finally {
            getter.dispose();
        }
    }
    XAtomList getAtomListPropertyList(long window) {
        return new XAtomList(getAtomListProperty(window));
    }
    XAtomList getAtomListPropertyList(XBaseWindow window) {
        return getAtomListPropertyList(window.getWindow());
    }
    XAtom[] getAtomListProperty(XBaseWindow window) {
        return getAtomListProperty(window.getWindow());
    }
    void setAtomListProperty(long window, XAtom[] atoms) {
        long data = toData(atoms);
        setAtomData(window, XAtom.XA_ATOM, data, atoms.length);
        unsafe.freeMemory(data);
    }
    void setAtomListProperty(long window, XAtomList atoms) {
        long data = atoms.getAtomsData();
        setAtomData(window, XAtom.XA_ATOM, data, atoms.size());
        unsafe.freeMemory(data);
    }
    public void setAtomListProperty(XBaseWindow window, XAtom[] atoms) {
        setAtomListProperty(window.getWindow(), atoms);
    }
    public void setAtomListProperty(XBaseWindow window, XAtomList atoms) {
        setAtomListProperty(window.getWindow(), atoms);
    }
    long getAtom() {
        return atom;
    }
    void putAtom(long ptr) {
        Native.putLong(ptr, atom);
    }
    static long getAtom(long ptr) {
        return Native.getLong(ptr);
    }
    static long toData(XAtom[] atoms) {
        long data = unsafe.allocateMemory(getAtomSize() * atoms.length);
        for (int i = 0; i < atoms.length; i++ ) {
            if (atoms[i] != null) {
                atoms[i].putAtom(data + i * getAtomSize());
            }
        }
        return data;
    }
    void checkWindow(long window) {
        if (window == 0) {
            throw new IllegalArgumentException("Window must not be zero");
        }
    }
    public boolean equals(Object o) {
        if (!(o instanceof XAtom)) {
            return false;
        }
        XAtom ot = (XAtom)o;
        return (atom == ot.atom && display == ot.display);
    }
    public int hashCode() {
        return (int)((atom ^ display)& 0xFFFFL);
    }
    public void setWindowProperty(long window, long window_value) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        XToolkit.awtLock();
        try {
            Native.putWindow(XlibWrapper.larg1, window_value);
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                                    atom, XA_WINDOW, 32, XConstants.PropModeReplace,
                                    XlibWrapper.larg1, 1);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setWindowProperty(XBaseWindow window, XBaseWindow window_value) {
        setWindowProperty(window.getWindow(), window_value.getWindow());
    }
    public long getWindowProperty(long window) {
        if (atom == 0) {
            throw new IllegalStateException("Atom should be initialized");
        }
        checkWindow(window);
        WindowPropertyGetter getter =
            new WindowPropertyGetter(window, this, 0, 1,
                                     false, XA_WINDOW);
        try {
            int status = getter.execute();
            if (status != XConstants.Success || getter.getData() == 0) {
                return 0;
            }
            if (getter.getActualType() != XA_WINDOW || getter.getActualFormat() != 32) {
                return 0;
            }
            return Native.getWindow(getter.getData());
        } finally {
            getter.dispose();
        }
    }
}
