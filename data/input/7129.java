public class bug6690791 {
    public static void main(String[] args) throws Exception {
        MouseEvent me = new MouseEvent(new JLabel(), MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), MouseEvent.ALT_MASK,
                10, 10, 100, 100, 1, false, MouseEvent.BUTTON1);
        me.setSource(new Object());
        MenuSelectionManager.defaultManager().processMouseEvent(me);
    }
}
