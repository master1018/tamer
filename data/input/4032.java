class ObjectElementHandler extends NewElementHandler {
    private String idref;
    private String field;
    private Integer index;
    private String property;
    private String method;
    @Override
    public final void addAttribute(String name, String value) {
        if (name.equals("idref")) { 
            this.idref = value;
        } else if (name.equals("field")) { 
            this.field = value;
        } else if (name.equals("index")) { 
            this.index = Integer.valueOf(value);
            addArgument(this.index); 
        } else if (name.equals("property")) { 
            this.property = value;
        } else if (name.equals("method")) { 
            this.method = value;
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    public final void startElement() {
        if ((this.field != null) || (this.idref != null)) {
            getValueObject();
        }
    }
    @Override
    protected boolean isArgument() {
        return true; 
    }
    @Override
    protected final ValueObject getValueObject(Class<?> type, Object[] args) throws Exception {
        if (this.field != null) {
            return ValueObjectImpl.create(FieldElementHandler.getFieldValue(getContextBean(), this.field));
        }
        if (this.idref != null) {
            return ValueObjectImpl.create(getVariable(this.idref));
        }
        Object bean = getContextBean();
        String name;
        if (this.index != null) {
            name = (args.length == 2)
                    ? PropertyElementHandler.SETTER
                    : PropertyElementHandler.GETTER;
        } else if (this.property != null) {
            name = (args.length == 1)
                    ? PropertyElementHandler.SETTER
                    : PropertyElementHandler.GETTER;
            if (0 < this.property.length()) {
                name += this.property.substring(0, 1).toUpperCase(ENGLISH) + this.property.substring(1);
            }
        } else {
            name = (this.method != null) && (0 < this.method.length())
                    ? this.method
                    : "new"; 
        }
        Expression expression = new Expression(bean, name, args);
        return ValueObjectImpl.create(expression.getValue());
    }
}
