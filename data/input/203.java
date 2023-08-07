public class TestSmsController extends BaseHotelTestCase {
    protected ISmsController smsReceiver;
    protected IUserContainer userContainer;
    public MockHttpServletRequest req = new MockHttpServletRequest();
    public MockHttpServletResponse resp = new MockHttpServletResponse();
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        User user = new User();
        Role role = new Role();
        role.setDescription("test");
        role.setName("test");
        user.setRole(role);
        user.setStructureId(1);
        userContainer.setUser(user);
    }
    public void testReceiveMessage() throws Exception {
        req.setMethod("POST");
        req.setRequestURI("http:
        String receipt = "2#12345:source:079456345:200:xxx:1234567809:userfred:";
        String message = "11796 book owner2 password 238 12.09.2008 3 testname surname";
        req.addParameter("TextMessage", (message));
        ModelAndView mav = smsReceiver.create(req, resp);
        assertEquals("sms.ok", mav.getViewName());
        assertEquals(bookingRawManager.getAll().size(), 7);
    }
    public void testReceiveMessageWithHttpPost() throws ClientProtocolException, IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http:
        String receipt = "2#12345:source:079456345:200:xxx:1234567809:userfred:";
        String message = "11796 book owner2 password 238 12.09.2008 3 testname surname";
        HttpParams params = new BasicHttpParams();
        params.setParameter("TextMessage", message);
        httpPost.setParams(params);
        HttpResponse response = httpclient.execute(httpPost);
        HttpEntity entity = response.getEntity();
    }
}
