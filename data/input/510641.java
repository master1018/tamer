public class T_invoke_interface_14 {
    public int run(ITest test) {
        int a = 123;
        int b = 345;
        if(test.testArgsOrder(64, 2) == 32)
        {
            if(a == 123)
                if(b == 345)
                    return 1;
        }
        return 0;
    }
}
