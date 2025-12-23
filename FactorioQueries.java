import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries 
{

    public static void main(String[] args)
    {
        RecipeBrowser recipes = initialiseBrowser();
        ArrayList<Recipe> coppRecipes = recipes.findRecipes("cable");
        System.out.println("Hi");
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
