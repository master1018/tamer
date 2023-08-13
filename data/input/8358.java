public class AcceptExtraButton extends Frame {
    static int [] eventID = new int []{MouseEvent.MOUSE_PRESSED, MouseEvent.MOUSE_RELEASED, MouseEvent.MOUSE_CLICKED};
    public static void main(String []s){
        AcceptExtraButton f = new AcceptExtraButton();
        f.setSize(300, 300);
        f.setVisible(true);
        for (int buttonId = 0; buttonId<eventID.length; buttonId++) {
            for (int button = 0; button <= MouseInfo.getNumberOfButtons(); button++){
                System.out.println("button == "+button);
                MouseEvent me = new MouseEvent(f,
                                               eventID[buttonId],
                                               System.currentTimeMillis(),
                                               0, 
                                               100, 100, 
                                               150, 150, 
                                               1,        
                                               false,              
                                               button );
                System.out.println("dispatching >>>"+me);
                f.dispatchEvent( ( AWTEvent )me );
            }
        }
        MouseAdapter ma1 = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    System.out.println("PRESSED "+e);
                }
                public void mouseReleased(MouseEvent e) {
                    System.out.println("RELEASED "+e);
                }
                public void mouseClicked(MouseEvent e) {
                    System.out.println("CLICKED "+e);
                }
            };
        f.addMouseListener(ma1);
    }
}
