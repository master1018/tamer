public class FlickrAuthCheck extends HttpServlet {
    private static final long serialVersionUID = 788293116188634227L;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Flickr f;
        try {
            f = FlickrService.createFlickr();
        } catch (ParserConfigurationException e) {
            throw new ServletException(e);
        }
        AuthInterface authInterface = f.getAuthInterface();
        resp.setContentType("text/plain");
        String frob = "unknown";
        try {
            frob = req.getParameter("frob");
            Auth auth = authInterface.getToken(frob);
            resp.getWriter().println("Authentication success");
            resp.getWriter().println("Token: " + auth.getToken());
            resp.getWriter().println("nsid: " + auth.getUser().getId());
            resp.getWriter().println("Realname: " + auth.getUser().getRealName());
            resp.getWriter().println("Username: " + auth.getUser().getUsername());
            resp.getWriter().println("Permission: " + auth.getPermission().getType());
            new JDOAuthStore().store(auth);
        } catch (FlickrException e) {
            resp.getWriter().println("Authentication failure!");
            resp.getWriter().println("Frob: " + frob);
            resp.getWriter().println(e.getLocalizedMessage());
        } catch (SAXException e) {
            throw new ServletException(e);
        }
    }
}
