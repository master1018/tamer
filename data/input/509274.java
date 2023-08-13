public class Blort 
{
    static public void blort() {
    }
    public int test1(int x) {
        try {
            switch (x) {
                case 1: {
                    x = 10;
                    break;
                }
                case 2: {
                    x = 20;
                    break;
                }
            }
        } catch (RuntimeException ex) {
        }
        return x;
    }
    public int test2(int x) {
        try {
            switch (x) {
                case 1: {
                    x = 10;
                    blort();
                    break;
                }
                case 2: {
                    x = 20;
                    break;
                }
            }
        } catch (RuntimeException ex) {
        }
        return x;
    }
    public int test3(int x) {
        switch (x) {
            case 1: {
                try {
                    x = 10;
                    blort();
                } catch (RuntimeException ex) {
                }
                break;
            }
            case 2: {
                x = 20;
                break;
            }
        }
        return x;
    }
    public int test4(int x) {
        try {
            switch (x) {
                case 1: {
                    try {
                        x = 10;
                        blort();
                    } catch (RuntimeException ex) {
                    }
                    break;
                }
                case 2: {
                    x = 20;
                    break;
                }
            }
        } catch (RuntimeException ex) {
            return 4;
        }
        return x;
    }
}
