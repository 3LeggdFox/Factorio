import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries 
{

    public static void main(String[] args)
    {
        RecipeBrowser recipes = initialiseBrowser();
        System.out.println(recipes.quantIn("copper", "r_circuit"));
    }

    static RecipeBrowser initialiseBrowser()
    {
        try
        {
            File file = new File("recipes.txt");
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            while (scanner.hasNextLine())
            {
                recipes.add(new Recipe(scanner.nextLine()));
            }
            scanner.close();
            return new RecipeBrowser(recipes);
        } catch (FileNotFoundException e)
        {
            System.out.print(e);
        }
        return null;
    }
}
