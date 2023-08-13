public class TemplateStore {
    private static StringTemplateGroup group = initialize();
    private static StringTemplateGroup initialize() {
        InputStream modelStream = TemplateStore.class.getClassLoader()
                .getResourceAsStream("model/model.stg");
        InputStreamReader modelReader = new InputStreamReader(modelStream);
        StringTemplateGroup modelGroup = new StringTemplateGroup(modelReader,
                DefaultTemplateLexer.class);
        InputStream stream = TemplateStore.class.getClassLoader()
                .getResourceAsStream("delta/deltas.stg");
        InputStreamReader reader = new InputStreamReader(stream);
        StringTemplateGroup deltaGroup = new StringTemplateGroup(reader,
                DefaultTemplateLexer.class);
        StringTemplateGroup group = new StringTemplateGroup("signature", null,
                DefaultTemplateLexer.class);
        group.setSuperGroup(deltaGroup);
        deltaGroup.setSuperGroup(modelGroup);
        return group;
    }
    private TemplateStore() {
    }
    public static StringTemplate getStringTemplate(String name) {
        return group.getInstanceOf(name);
    }
}
