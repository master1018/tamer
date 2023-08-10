public class OsmTypWidgetManagerImpl extends BaseManager implements OsmTypWidgetManager {
    OsmTypWidgetDao osmTypWidgetDao;
    public void setOsmTypWidgetDao(OsmTypWidgetDao osmTypWidgetDao) {
        this.osmTypWidgetDao = osmTypWidgetDao;
    }
    public OsmTypWidget getOsmTypWidget(final int idnTypWidget) {
        return osmTypWidgetDao.getOsmTypWidget(idnTypWidget);
    }
    public OsmTypWidget getOsmTypWidgetByDesName(final String desName) {
        return osmTypWidgetDao.getOsmTypWidgetByDesName(desName);
    }
    public List getOsmTypWidgets() {
        return osmTypWidgetDao.getOsmTypWidgets();
    }
    public List getOsmTypWidgetsNotAdmin() {
        return osmTypWidgetDao.getOsmTypWidgetsNotAdmin();
    }
}
