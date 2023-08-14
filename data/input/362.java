public class test {
    public static WritableByteChannel getChannel(OutputStream outputStream) {
        return (outputStream != null) ? Channels.newChannel(outputStream) : null;
    }
}
