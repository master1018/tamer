public class InitialOrientationProperty extends SelectionListProperty {
    public InitialOrientationProperty(Properties xmlvm) {
        super("orientations.initial", xmlvm, new String[] { "UIInterfaceOrientationPortrait", "UIInterfaceOrientationPortraitUpsideDown", "UIInterfaceOrientationLandscapeLeft", "UIInterfaceOrientationLandscapeRight" }, 0);
    }
    @Override
    public String getTitle() {
        return "Initial orientation";
    }
    @Override
    public String getHelp() {
        return "Set the initial orientation for this applciation.\nValid options are:\nUIInterfaceOrientationPortrait\nUIInterfaceOrientationPortraitUpsideDown\nUIInterfaceOrientationLandscapeLeft\nUIInterfaceOrientationLandscapeRight";
    }
}
