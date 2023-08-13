public class T_sparse_switch_2 {
    public int run(float i) {
        switch ((int)i) {
        case -1:
            return 2;
        case 10:
        case 15:
            return 20;
        default:
            return -1;
        }
    }
}
