public class SubscriptionAuthorizationResponse {
    public static final String AUTHORISATION_GRANTED = "Authorisation Granted";
    public static final String AUTHORISATION_REFUSED = "Authorisation Refused";
    public static final String[] ACCEPTED_RESPONSES = new String[] { AUTHORISATION_GRANTED, AUTHORISATION_REFUSED };
    private String responseCode = AUTHORISATION_REFUSED;
    private SubscriptionAuthorizationResponse() {
    }
    public static SubscriptionAuthorizationResponse createResponse(String responseID) {
        SubscriptionAuthorizationResponse response = new SubscriptionAuthorizationResponse();
        response.responseCode = responseID;
        return response;
    }
    public String getResponseCode() {
        return responseCode;
    }
}
