public class ViewCrawlAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws WgfaCrawlNotFoundException, WgfaDatabaseException {
        CrawlController cc = CrawlController.getSingleton();
        Crawl crawl = cc.getCrawl(request.getParameter("crawl"));
        request.getSession().setAttribute("c", crawl);
        return mapping.findForward("success");
    }
}
