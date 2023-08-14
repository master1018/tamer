public class Bean4 {
    private String name;
    private int number;
    public Bean4() {
        this("Bean4", 1);
    }
    public Bean4(String name, int number) {
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
