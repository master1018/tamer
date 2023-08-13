    TypeMirror getSuperclass();
    List<? extends TypeMirror> getInterfaces();
    List<? extends TypeParameterElement> getTypeParameters();
    @Override
    Element getEnclosingElement();
}
