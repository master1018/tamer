public class T_invoke_static_15 {
    public boolean run(){
        int i = 123;
        int j = 345;
        if(TestClass.testArgsOrder(12, 2) == 6)
            if(i == 123)
                if(j == 345)
                    return true;
        return false;
    }
}
