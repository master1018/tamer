public class test {
    public String connectToServlet() {
        URL urlStory = null;
        BufferedReader brStory;
        String result = "";
        try {
            urlStory = new URL(getCodeBase(), "http:
        } catch (MalformedURLException MUE) {
            MUE.printStackTrace();
        }
        try {
            brStory = new BufferedReader(new InputStreamReader(urlStory.openStream()));
            while (brStory.ready()) {
                result += brStory.readLine();
            }
        } catch (IOException IOE) {
            IOE.printStackTrace();
        }
        return result;
    }
}
