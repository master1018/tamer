public class ImageOutputStreamWrapper extends OutputStream {
	protected ImageOutputStream mIos;
	private byte[] mBuff;
	public ImageOutputStreamWrapper(ImageOutputStream ios) {
		if (null == ios) {
			throw new IllegalArgumentException("ImageOutputStream must not be null");
		}
		this.mIos = ios;
		this.mBuff = new byte[1];
	}
	public ImageOutputStream getImageOutputStream() {
		return mIos;
	}
	@Override
	public void write(int oneByte) throws IOException {
		mBuff[0] = (byte)oneByte;
		mIos.write(mBuff, 0, 1);
	}
	public void write(byte[] b) throws IOException {
		mIos.write(b, 0, b.length);
	}
	public void write(byte[] b, int off, int len) throws IOException {
		mIos.write(b, off, len);
	}
	public void flush() throws IOException {
		mIos.flush();
	}
    public void close() throws IOException {
    	if (mIos == null) {
    		throw new IOException("Stream already closed");
    	}
        mIos = null;
    }
}
