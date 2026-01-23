package core;

/**
 * MathQuery
 * Class to manage queries regarding simple mathematical expressions
 * 
 * @version 1.0
 */
public class MathQuery extends Query {
    double number1;
    char operation;
    double number2;

    /**
     * Constructor
     * 
     * @param number1   First term
     * @param operation Operation
     * @param number2   Second term
     */
    public MathQuery(double number1, char operation, double number2) {
        this.number1 = number1;
        this.operation = operation;
        this.number2 = number2;
    }

    /**
     * Performs a mathematical operation on the two numbers based on the given
     * operation
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        double result;
        switch (operation) {
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
        System.out.println(String.format("%.3f", result));
    }
}