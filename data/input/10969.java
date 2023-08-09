public class PreserveCombiner {
    public static void main(String[] args) throws Exception {
        Subject s = new Subject();
        s.getPrincipals().add(new X500Principal("cn=duke"));
        String result = (String)Subject.doAs(s, new PrivilegedAction() {
            public Object run() {
                Subject doAsSubject =
                        Subject.getSubject(AccessController.getContext());
                if (doAsSubject == null) {
                    return "test 1 failed";
                } else {
                    System.out.println(doAsSubject);
                    System.out.println("test 1 passed");
                }
                String result = AccessController.doPrivilegedWithCombiner
                    (new PrivilegedAction<String>() {
                    public String run() {
                        Subject doPrivSubject =
                            Subject.getSubject(AccessController.getContext());
                        if (doPrivSubject == null) {
                            return "test 2 failed";
                        } else {
                            System.out.println(doPrivSubject);
                            return "test 2 passed";
                        }
                    }
                });
                if ("test 2 failed".equals(result)) {
                    return result;
                } else {
                    System.out.println(result);
                }
                try {
                    result = AccessController.doPrivilegedWithCombiner
                        (new PrivilegedExceptionAction<String>() {
                        public String run() throws PrivilegedActionException {
                            Subject doPrivSubject = Subject.getSubject
                                (AccessController.getContext());
                            if (doPrivSubject == null) {
                                return "test 3 failed";
                            } else {
                                System.out.println(doPrivSubject);
                                return "test 3 passed";
                            }
                        }
                    });
                } catch (PrivilegedActionException pae) {
                    result = "test 3 failed";
                }
                if ("test 3 failed".equals(result)) {
                    return result;
                } else {
                    System.out.println(result);
                }
                return result;
            }
        });
        if (result.indexOf("passed") <= 0) {
            throw new SecurityException("overall test failed");
        }
    }
}
