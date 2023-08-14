public class test {
                    public void run() {
                        try {
                            URL url = new URL("http:
                            InputStream is = url.openConnection().getInputStream();
                            is.close();
                        } catch (Throwable t) {
                        }
                    }
}
