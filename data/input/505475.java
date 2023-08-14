    class Info {
        private Class <?> lineClass;
        public Info(Class <?> lineClass) {
            this.lineClass = lineClass;
        }
        public Class <?> getLineClass( ){
            return lineClass;
        }
        public boolean matches(Line.Info info) {
            return lineClass.isAssignableFrom(info.getLineClass());
        }
        @Override
        public String toString() {
            return lineClass.toString();
        }
    }
    void addLineListener(LineListener listener);
    void close();
    Control getControl(Control.Type control);
    Control[] getControls();
    Line.Info getLineInfo();
    boolean isControlSupported(Control.Type control);
    boolean isOpen();
    void open() throws LineUnavailableException;
    void removeLineListener(LineListener listener);
}
