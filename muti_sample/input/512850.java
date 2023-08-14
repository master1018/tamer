@TestTargetClass(Stack.class) 
public class StackTest extends junit.framework.TestCase {
    Stack s;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Stack",
        args = {}
    )
    public void test_Constructor() {
        assertEquals("Stack creation failed", 0, s.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "empty",
        args = {}
    )
    public void test_empty() {
        assertTrue("New stack answers non-empty", s.empty());
        s.push("blah");
        assertTrue("Stack should not be empty but answers empty", !s.empty());
        s.pop();
        assertTrue("Stack should be empty but answers non-empty", s.empty());
        s.push(null);
        assertTrue("Stack with null should not be empty but answers empty", !s
                .empty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "peek",
        args = {}
    )
    public void test_peek() {
        String item1 = "Ichi";
        String item2 = "Ni";
        String item3 = "San";
        s.push(item1);
        assertTrue("Peek did not return top item when it was the only item", s
                .peek() == item1);
        s.push(item2);
        s.push(item3);
        assertTrue("Peek did not return top item amoung many other items", s
                .peek() == item3);
        s.pop();
        assertTrue("Peek did not return top item after a pop", s.pop() == item2);
        s.push(null);
        assertNull("Peek did not return top item (wanted: null)",
                s.peek());
        s.pop();
        s.pop();
        try {
            s.pop();
            fail("EmptyStackException expected");
        } catch (EmptyStackException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "pop",
        args = {}
    )
    public void test_pop() {
        String item1 = "Ichi";
        String item2 = "Ni";
        Object lastPopped;
        s.push(item1);
        s.push(item2);
        try {
            lastPopped = s.pop();
            assertTrue("a) Pop did not return top item", lastPopped == item2);
        } catch (EmptyStackException e) {
            fail(
                    "a) Pop threw EmptyStackException when stack should not have been empty");
        }
        try {
            lastPopped = s.pop();
            assertTrue("b) Pop did not return top item", lastPopped == item1);
        } catch (EmptyStackException e) {
            fail(
                    "b) Pop threw EmptyStackException when stack should not have been empty");
        }
        s.push(null);
        try {
            lastPopped = s.pop();
            assertNull("c) Pop did not return top item", lastPopped);
        } catch (EmptyStackException e) {
            fail(
                    "c) Pop threw EmptyStackException when stack should not have been empty");
        }
        try {
            lastPopped = s.pop();
            fail(
                    "d) Pop did not throw EmptyStackException when stack should have been empty");
        } catch (EmptyStackException e) {
            return;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "push",
        args = {java.lang.Object.class}
    )
    public void test_pushLjava_lang_Object() {
        Object [] array = {new Integer(0), new Object(), 
                           new Float(0), new String()};
        Stack<Object> stack = new Stack<Object>();
        for(int i = 0; i < array.length; i++) {
            stack.push(array[i]);
        }
        for(int i = 0; i < array.length; i++) {
            assertEquals(array.length - i, stack.search(array[i]));
        }       
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "search",
        args = {java.lang.Object.class}
    )
    public void test_searchLjava_lang_Object() {
        String item1 = "Ichi";
        String item2 = "Ni";
        String item3 = "San";
        s.push(item1);
        s.push(item2);
        s.push(item3);
        assertEquals("Search returned incorrect value for equivalent object", 3, s
                .search(item1));
        assertEquals("Search returned incorrect value for equal object", 3, s
                .search("Ichi"));
        s.pop();
        assertEquals("Search returned incorrect value for equivalent object at top of stack",
                1, s.search(item2));
        assertEquals("Search returned incorrect value for equal object at top of stack",
                1, s.search("Ni"));
        s.push(null);
        assertEquals("Search returned incorrect value for search for null at top of stack",
                1, s.search(null));
        s.push("Shi");
        assertEquals("Search returned incorrect value for search for null", 2, s
                .search(null));
        s.pop();
        s.pop();
        assertEquals("Search returned incorrect value for search for null--wanted -1",
                -1, s.search(null));
    }
    protected void setUp() {
        s = new Stack();
    }
    protected void tearDown() {
    }
}
