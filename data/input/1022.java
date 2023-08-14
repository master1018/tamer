public class UngrabID {
    public static void main(String[] args){
        Frame f = new Frame("Dummy");
        sun.awt.UngrabEvent event = new sun.awt.UngrabEvent(f);
        if (event.getID() > AWTEvent.RESERVED_ID_MAX) {
                System.out.println( " Event ID : "+event.getID() + " " + event.toString());
                throw new RuntimeException(" Ungrab Event ID should be less than AWTEvent.RESERVED_ID_MAX ("+AWTEvent.RESERVED_ID_MAX+"). Actual value : "+event.getID() + " Event:" + event.toString());
        }
        System.out.println("Test passed. ");
   }
}
