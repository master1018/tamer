public class CFreemarkerServiceImpl implements IFreemarkerService {
    protected Configuration configuration;
    public Configuration getConfiguration() {
        return this.configuration;
    }
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
    public String format(String path, Object... args) {
        return format(path, CStringHelper.createMap(args));
    }
    public String format(String path, Map<String, Object> model) {
        try {
            Template template = this.configuration.getTemplate(path);
            return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (Exception e) {
            throw new CVardbException(e);
        }
    }
    @SuppressWarnings("unchecked")
    public void addEnum(Map<String, Object> model, String name, Enumeration enm) {
        try {
            BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
            TemplateHashModel enumModels = wrapper.getEnumModels();
            TemplateHashModel enumModel = (TemplateHashModel) enumModels.get(enm.getClass().getName());
            model.put(name, enumModel);
        } catch (Exception e) {
            throw new CVardbException(e);
        }
    }
}
