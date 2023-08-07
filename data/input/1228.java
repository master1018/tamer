public class StaticPageService {
    private static final Log log = LogFactory.getLog(StaticPageService.class);
    public List<StaticPage> getStaticPages(Blog blog) throws StaticPageServiceException {
        List<StaticPage> staticPages = new ArrayList<StaticPage>();
        try {
            DAOFactory factory = DAOFactory.getConfiguredFactory();
            StaticPageDAO dao = factory.getStaticPageDAO();
            staticPages.addAll(dao.loadStaticPages(blog));
        } catch (PersistenceException pe) {
            throw new StaticPageServiceException(blog, pe);
        }
        Collections.sort(staticPages, new StaticPageByNameComparator());
        return staticPages;
    }
    public StaticPage getStaticPageById(Blog blog, String pageId) throws StaticPageServiceException {
        StaticPage staticPage;
        ContentCache cache = ContentCache.getInstance();
        try {
            staticPage = cache.getStaticPage(blog, pageId);
            if (staticPage != null) {
                log.debug("Got static page " + pageId + " from cache");
            } else {
                log.debug("Loading static page " + pageId + " from disk");
                DAOFactory factory = DAOFactory.getConfiguredFactory();
                StaticPageDAO dao = factory.getStaticPageDAO();
                staticPage = dao.loadStaticPage(blog, pageId);
                if (staticPage != null) {
                    staticPage.setPersistent(true);
                    cache.putStaticPage(staticPage);
                }
            }
        } catch (PersistenceException pe) {
            throw new StaticPageServiceException(blog, pe);
        }
        if (staticPage != null) {
            staticPage = (StaticPage) staticPage.clone();
        }
        return staticPage;
    }
    public StaticPage getStaticPageByName(Blog blog, String name) throws StaticPageServiceException {
        String id = blog.getStaticPageIndex().getStaticPage(name);
        return getStaticPageById(blog, id);
    }
    public void putStaticPage(StaticPage staticPage) throws StaticPageServiceException {
        ContentCache cache = ContentCache.getInstance();
        DAOFactory factory = DAOFactory.getConfiguredFactory();
        StaticPageDAO dao = factory.getStaticPageDAO();
        Blog blog = staticPage.getBlog();
        synchronized (blog) {
            try {
                StaticPage sp = getStaticPageById(blog, staticPage.getId());
                if (!staticPage.isPersistent() && sp != null) {
                    staticPage.setDate(new Date(staticPage.getDate().getTime() + 1));
                    putStaticPage(staticPage);
                } else {
                    dao.storeStaticPage(staticPage);
                    staticPage.setPersistent(true);
                    cache.removeStaticPage(staticPage);
                }
                staticPage.getBlog().getSearchIndex().index(staticPage);
                staticPage.getBlog().getStaticPageIndex().index(staticPage);
            } catch (PersistenceException pe) {
                throw new StaticPageServiceException(blog, pe);
            }
        }
    }
    public void removeStaticPage(StaticPage staticPage) throws StaticPageServiceException {
        ContentCache cache = ContentCache.getInstance();
        DAOFactory factory = DAOFactory.getConfiguredFactory();
        StaticPageDAO dao = factory.getStaticPageDAO();
        Blog blog = staticPage.getBlog();
        try {
            dao.removeStaticPage(staticPage);
            cache.removeStaticPage(staticPage);
            staticPage.getBlog().getSearchIndex().unindex(staticPage);
            staticPage.getBlog().getStaticPageIndex().unindex(staticPage);
        } catch (PersistenceException pe) {
            cache.removeStaticPage(staticPage);
            throw new StaticPageServiceException(staticPage.getBlog(), pe);
        }
    }
    public boolean lock(StaticPage staticPage) {
        if (staticPage.isPersistent()) {
            boolean success = DAOFactory.getConfiguredFactory().getStaticPageDAO().lock(staticPage);
            ContentCache.getInstance().removeStaticPage(staticPage);
            return success;
        } else {
            return true;
        }
    }
    public boolean unlock(StaticPage staticPage) {
        if (staticPage.isPersistent()) {
            boolean success = DAOFactory.getConfiguredFactory().getStaticPageDAO().unlock(staticPage);
            ContentCache.getInstance().removeStaticPage(staticPage);
            return success;
        } else {
            return true;
        }
    }
}
