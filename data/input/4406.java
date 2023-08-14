class AbsoluteComponentCenterCalculator {
    private AbsoluteComponentCenterCalculator () {}
    public static int calculateXCenterCoordinate(Component component) {
        return (int)component.getLocationOnScreen().getX()+(component.getWidth()/2);
    }
    public static int calculateYCenterCoordinate(Component component) {
        return (int)component.getLocationOnScreen().getY()+(component.getHeight()/2);
    }
}
