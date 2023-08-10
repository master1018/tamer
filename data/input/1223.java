public class RestletUtils {
    public static String getAsString(Client client, Reference reference) throws IOException {
        String result = "";
        Request r = new Request(Method.GET, reference);
        Preference<MediaType> mt = new Preference<MediaType>();
        mt.setMetadata(MediaType.APPLICATION_RDF_XML);
        List<Preference<MediaType>> lmt = new LinkedList<Preference<MediaType>>();
        lmt.add(mt);
        r.getClientInfo().setAcceptedMediaTypes(lmt);
        Response response = client.handle(r);
        if (response.getStatus().isSuccess()) {
            if (response.isEntityAvailable()) {
                result = response.getEntity().getText();
            }
        }
        return result;
    }
    public static InputStream getAsInputStream(Client client, Reference reference) {
        String result = "";
        try {
            result = getAsString(client, reference);
        } catch (IOException e) {
            System.out.println("getAsInputStream: Could not get Response as String");
            System.out.println(e.getStackTrace());
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(result.getBytes());
        return bais;
    }
}
