@ContextConfiguration(locations = { "classpath:/applicationContext-resources.xml", "classpath:/applicationContext-dao.xml", "classpath:/applicationContext-service.xml", "classpath*:applicationContext.xml" })
public abstract class BaseManagerTestCase extends AbstractTransactionalJUnit4SpringContextTests {
    protected final Log log = LogFactory.getLog(getClass());
    protected ResourceBundle rb;
    public BaseManagerTestCase() {
        String className = this.getClass().getName();
        try {
            rb = ResourceBundle.getBundle(className);
        } catch (MissingResourceException mre) {
        }
    }
    protected Object populate(Object obj) throws Exception {
        Map map = ConvertUtil.convertBundleToMap(rb);
        BeanUtils.copyProperties(obj, map);
        return obj;
    }
}
