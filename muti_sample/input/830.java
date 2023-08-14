public class Test4120351 {
    public static void main(String[] args) {
        BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.BLACK, Color.WHITE);
        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, Color.WHITE, Color.BLACK);
    }
}
