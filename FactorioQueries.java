import java.util.Scanner;

public class FactorioQueries {
    public static void main(String[] args) {
        Scanner scanInp = new Scanner(System.in);
        RecipeBrowser recipes = RecipeBrowser.initialiseBrowser(scanInp);
        while (true) {
            System.out.print("Command: ");
            try {
                String nextLine = scanInp.nextLine();
                recipes.query(nextLine);
                System.out.println();
            } catch (QueryException e) {
                System.err.println(e.getMessage());
                System.out.println();
            }
        }
    }
}
