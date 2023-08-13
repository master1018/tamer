public class test {
    public List<String> makeQuery(String query) {
        List<String> result = new ArrayList<String>();
        try {
            query = URLUTF8Encoder.encode(query);
            URL url = new URL("http:
            URLConnection connection = url.openConnection();
            connection.addRequestProperty("Referer", "http:
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String response = builder.toString();
            JSONObject json = new JSONObject(response);
            Long count = Long.decode(json.getJSONObject("responseData").getJSONObject("cursor").getString("estimatedResultCount"));
            LOG.info("Found " + count + " potential pages");
            JSONArray ja = json.getJSONObject("responseData").getJSONArray("results");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject j = ja.getJSONObject(i);
                result.add(j.getString("url"));
            }
        } catch (Exception e) {
            LOG.error("Couldnt query Google for some reason check exception below");
            e.printStackTrace();
        }
        return result;
    }
}
