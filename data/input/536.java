public class FooterPanel extends Panel {
    public FooterPanel(String id) {
        super(id);
        add(new Label("version", Ontopia.getVersion()) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                tag.put("title", Ontopia.getBuild());
                super.onComponentTag(tag);
            }
        });
    }
}
