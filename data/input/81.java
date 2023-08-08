public class IndexServlet extends HttpServlet {
    private static final long serialVersionUID = 10743348504190132L;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Website> websites = new WebsiteDAO(this.getServletContext()).loadAll();
        int size = websites.size();
        switch(size) {
            case 0:
                request.getRequestDispatcher("editWebsite.do").forward(request, response);
                break;
            default:
                request.getRequestDispatcher("websiteResults.do?id=" + websites.get(0).getId()).forward(request, response);
                break;
        }
    }
}
