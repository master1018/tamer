public class CSPage extends HttpServlet {
    public HDF hdf;
    public CS cs;
    public boolean page_debug = true; 
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
	hdf = new HDF();
	cs = new CS(hdf);
        Enumeration e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            String headerName = (String)e.nextElement();
            String headerValue = request.getHeader(headerName);
	    hdf.setValue("HTTP." + headerName,headerValue);
        }
	hdf.setValue("HTTP.PATH_INFO",request.getPathInfo());
	hdf.setValue("CGI.QueryString",request.getQueryString());
	hdf.setValue("CGI.RequestMethod",request.getMethod());
	e = request.getParameterNames();
	while (e.hasMoreElements()) {
	    String paramName = (String)e.nextElement();
	    String paramValue = request.getParameter(paramName);
	    hdf.setValue("Query." + paramName,paramValue);
	}
        Cookie[] cookies = request.getCookies();
        if (cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
		hdf.setValue("Cookie." + cookie.getName(),cookie.getValue());
            }
        }
	this.display();
        response.setContentType("text/html");
	out.print(cs.render());
	if (page_debug) {
	  out.print("<HR><PRE>");
	  out.print(hdf.dump());
	  out.print("</PRE>");
	}
    }
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response) throws IOException, ServletException {
        doGet(request, response);
    }
    public void display() {
	hdf.setValue("Foo.Bar","1");
	cs.parseStr("Hello Clearsilver<p><TABLE BORDER=1><TR><TD>Foo.Bar</TD><TD><?cs var:Foo.Bar ?></TD></TR></TABLE>");
    }
}
