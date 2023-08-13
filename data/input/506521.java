public final class RawImage {
    public int version;
    public int bpp;
    public int size;
    public int width;
    public int height;
    public int red_offset;
    public int red_length;
    public int blue_offset;
    public int blue_length;
    public int green_offset;
    public int green_length;
    public int alpha_offset;
    public int alpha_length;
    public byte[] data;
    public boolean readHeader(int version, ByteBuffer buf) {
        this.version = version;
        if (version == 16) {
            this.bpp = 16;
            this.size = buf.getInt();
            this.width = buf.getInt();
            this.height = buf.getInt();
            this.red_offset = 11;
            this.red_length = 5;
            this.green_offset = 5;
            this.green_length = 6;
            this.blue_offset = 0;
            this.blue_length = 5;
            this.alpha_offset = 0;
            this.alpha_length = 0;
        } else if (version == 1) {
            this.bpp = buf.getInt();
            this.size = buf.getInt();
            this.width = buf.getInt();
            this.height = buf.getInt();
            this.red_offset = buf.getInt();
            this.red_length = buf.getInt();
            this.blue_offset = buf.getInt();
            this.blue_length = buf.getInt();
            this.green_offset = buf.getInt();
            this.green_length = buf.getInt();
            this.alpha_offset = buf.getInt();
            this.alpha_length = buf.getInt();
        } else {
            return false;
        }
        return true;
    }
    public int getRedMask() {
        return getMask(red_length, red_offset);
    }
    public int getGreenMask() {
        return getMask(green_length, green_offset);
    }
    public int getBlueMask() {
        return getMask(blue_length, blue_offset);
    }
    public static int getHeaderSize(int version) {
        switch (version) {
            case 16: 
                return 3; 
            case 1:
                return 12; 
        }
        return 0;
    }
    public RawImage getRotated() {
        RawImage rotated = new RawImage();
        rotated.version = this.version;
        rotated.bpp = this.bpp;
        rotated.size = this.size;
        rotated.red_offset = this.red_offset;
        rotated.red_length = this.red_length;
        rotated.blue_offset = this.blue_offset;
        rotated.blue_length = this.blue_length;
        rotated.green_offset = this.green_offset;
        rotated.green_length = this.green_length;
        rotated.alpha_offset = this.alpha_offset;
        rotated.alpha_length = this.alpha_length;
        rotated.width = this.height;
        rotated.height = this.width;
        int count = this.data.length;
        rotated.data = new byte[count];
        int byteCount = this.bpp >> 3; 
        final int w = this.width;
        final int h = this.height;
        for (int y = 0 ; y < h ; y++) {
            for (int x = 0 ; x < w ; x++) {
                System.arraycopy(
                        this.data, (y * w + x) * byteCount,
                        rotated.data, ((w-x-1) * h + y) * byteCount,
                        byteCount);
            }
        }
        return rotated;
    }
    public int getARGB(int index) {
        int value;
        if (bpp == 16) {
            value = data[index] & 0x00FF;
            value |= (data[index+1] << 8) & 0x0FF00;
        } else if (bpp == 32) {
            value = data[index] & 0x00FF;
            value |= (data[index+1] & 0x00FF) << 8;
            value |= (data[index+2] & 0x00FF) << 16;
            value |= (data[index+3] & 0x00FF) << 24;
        } else {
            throw new UnsupportedOperationException("RawImage.getARGB(int) only works in 16 and 32 bit mode.");
        }
        int r = ((value >>> red_offset) & getMask(red_length)) << (8 - red_length);
        int g = ((value >>> green_offset) & getMask(green_length)) << (8 - green_length);
        int b = ((value >>> blue_offset) & getMask(blue_length)) << (8 - blue_length);
        int a;
        if (alpha_length == 0) {
            a = 0xFF; 
        } else {
            a = ((value >>> alpha_offset) & getMask(alpha_length)) << (8 - alpha_length);
        }
        return a << 24 | r << 16 | g << 8 | b;
    }
    private int getMask(int length, int offset) {
        int res = getMask(length) << offset;
        if (bpp == 32) {
            return Integer.reverseBytes(res);
        }
        return res;
    }
    private int getMask(int length) {
        int res = 0;
        for (int i = 0 ; i < length ; i++) {
            res = (res << 1) + 1;
        }
        return res;
    }
}
