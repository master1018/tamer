public class InvalidApkPathException extends Exception {
	private String mPath;
    public InvalidApkPathException(String path) {
        super();
        mPath = path;
    }
    @Override
    public String getMessage() {
        return "Invalid APK path: " + mPath;
    }
}
