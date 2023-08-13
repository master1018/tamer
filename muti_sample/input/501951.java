class IconLoader extends Handler {
    private int mState = STATE_SINGLE_ICON;
    private ImageDescriptor mId = null;
    private Bitmap mCurrentIcon = null;
    private int mRecordNumber;
    private SIMFileHandler mSimFH = null;
    private Message mEndMsg = null;
    private byte[] mIconData = null;
    private int[] mRecordNumbers = null;
    private int mCurrentRecordIndex = 0;
    private Bitmap[] mIcons = null;
    private HashMap<Integer, Bitmap> mIconsCache = null;
    private static IconLoader sLoader = null;
    private static final int STATE_SINGLE_ICON = 1;
    private static final int STATE_MULTI_ICONS = 2;
    private static final int EVENT_READ_EF_IMG_RECOED_DONE  = 1;
    private static final int EVENT_READ_ICON_DONE           = 2;
    private static final int EVENT_READ_CLUT_DONE           = 3;
    private static final int CLUT_LOCATION_OFFSET = 4;
    private static final int CLUT_ENTRY_SIZE = 3;
    private IconLoader(Looper looper , SIMFileHandler fh) {
        super(looper);
        mSimFH = fh;
        mIconsCache = new HashMap<Integer, Bitmap>(50);
    }
    static IconLoader getInstance(Handler caller, SIMFileHandler fh) {
        if (sLoader != null) {
            return sLoader;
        }
        if (fh != null) {
            HandlerThread thread = new HandlerThread("Stk Icon Loader");
            thread.start();
            return new IconLoader(thread.getLooper(), fh);
        }
        return null;
    }
    void loadIcons(int[] recordNumbers, Message msg) {
        if (recordNumbers == null || recordNumbers.length == 0 || msg == null) {
            return;
        }
        mEndMsg = msg;
        mIcons = new Bitmap[recordNumbers.length];
        mRecordNumbers = recordNumbers;
        mCurrentRecordIndex = 0;
        mState = STATE_MULTI_ICONS;
        startLoadingIcon(recordNumbers[0]);
    }
    void loadIcon(int recordNumber, Message msg) {
        if (msg == null) {
            return;
        }
        mEndMsg = msg;
        mState = STATE_SINGLE_ICON;
        startLoadingIcon(recordNumber);
    }
    private void startLoadingIcon(int recordNumber) {
        mId = null;
        mIconData = null;
        mCurrentIcon = null;
        mRecordNumber = recordNumber;
        if (mIconsCache.containsKey(recordNumber)) {
            mCurrentIcon = mIconsCache.get(recordNumber);
            postIcon();
            return;
        }
        readId();
    }
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        try {
            switch (msg.what) {
            case EVENT_READ_EF_IMG_RECOED_DONE:
                ar = (AsyncResult) msg.obj;
                if (handleImageDescriptor((byte[]) ar.result)) {
                    readIconData();
                } else {
                    throw new Exception("Unable to parse image descriptor");
                }
                break;
            case EVENT_READ_ICON_DONE:
                ar = (AsyncResult) msg.obj;
                byte[] rawData = ((byte[]) ar.result);
                if (mId.codingScheme == ImageDescriptor.CODING_SCHEME_BASIC) {
                    mCurrentIcon = parseToBnW(rawData, rawData.length);
                    mIconsCache.put(mRecordNumber, mCurrentIcon);
                    postIcon();
                } else if (mId.codingScheme == ImageDescriptor.CODING_SCHEME_COLOUR) {
                    mIconData = rawData;
                    readClut();
                }
                break;
            case EVENT_READ_CLUT_DONE:
                ar = (AsyncResult) msg.obj;
                byte [] clut = ((byte[]) ar.result);
                mCurrentIcon = parseToRGB(mIconData, mIconData.length,
                        false, clut);
                mIconsCache.put(mRecordNumber, mCurrentIcon);
                postIcon();
                break;
            }
        } catch (Exception e) {
            StkLog.d(this, "Icon load failed");
            postIcon();
        }
    }
    private boolean handleImageDescriptor(byte[] rawData) {
        mId = ImageDescriptor.parse(rawData, 1);
        if (mId == null) {
            return false;
        }
        return true;
    }
    private void readClut() {
        int length = mIconData[3] * CLUT_ENTRY_SIZE;
        Message msg = this.obtainMessage(EVENT_READ_CLUT_DONE);
        mSimFH.loadEFImgTransparent(mId.imageId,
                mIconData[CLUT_LOCATION_OFFSET],
                mIconData[CLUT_LOCATION_OFFSET + 1], length, msg);
    }
    private void readId() {
        if (mRecordNumber < 0) {
            mCurrentIcon = null;
            postIcon();
            return;
        }
        Message msg = this.obtainMessage(EVENT_READ_EF_IMG_RECOED_DONE);
        mSimFH.loadEFImgLinearFixed(mRecordNumber, msg);
    }
    private void readIconData() {
        Message msg = this.obtainMessage(EVENT_READ_ICON_DONE);
        mSimFH.loadEFImgTransparent(mId.imageId, 0, 0, mId.length ,msg);
    }
    private void postIcon() {
        if (mState == STATE_SINGLE_ICON) {
            mEndMsg.obj = mCurrentIcon;
            mEndMsg.sendToTarget();
        } else if (mState == STATE_MULTI_ICONS) {
            mIcons[mCurrentRecordIndex++] = mCurrentIcon;
            if (mCurrentRecordIndex < mRecordNumbers.length) {
                startLoadingIcon(mRecordNumbers[mCurrentRecordIndex]);
            } else {
                mEndMsg.obj = mIcons;
                mEndMsg.sendToTarget();
            }
        }
    }
    public static Bitmap parseToBnW(byte[] data, int length){
        int valueIndex = 0;
        int width = data[valueIndex++] & 0xFF;
        int height = data[valueIndex++] & 0xFF;
        int numOfPixels = width*height;
        int[] pixels = new int[numOfPixels];
        int pixelIndex = 0;
        int bitIndex = 7;
        byte currentByte = 0x00;
        while (pixelIndex < numOfPixels) {
            if (pixelIndex % 8 == 0) {
                currentByte = data[valueIndex++];
                bitIndex = 7;
            }
            pixels[pixelIndex++] = bitToBnW((currentByte >> bitIndex-- ) & 0x01);
        }
        if (pixelIndex != numOfPixels) {
            StkLog.d("IconLoader", "parseToBnW; size error");
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }
    private static int bitToBnW(int bit){
        if(bit == 1){
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }
    public static Bitmap parseToRGB(byte[] data, int length,
            boolean transparency, byte[] clut) {
        int valueIndex = 0;
        int width = data[valueIndex++] & 0xFF;
        int height = data[valueIndex++] & 0xFF;
        int bitsPerImg = data[valueIndex++] & 0xFF;
        int numOfClutEntries = data[valueIndex++] & 0xFF;
        if (true == transparency) {
            clut[numOfClutEntries - 1] = Color.TRANSPARENT;
        }
        int numOfPixels = width * height;
        int[] pixels = new int[numOfPixels];
        valueIndex = 6;
        int pixelIndex = 0;
        int bitsStartOffset = 8 - bitsPerImg;
        int bitIndex = bitsStartOffset;
        byte currentByte = data[valueIndex++];
        int mask = getMask(bitsPerImg);
        boolean bitsOverlaps = (8 % bitsPerImg == 0);
        while (pixelIndex < numOfPixels) {
            if (bitIndex < 0) {
                currentByte = data[valueIndex++];
                bitIndex = bitsOverlaps ? (bitsStartOffset) : (bitIndex * -1);
            }
            int clutEntry = ((currentByte >> bitIndex) & mask);
            int clutIndex = clutEntry * CLUT_ENTRY_SIZE;
            pixels[pixelIndex++] = Color.rgb(clut[clutIndex],
                    clut[clutIndex + 1], clut[clutIndex + 2]);
            bitIndex -= bitsPerImg;
        }
        return Bitmap.createBitmap(pixels, width, height,
                Bitmap.Config.ARGB_8888);
    }
    private static int getMask(int numOfBits) {
        int mask = 0x00;
        switch (numOfBits) {
        case 1:
            mask = 0x01;
            break;
        case 2:
            mask = 0x03;
            break;
        case 3:
            mask = 0x07;
            break;
        case 4:
            mask = 0x0F;
            break;
        case 5:
            mask = 0x1F;
            break;
        case 6:
            mask = 0x3F;
            break;
        case 7:
            mask = 0x7F;
            break;
        case 8:
            mask = 0xFF;
            break;
        }
        return mask;
    }
}
