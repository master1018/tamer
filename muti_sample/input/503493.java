public class HelloCSServlet extends HttpServlet {
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
	HDF hdf = new HDF();
	hdf.setValue("Foo.Bar","1");
	CS cs = new CS(hdf);
	cs.parseStr("Hello Clearsilver<p>Foo.Bar: <?cs var:Foo.Bar ?>");
        response.setContentType("text/html");
	out.print(cs.render());
    }
}
