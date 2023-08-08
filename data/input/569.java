public class PostServlet extends HttpServlet {
    private static final long serialVersionUID = 5632497271094661368L;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(444, "only post method is supported");
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        InputStream is = request.getInputStream();
        OutputStream os = response.getOutputStream();
        response.setBufferSize(5);
        int b = 0;
        do {
            b = is.read();
            if (b != -1) {
                os.write(b);
            }
            byte[] readBuffer = new byte[5];
            b = is.read(readBuffer);
            if (b != -1) {
                for (int i = 0; i < b; i++) {
                    os.write(readBuffer[i]);
                }
            }
        } while (b != -1);
        os.flush();
        os.close();
    }
}
