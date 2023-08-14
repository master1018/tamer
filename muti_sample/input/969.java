public class InstTarg {
    static InstTarg first = new InstTarg();
    static InstTarg second = new InstTarg();
    static InstTarg third = new InstTarg();
    public static void main(String args[]) {
        start();
    }
    static void start() {
        first.go();
        second.go();
        third.go();
    }
    void go() {
        one();
        two();
        three();
    }
    void one() {
    }
    void two() {
    }
    void three() {
    }
}
