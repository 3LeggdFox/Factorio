import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class FactorioQueries {
    public static void main(String[] args) {
        Scanner scanInp = new Scanner(System.in);
        File config = new File("config.txt");
        try (Scanner scanner = new Scanner(config)) {
            String start_factory = scanner.nextLine();
            scanner.close();
            RecipeBrowser recipes = RecipeBrowser.initialiseBrowser(start_factory, scanInp);
            while (true) {
                System.out.print("Command: ");
                try {
                    String nextLine = scanInp.nextLine();
                    recipes.query(nextLine);
                    System.out.println();
                } catch (QueryException e)
                {
                    System.err.println(e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Fatal Error: Configuration file not found. Requires config.txt containing initial factory file.");
            System.exit(1);
        }
        

    }
}
