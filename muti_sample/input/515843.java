public class T_sparse_switch_1 {
    public int run(int i) {
        switch (i) {
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
