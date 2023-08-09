public class DownloadFileInfo {
    String mFileName;
    FileOutputStream mStream;
    int mStatus;
    public DownloadFileInfo(String fileName, FileOutputStream stream, int status) {
        mFileName = fileName;
        mStream = stream;
        mStatus = status;
    }
}
