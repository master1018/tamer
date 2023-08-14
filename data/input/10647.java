package com.sun.tools.classfile;
public interface Dependency {
    public interface Filter {
        boolean accepts(Dependency dependency);
    }
    public interface Finder {
        public Iterable<? extends Dependency> findDependencies(ClassFile classfile);
    }
    public interface Location {
        String getClassName();
    }
    Location getOrigin();
    Location getTarget();
}
