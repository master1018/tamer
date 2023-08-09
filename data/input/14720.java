     static final EAttribute[] atts = EAttribute.class.getEnumConstants();
    public static EAttribute forAttribute(Attribute ta) {
        for (EAttribute ea: atts) {
            if (ea.att == ta) {
                return ea;
            }
        }
        return null;
    }
    public String toString() {
        return name().substring(1).toLowerCase();
    }
}
