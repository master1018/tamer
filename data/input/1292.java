public class CsvReportWriter extends ReportWriter {
    public static boolean createReport() {
        REPORT_FILE_EXTENSION = ".csv";
        isSuccessful_ = true;
        fileName_ = null;
        timeStamp_ = Calendar.getInstance();
        checkDirectory();
        checkFileName();
        compileReport();
        writeReport();
        return isSuccessful_;
    }
    protected static void compileReport() {
        ArrayList<CategorizedImage> indexedImages = case_.getIndexedImages();
        toWrite_ = new StringBuilder();
        toWrite_.append("\"Avg Skin %\",\"Canonical Path\",\"MD5\",\"SHA1\",");
        toWrite_.append("\"Last Modified\",\"RGB DetectorValue\",");
        toWrite_.append("\"YCbCr DetectorValue\"");
        toWrite_.append(currentSystem_.getLineSeparator());
        for (int i = 0; i < indexedImages.size(); i++) {
            toWrite_.append(indexedImages.get(i).getPreciseAveragePercentage());
            toWrite_.append(",\"" + indexedImages.get(i).getCanonicalPath());
            toWrite_.append("\",\"" + indexedImages.get(i).getMd5());
            toWrite_.append("\",\"" + indexedImages.get(i).getSha1());
            toWrite_.append("\"," + indexedImages.get(i).getLastModified());
            toWrite_.append(",");
            toWrite_.append(indexedImages.get(i).getPreciseRgbPercentage());
            toWrite_.append(",");
            toWrite_.append(indexedImages.get(i).getPreciseYCbCrPercentage());
            toWrite_.append(currentSystem_.getLineSeparator());
        }
    }
}
