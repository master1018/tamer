    CLASS("class"), INTERFACE("interface"), ANNOTATION("@interface"), ENUM(
            "enum"), UNINITIALIZED("UNINITIALIZED");
    private final String name;
    Kind(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }
}
