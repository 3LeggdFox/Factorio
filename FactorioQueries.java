import java.util.Scanner;
import core.*;

public class FactorioQueries {
    public static void main(String[] args) {
        Scanner stdIn = new Scanner(System.in);
        RecipeBrowser recipes = RecipeBrowser.initialiseBrowser(stdIn);
        while (true) {
            System.out.print("Command: ");
            try {
                String nextLine = stdIn.nextLine();
                recipes.query(nextLine);
                System.out.println(); // Extra spacing
            } catch (QueryException e) {
                System.err.println(e.getMessage());
                System.out.println(); // Extra spacing
            }
        }
    }
}
