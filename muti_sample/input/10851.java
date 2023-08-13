public class TrapezoidList {
    public static final int TRAP_START_INDEX = 5;
    public static final int TRAP_SIZE = 10;
    int[] trapArray;
    public TrapezoidList(int[] trapArray) {
        this.trapArray = trapArray;
    }
    public final int[] getTrapArray() {
        return trapArray;
    }
    public final int getSize() {
        return trapArray[0];
    }
    public final void setSize(int size) {
        trapArray[0] = 0;
    }
    public final int getLeft() {
        return trapArray[1];
    }
    public final int getTop() {
        return trapArray[2];
    }
    public final int getRight() {
        return trapArray[3];
    }
    public final int getBottom() {
        return trapArray[4];
    }
    private final int getTrapStartAddresse(int pos) {
        return TRAP_START_INDEX + TRAP_SIZE * pos;
    }
    public final int getTop(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 0];
    }
    public final int getBottom(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 1];
    }
    public final int getP1XLeft(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 2];
    }
    public final int getP1YLeft(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 3];
    }
    public final int getP2XLeft(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 4];
    }
    public final int getP2YLeft(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 5];
    }
    public final int getP1XRight(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 6];
    }
    public final int getP1YRight(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 7];
    }
    public final int getP2XRight(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 8];
    }
    public final int getP2YRight(int pos) {
        return trapArray[getTrapStartAddresse(pos) + 9];
    }
}
