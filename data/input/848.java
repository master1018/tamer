public class Locus implements RequestHandler {
    public void handleRequest(RequestHandlerState state) throws IOException, ServletException {
        handleRequest(state.getRequest(), state.getResponse(), state.getConn(), state.getParent(), state);
    }
    private void handleRequest(HttpServletRequest request, HttpServletResponse response, PubConnection conn, PubServlet parentServlet, RequestHandlerState state) throws IOException, ServletException {
        parentServlet.includeWithClear("/DisplayLocus?locus_id=" + request.getAttribute("key"), request, response);
    }
}
