public final class FactoryEnumeration {
    private List factories;
    private int posn = 0;
    private ClassLoader loader;
    FactoryEnumeration(List factories, ClassLoader loader) {
        this.factories = factories;
        this.loader = loader;
    }
    public Object next() throws NamingException {
        synchronized (factories) {
            NamedWeakReference ref = (NamedWeakReference) factories.get(posn++);
            Object answer = ref.get();
            if ((answer != null) && !(answer instanceof Class)) {
                return answer;
            }
            String className = ref.getName();
            try {
                if (answer == null) {   
                    answer = Class.forName(className, true, loader);
                }
                answer = ((Class) answer).newInstance();
                ref = new NamedWeakReference(answer, className);
                factories.set(posn-1, ref);  
                return answer;
            } catch (ClassNotFoundException e) {
                NamingException ne =
                    new NamingException("No longer able to load " + className);
                ne.setRootCause(e);
                throw ne;
            } catch (InstantiationException e) {
                NamingException ne =
                    new NamingException("Cannot instantiate " + answer);
                ne.setRootCause(e);
                throw ne;
            } catch (IllegalAccessException e) {
                NamingException ne = new NamingException("Cannot access " + answer);
                ne.setRootCause(e);
                throw ne;
            }
        }
    }
    public boolean hasMore() {
        synchronized (factories) {
            return posn < factories.size();
        }
    }
}
