public class EditCompanyProfile extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String module = request.getParameter("module");
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        try {
            Connection conn = (Connection) this.getServletContext().getAttribute("connection");
            if (conn.getConn() == null) response.sendError(1001);
            String title = request.getParameter("title");
            request.getRequestDispatcher("/WEB-INF/base/" + module + "/index.jsp").include(request, response);
        } catch (Exception e) {
            out.println("Error: " + e.toString());
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
