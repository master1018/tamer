@TestTargetClass(AbstractList.class) 
public class ConcurrentModTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Doesn't verify IndexOutOfBoundsException.",
        method = "get",
        args = {int.class}
    )
    public void testGet() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);
        al.remove(1); 
        try {
            sub.get(1);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
        try {
            al.get(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        try {
            al.get(al.size()+1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify UnsupportedOperationException, ClassCastException, IllegalArgumentException, IndexOutOfBoundsException.",
        method = "set",
        args = {int.class, java.lang.Object.class}
    )
    public void testSet() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);
        al.remove(1); 
        try {
            sub.set(1, two);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify UnsupportedOperationException, ClassCastException, IllegalArgumentException, IndexOutOfBoundsException.",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void testAdd() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);
        al.remove(1); 
        try {
            sub.add(1, two);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {int.class}
    )
    public void testRemove() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);
        al.remove(1); 
        try {
            sub.remove(1);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
        try {
            sub.remove(-1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        try {
            sub.remove(sub.size() + 1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        al = new AbstractList() {
            @Override
            public Object get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            al.remove(1);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify UnsupportedOperationException, ClassCastException, IllegalArgumentException,  NullPointerException.",
        method = "addAll",
        args = {int.class, java.util.Collection.class}
    )
    public void testAddAll() {
        AbstractList al = new ArrayList();
        Double one = new Double(1.0);
        Double two = new Double(2.0);
        Double three = new Double(3.0);
        Double four = new Double(4.0);
        al.add(one);
        al.add(two);
        al.add(three);
        al.add(four);
        List sub = al.subList(1, 3);
        assertEquals(2, sub.size());
        assertTrue(((Double) sub.get(1)).doubleValue() <= 3.0);
        assertTrue(((Double) sub.get(1)).doubleValue() > 2.0);
        al.remove(1); 
        try {
            Collection c = new Vector();
            Double five = new Double(5.0);
            c.add(five);
            sub.addAll(1, c);
            fail("It should throws ConcurrentModificationException.");
        } catch (ConcurrentModificationException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Class is abstract - test testing simplest implementation.",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void test_addLjava_lang_Object() {
        AbstractList abstr = new AbstractList() {
            @Override
            public Object get(int arg0) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.add(null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
        abstr = new AbstractList<Double>() {
            @Override
            public boolean add(Double value) {
                return true;
            }
            @Override
            public Double get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.add(1);
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
        }
        abstr = new AbstractList<Integer>() {
            final int forbiddenValue = 33;
            @Override
            public boolean add(Integer value) {
                if (value == forbiddenValue) {
                    throw new IllegalArgumentException();
                }
                return true;
            }
            @Override
            public Integer get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        abstr.add(1);
        try {
            abstr.add(33);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "add",
            args = {int.class, java.lang.Object.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "AbstractList",
            args = {}
        )
    })
    public void test_addILjava_lang_Object() {
        AbstractList abstr = new AbstractList() {
            @Override
            public Object get(int arg0) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.add(1, null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
        abstr = new AbstractList<Double>() {
            @Override
            public void add(int index, Double value) {
            }
            @Override
            public Double get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.add(1, 1);
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
        }
        abstr = new AbstractList<Integer>() {
            final int forbiddenValue = 33;
            @Override
            public void add(int index, Integer value) {
                if (value == forbiddenValue) {
                    throw new IllegalArgumentException();
                }
            }
            @Override
            public Integer get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        abstr.add(1, 1);
        try {
            abstr.add(1, 33);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
        abstr = new ArrayList();
        abstr.add(0, "element");
        abstr.add(1, null);
        abstr.add(2, 1);
        abstr.add(3, new Double(33));
        try {
            abstr.add(-3, new Double(33));
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        try {
            abstr.add(abstr.size() + 1, new Double(33));
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addAll",
        args = {int.class, java.util.Collection.class}
    )
    public void test_addAllILjava_util_Collection() {
        Collection c = new Vector();
        c.add(new Double(33));
        c.add(10);
        c.add("String");
        AbstractList abstr = new AbstractList() {
            @Override
            public Object get(int arg0) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.addAll(0, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
        }
        try {
            abstr.addAll(0, c);
            fail("UnsuportedOperationException expected");
        } catch (UnsupportedOperationException ee) {
        }
        abstr = new AbstractList<Double>() {
            @Override
            public void add(int index, Double value) {
            }
            @Override
            public Double get(int arg0) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.addAll(0, c);
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
        }
        abstr = new AbstractList<Integer>() {
            final int forbiddenValue = 33;
            @Override
            public void add(int index, Integer value) {
                if (value == forbiddenValue) {
                    throw new IllegalArgumentException();
                }
            }
            @Override
            public Integer get(int arg0) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        c.clear();
        c.add(new Integer(1));
        c.add(new Integer(2));
        c.add(new Integer(3));
        c.add(new Integer(4));
        c.add(new Integer(5));
        abstr.addAll(0, c);
        c.add(new Integer(33));
        try {
            abstr.addAll(0, c);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
        abstr = new ArrayList();
        abstr.addAll(0, c);
        try {
            abstr.addAll(-1, c);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        try {
            abstr.addAll(abstr.size() + 1, c);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        AbstractList abstr = new ArrayList();
        assertEquals(0, abstr.size());
        abstr.add("String");
        abstr.add("1");
        abstr.add(2);
        abstr.add(new Double(3));
        assertEquals(4, abstr.size());
        abstr.clear();
        assertEquals(0, abstr.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        Collection c = new Vector();
        c.add(new Double(33));
        c.add(10);
        c.add("String");
        AbstractList abstr = new ArrayList();
        AbstractList abstr1 = new ArrayList();
        assertFalse(abstr.equals(this));
        abstr.add(new Double(33));
        abstr.add(10);
        abstr.add("String");
        assertTrue(abstr.equals(c));
        abstr1.addAll(c);
        assertTrue(abstr.equals(abstr1));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "set",
        args = {int.class, java.lang.Object.class}
    )
    public void test_setILjava_lang_Object() {
        Collection c = new Vector();
        c.add(new Double(33));
        c.add(10);
        c.add("String");
        AbstractList abstr1 = new ArrayList();
        AbstractList abstr2 = new ArrayList();
        abstr1.addAll(c);
        abstr2.addAll(c);
        assertTrue(abstr1.equals(abstr2));
        abstr1.set(1, 1);
        assertFalse(abstr1.equals(abstr2));
        try {
            abstr1.set(abstr1.size() + 1, 1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        try {
            abstr1.set(-1, 1);
            fail("IndexOutOfBoundsException expected");
        } catch (IndexOutOfBoundsException ee) {
        }
        AbstractList abstr = new AbstractList() {
            @Override
            public Object get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.set(0, null);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException ee) {
        }
        abstr = new AbstractList<Double>() {
            @Override
            public Double set(int index, Double value) {
                return value;
            }
            @Override
            public Double get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.set(0, 1);
            fail("ClassCastException expected");
        } catch (ClassCastException ee) {
        }
        abstr = new AbstractList<Integer>() {
            final int forbiddenValue = 33;
            @Override
            public Integer set(int index, Integer value) {
                if (value == forbiddenValue) {
                    throw new IllegalArgumentException();
                }
                return value;
            }
            @Override
            public Integer get(int index) {
                return null;
            }
            @Override
            public int size() {
                return 0;
            }
        };
        try {
            abstr.set(0, 33);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
    }
    class Mock_ArrayList extends ArrayList {
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeRange",
        args = {int.class, int.class}
    )
    public void test_removeRangeII() {
        Mock_ArrayList al1 = new Mock_ArrayList();
        al1.add(1);
        al1.add(2);
        al1.add(3);
        al1.add(4);
        al1.add(5);
        Mock_ArrayList al2 = new Mock_ArrayList();
        al2.add(1);
        al2.add(5);
        assertNotSame(al1,al2);
        al1.removeRange(1, 4);
        assertEquals(al1,al2);
    }
}
