public class EncodingFilter implements Filter {
    private String targetEncoding;
    public void init(FilterConfig filterConfig) throws ServletException {
        this.targetEncoding = filterConfig.getInitParameter("encoding");
    }
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        req.setCharacterEncoding(this.targetEncoding);
        chain.doFilter(request, response);
    }
    public void destroy() {
    }
}
