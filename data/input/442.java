public class ComponentFactory {
    private Hashtable<String, ComponentBuilder> componentBuilders;
    public ComponentFactory(Context context) {
        componentBuilders = new Hashtable();
        componentBuilders.put("button", new ButtonBuilder(context));
    }
    public Component getComponent(Element componentElement, String commandParam) {
        String componentType = componentElement.getTagName();
        ComponentBuilder componentBuilder = componentBuilders.get(componentType);
        if (componentBuilder == null) {
            System.out.println("No Such component builder with the component " + componentElement.getTagName());
        }
        return (Component) componentBuilder.build(componentElement, commandParam);
    }
}
