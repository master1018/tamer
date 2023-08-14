@TestTargetClass(java.util.AbstractCollection.class)
public class AbstractCollectionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void test_addLjava_lang_Object() {
        AbstractCollection<Object> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                fail("iterator should not get called");
                return null;
            }
            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }
        };
        try {
            ac.add(null);
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "addAll",
        args = {java.util.Collection.class}
    )
    public void test_addAllLjava_util_Collection() {
        final Collection<String> fixtures = Arrays.asList("0", "1", "2");
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public boolean add(String object) {
                assertTrue(fixtures.contains(object));
                return true;
            }
            @Override
            public Iterator<String> iterator() {
                fail("iterator should not get called");
                return null;
            }
            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }
        };
        assertTrue(ac.addAll(fixtures));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "containsAll",
        args = {java.util.Collection.class}
    )
    public void test_containsAllLjava_util_Collection() {
        final Collection<String> fixtures = Arrays.asList("0", "1", "2");
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public boolean contains(Object object) {
                assertTrue(fixtures.contains(object));
                return true;
            }
            @Override
            public Iterator<String> iterator() {
                fail("iterator should not get called");
                return null;
            }
            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }
        };
        assertTrue(ac.containsAll(fixtures));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        final boolean[] sizeCalled = new boolean[1];
        AbstractCollection<Object> ac = new AbstractCollection<Object>(){
            @Override
            public Iterator<Object> iterator() {
                fail("iterator should not get called");
                return null;
            }
            @Override
            public int size() {
                sizeCalled[0] = true;
                return 0;
            }
        };
        assertTrue(ac.isEmpty());
        assertTrue(sizeCalled[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "removeAll",
        args = {java.util.Collection.class}
    )
    public void test_removeAllLjava_util_Collection() {
        final String[] removed = new String[3];
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] {"0", "1", "2"};
                    int index;
                    public boolean hasNext() {
                        return index < values.length;
                    }
                    public String next() {
                        return values[index++];
                    }
                    public void remove() {
                        removed[index - 1] = values[index - 1];
                    }
                };
            }
            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }
        };
        assertTrue(ac.removeAll(Arrays.asList("0", "1", "2")));
        for (String r : removed) {
            if (!"0".equals(r) && !"1".equals(r) && !"2".equals(r)) {
                fail("an unexpected element was removed");
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "retainAll",
        args = {java.util.Collection.class}
    )
    public void test_retainAllLjava_util_Collection() {
        final String[] removed = new String[1];
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] {"0", "1", "2"};
                    int index;
                    public boolean hasNext() {
                        return index < values.length;
                    }
                    public String next() {
                        return values[index++];
                    }
                    public void remove() {
                        removed[index - 1] = values[index - 1];
                    }
                };
            }
            @Override
            public int size() {
                fail("size should not get called");
                return 0;
            }
        };
        assertTrue(ac.retainAll(Arrays.asList("1", "2")));
        assertEquals("0", removed[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toArray",
        args = {}
    )
    public void test_toArray() {
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] {"0", "1", "2"};
                    int index;
                    public boolean hasNext() {
                        return index < values.length;
                    }
                    public String next() {
                        return values[index++];
                    }
                    public void remove() {
                        fail("remove should not get called");
                    }
                };
            }
            @Override
            public int size() {
                return 3;
            }
        };
        Object[] array = ac.toArray();
        assertEquals(3, array.length);
        for (Object o : array) {
            if (!"0".equals(o) && !"1".equals(o) && !"2".equals(o)) {
                fail("an unexpected element was removed");
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toArray",
        args = {java.lang.Object[].class}
    )
    public void test_toArray$Ljava_lang_Object() {
        AbstractCollection<String> ac = new AbstractCollection<String>() {
            @Override
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    String[] values = new String[] {"0", "1", "2"};
                    int index;
                    public boolean hasNext() {
                        return index < values.length;
                    }
                    public String next() {
                        return values[index++];
                    }
                    public void remove() {
                        fail("remove should not get called");
                    }
                };
            }
            @Override
            public int size() {
                return 3;
            }
        };
        try {
            ac.toArray(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            ac.toArray(new StringBuffer[ac.size()]);
            fail("No expected ArrayStoreException");
        } catch (ArrayStoreException e) {
        }
        String[] a = new String[3];
        assertSame(a, ac.toArray(a));
        a = new String[0];
        assertNotSame(a, ac.toArray(a));
        a = ac.toArray(a);
        assertEquals(3, a.length);
        CharSequence[] csa = new CharSequence[3];
        ac.toArray(csa);
        assertEquals(3, csa.length);
        assertEquals("0", csa[0]);
        assertEquals("1", csa[1]);
        assertEquals("2", csa[2]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        AbstractCollection<?> c = new AbstractCollection<Object>() {
            @Override
            public int size() {
                return 1;
            }
            @Override
            public Iterator<Object> iterator() {
                return null;
            }
        };
        try {
            c.toString();
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "AbstractCollection",
        args = {}
    )
    public void test_Constructor() {
        AbstractCollection<?> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        assertNotNull(ac);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        AbstractCollection<?> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    public boolean hasNext() {
                        return false;
                    }
                    public Object next() {
                        return null;
                    }
                    public void remove() {
                    }
                };
            }
            @Override
            public int size() {
                return 0;
            }
        };
        ac.clear();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "contains",
        args = {java.lang.Object.class}
    )
    public void test_containsLjava_lang_Object() {
        AbstractCollection<?> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    public boolean hasNext() {
                        return false;
                    }
                    public Object next() {
                        return null;
                    }
                    public void remove() {
                    }
                };
            }
            @Override
            public int size() {
                return 0;
            }
        };
        assertFalse(ac.contains(this));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract. Functionality tested in subclasses for example in java.util.Vector.",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        AbstractCollection<?> ac = new AbstractCollection<Object>() {
            @Override
            public Iterator<Object> iterator() {
                return new Iterator<Object>() {
                    public boolean hasNext() {
                        return false;
                    }
                    public Object next() {
                        return null;
                    }
                    public void remove() {
                    }
                };
            }
            @Override
            public int size() {
                return 0;
            }
        };
        assertFalse(ac.remove(this));
    }
}
