public class ReportServiceHandler extends ServiceEventHandler {
    @Override
    public String invoke(String eventPath, String eventMethod, HttpServletRequest request, HttpServletResponse response) throws EventHandlerException {
        try {
            request.setAttribute("outputStream", response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.invoke(eventPath, eventMethod, request, response);
    }
}
