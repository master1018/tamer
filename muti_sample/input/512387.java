public class HostUtils {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",
            Locale.ENGLISH);
    public static boolean isFileExist(final String name) {
        return new File(name).exists();
    }
    public static BufferedImage convertRawImageToBufferedImage(RawImage rawImage) {
        assert rawImage.bpp == 16;
        BufferedImage im = new BufferedImage(rawImage.width,
                rawImage.height, BufferedImage.TYPE_USHORT_565_RGB);
        SampleModel sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_USHORT,
                rawImage.width,
                rawImage.height,
                new int[] { 0xf800, 0x07e0, 0x001f });
        short shortData[] = new short[rawImage.size / 2];
        for (int x = 0; x < shortData.length; x++) {
            int rawImageOffset = x * 2;
            int a = 0xff & rawImage.data[rawImageOffset];
            int b = 0xff & rawImage.data[rawImageOffset + 1];
            shortData[x] = (short)((b << 8) | a);
        }
        DataBuffer db = new DataBufferUShort(shortData, shortData.length);
        Raster raster = Raster.createRaster(sampleModel,
                db, null);
        im.setData(raster);
        return im;
    }
    public interface FileVisitor {
        void visitFile(File f);
    }
    public static void visitAllFilesUnder(File root, FilenameFilter filter, FileVisitor visitor) {
        File[] files = root.listFiles(filter);
        if (files != null) {
            for (File f : files) {
                visitor.visitFile(f);
                if (f.isDirectory()) {
                    visitAllFilesUnder(f, filter, visitor);
                }
            }
        }
    }
    public static void visitAllFilesUnder(String path, FilenameFilter filter, FileVisitor visitor) {
        visitAllFilesUnder(new File(path), filter, visitor);
    }
    private static class ZipFileVisitor implements FileVisitor {
        private final ZipOutputStream zipOutputStream;
        private boolean ok = true;
        private IOException caughtException;
        private final ZipFilenameTransformer transformer;
        public ZipFileVisitor(ZipOutputStream zipOutputStream,
                ZipFilenameTransformer transformer) {
            this.zipOutputStream = zipOutputStream;
            this.transformer = transformer;
        }
        public void visitFile(File f) {
            String path = f.getPath();
            if (transformer != null) {
                path = transformer.transform(path);
            }
            ZipEntry ze = new ZipEntry(path);
            try {
                zipOutputStream.putNextEntry(ze);
                InputStream is = new BufferedInputStream(new FileInputStream(f));
                byte[] buffer = new byte[4096];
                int bytesRead = is.read(buffer);
                while (bytesRead > 0) {
                    zipOutputStream.write(buffer, 0, bytesRead);
                    bytesRead = is.read(buffer);
                }
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                ok = false;
                caughtException = e;
            }
        }
        boolean isOk() {
            return ok;
        }
        IOException getCaughtException() {
            return caughtException;
        }
    }
    static class ZipFileException extends Exception {
        ZipFileException(IOException ioException) {
            super("Caught wrapped exception", ioException);
        }
    }
    public interface ZipFilenameTransformer {
        String transform(String filename);
    }
    public static void zipUpDirectory(String sourceDir,
            String outputFilePath,
            ZipFilenameTransformer transformer)
    throws IOException, ZipFileException {
        FileOutputStream fileOut = new FileOutputStream(outputFilePath);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
        final ZipOutputStream zipOutputStream = new ZipOutputStream(bufOut);
        ZipFileVisitor zfv = new ZipFileVisitor(zipOutputStream, transformer);
        visitAllFilesUnder(sourceDir, null, zfv);
        zipOutputStream.close();
        if (!zfv.isOk()) {
            throw new ZipFileException(zfv.getCaughtException());
        }
    }
    public static String getFormattedTimeString(long milliSec, String separator,
            String dateSeparator, String timeSeparator) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliSec);
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int date  = cal.get(Calendar.DATE);
        int hour  = cal.get(Calendar.HOUR_OF_DAY);
        int min   = cal.get(Calendar.MINUTE);
        int sec   = cal.get(Calendar.SECOND);
        Formatter fmt = new Formatter();
        if ((separator == null) || (separator.length() == 0)) {
            separator = "_";
        }
        if ((dateSeparator == null) || (dateSeparator.length() == 0)) {
            dateSeparator = ".";
        }
        if ((timeSeparator == null) || (timeSeparator.length() == 0)) {
            timeSeparator = ".";
        }
        final String formatStr = "%4d" + dateSeparator + "%02d" + dateSeparator + "%02d"
                         + separator + "%02d" + timeSeparator + "%02d" + timeSeparator + "%02d";
        fmt.format(formatStr, year, month, date, hour, min, sec);
        return fmt.toString();
    }
    public static String toHexString(byte[] arr) {
        StringBuffer buf = new StringBuffer(arr.length * 2);
        for (byte b : arr) {
            buf.append(String.format("%02x", b & 0xFF));
        }
        return buf.toString();
    }
    public static String replaceControlChars(String s) {
        return s.replaceAll("[\\x00-\\x1f&&[^\t\n\r]]", "?");
    }
    public static Date dateFromString(String s) throws ParseException {
        return dateFormat.parse(s);
    }
    public static String dateToString(Date d) {
        return dateFormat.format(d);
    }
}
