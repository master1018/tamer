public class LoginFilterTest {
    private static LoginFilter _filter;
    private static HttpServletRequestMock _httpServletRequestMock;
    private static HttpServletResponseMock _httpServletResponseMock;
    private static FilterChainMock _filterChainMock;
    @Before
    public void setUp() throws IOException {
        _httpServletRequestMock = new HttpServletRequestMock();
        _httpServletResponseMock = new HttpServletResponseMock();
        _filterChainMock = new FilterChainMock();
        _filter = new LoginFilter();
        ApplicationProperties.init();
    }
    @After
    public void tearDown() {
        _filter = null;
        _filterChainMock = null;
        _httpServletResponseMock = null;
        _httpServletRequestMock = null;
    }
    @Test
    public void testDoFilterValidateUser() throws Exception {
        _httpServletRequestMock.getSession().setAttribute(ParamKey.USUARIO.toString(), new UserDto("user", "passwrod"));
        _filter.doFilter(_httpServletRequestMock, _httpServletResponseMock, _filterChainMock);
        assertTrue(_filterChainMock._calledDoFilter);
    }
    @Test
    public void testDoFilterLogin() throws Exception {
        _httpServletRequestMock.setParameter(ParamKey.ACTION.toString(), Action.LOGIN.toString());
        _filter.doFilter(_httpServletRequestMock, _httpServletResponseMock, _filterChainMock);
        assertTrue(_filterChainMock._calledDoFilter);
    }
    @Test
    public void testDoFilterInValidateUser() throws Exception {
        _filter.doFilter(_httpServletRequestMock, _httpServletResponseMock, _filterChainMock);
        assertFalse(_filterChainMock._calledDoFilter);
        assertTrue(_httpServletRequestMock.getDispatcherMock()._calledForward);
    }
    private class FilterChainMock implements FilterChain {
        public boolean _calledDoFilter;
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            _calledDoFilter = true;
        }
    }
}
