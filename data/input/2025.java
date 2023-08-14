public class test {
    public static void main(String[] args) throws IOException {
        System.out.println("start");
        URL url = new URL("https:
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            String[] mass = line.split("<entry>");
            for (String m : mass) {
                System.out.println(m);
            }
        }
    }
}
