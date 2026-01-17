import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;

public class RecipeBrowser {
    ArrayList<Recipe> recipes;
    HashMap<String, Setting> settings;
    String factory;
    Scanner stdin;
    HashMap<String, Station> stations;
    HashMap<String, Integer> allMaterials;
    boolean toggle_verbose = false;
    HashMap<Recipe, Double> steps = new HashMap<>();
    ArrayList<Recipe> recipe_order = new ArrayList<>();
    HashMap<String, Double> resources = new HashMap<>();
    HashMap<String, Double> all_resources = new HashMap<>();
    ArrayList<String> required_resources = new ArrayList<>();
    HashSet<String> cycle_check = new HashSet<>();

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings,
            HashMap<String, Station> stations, String factory, HashMap<String, Integer> allMaterials) {
        this(recipes, settings, stations, factory, allMaterials, new Scanner(System.in));
    }

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings,
            HashMap<String, Station> stations, String factory, HashMap<String, Integer> allMaterials, Scanner scanner) {
        this.recipes = recipes;
        this.settings = settings;
        this.stations = stations;
        this.factory = factory;
        this.allMaterials = allMaterials;
        this.stdin = scanner;
    }

    public ArrayList<Recipe> findRecipes(String material) {
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        for (Recipe recipe : this.recipes) {
            if (recipe.hasOutput(material)) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    private <T> int giveOptions(Iterable<T> list) {
        return giveOptions("Decision must be made between:", list);
    }

    public void getMachinesIn(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        boolean first = true;
        for (Recipe recipe : steps.keySet()) {
            if (!first) {
                System.out.println();
            } else {
                first = false;
            }
            Station station = pickStation(recipe);
            double crafting_time = recipe.getCraftingTime(station, prod_mod_level);
            double machines = steps.get(recipe) * crafting_time;
            if (verbose) {
                String station_string = recipe.toStringSpecificVerbose(station, prod_mod_level);
                System.out.println(String.format("%s\nNeed: %.3f %ss", station_string, machines, station.name));
            } else {
                machines = Math.ceil(machines);
                System.out.println(String.format("Using Recipe: %s\nNeed: %.0f %ss", recipe.toStringSpecific(station), machines, station.name));
            }
        }
        reset();
    }

    public void getBasicIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        for (String material : resources.keySet()) {
            double quantity = resources.get(material);
            if (quantity != 0) {
                System.out.println(material + ": " + quantity);
            }
        }
        reset();
    }

    public void getAllIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        for (String material : all_resources.keySet()) {
            double quantity = all_resources.get(material);
            if (quantity != 0) {
                System.out.println(material + ": " + quantity);
            }
        }
        reset();
    }

    public double quantIn(String ingredient, String product, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(product, amount, prod_mod_level);
        double result = all_resources.getOrDefault(ingredient, 0.0);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        reset();
        return result;
    }

    private void clearUnecessary(String output, double amount, int prod_mod_level) {
        Recipe recipe = pickRecipe(output);
        if (recipe == null) {
            return;
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        double undos = 0;
        boolean can_undo = true;
        while (true) {
            for (Material material : recipe.outputs) {
                double this_amount = amount * material.quantity / recipe.amountOutput(output);
                double current_amount = resources.get(material.name);
                if (current_amount - this_amount > 0)
                {
                    can_undo  = false;
                }
            }
            if (can_undo) {
                undos++;
            } else {
                break;
            }
        }
        double recipe_count = steps.get(recipe);
        if (undos >= recipe_count) {
            undos = recipe_count;
            recipe_order.remove(recipe);
        }
        steps.put(recipe, recipe_count - undos);
        for (int i = 0; i < undos; ++i) {
            for (Material material : recipe.outputs) {
                double this_amount = amount * material.quantity / recipe.amountOutput(output);
                double current_amount = resources.get(material.name);
                resources.put(material.name, current_amount - this_amount);
                all_resources.put(material.name, all_resources.get(material.name) - this_amount);
            }
            for (Material material : recipe.inputs) {
                double this_amount = amount * material.quantity / (recipe.amountOutput(output) / productivity);
                double current_amount = resources.get(material.name);
                resources.put(material.name, current_amount + this_amount);
                all_resources.put(material.name, all_resources.get(material.name) + this_amount);
                if (current_amount + this_amount < 0) {
                    clearUnecessary(material.name, current_amount + this_amount, prod_mod_level);
                }
            }
        }
    }

    private void printRecipePath(int prod_mod_level) {
        for (Recipe recipe : recipe_order) {
            Station station = pickStation(recipe);
            System.out.println(recipe.toStringSpecificVerbose(station, prod_mod_level));
        }
    }

    private void getRecipesIn(String output, double amount, int prod_mod_level) {
        resources.put(output, amount);
        all_resources.put(output, amount);
        required_resources.add(output);
        while (!required_resources.isEmpty()) {
            String material = required_resources.get(0);
            double still_need = resources.get(material);
            addRecipes(material, still_need, prod_mod_level);
        }
        for (String material : resources.keySet()) {
            double quantity = resources.get(material);
            if (quantity < 0) {
                clearUnecessary(material, quantity, prod_mod_level);
            }
        }
    }

    private void addRecipes(String output, double amount, int prod_mod_level) {
        Recipe recipe = pickRecipe(output);
        if (recipe == null) {
            required_resources.remove(output);
            return;
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        double recipe_completions = amount / (recipe.amountOutput(output) * productivity);
        if (!steps.containsKey(recipe)) {
            recipe_order.add(recipe);
        }
        double recipe_count = steps.getOrDefault(recipe, 0.0) + recipe_completions;
        steps.put(recipe, recipe_count);
        for (Material material : recipe.inputs) {
            double this_amount = recipe_completions * material.quantity;
            if (resources.containsKey(material.name)) {
                double current = resources.get(material.name);
                resources.put(material.name, current + this_amount);
                all_resources.put(material.name, all_resources.get(material.name) + this_amount);
                if (current <= 0 && current + this_amount > 0) {
                    required_resources.add(material.name);
                }
            } else {
                resources.put(material.name, this_amount);
                all_resources.put(material.name, this_amount);
                required_resources.add(material.name);
            }
        }
        for (Material material : recipe.outputs) {
            double this_amount = -material.quantity * amount / recipe.amountOutput(output);
            if (resources.containsKey(material.name)) {
                double current = resources.get(material.name);
                resources.put(material.name, current + this_amount);
                if (current + this_amount <= 0) {
                    required_resources.remove(material.name);
                }
            } else {
                resources.put(material.name, this_amount);
            }
        }
    }

    private void reset() {
        steps.clear();
        resources.clear();
        all_resources.clear();
        recipe_order = new ArrayList<>();
    }

    private double getProductivity(Recipe recipe, Station station, int prod_mod_level) {
        if (recipe.can_prod) {
            return station.getProd(prod_mod_level);
        }
        return station.getProd(0);

    }

    private <T> int giveOptions(String heading, Iterable<T> list) {
        System.out.println(heading);
        int counter = 0;
        for (T element : list) {
            System.out.print(counter + ": ");
            System.out.println(element);
            counter++;
        }
        return counter;
    }

    private boolean giveYesNo() {
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 1) == 1;

    }

    private int getUserInt(int min, int max) {
        int userIn = stdin.nextInt();
        stdin.nextLine();
        while (userIn < min && userIn >= max) {
            System.out.println("Please select a valid option (in the range [" + min + "," + (max - 1) + "]).");
            userIn = stdin.nextInt();
            stdin.nextLine();
        }
        return userIn;
    }

    private int hasStation(String station) {
        System.out.println("Does this factory use " + station + "?");
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 1);
    }

    private int moduleLevel(String moduleType) {
        System.out.println("What level of " + moduleType + " module does this factory have?");
        System.out.println("0: None.");
        System.out.println("1: Level 1.");
        System.out.println("2: Level 2.");
        System.out.println("3: Level 3.");
        return getUserInt(0, 3);
    }

    public ArrayList<String> baseIngredients(String output) {
        HashSet<String> base_ingredients = new HashSet<>();
        baseIngredients(output, base_ingredients);
        return new ArrayList<>(base_ingredients);
    }

    private void baseIngredients(String output, HashSet<String> hash_set) {
        ArrayList<Recipe> recipes = findRecipes(output);
        if (recipes.isEmpty()) {
            hash_set.add(output);
            return;
        }
        Recipe recipe = pickRecipe(output, recipes);
        for (Material material : recipe.inputs) {
            baseIngredients(material.name, hash_set);
        }
    }

    public ArrayList<String> nonBaseIngredients(String output) {
        HashSet<String> non_base_ingredients = new HashSet<>();
        nonBaseIngredients(output, non_base_ingredients);
        return new ArrayList<>(non_base_ingredients);
    }

    private void nonBaseIngredients(String output, HashSet<String> hash_set) {
        ArrayList<Recipe> recipes = findRecipes(output);
        if (recipes.isEmpty()) {
            return;
        }
        hash_set.add(output);
        Recipe recipe = pickRecipe(output, recipes);
        for (Material material : recipe.inputs) {
            nonBaseIngredients(material.name, hash_set);
        }
    }

    public void listQuery(String item, boolean verbose) {
        ArrayList<Recipe> isInput = new ArrayList<Recipe>();
        ArrayList<Recipe> isOutput = new ArrayList<Recipe>();
        ArrayList<Recipe> isStation = new ArrayList<Recipe>();
        for (Recipe recipe : recipes) {
            if (recipe.hasInput(item)) {
                isInput.add(recipe);
            }
            if (recipe.hasOutput(item)) {
                isOutput.add(recipe);
            }
            if (recipe.hasStation(item)) {
                isStation.add(recipe);
            }
        }
        boolean allEmpty = true;
        if (!isOutput.isEmpty()) {
            System.out.println("Item is made in recipes:");
            for (Recipe recipe : isOutput) {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (!isInput.isEmpty()) {
            System.out.println("Item is used in recipes:");
            for (Recipe recipe : isInput) {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (!isStation.isEmpty()) {
            System.out.println("Item serves as station in recipes:");
            for (Recipe recipe : isStation) {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (allEmpty) {
            System.out.println("This item was not found in any recipes.");
        }
    }

    public Recipe pickRecipe(String output, ArrayList<Recipe> recipes) {
        if (recipes.size() == 1) {
            return recipes.get(0);
        }
        if (recipes.size() == 0) {
            return null;
        }
        Setting setting = settings.get(output);
        Recipe recipe = null;
        if (setting == null) {
            int counter = giveOptions(recipes);
            int userIn = getUserInt(0, counter);
            recipe = recipes.get(userIn);
            addNewSetting(new Setting(output, recipe.alt_name));
        } else {
            for (Recipe r : recipes) {
                if (r.alt_name.equals(setting.value) || (r.alt_name == null && setting.value.equals("default"))) {
                    recipe = r;
                    break;
                }
            }
        }
        return recipe;
    }

    public Recipe pickRecipe(String output) {
        return pickRecipe(output, findRecipes(output));
    }

    public Station pickStation(Recipe recipe) throws StationNotFoundException {
        Station station = null;
        int highestPrio = -1;
        ArrayList<String> allowedStations = (ArrayList<String>) recipe.stations.clone();
        for (String station_name : allowedStations) {
            String setting_name = "has" + station_name;
            Setting setting = settings.get(setting_name);
            if (setting == null) {
                setting = new Setting(setting_name, Integer.toString(hasStation(station_name)));
                addNewSetting(setting);
            }
            if (setting.value.equals("1")) {
                Station searchStation = stations.get(station_name);
                if (searchStation == null) {
                    throw new StationNotFoundException("Error: Recipe uses station not found in station.txt.",
                            searchStation);
                }
                if (searchStation.priority > highestPrio) {
                    station = searchStation;
                    highestPrio = searchStation.priority;
                }
            }

        }
        return station;
    }

    public double getProd(Station station, int prod_mod_level) {
        String moduleString = "prodModLevel";
        Setting moduleSetting = settings.get(moduleString);
        if (moduleSetting == null) {
            moduleSetting = new Setting(moduleString, Integer.toString(moduleLevel("production")));
            addNewSetting(moduleSetting);
        }
        return station.getProd(Integer.parseInt(moduleSetting.value));
    }

    public void query(String line) throws ParsingException, InvalidMaterialException {
        Query query = Parser.parseQuery(line, toggle_verbose);
        query.query(this);
    }

    private void addNewSetting(Setting setting) {
        settings.put(setting.topic, setting);
        try (FileWriter writer = new FileWriter(factory, true)) {
            writer.write("\n");
            writer.write(setting.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void changeSetting(Setting setting) {
        if (!settings.containsKey(setting.topic)) {
            System.out
                    .println("The setting '" + setting.topic + "' was not found. Would you like to add this setting?");
            if (giveYesNo()) {
                addNewSetting(setting);
                System.out.println("Setting '" + setting.toString() + "' was added.");
            }
        } else {
            String old_value = settings.get(setting.topic).value;
            settings.put(setting.topic, setting);
            System.out.println(
                    "Setting '" + setting.topic + "' was updated from '" + old_value + "' to '" + setting.value + "'.");
        }

        try (FileWriter writer = new FileWriter(factory)) {
            boolean first = true;
            for (Setting set : settings.values()) {
                if (!first) {
                    writer.write("\n");
                } else {
                    first = false;
                }
                writer.write(set.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class StationNotFoundException extends QueryException {
    Station searchStationName;

    public StationNotFoundException(String message, Station searchStationName) {
        super(message);
        this.searchStationName = searchStationName;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("Searched for '" + searchStationName + "'.");
        return builder.toString();
    }
}

class InvalidMaterialException extends QueryException {
    public InvalidMaterialException(String output) {
        super("Error: '" + output + "' not found in any recipes.");
    }
}

class CycleException extends QueryException {
    public CycleException(String material) {
        super("Error: '" + material + "' recipe contains a cycle.");
    }
}