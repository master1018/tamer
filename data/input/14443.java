public class DefaultFtpClientProvider extends sun.net.ftp.FtpClientProvider {
    @Override
    public sun.net.ftp.FtpClient createFtpClient() {
        return sun.net.ftp.impl.FtpClient.create();
    }
}
