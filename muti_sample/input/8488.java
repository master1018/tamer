public class Bean2 {
    private String name;
    private int number;
    public Bean2() {
        this("Bean2", 1);
    }
    public Bean2(String name, int number) {
        this.name = name;
        this.number = number;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getNumber() {
        return this.number;
    }
    public void setNumber(int i) {
        this.number = i;
    }
}
