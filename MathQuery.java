
public class MathQuery extends Query {
    double number1;
    char operation;
    double number2;
  

    public MathQuery(double number1, char operation, double number2)
    {
        this.number1 = number1;
        this.operation = operation;
        this.number2 = number2;
    }

    public void query(RecipeBrowser browser)
    {
        double result;
        switch (operation)
        {
            case '+':
                result = number1 + number2;
            break;
            case '-':
                result = number1 - number2;
            break;
            case '/':
                result = number1 / number2;
            break;
            case '*':
                result = number1 * number2;
            break;
            default:
                System.err.println("Error: Invalid Operator.");
            return;
        }
        System.out.println(result);
    }
}
