import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries 
{

    public static void main(String[] args)
    {
        RecipeBrowser recipes = initialiseBrowser("examplefactory.txt");
        System.out.println(recipes.quantIn("copper", "r_circuit"));
    }

    static RecipeBrowser initialiseBrowser(String factory)
    {
        try
        {
            File file = new File("recipes.txt");
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            while (scanner.hasNextLine())
            {
                recipes.add(Parser.parseRecipe(scanner.nextLine()));
            }
            scanner.close();

            file = new File(factory);
            scanner = new Scanner(file);
            ArrayList<Setting> settings = new ArrayList<Setting>();
            while (scanner.hasNextLine())
            {
                settings.add(Parser.parseFactory(scanner.nextLine()));
            }
            scanner.close();

            return new RecipeBrowser(recipes, settings, factory);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
