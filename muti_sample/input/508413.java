public class MouseWheelEvent extends MouseEvent {
    private static final long serialVersionUID = -9187413581993563929L;
    public static final int WHEEL_UNIT_SCROLL = 0;
    public static final int WHEEL_BLOCK_SCROLL = 1;
    private int wheelRotation;
    private int scrollAmount;
    private int scrollType;
    public MouseWheelEvent(Component source, int id, long when, int modifiers,
            int x, int y, int clickCount, boolean popupTrigger, int scrollType,
            int scrollAmount, int wheelRotation) {
        super(source, id, when, modifiers, x, y, clickCount, popupTrigger);
        this.scrollType = scrollType;
        this.scrollAmount = scrollAmount;
        this.wheelRotation = wheelRotation;
    }
    public int getScrollAmount() {
        return scrollAmount;
    }
    public int getScrollType() {
        return scrollType;
    }
    public int getWheelRotation() {
        return wheelRotation;
    }
    public int getUnitsToScroll() {
        return (scrollAmount * wheelRotation);
    }
    @Override
    public String paramString() {
        String paramString = super.paramString();
        String typeString = null;
        switch (scrollType) {
        case WHEEL_UNIT_SCROLL:
            typeString = "WHEEL_UNIT_SCROLL"; 
            break;
        case WHEEL_BLOCK_SCROLL:
            typeString = "WHEEL_BLOCK_SCROLL"; 
            break;
        default:
            typeString = "unknown type"; 
        }
        paramString += ",scrollType=" + typeString + 
                ",scrollAmount=" + scrollAmount +  
                ",wheelRotation=" + wheelRotation; 
        return paramString;
    }
}
