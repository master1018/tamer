public class DescriptorTest {
    private static String failureMessage;
    private static final String[] testFieldNames = {
        "a", "C", "aa", "int", "nul",
    };
    private static final Object[] testFieldValues = {
        "b", "D", "bb", 5, null,
    };
    private static final String[] testFieldStrings = {
        "a=b", "C=D", "aa=bb", "int=(5)", "nul=",
    };
    public static void main(String[] args) throws Exception {
        genericTests(ImmutableDescriptor.class);
        genericTests(javax.management.modelmbean.DescriptorSupport.class);
        if (failureMessage != null)
            throw new Exception("TEST FAILED: " + failureMessage);
        else
            System.out.println("Test passed");
    }
    private static void genericTests(Class<? extends Descriptor> descrClass) {
        System.out.println("--- generic tests for " + descrClass.getName() +
                           " ---");
        for (Case<Class<? extends Descriptor>, ?, ?> test :
                 genericDescriptorTests)
            test.run(descrClass);
    }
    private static abstract class Case<P, T, C> {
        Case(String name) {
            this.name = name;
        }
        void run(P p) {
            System.out.println("test: " + name);
            try {
                T t = prepare(p);
                C c = test(t);
                String failed = check(c);
                if (failed != null) {
                    System.out.println("FAILED: " + name + ": " + failed);
                    failureMessage = failed;
                }
            } catch (Exception e) {
                System.out.println("FAILED: " + name + ": exception:");
                e.printStackTrace(System.out);
                failureMessage = e.toString();
            }
        }
        abstract T prepare(P p) throws Exception;
        abstract C test(T t) throws Exception;
        abstract String check(C c) throws Exception;
        private final String name;
    }
    private static abstract class ProtoCase<C>
            extends Case<Class<? extends Descriptor>, Descriptor, C> {
        ProtoCase(String name) {
            super(name);
        }
        Descriptor prepare(Class<? extends Descriptor> descrClass)
                throws Exception {
            Constructor<? extends Descriptor> con =
                descrClass.getConstructor(String[].class, Object[].class);
            return con.newInstance(testFieldNames, testFieldValues);
        }
    }
    private static abstract class ValueProtoCase<C> extends ProtoCase<C> {
        ValueProtoCase(String name, C testValue) {
            super(name);
            this.testValue = testValue;
        }
        String check(C c) {
            final boolean array = (testValue instanceof Object[]);
            final boolean equal =
                array ?
                    Arrays.deepEquals((Object[]) testValue, (Object[]) c) :
                    testValue.equals(c);
            if (equal)
                return null;
            return "wrong value: " + string(c) + " should be " +
                string(testValue);
        }
        private final C testValue;
    }
    private static abstract class UnchangedCase extends ProtoCase<Descriptor> {
        UnchangedCase(String name) {
            super(name);
        }
        Descriptor test(Descriptor d) {
            dontChange(d);
            return d;
        }
        String check(Descriptor d) {
            String[] dnames = d.getFieldNames();
            if (!strings(dnames).equals(strings(testFieldNames)))
                return "descriptor names changed: " + strings(dnames);
            Object[] values = d.getFieldValues(testFieldNames);
            if (values.length != testFieldValues.length)
                return "getFieldValues: bogus length: " + values.length;
            for (int i = 0; i < values.length; i++) {
                Object expected = testFieldValues[i];
                Object found = values[i];
                if ((expected == null) ?
                        found != null :
                        !expected.equals(found))
                    return "descriptor value changed: " + testFieldNames[i] +
                        " was " + expected + " now " + found;
            }
            return null;
        }
        abstract void dontChange(Descriptor d);
    }
    private static abstract class ChangedCase extends ProtoCase<Object> {
        ChangedCase(String name, Object... fieldsAndValues) {
            super(name);
            if (fieldsAndValues.length % 2 != 0)
                throw new AssertionError("test wrong: odd fieldsAndValues");
            this.fieldsAndValues = fieldsAndValues;
            this.immutableTest = new UnsupportedExceptionCase(name) {
                void provoke(Descriptor d) {
                    ChangedCase.this.change(d);
                }
            };
        }
        Object test(Descriptor d) {
            if (immutable(d))
                return immutableTest.test(d);
            else {
                change(d);
                return d;
            }
        }
        String check(Object c) {
            if (c instanceof Exception)
                return immutableTest.check((Exception) c);
            else if (!(c instanceof Descriptor)) {
                return "test returned strange value: " +
                        c.getClass() + ": " + c;
            } else {
                Descriptor d = (Descriptor) c;
                String[] names = new String[fieldsAndValues.length / 2];
                Object[] expected = new Object[names.length];
                for (int i = 0; i < fieldsAndValues.length; i += 2) {
                    names[i / 2] = (String) fieldsAndValues[i];
                    expected[i / 2] = fieldsAndValues[i + 1];
                }
                String[] foundNames = d.getFieldNames();
                if (!strings(foundNames).equals(strings(names))) {
                    return "wrong field names after change: found " +
                        strings(foundNames) + ", expected " + strings(names);
                }
                Object[] found = d.getFieldValues(names);
                if (!Arrays.deepEquals(expected, found)) {
                    return "wrong value after change: for fields " +
                        Arrays.asList(names) + " values are " +
                        Arrays.asList(found) + ", should be " +
                        Arrays.asList(expected);
                }
                return null;
            }
        }
        abstract void change(Descriptor d);
        private final Object[] fieldsAndValues;
        private final ExceptionCase immutableTest;
    }
    private static abstract class ExceptionCase extends ProtoCase<Exception> {
        ExceptionCase(String name) {
            super(name);
        }
        Exception test(Descriptor d) {
            try {
                provoke(d);
                return null;
            } catch (Exception e) {
                return e;
            }
        }
        String check(Exception e) {
            if (e == null)
                return "did not throw exception: " + expected();
            if (!(e instanceof RuntimeOperationsException)) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();
                return "wrong exception: " + expected() + ": found: " + sw;
            }
            Throwable cause = e.getCause();
            if (!exceptionClass().isInstance(cause))
                return "wrong wrapped exception: " + cause + ": " + expected();
            return null;
        }
        String expected() {
            return "expected " + RuntimeOperationsException.class.getName() +
                " wrapping " + exceptionClass().getName();
        }
        abstract Class<? extends Exception> exceptionClass();
        abstract void provoke(Descriptor d);
    }
    private static abstract class IllegalExceptionCase extends ExceptionCase {
        IllegalExceptionCase(String name) {
            super(name);
        }
        Class<IllegalArgumentException> exceptionClass() {
            return IllegalArgumentException.class;
        }
    }
    private static abstract class UnsupportedExceptionCase
            extends ExceptionCase {
        UnsupportedExceptionCase(String name) {
            super(name);
        }
        Class<UnsupportedOperationException> exceptionClass() {
            return UnsupportedOperationException.class;
        }
    }
    private static final Case<Class<? extends Descriptor>, ?, ?>
            genericDescriptorTests[] = constantArray(
        new Case<Class<? extends Descriptor>, Descriptor, Object[]>(
                "getFieldValues on empty Descriptor") {
            Descriptor prepare(Class<? extends Descriptor> c)
                    throws Exception {
                Constructor<? extends Descriptor> con =
                        c.getConstructor(String[].class);
                return con.newInstance(new Object[] {new String[0]});
            }
            Object[] test(Descriptor d) {
                return d.getFieldValues("foo", "bar");
            }
            String check(Object[] v) {
                if (v.length == 2 && v[0] == null && v[1] == null)
                    return null;
                return "value should be array with null elements: " +
                        Arrays.deepToString(v);
            }
        },
        new ValueProtoCase<Set<String>>("getFieldNames",
                                        strings(testFieldNames)) {
            Set<String> test(Descriptor d) {
                return set(d.getFieldNames());
            }
        },
        new ValueProtoCase<Set<String>>("getFields",
                                        strings(testFieldStrings)) {
            Set<String> test(Descriptor d) {
                return set(d.getFields());
            }
        },
        new ValueProtoCase<Object>("getFieldValue with exact case", "b") {
            Object test(Descriptor d) {
                return d.getFieldValue("a");
            }
        },
        new ValueProtoCase<Object>("getFieldValue with lower case for upper",
                                   "D") {
            Object test(Descriptor d) {
                return d.getFieldValue("c");
            }
        },
        new ValueProtoCase<Object>("getFieldValue with upper case for lower",
                                   "bb") {
            Object test(Descriptor d) {
                return d.getFieldValue("AA");
            }
        },
        new ValueProtoCase<Object>("getFieldValue with mixed case for lower",
                                   "bb") {
            Object test(Descriptor d) {
                return d.getFieldValue("aA");
            }
        },
        new ValueProtoCase<Set<?>>("getFieldValues with null arg",
                                   set(testFieldValues)) {
            Set<?> test(Descriptor d) {
                return set(d.getFieldValues((String[]) null));
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with not all values",
                                     new Object[] {"b", "D", 5}) {
            Object[] test(Descriptor d) {
                return d.getFieldValues("a", "c", "int");
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with all values " +
                                     "lower case",
                                     new Object[]{"bb", "D", "b", 5}) {
            Object[] test(Descriptor d) {
                return d.getFieldValues("aa", "c", "a", "int");
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with all values " +
                                     "upper case",
                                     new Object[] {5, "b", "D", "bb"}) {
            Object[] test(Descriptor d) {
                return d.getFieldValues("int", "A", "C", "AA");
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with null name",
                                     new Object[] {null}) {
            Object[] test(Descriptor d) {
                return d.getFieldValues((String) null);
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with empty name",
                                     new Object[] {null}) {
            Object[] test(Descriptor d) {
                return d.getFieldValues("");
            }
        },
        new ValueProtoCase<Object[]>("getFieldValues with no names",
                                     new Object[0]) {
            Object[] test(Descriptor d) {
                return d.getFieldValues();
            }
        },
        new UnchangedCase("removeField with nonexistent field") {
            void dontChange(Descriptor d) {
                d.removeField("noddy");
            }
        },
        new UnchangedCase("removeField with null field") {
            void dontChange(Descriptor d) {
                d.removeField(null);
            }
        },
        new UnchangedCase("removeField with empty field") {
            void dontChange(Descriptor d) {
                d.removeField("");
            }
        },
        new UnchangedCase("setField leaving string unchanged") {
            void dontChange(Descriptor d) {
                d.setField("a", "b");
            }
        },
        new UnchangedCase("setField leaving int unchanged") {
            void dontChange(Descriptor d) {
                d.setField("int", 5);
            }
        },
        new UnchangedCase("setFields with empty arrays") {
            void dontChange(Descriptor d) {
                d.setFields(new String[0], new Object[0]);
            }
        },
        new UnchangedCase("setFields with unchanged values") {
            void dontChange(Descriptor d) {
                d.setFields(new String[] {"a", "int"},
                            new Object[] {"b", 5});
            }
        },
        new ChangedCase("removeField with exact case",
                        "a", "b", "C", "D", "int", 5, "nul", null) {
            void change(Descriptor d) {
                d.removeField("aa");
            }
        },
        new ChangedCase("removeField with upper case for lower",
                        "a", "b", "C", "D", "int", 5, "nul", null) {
            void change(Descriptor d) {
                d.removeField("AA");
            }
        },
        new ChangedCase("removeField with lower case for upper",
                        "a", "b", "aa", "bb", "int", 5, "nul", null) {
            void change(Descriptor d) {
                d.removeField("c");
            }
        },
        new ChangedCase("setField keeping lower case",
                        "a", "x", "C", "D", "aa", "bb", "int", 5,
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("a", "x");
            }
        },
        new ChangedCase("setField changing lower case to upper",
                        "a", "x", "C", "D", "aa", "bb", "int", 5,
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("A", "x");
            }
        },
        new ChangedCase("setField changing upper case to lower",
                        "a", "b", "C", "x", "aa", "bb", "int", 5,
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("c", "x");
            }
        },
        new ChangedCase("setField adding new field",
                        "a", "b", "C", "D", "aa", "bb", "int", 5, "xX", "yY",
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("xX", "yY");
            }
        },
        new ChangedCase("setField changing type of field",
                        "a", true, "C", "D", "aa", "bb", "int", 5,
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("a", true);
            }
        },
        new ChangedCase("setField changing non-null to null",
                        "a", null, "C", "D", "aa", "bb", "int", 5,
                        "nul", null) {
            void change(Descriptor d) {
                d.setField("a", null);
            }
        },
        new ChangedCase("setField changing null to non-null",
                        "a", "b", "C", "D", "aa", "bb", "int", 5,
                        "nul", 3.14) {
            void change(Descriptor d) {
                d.setField("nul", 3.14);
            }
        },
        new IllegalExceptionCase("getFieldValue with null name") {
            void provoke(Descriptor d) {
                d.getFieldValue(null);
            }
        },
        new IllegalExceptionCase("getFieldValue with empty name") {
            void provoke(Descriptor d) {
                d.getFieldValue("");
            }
        },
        new IllegalExceptionCase("setField with null name") {
            void provoke(Descriptor d) {
                d.setField(null, "x");
            }
        },
        new IllegalExceptionCase("setField with empty name") {
            void provoke(Descriptor d) {
                d.setField("", "x");
            }
        },
        new IllegalExceptionCase("setFields with null fieldNames") {
            void provoke(Descriptor d) {
                d.setFields(null, new Object[] {"X"});
            }
        },
        new IllegalExceptionCase("setFields with null fieldValues") {
            void provoke(Descriptor d) {
                d.setFields(new String[] {"X"}, null);
            }
        },
        new IllegalExceptionCase("setFields with null fieldNames and " +
                                 "fieldValues") {
            void provoke(Descriptor d) {
                d.setFields(null, null);
            }
        },
        new IllegalExceptionCase("setFields with more fieldNames than " +
                                 "fieldValues") {
            void provoke(Descriptor d) {
                d.setFields(new String[] {"A", "B"}, new String[] {"C"});
            }
        },
        new IllegalExceptionCase("setFields with more fieldValues than " +
                                 "fieldNames") {
            void provoke(Descriptor d) {
                d.setFields(new String[] {"A"}, new String[] {"B", "C"});
            }
        },
        new IllegalExceptionCase("setFields with null element of fieldNames") {
            void provoke(Descriptor d) {
                d.setFields(new String[] {null}, new String[] {"X"});
            }
        }
    );
    static <T> T[] constantArray(T... array) {
        return array;
    }
    static String string(Object x) {
        if (x instanceof Object[])
            return Arrays.asList((Object[]) x).toString();
        else
            return String.valueOf(x);
    }
    static Set<String> strings(String... values) {
        return new TreeSet<String>(Arrays.asList(values));
    }
    static <T> Set<T> set(T[] values) {
        return new HashSet<T>(Arrays.asList(values));
    }
    static boolean immutable(Descriptor d) {
        return (d instanceof ImmutableDescriptor);
    }
}
