import java.util.Scanner;

public class AnotherSimpleCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("另一个简单计算器");
        System.out.print("请输入一个表达式（例如 2 + 3 或 5 - 1）: ");
        String expression = scanner.nextLine();
        
        String[] tokens = expression.split(" ");
        if (tokens.length != 3) {
            System.out.println("无效的表达式");
            return;
        }
        
        double num1 = Double.parseDouble(tokens[0]);
        char operator = tokens[1].charAt(0);
        double num2 = Double.parseDouble(tokens[2]);
        
        double result = 0.0;
        
        switch (operator) {
            case '+':
                result = num1 + num2;
                break;
            case '-':
                result = num1 - num2;
                break;
            default:
                System.out.println("无效的操作符");
                return;
        }
        
        System.out.println("结果: " + result);
        
        scanner.close();
    }
}
