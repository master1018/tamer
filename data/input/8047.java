class MotifDnDConstants {
    private MotifDnDConstants() {}
    private static final Unsafe unsafe = XlibWrapper.unsafe;
    static final XAtom XA_MOTIF_ATOM_0 = XAtom.get("_MOTIF_ATOM_0");
    static final XAtom XA_MOTIF_DRAG_WINDOW = XAtom.get("_MOTIF_DRAG_WINDOW");
    static final XAtom XA_MOTIF_DRAG_TARGETS = XAtom.get("_MOTIF_DRAG_TARGETS");
    static final XAtom XA_MOTIF_DRAG_INITIATOR_INFO =
        XAtom.get("_MOTIF_DRAG_INITIATOR_INFO");
    static final XAtom XA_MOTIF_DRAG_RECEIVER_INFO =
        XAtom.get("_MOTIF_DRAG_RECEIVER_INFO");
    static final XAtom XA_MOTIF_DRAG_AND_DROP_MESSAGE =
        XAtom.get("_MOTIF_DRAG_AND_DROP_MESSAGE");
    static final XAtom XA_XmTRANSFER_SUCCESS =
        XAtom.get("XmTRANSFER_SUCCESS");
    static final XAtom XA_XmTRANSFER_FAILURE =
        XAtom.get("XmTRANSFER_FAILURE");
    static final XSelection MotifDnDSelection = new XSelection(XA_MOTIF_ATOM_0);
    public static final byte MOTIF_DND_PROTOCOL_VERSION = 0;
    public static final int MOTIF_PREFER_PREREGISTER_STYLE = 2;
    public static final int MOTIF_PREFER_DYNAMIC_STYLE     = 4;
    public static final int MOTIF_DYNAMIC_STYLE            = 5;
    public static final int MOTIF_PREFER_RECEIVER_STYLE    = 6;
    public static final int MOTIF_INITIATOR_INFO_SIZE      = 8;
    public static final int MOTIF_RECEIVER_INFO_SIZE       = 16;
    public static final byte MOTIF_MESSAGE_REASON_MASK      = (byte)0x7F;
    public static final byte MOTIF_MESSAGE_SENDER_MASK      = (byte)0x80;
    public static final byte MOTIF_MESSAGE_FROM_RECEIVER    = (byte)0x80;
    public static final byte MOTIF_MESSAGE_FROM_INITIATOR   = (byte)0;
    public static final int MOTIF_DND_ACTION_MASK   = 0x000F;
    public static final int MOTIF_DND_ACTION_SHIFT  =      0;
    public static final int MOTIF_DND_STATUS_MASK   = 0x00F0;
    public static final int MOTIF_DND_STATUS_SHIFT  =      4;
    public static final int MOTIF_DND_ACTIONS_MASK  = 0x0F00;
    public static final int MOTIF_DND_ACTIONS_SHIFT =      8;
    public static final byte TOP_LEVEL_ENTER   = 0;
    public static final byte TOP_LEVEL_LEAVE   = 1;
    public static final byte DRAG_MOTION       = 2;
    public static final byte DROP_SITE_ENTER   = 3;
    public static final byte DROP_SITE_LEAVE   = 4;
    public static final byte DROP_START        = 5;
    public static final byte DROP_FINISH       = 6;
    public static final byte DRAG_DROP_FINISH  = 7;
    public static final byte OPERATION_CHANGED = 8;
    public static final int MOTIF_DND_NOOP = 0;
    public static final int MOTIF_DND_MOVE = 1 << 0;
    public static final int MOTIF_DND_COPY = 1 << 1;
    public static final int MOTIF_DND_LINK = 1 << 2;
    public static final byte MOTIF_NO_DROP_SITE      = (byte)1;
    public static final byte MOTIF_INVALID_DROP_SITE = (byte)2;
    public static final byte MOTIF_VALID_DROP_SITE   = (byte)3;
    private static long readMotifWindow() throws XException {
        long defaultScreenNumber = XlibWrapper.DefaultScreen(XToolkit.getDisplay());
        long defaultRootWindow =
            XlibWrapper.RootWindow(XToolkit.getDisplay(), defaultScreenNumber);
        long motifWindow = 0;
        WindowPropertyGetter wpg = new WindowPropertyGetter(defaultRootWindow,
                                                            XA_MOTIF_DRAG_WINDOW,
                                                            0, 1,
                                                            false,
                                                            XConstants.AnyPropertyType);
        try {
            int status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == XConstants.Success &&
                wpg.getData() != 0 &&
                wpg.getActualType() == XAtom.XA_WINDOW &&
                wpg.getActualFormat() == 32 &&
                wpg.getNumberOfItems() == 1) {
                long data = wpg.getData();
                motifWindow = Native.getLong(data);
            }
            return motifWindow;
        } finally {
            wpg.dispose();
        }
    }
    private static long createMotifWindow() throws XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        long defaultScreenNumber =
            XlibWrapper.DefaultScreen(XToolkit.getDisplay());
        long defaultRootWindow =
            XlibWrapper.RootWindow(XToolkit.getDisplay(), defaultScreenNumber);
        long motifWindow = 0;
        long displayString = XlibWrapper.XDisplayString(XToolkit.getDisplay());
        if (displayString == 0) {
            throw new XException("XDisplayString returns NULL");
        }
        long newDisplay = XlibWrapper.XOpenDisplay(displayString);
        if (newDisplay == 0) {
            throw new XException("XOpenDisplay returns NULL");
        }
        XlibWrapper.XGrabServer(newDisplay);
        try {
            XlibWrapper.XSetCloseDownMode(newDisplay, (int)XConstants.RetainPermanent);
            XSetWindowAttributes xwa = new XSetWindowAttributes();
            try {
                xwa.set_override_redirect(true);
                xwa.set_event_mask(XConstants.PropertyChangeMask);
                motifWindow = XlibWrapper.XCreateWindow(newDisplay, defaultRootWindow,
                                                        -10, -10, 1, 1, 0, 0,
                                                        XConstants.InputOnly,
                                                        XConstants.CopyFromParent,
                                                        (XConstants.CWOverrideRedirect |
                                                         XConstants.CWEventMask),
                                                        xwa.pData);
                if (motifWindow == 0) {
                    throw new XException("XCreateWindow returns NULL");
                }
                XlibWrapper.XMapWindow(newDisplay, motifWindow);
                long data = Native.allocateLongArray(1);
                try {
                    Native.putLong(data, motifWindow);
                    XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                    XlibWrapper.XChangeProperty(XToolkit.getDisplay(),
                                                defaultRootWindow,
                                                XA_MOTIF_DRAG_WINDOW.getAtom(),
                                                XAtom.XA_WINDOW, 32,
                                                XConstants.PropModeReplace,
                                                data, 1);
                    XToolkit.RESTORE_XERROR_HANDLER();
                    if (XToolkit.saved_error != null &&
                        XToolkit.saved_error.get_error_code() != XConstants.Success) {
                        throw new XException("Cannot write motif drag window handle.");
                    }
                    return motifWindow;
                } finally {
                    unsafe.freeMemory(data);
                }
            } finally {
                xwa.dispose();
            }
        } finally {
            XlibWrapper.XUngrabServer(newDisplay);
            XlibWrapper.XCloseDisplay(newDisplay);
        }
    }
    private static long getMotifWindow() throws XException {
        long motifWindow = readMotifWindow();
        if (motifWindow == 0) {
            motifWindow = createMotifWindow();
        }
        return motifWindow;
    }
    public static final class Swapper {
        private Swapper() {}
        public static short swap(short s) {
            return (short)(((s & 0xFF00) >>> 8) | ((s & 0xFF) << 8));
        }
        public static int swap(int i) {
            return ((i & 0xFF000000) >>> 24) | ((i & 0x00FF0000) >>> 8) |
                ((i & 0x0000FF00) << 8) | ((i & 0x000000FF) << 24);
        }
        public static short getShort(long data, byte order) {
            short s = unsafe.getShort(data);
            if (order != MotifDnDConstants.getByteOrderByte()) {
                return swap(s);
            } else {
                return s;
            }
        }
        public static int getInt(long data, byte order) {
            int i = unsafe.getInt(data);
            if (order != MotifDnDConstants.getByteOrderByte()) {
                return swap(i);
            } else {
                return i;
            }
        }
    }
    private static long[][] getTargetListTable(long motifWindow)
      throws XException {
        WindowPropertyGetter wpg = new WindowPropertyGetter(motifWindow,
                                                            XA_MOTIF_DRAG_TARGETS,
                                                            0, 100000L,
                                                            false,
                                                            XA_MOTIF_DRAG_TARGETS.getAtom());
        try {
            int status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status != XConstants.Success
                || wpg.getActualType() != XA_MOTIF_DRAG_TARGETS.getAtom()
                || wpg.getData() == 0) {
                return null;
            }
            long data = wpg.getData();
            if (unsafe.getByte(data + 1) != MOTIF_DND_PROTOCOL_VERSION) {
                return null;
            }
            boolean swapNeeded = unsafe.getByte(data + 0) != getByteOrderByte();
            short numTargetLists = unsafe.getShort(data + 2);
            if (swapNeeded) {
                numTargetLists = Swapper.swap(numTargetLists);
            }
            long[][] table = new long[numTargetLists][];
            ByteOrder byteOrder = ByteOrder.nativeOrder();
            if (swapNeeded) {
                byteOrder = (byteOrder == ByteOrder.LITTLE_ENDIAN) ?
                    ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
            }
            long bufptr = data + 8;
            for (short i = 0; i < numTargetLists; i++) {
                short numTargets = unsafe.getShort(bufptr);
                bufptr += 2;
                if (swapNeeded) {
                    numTargets = Swapper.swap(numTargets);
                }
                table[i] = new long[numTargets];
                for (short j = 0; j < numTargets; j++) {
                    int target = 0;
                    if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                        for (int idx = 0; idx < 4; idx++) {
                            target |= (unsafe.getByte(bufptr + idx) << 8*idx)
                                & (0xFF << 8*idx);
                        }
                    } else {
                        for (int idx = 0; idx < 4; idx++) {
                            target |= (unsafe.getByte(bufptr + idx) << 8*(3-idx))
                                & (0xFF << 8*(3-idx));
                        }
                    }
                    table[i][j] = target;
                    bufptr += 4;
                }
            }
            return table;
        } finally {
            wpg.dispose();
        }
    }
    private static void putTargetListTable(long motifWindow, long[][] table)
      throws XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        int tableSize = 8; 
        for (int i = 0; i < table.length; i++) {
            tableSize += table[i].length * 4 + 2;
        }
        long data = unsafe.allocateMemory(tableSize);
        try {
            unsafe.putByte(data + 0, getByteOrderByte());
            unsafe.putByte(data + 1, MOTIF_DND_PROTOCOL_VERSION);
            unsafe.putShort(data + 2, (short)table.length);
            unsafe.putInt(data + 4, tableSize);
            long bufptr = data + 8;
            for (int i = 0; i < table.length; i++) {
                unsafe.putShort(bufptr, (short)table[i].length);
                bufptr += 2;
                for (int j = 0; j < table[i].length; j++) {
                    int target = (int)table[i][j];
                    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                        for (int idx = 0; idx < 4; idx++) {
                            byte b = (byte)((target & (0xFF << (8*idx))) >> (8*idx));
                            unsafe.putByte(bufptr + idx, b);
                        }
                    } else {
                        for (int idx = 0; idx < 4; idx++) {
                            byte b = (byte)((target & (0xFF << (8*idx))) >> (8*idx));
                            unsafe.putByte(bufptr + (3-idx), b);
                        }
                    }
                    bufptr += 4;
                }
            }
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(),
                                        motifWindow,
                                        XA_MOTIF_DRAG_TARGETS.getAtom(),
                                        XA_MOTIF_DRAG_TARGETS.getAtom(), 8,
                                        XConstants.PropModeReplace,
                                        data, tableSize);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                motifWindow = createMotifWindow();
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                XlibWrapper.XChangeProperty(XToolkit.getDisplay(),
                                            motifWindow,
                                            XA_MOTIF_DRAG_TARGETS.getAtom(),
                                            XA_MOTIF_DRAG_TARGETS.getAtom(), 8,
                                            XConstants.PropModeReplace,
                                            data, tableSize);
                XToolkit.RESTORE_XERROR_HANDLER();
                if (XToolkit.saved_error != null &&
                    XToolkit.saved_error.get_error_code() != XConstants.Success) {
                    throw new XException("Cannot write motif drag targets property.");
                }
            }
        } finally {
            unsafe.freeMemory(data);
        }
    }
    static int getIndexForTargetList(long[] formats) throws XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (formats.length > 0) {
            formats = (long[])formats.clone();
            Arrays.sort(formats);
        }
        long motifWindow = getMotifWindow();
        XlibWrapper.XGrabServer(XToolkit.getDisplay());
        try {
            long[][] table = getTargetListTable(motifWindow);
            if (table != null) {
                for (int i = 0; i < table.length; i++) {
                    boolean equals = true;
                    if (table[i].length == formats.length) {
                        for (int j = 0; j < table[i].length; j++) {
                            if (table[i][j] != formats[j]) {
                                equals = false;
                                break;
                            }
                        }
                    } else {
                        equals = false;
                    }
                    if (equals) {
                        XlibWrapper.XUngrabServer(XToolkit.getDisplay());
                        return i;
                    }
                }
            } else {
                table = new long[2][];
                table[0] = new long[] { 0 };
                table[1] = new long[] { XAtom.XA_STRING };
            }
            long[][] new_table = new long[table.length + 1][];
            for (int i = 0; i < table.length; i++) {
                new_table[i] = table[i];
            }
            new_table[new_table.length - 1] = formats;
            putTargetListTable(motifWindow, new_table);
            return new_table.length - 1;
        } finally {
            XlibWrapper.XUngrabServer(XToolkit.getDisplay());
        }
    }
    static long[] getTargetListForIndex(int index) {
        long motifWindow = getMotifWindow();
        long[][] table = getTargetListTable(motifWindow);
        if (index < 0 || index >= table.length) {
            return new long[0];
        } else {
            return table[index];
        }
    }
    static byte getByteOrderByte() {
        return ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ?
            (byte)0x6C : (byte)0x42;
    }
    static void writeDragInitiatorInfoStruct(long window, int index) throws XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        long structData = unsafe.allocateMemory(MOTIF_INITIATOR_INFO_SIZE);
        try {
            unsafe.putByte(structData, getByteOrderByte());
            unsafe.putByte(structData + 1, MOTIF_DND_PROTOCOL_VERSION);
            unsafe.putShort(structData + 2, (short)index);
            unsafe.putInt(structData + 4, (int)XA_MOTIF_ATOM_0.getAtom());
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                                        XA_MOTIF_ATOM_0.getAtom(),
                                        XA_MOTIF_DRAG_INITIATOR_INFO.getAtom(),
                                        8, XConstants.PropModeReplace,
                                        structData, MOTIF_INITIATOR_INFO_SIZE);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                throw new XException("Cannot write drag initiator info");
            }
        } finally {
            unsafe.freeMemory(structData);
        }
    }
    static void writeDragReceiverInfoStruct(long window) throws XException {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        int dataSize = MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE;
        long data = unsafe.allocateMemory(dataSize);
        try {
            unsafe.putByte(data, MotifDnDConstants.getByteOrderByte()); 
            unsafe.putByte(data + 1, MotifDnDConstants.MOTIF_DND_PROTOCOL_VERSION); 
            unsafe.putByte(data + 2, (byte)MotifDnDConstants.MOTIF_DYNAMIC_STYLE); 
            unsafe.putByte(data + 3, (byte)0); 
            unsafe.putInt(data + 4, (int)window); 
            unsafe.putShort(data + 8, (short)0); 
            unsafe.putShort(data + 10, (short)0); 
            unsafe.putInt(data + 12, dataSize);
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), window,
                                        XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                        XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                        8, XConstants.PropModeReplace,
                                        data, dataSize);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                throw new XException("Cannot write Motif receiver info property");
            }
        } finally {
            unsafe.freeMemory(data);
        }
    }
    public static int getMotifActionsForJavaActions(int javaActions) {
        int motifActions = MOTIF_DND_NOOP;
        if ((javaActions & DnDConstants.ACTION_MOVE) != 0) {
            motifActions |= MOTIF_DND_MOVE;
        }
        if ((javaActions & DnDConstants.ACTION_COPY) != 0) {
            motifActions |= MOTIF_DND_COPY;
        }
        if ((javaActions & DnDConstants.ACTION_LINK) != 0) {
            motifActions |= MOTIF_DND_LINK;
        }
        return motifActions;
    }
    public static int getJavaActionsForMotifActions(int motifActions) {
        int javaActions = DnDConstants.ACTION_NONE;
        if ((motifActions & MOTIF_DND_MOVE) != 0) {
            javaActions |= DnDConstants.ACTION_MOVE;
        }
        if ((motifActions & MOTIF_DND_COPY) != 0) {
            javaActions |= DnDConstants.ACTION_COPY;
        }
        if ((motifActions & MOTIF_DND_LINK) != 0) {
            javaActions |= DnDConstants.ACTION_LINK;
        }
        return javaActions;
    }
}
