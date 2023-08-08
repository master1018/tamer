public class IceListener implements ServletContextListener {
    public void contextDestroyed(ServletContextEvent sce) {
        Config.unload(sce.getServletContext());
    }
    public void contextInitialized(ServletContextEvent sce) {
        Config.load(sce.getServletContext());
    }
}
