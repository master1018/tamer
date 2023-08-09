public class PaletteFactory {
    private PaletteFactory() {
    }
    public static PaletteRoot createPaletteRoot(PaletteRoot currentPalette,
            AndroidTargetData targetData) {
        if (currentPalette == null) {
            currentPalette = new PaletteRoot();
        }
        for (int n = currentPalette.getChildren().size() - 1; n >= 0; n--) {
            currentPalette.getChildren().remove(n);
        }
        if (targetData != null) {
            addTools(currentPalette);
            addViews(currentPalette, "Layouts",
                    targetData.getLayoutDescriptors().getLayoutDescriptors());
            addViews(currentPalette, "Views",
                    targetData.getLayoutDescriptors().getViewDescriptors());
        }
        return currentPalette;
    }
    private static void addTools(PaletteRoot paletteRoot) {
        PaletteGroup group = new PaletteGroup("Tools");
        paletteRoot.add(group);
    }
    private static void addViews(PaletteRoot paletteRoot, String groupName,
            List<ElementDescriptor> descriptors) {
        PaletteDrawer group = new PaletteDrawer(groupName);
        for (ElementDescriptor desc : descriptors) {
            PaletteTemplateEntry entry = new PaletteTemplateEntry(
                    desc.getUiName(),           
                    desc.getTooltip(),          
                    desc,                       
                    desc.getImageDescriptor(),  
                    desc.getImageDescriptor()   
                    );
            group.add(entry);
        }
        paletteRoot.add(group);
    }
}
