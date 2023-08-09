        CLASS(".class"),
        HTML(".html"),
        OTHER("");
        public final String extension;
        private Kind(String extension) {
            extension.getClass(); 
            this.extension = extension;
        }
    };
    Kind getKind();
    boolean isNameCompatible(String simpleName, Kind kind);
    NestingKind getNestingKind();
    Modifier getAccessLevel();
}
