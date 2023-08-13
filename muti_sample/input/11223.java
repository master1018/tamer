public class CheckMethods {
    static boolean debug = false;
    static class MethodSignature {
        String name;
        Class[] paramTypes;
        MethodSignature(String name, Class[] paramTypes) {
            this.name = name;
            this.paramTypes = paramTypes;
        }
        public boolean equals(Object obj) {
            if (debug) {
                System.out.println("comparing " + this + " against: " + obj);
            }
            if (!(obj instanceof MethodSignature)) {
                if (debug)
                    System.out.println(false);
                return false;
            }
            MethodSignature ms = (MethodSignature) obj;
            Class[] types = ms.paramTypes;
            try {
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].equals(paramTypes[i])) {
                        if (debug)
                            System.out.println(false);
                        return false;
                    }
                }
            } catch (Exception e) {
                if (debug)
                    System.out.println(false);
                return false;
            }
            boolean result = this.name.equals(ms.name);
            if (debug)
                System.out.println(result);
            return result;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer(name + "(");
            for (int i = 0; i < paramTypes.length; i++) {
                sb.append(paramTypes[i].getName() + ",");
                if (i == (paramTypes.length - 1))
                    sb.deleteCharAt(sb.length() - 1);
            }
            sb.append(")");
            return sb.toString();
        }
    }
    public static void main(String[] args) throws Exception {
        ArrayList allMethods = new ArrayList(
                Arrays.asList(Socket.class.getDeclaredMethods()));
        ArrayList allMethodSignatures = new ArrayList();
        for (Iterator itr = allMethods.iterator(); itr.hasNext();) {
            Method m = (Method) itr.next();
            if (!Modifier.isStatic(m.getModifiers()) &&
                    (Modifier.isPublic(m.getModifiers()) ||
                    Modifier.isProtected(m.getModifiers()))) {
                allMethodSignatures.add( new MethodSignature(m.getName(),
                        m.getParameterTypes()));
            }
        }
        Class sslSI = Class.forName(
            "sun.security.ssl.SSLSocketImpl");
        Class baseSSLSI = Class.forName(
            "sun.security.ssl.BaseSSLSocketImpl");
        ArrayList sslSocketMethods =
                new ArrayList(Arrays.asList(sslSI.getDeclaredMethods()));
        sslSocketMethods.addAll( new ArrayList(
                Arrays.asList(baseSSLSI.getDeclaredMethods())));
        ArrayList sslSocketMethodSignatures = new ArrayList();
        for (Iterator itr = sslSocketMethods.iterator(); itr.hasNext();) {
            Method m = (Method) itr.next();
            if (!Modifier.isStatic(m.getModifiers())) {
                sslSocketMethodSignatures.add(
                        new MethodSignature(m.getName(),
                        m.getParameterTypes()));
            }
        }
        if (!sslSocketMethodSignatures.containsAll(allMethodSignatures)) {
            throw new RuntimeException(
                "Method definition test failed on SSLSocketImpl");
        }
        ArrayList allFields =
                new ArrayList(Arrays.asList(Socket.class.getFields()));
        for (Iterator itr = allFields.iterator(); itr.hasNext();) {
            Field f = (Field) itr.next();
            if (!Modifier.isStatic(f.getModifiers())) {
                throw new RuntimeException("Non static Public fields" +
                        " declared in superclasses");
            }
        }
    }
}
