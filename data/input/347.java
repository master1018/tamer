public class GetHashAndSize {
    public static void main(String[] args) {
        String release = "0.10.2";
        String date = "25/04/2012";
        System.out.println("lastestRelease=" + release + " (" + date + ")");
        System.out.println("updateURL=http:
        System.out.println();
        SecureDownloader sd = new SecureDownloader(Proxy.NO_PROXY);
        try {
            String zipURL = "http:
            System.out.println("autoUpdateURL=" + zipURL);
            sd.download(new URL(zipURL), null);
            System.out.println("autoUpdateCHKSUM=" + sd.getCheckSum());
            System.out.println("autoUpdateSize=" + sd.getDownloadedSize());
            System.out.println();
            String updaterURL = "http:
            System.out.println("autoUpdateUpdaterURL=" + updaterURL);
            sd.download(new URL(updaterURL), null);
            System.out.println("autoUpdateUpdaterCHKSUM=" + sd.getCheckSum());
            System.out.println("autoUpdateUpdaterSize=" + sd.getDownloadedSize());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
