class Main {
    public static void main(String[] args) {
        A a = Main.class.getAnnotation(A.class);
        System.out.println(a);
    }
}
