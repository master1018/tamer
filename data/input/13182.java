class OptionKey {
    private int level;
    private int name;
    OptionKey(int level, int name) {
        this.level = level;
        this.name = name;
    }
    int level() {
        return level;
    }
    int name() {
        return name;
    }
}
