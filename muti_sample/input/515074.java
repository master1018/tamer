public class T_packed_switch_2 {
    public int run(float i) {
        switch ((int)i) {
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
