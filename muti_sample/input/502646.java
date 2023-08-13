@TestTargetClass(java.io.Serializable.class) 
public class SerializationStressTest4 extends SerializationStressTest {
    private static class GuardImplementation implements java.security.Guard,
            java.io.Serializable {
        public GuardImplementation() {
        }
        public void checkGuard(Object o) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_EventObject() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.EventObject("Source");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = true; 
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_PermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.security.PermissionCollection() {
                boolean added = false;
                public void add(java.security.Permission p1) {
                    added = true;
                }
                public java.util.Enumeration elements() {
                    return (new java.util.Vector()).elements();
                }
                public boolean implies(java.security.Permission p1) {
                    return added;
                }
                public boolean equals(Object obj) {
                    if (!(obj instanceof java.security.PermissionCollection))
                        return false;
                    return implies(null) == ((PermissionCollection) obj)
                            .implies(null);
                }
            };
            ((java.security.PermissionCollection) objToSave).add(null);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_EmptySet() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.EMPTY_SET;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = objToSave.equals(objLoaded);
            if (equals)
                equals = ((Set) objLoaded).size() == 0;
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_EmptyMap() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.EMPTY_MAP;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = objToSave.equals(objLoaded);
            if (equals)
                equals = ((Map) objLoaded).size() == 0;
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_BasicPermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = (new RuntimePermission("test"))
                    .newPermissionCollection();
            ((java.security.PermissionCollection) objToSave)
                    .add(new RuntimePermission("test"));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().equals(enum2.nextElement());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_UnresolvedPermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.UnresolvedPermission("type", "name",
                    "actions", null);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = objToSave.toString().equals(objLoaded.toString());
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Character() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Character('c');
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_UnmodifiableCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.unmodifiableCollection(SET);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((java.util.Collection) objToSave).size() == ((java.util.Collection) objLoaded)
                    .size();
            if (equals) {
                java.util.Iterator iter1 = ((java.util.Collection) objToSave)
                        .iterator(), iter2 = ((java.util.Collection) objLoaded)
                        .iterator();
                while (iter1.hasNext())
                    equals = equals && iter1.next().equals(iter2.next());
            }
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Format() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.text.Format() {
                String save = "default";
                public StringBuffer format(Object p1, StringBuffer p2,
                        java.text.FieldPosition p3) {
                    return new StringBuffer();
                }
                public Object parseObject(String p1, java.text.ParsePosition p2) {
                    if (p1 != null)
                        save = p1;
                    return save;
                }
                public boolean equals(Object obj) {
                    if (!(obj instanceof java.text.Format))
                        return false;
                    return save.equals(((java.text.Format) obj).parseObject(
                            null, null));
                }
            };
            ((java.text.Format) objToSave).parseObject("Test", null);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_BigDecimal() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.math.BigDecimal("1.2345");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_UnresolvedPermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = (new java.security.UnresolvedPermission("type", "name",
                    "actions", null)).newPermissionCollection();
            ((java.security.PermissionCollection) objToSave)
                    .add(new java.security.UnresolvedPermission("type", "name",
                            "actions", null));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().toString().equals(
                            enum2.nextElement().toString());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_SecureRandomSpi() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.security.SecureRandomSpi() {
                protected byte[] engineGenerateSeed(int p1) {
                    return new byte[0];
                }
                protected void engineNextBytes(byte[] p1) {
                }
                protected void engineSetSeed(byte[] p1) {
                }
                public boolean equals(Object obj) {
                    return true;
                }
            };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Short() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Short((short) 107);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Byte() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Byte((byte) 107);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_String_CaseInsensitiveComparator() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.lang.String.CASE_INSENSITIVE_ORDER;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((Comparator) objToSave).compare("apple", "Banana") == ((Comparator) objLoaded)
                    .compare("apple", "Banana");
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Calendar() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.Calendar(TimeZone.getTimeZone("EST"),
                    Locale.CANADA) {
                public void add(int p1, int p2) {
                }
                protected void computeFields() {
                }
                protected void computeTime() {
                }
                public int getGreatestMinimum(int p1) {
                    return 0;
                }
                public int getLeastMaximum(int p1) {
                    return 0;
                }
                public int getMaximum(int p1) {
                    return 0;
                }
                public int getMinimum(int p1) {
                    return 0;
                }
                public void roll(int p1, boolean p2) {
                }
            };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + "Calendar", objToSave
                    .equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_ReflectPermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.reflect.ReflectPermission(
                    "TestSerialization", "test");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_StringBuffer() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.StringBuffer("This is a test.");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((java.lang.StringBuffer) objToSave).toString().equals(
                    ((java.lang.StringBuffer) objLoaded).toString());
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_File() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new File("afile.txt");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_AllPermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = (new java.security.AllPermission())
                    .newPermissionCollection();
            ((java.security.PermissionCollection) objToSave)
                    .add(new java.security.AllPermission());
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().equals(enum2.nextElement());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_BitSet() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.BitSet();
            ((java.util.BitSet) objToSave).set(3);
            ((java.util.BitSet) objToSave).set(5);
            ((java.util.BitSet) objToSave).set(61, 89);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_DateFormat() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.text.DateFormat() {
                java.util.Date save = new java.util.Date(981007261000L);
                public StringBuffer format(Date p1, StringBuffer p2,
                        java.text.FieldPosition p3) {
                    if (p1 != null)
                        save = p1;
                    return new StringBuffer(Long.toString(save.getTime()));
                }
                public Date parse(String p1, java.text.ParsePosition p2) {
                    return save;
                }
                public String toString() {
                    return save.toString();
                }
                public boolean equals(Object obj) {
                    if (!(obj instanceof java.text.DateFormat))
                        return false;
                    return save.equals(((java.text.DateFormat) obj).parse(null,
                            null));
                }
            };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_CopiesList() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.nCopies(2, new Integer(2));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((List) objToSave).get(0)
                    .equals(((List) objLoaded).get(0));
            if (equals)
                equals = ((List) objToSave).get(1).equals(
                        ((List) objLoaded).get(1));
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_SerializablePermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.io.SerializablePermission("TestSerialization",
                    "Test");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Properties() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.Properties();
            ((java.util.Properties) objToSave).put("key1", "value1");
            ((java.util.Properties) objToSave).put("key2", "value2");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.util.Properties) objToSave).elements(), enum2 = ((java.util.Properties) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().equals(enum2.nextElement());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_UnmodifiableMap() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.unmodifiableMap(MAP);
            if (DEBUG) System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertEquals(MSG_TEST_FAILED, objToSave, objLoaded);
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_NumberFormat() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.text.NumberFormat() {
                long save = 107;
                public StringBuffer format(double p1, StringBuffer p2,
                        java.text.FieldPosition p3) {
                    return new StringBuffer();
                }
                public StringBuffer format(long p1, StringBuffer p2,
                        java.text.FieldPosition p3) {
                    if (p1 != 0)
                        save = p1;
                    return new StringBuffer(Long.toString(save));
                }
                public Number parse(String p1, java.text.ParsePosition p2) {
                    return new Long(save);
                }
                public boolean equals(Object obj) {
                    if (!(obj instanceof java.text.NumberFormat))
                        return false;
                    return save == ((Long) ((java.text.NumberFormat) obj)
                            .parse(null, null)).longValue();
                }
            };
            ((java.text.NumberFormat) objToSave).format(63L, null, null);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_TimeZone() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.util.TimeZone() {
                int save = 0;
                public int getOffset(int p1, int p2, int p3, int p4, int p5,
                        int p6) {
                    return 0;
                }
                public int getRawOffset() {
                    return save;
                }
                public boolean inDaylightTime(java.util.Date p1) {
                    return false;
                }
                public void setRawOffset(int p1) {
                    save = p1;
                }
                public boolean useDaylightTime() {
                    return false;
                }
                public boolean equals(Object obj) {
                    if (obj instanceof TimeZone)
                        return save == ((TimeZone) obj).getRawOffset();
                    return false;
                }
            };
            ((java.util.TimeZone) objToSave).setRawOffset(48);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Double() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Double(1.23);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Number() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new Number() {
                int numCalls = 0;
                public double doubleValue() {
                    return ++numCalls;
                }
                public float floatValue() {
                    return ++numCalls;
                }
                public int intValue() {
                    return numCalls;
                }
                public long longValue() {
                    return ++numCalls;
                }
                public boolean equals(Object obj) {
                    if (!(obj instanceof java.lang.Number))
                        return false;
                    return intValue() == ((Number) obj).intValue();
                }
            };
            ((java.lang.Number) objToSave).doubleValue();
            ((java.lang.Number) objToSave).floatValue();
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_AllPermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.AllPermission();
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_ReverseComparator() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.reverseOrder();
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((Comparator) objToSave).compare("Hello", "Jello") == ((Comparator) objLoaded)
                    .compare("Hello", "Jello");
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("IOException serializing " + objToSave + " : "
                    + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type : "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_DateFormatSymbols() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.text.DateFormatSymbols(Locale.CHINESE);
            ((java.text.DateFormatSymbols) objToSave)
                    .setZoneStrings(new String[][] { { "a", "b", "c", "d" },
                            { "e", "f", "g", "h" } });
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_EmptyList() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.EMPTY_LIST;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = objToSave.equals(objLoaded);
            if (equals)
                equals = ((List) objLoaded).size() == 0;
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Boolean() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Boolean(true);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_SingletonSet() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.singleton(new Byte((byte) 107));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            java.util.Iterator iter = ((Set) objLoaded).iterator();
            equals = iter.hasNext();
            if (equals)
                equals = iter.next().equals(new Byte((byte) 107));
            if (equals)
                equals = !iter.hasNext();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_SingletonList() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections
                    .singletonList(new Byte((byte) 107));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            java.util.Iterator iter = ((List) objLoaded).iterator();
            equals = objLoaded.equals(objToSave) && iter.hasNext()
                    && iter.next().equals(new Byte((byte) 107))
                    && !iter.hasNext();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_SingletonMap() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.singletonMap("key", new Byte(
                    (byte) 107));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            java.util.Iterator iter = ((Map) objLoaded).entrySet().iterator();
            equals = objLoaded.equals(objToSave) && iter.hasNext();
            Map.Entry entry = (Map.Entry) iter.next();
            equals = equals && entry.getKey().equals("key")
                    && entry.getValue().equals(new Byte((byte) 107))
                    && !iter.hasNext();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_FilePermission_FilePermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = (new java.io.FilePermission("<<ALL FILES>>", "read"))
                    .newPermissionCollection();
            ((java.security.PermissionCollection) objToSave)
                    .add(new FilePermission("<<ALL FILES>>", "read"));
            ((java.security.PermissionCollection) objToSave)
                    .add(new FilePermission("d:\\", "read"));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            java.util.Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().equals(enum2.nextElement());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_SecureRandom() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.SecureRandom();
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = true; 
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_FilePermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.io.FilePermission("<<ALL FILES>>", "read");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_InetAddress() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.net.InetAddress
                    .getByName(Support_Configuration.InetTestIP);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Inet6Address() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.net.Inet6Address
                    .getByName(Support_Configuration.InetTestIP6);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_RuntimePermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.RuntimePermission("TestSerialization",
                    "Test");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Permissions() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.Permissions();
            ((java.security.Permissions) objToSave).add(new AllPermission());
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            java.util.Vector vec1 = new java.util.Vector(), vec2 = new java.util.Vector();
            while (enum1.hasMoreElements())
                vec1.add(enum1.nextElement());
            while (enum2.hasMoreElements())
                vec2.add(enum2.nextElement());
            equals = vec1.size() == vec2.size();
            if (equals) {
                int length = vec1.size();
                Object[] perms1 = new Object[length], perms2 = new Object[length];
                for (int i = 0; i < length; ++i) {
                    perms1[i] = vec1.elementAt(i);
                    perms2[i] = vec2.elementAt(i);
                }
                Comparator comparator = new Comparator() {
                    public int compare(Object o1, Object o2) {
                        return o1.toString().compareTo(o2.toString());
                    }
                    public boolean equals(Object o1, Object o2) {
                        return o1.toString().equals(o2.toString());
                    }
                };
                java.util.Arrays.sort(perms1, comparator);
                java.util.Arrays.sort(perms2, comparator);
                for (int i = 0; i < length && equals; ++i)
                    equals = perms1[i].equals(perms2[i]);
            }
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Date() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.Date(981007261000L); 
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Float() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Float(1.23f);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_SecurityPermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.SecurityPermission(
                    "TestSerialization", "Test");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_SocketPermission_SocketPermissionCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = (new java.net.SocketPermission("www.yahoo.com",
                    "connect")).newPermissionCollection();
            ((java.security.PermissionCollection) objToSave)
                    .add(new java.net.SocketPermission("www.yahoo.com",
                            "connect"));
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            Enumeration enum1 = ((java.security.PermissionCollection) objToSave)
                    .elements(), enum2 = ((java.security.PermissionCollection) objLoaded)
                    .elements();
            equals = true;
            while (enum1.hasMoreElements() && equals) {
                if (enum2.hasMoreElements())
                    equals = enum1.nextElement().equals(enum2.nextElement());
                else
                    equals = false;
            }
            if (equals)
                equals = !enum2.hasMoreElements();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Stack() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.Stack();
            ((java.util.Stack) objToSave).push("String 1");
            ((java.util.Stack) objToSave).push("String 2");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = true;
            while (!((java.util.Stack) objToSave).empty() && equals) {
                if (!((java.util.Stack) objLoaded).empty())
                    equals = ((java.util.Stack) objToSave).pop().equals(
                            ((java.util.Stack) objLoaded).pop());
                else
                    equals = false;
            }
            if (equals)
                equals = ((java.util.Stack) objLoaded).empty();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_DecimalFormatSymbols() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.text.DecimalFormatSymbols(Locale.CHINESE);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_NetPermission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.net.NetPermission("TestSerialization", "Test");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_AttributedCharacterIterator_Attribute() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.text.AttributedCharacterIterator.Attribute.LANGUAGE;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Long() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.lang.Long(107L);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_CodeSource() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            try {
                objToSave = new java.security.CodeSource(new java.net.URL(
                        "http:
                        (Certificate[])null);
            } catch (Exception e) {
                fail("Exception creating object : " + e.getMessage());
            }
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Collections_SynchronizedCollection() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Collections.synchronizedCollection(SET);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((java.util.Collection) objToSave).size() == ((java.util.Collection) objLoaded)
                    .size();
            if (equals) {
                java.util.Iterator iter1 = ((java.util.Collection) objToSave)
                        .iterator(), iter2 = ((java.util.Collection) objLoaded)
                        .iterator();
                while (iter1.hasNext())
                    equals = equals && iter1.next().equals(iter2.next());
            }
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Permission() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = null;
            objToSave = new java.security.Permission("test") {
                public boolean equals(Object p1) {
                    if (!(p1 instanceof java.security.Permission))
                        return false;
                    return getName().equals(
                            ((java.security.Permission) p1).getName());
                }
                public int hashCode() {
                    return 0;
                }
                public String getActions() {
                    return null;
                }
                public boolean implies(java.security.Permission p1) {
                    return false;
                }
            };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Random() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.util.Random(107L);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((java.util.Random) objToSave).nextInt() == ((java.util.Random) objLoaded)
                    .nextInt();
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_GuardedObject() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = new java.security.GuardedObject("Test Object",
                    new GuardImplementation());
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            boolean equals;
            equals = ((java.security.GuardedObject) objToSave).getObject()
                    .equals(
                            ((java.security.GuardedObject) objLoaded)
                                    .getObject());
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    static class MyInvocationHandler implements InvocationHandler, Serializable {
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            if (method.getName().equals("equals"))
                return new Boolean(proxy == args[0]);
            if (method.getName().equals("array"))
                return new int[] { (int) ((long[]) args[0])[1], -1 };
            if (method.getName().equals("string")) {
                if ("error".equals(args[0]))
                    throw new ArrayStoreException();
                if ("any".equals(args[0]))
                    throw new IllegalAccessException();
            }
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void test_writeObject_Proxy()
            throws ClassNotFoundException, IOException {
        Class<?> proxyClass = Proxy.getProxyClass(Support_Proxy_I1.class
                .getClassLoader(), new Class[]{Support_Proxy_I1.class});
        Class<?> proxyClassOut = (Class<?>) dumpAndReload(proxyClass);
        assertTrue(Proxy.isProxyClass(proxyClassOut));
        assertEquals(Collections.<Class>singletonList(Support_Proxy_I1.class),
                Arrays.asList(proxyClassOut.getInterfaces()));
        InvocationHandler handler = new MyInvocationHandler();
        Object proxyInstance = Proxy.newProxyInstance(Support_Proxy_I1.class
                .getClassLoader(), new Class[] { Support_Proxy_I1.class },
                handler);
        Object proxyInstanceOut = dumpAndReload(proxyInstance);
        assertEquals(MyInvocationHandler.class,
                Proxy.getInvocationHandler(proxyInstanceOut).getClass());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_URI() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            try {
                objToSave = new URI[] {
                        new URI(
                                "http:
                        new URI(
                                "http:
                        new URI(
                                "ascheme:
                        new URI("http", "user%60%20info", "host", 80,
                                "/a%20path", "qu%60%20ery", "fr%5E%20ag"),
                        new URI("http", "user%C3%9F%C2%A3info", "host", -1,
                                "/a%E2%82%ACpath", "qu%C2%A9%C2%AEery",
                                "fr%C3%A4%C3%A8g"),
                        new URI("ascheme", "user\u00DF\u00A3info", "host", 80,
                                "/a\u20ACpath", "qu\u00A9\u00AEery",
                                "fr\u00E4\u00E8g"),
                        new URI("http", "user` info", "host", 81, "/a path",
                                "qu` ery", "fr^ ag"), 
                        new URI("http", "user%info", "host", 0, "/a%path",
                                "que%ry", "f%rag"),
                        new URI("mailto", "user@domain.com", null),
                        new URI("../adirectory/file.html#"),
                        new URI("news", "comp.infosystems.www.servers.unix",
                                null),
                        new URI(null, null, null, "fragment"),
                        new URI("telnet:
                        new URI("http:
                        new URI("file:
                };
            } catch (URISyntaxException e) {
                fail("Unexpected Exception:" + e);
            }
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, Arrays.equals(
                    (URI[]) objToSave, (URI[]) objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_URISyntaxException() {
        URISyntaxException objToSave = null;
        URISyntaxException objLoaded = null;
        try {
            objToSave = new URISyntaxException("str", "problem", 4);
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = (URISyntaxException) dumpAndReload(objToSave);
            boolean equals = objToSave.getMessage().equals(
                    objLoaded.getMessage())
                    && objToSave.getInput().equals(objLoaded.getInput())
                    && objToSave.getIndex() == objLoaded.getIndex()
                    && objToSave.getReason().equals(objLoaded.getReason());
            assertTrue(MSG_TEST_FAILED + objToSave, equals);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_Currency() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = java.util.Currency.getInstance("AMD");
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave == objToSave);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    @KnownFailure("deserialization of a date fomat field seems to fail")
    public void test_writeObject_DateFormat_Field() {
        DateFormat.Field[] objToSave = null;
        DateFormat.Field[] objLoaded = null;
        try {
            objToSave = new DateFormat.Field[] { DateFormat.Field.AM_PM,
                    DateFormat.Field.DAY_OF_MONTH, DateFormat.Field.ERA,
                    DateFormat.Field.HOUR0, DateFormat.Field.HOUR1,
                    DateFormat.Field.HOUR_OF_DAY0,
                    DateFormat.Field.HOUR_OF_DAY1, DateFormat.Field.TIME_ZONE,
                    DateFormat.Field.YEAR,
                    DateFormat.Field.DAY_OF_WEEK_IN_MONTH };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = (DateFormat.Field[]) dumpAndReload(objToSave);
            for (int i = 0; i < objToSave.length; i++) {
                assertTrue(MSG_TEST_FAILED + objToSave[i],
                        objToSave[i] == objLoaded[i]);
            }
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    @KnownFailure("deserialization of a number fomat field seems to fail")
    public void test_writeObject_NumberFormat_Field() {
        NumberFormat.Field[] objToSave = null;
        NumberFormat.Field[] objLoaded = null;
        try {
            objToSave = new NumberFormat.Field[] { NumberFormat.Field.CURRENCY,
                    NumberFormat.Field.DECIMAL_SEPARATOR,
                    NumberFormat.Field.EXPONENT,
                    NumberFormat.Field.EXPONENT_SIGN,
                    NumberFormat.Field.EXPONENT_SYMBOL,
                    NumberFormat.Field.FRACTION,
                    NumberFormat.Field.GROUPING_SEPARATOR,
                    NumberFormat.Field.INTEGER, NumberFormat.Field.PERCENT,
                    NumberFormat.Field.PERMILLE, NumberFormat.Field.SIGN };
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = (NumberFormat.Field[]) dumpAndReload(objToSave);
            for (int i = 0; i < objToSave.length; i++) {
                assertTrue(MSG_TEST_FAILED + objToSave[i],
                        objToSave[i] == objLoaded[i]);
            }
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_MessageFormat_Field() {
        Object objToSave = null;
        Object objLoaded = null;
        try {
            objToSave = MessageFormat.Field.ARGUMENT;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, objToSave == objLoaded);
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_LinkedHashMap() {
        Object objToSave = null;
        Object objLoaded;
        try {
            objToSave = LINKEDMAP;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, LINKEDMAP.equals(objLoaded));
            Map mapLoaded = (Map) objLoaded;
            Iterator loadedIterator = mapLoaded.keySet().iterator();
            Iterator iterator = LINKEDMAP.keySet().iterator();
            while (loadedIterator.hasNext()) {
                assertTrue("invalid iterator order", loadedIterator.next()
                        .equals(iterator.next()));
            }
            assertTrue("invalid iterator size", !iterator.hasNext());
            loadedIterator = mapLoaded.entrySet().iterator();
            iterator = LINKEDMAP.entrySet().iterator();
            while (loadedIterator.hasNext()) {
                assertTrue("invalid entry set iterator order", loadedIterator
                        .next().equals(iterator.next()));
            }
            assertTrue("invalid entry set iterator size", !iterator.hasNext());
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_LinkedHashSet() {
        Object objToSave = null;
        Object objLoaded;
        try {
            objToSave = LINKEDSET;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = dumpAndReload(objToSave);
            assertTrue(MSG_TEST_FAILED + objToSave, LINKEDSET.equals(objLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization.",
        method = "!Serialization",
        args = {}
    )
    public void test_writeObject_IdentityHashMap() {
        IdentityHashMap objToSave = null;
        IdentityHashMap objLoaded;
        try {
            objToSave = IDENTITYMAP;
            if (DEBUG)
                System.out.println("Obj = " + objToSave);
            objLoaded = (IdentityHashMap) dumpAndReload(objToSave);
            assertEquals(
                    "Loaded IdentityHashMap is not of the same size as the saved one.",
                    objToSave.size(), objLoaded.size());
            HashMap duplicateSaved = new HashMap();
            duplicateSaved.putAll(objToSave);
            HashMap duplicateLoaded = new HashMap();
            duplicateLoaded.putAll(objLoaded);
            assertTrue(MSG_TEST_FAILED + duplicateSaved, duplicateSaved
                    .equals(duplicateLoaded));
        } catch (IOException e) {
            fail("Exception serializing " + objToSave + " : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException reading Object type: "
                    + e.getMessage());
        } catch (Error err) {
            System.out.println("Error when obj = " + objToSave);
            throw err;
        }
    }
}
