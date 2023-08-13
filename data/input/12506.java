public class XbmImageDecoder extends ImageDecoder {
    private static byte XbmColormap[] = {(byte) 255, (byte) 255, (byte) 255,
                                         0, 0, 0};
    private static int XbmHints = (ImageConsumer.TOPDOWNLEFTRIGHT |
                                   ImageConsumer.COMPLETESCANLINES |
                                   ImageConsumer.SINGLEPASS |
                                   ImageConsumer.SINGLEFRAME);
    public XbmImageDecoder(InputStreamImageSource src, InputStream is) {
        super(src, is);
        if (!(input instanceof BufferedInputStream)) {
            input = new BufferedInputStream(input, 80);
        }
    }
    private static void error(String s1) throws ImageFormatException {
        throw new ImageFormatException(s1);
    }
    public void produceImage() throws IOException, ImageFormatException {
        char nm[] = new char[80];
        int c;
        int i = 0;
        int state = 0;
        int H = 0;
        int W = 0;
        int x = 0;
        int y = 0;
        boolean start = true;
        byte raster[] = null;
        IndexColorModel model = null;
        while (!aborted && (c = input.read()) != -1) {
            if ('a' <= c && c <= 'z' ||
                    'A' <= c && c <= 'Z' ||
                    '0' <= c && c <= '9' || c == '#' || c == '_') {
                if (i < 78)
                    nm[i++] = (char) c;
            } else if (i > 0) {
                int nc = i;
                i = 0;
                if (start) {
                    if (nc != 7 ||
                        nm[0] != '#' ||
                        nm[1] != 'd' ||
                        nm[2] != 'e' ||
                        nm[3] != 'f' ||
                        nm[4] != 'i' ||
                        nm[5] != 'n' ||
                        nm[6] != 'e')
                    {
                        error("Not an XBM file");
                    }
                    start = false;
                }
                if (nm[nc - 1] == 'h')
                    state = 1;  
                else if (nm[nc - 1] == 't' && nc > 1 && nm[nc - 2] == 'h')
                    state = 2;  
                else if (nc > 2 && state < 0 && nm[0] == '0' && nm[1] == 'x') {
                    int n = 0;
                    for (int p = 2; p < nc; p++) {
                        c = nm[p];
                        if ('0' <= c && c <= '9')
                            c = c - '0';
                        else if ('A' <= c && c <= 'Z')
                            c = c - 'A' + 10;
                        else if ('a' <= c && c <= 'z')
                            c = c - 'a' + 10;
                        else
                            c = 0;
                        n = n * 16 + c;
                    }
                    for (int mask = 1; mask <= 0x80; mask <<= 1) {
                        if (x < W) {
                            if ((n & mask) != 0)
                                raster[x] = 1;
                            else
                                raster[x] = 0;
                        }
                        x++;
                    }
                    if (x >= W) {
                        if (setPixels(0, y, W, 1, model, raster, 0, W) <= 0) {
                            return;
                        }
                        x = 0;
                        if (y++ >= H) {
                            break;
                        }
                    }
                } else {
                    int n = 0;
                    for (int p = 0; p < nc; p++)
                        if ('0' <= (c = nm[p]) && c <= '9')
                            n = n * 10 + c - '0';
                        else {
                            n = -1;
                            break;
                        }
                    if (n > 0 && state > 0) {
                        if (state == 1)
                            W = n;
                        else
                            H = n;
                        if (W == 0 || H == 0)
                            state = 0;
                        else {
                            model = new IndexColorModel(8, 2, XbmColormap,
                                                        0, false, 0);
                            setDimensions(W, H);
                            setColorModel(model);
                            setHints(XbmHints);
                            headerComplete();
                            raster = new byte[W];
                            state = -1;
                        }
                    }
                }
            }
        }
        input.close();
        imageComplete(ImageConsumer.STATICIMAGEDONE, true);
    }
}
