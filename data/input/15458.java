    public static class Info {
        private final Class lineClass;
        public Info(Class<?> lineClass) {
            if (lineClass == null) {
                this.lineClass = Line.class;
            } else {
                this.lineClass = lineClass;
            }
        }
        public Class<?> getLineClass() {
            return lineClass;
        }
        public boolean matches(Info info) {
            if (! (this.getClass().isInstance(info)) ) {
                return false;
            }
            if (! (getLineClass().isAssignableFrom(info.getLineClass())) ) {
                return false;
            }
            return true;
        }
        public String toString() {
            String fullPackagePath = "javax.sound.sampled.";
            String initialString = new String(getLineClass().toString());
            String finalString;
            int index = initialString.indexOf(fullPackagePath);
            if (index != -1) {
                finalString = initialString.substring(0, index) + initialString.substring( (index + fullPackagePath.length()), initialString.length() );
            } else {
                finalString = initialString;
            }
            return finalString;
        }
    } 
} 
