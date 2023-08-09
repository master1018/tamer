public class T_packed_switch_1 {
    public int run(int i) {
        switch (i) {
        case -1:
            return 2;
        case 2:
        case 3:
            return 20;
        default:
            return -1;
        }
    }
}
