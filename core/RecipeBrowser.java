package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;

public class RecipeBrowser {
    ArrayList<Recipe> recipes;
    HashMap<String, Setting> settings;
    String factory;
    Scanner stdin;
    HashMap<String, Station> stations;
    HashSet<String> all_materials;
    HashSet<String> base_ingredients;
    boolean toggle_verbose = false;
    HashMap<Recipe, Double> steps = new HashMap<>();
    ArrayList<Recipe> recipe_order = new ArrayList<>();
    HashMap<String, Double> resources = new HashMap<>();
    HashMap<String, Double> all_resources = new HashMap<>();
    ArrayList<String> required_resources = new ArrayList<>();
    HashSet<String> cycle_check = new HashSet<>();

    private static final String CORE_FOLDER = "core/";
    private static final String DATA_FOLDER = CORE_FOLDER + "essentialFiles/";
    private static final String RECIPE_FILE = DATA_FOLDER + "recipes.txt";
    private static final String STATION_FILE = DATA_FOLDER + "stations.txt";
    private static final String BASE_RESOURCE_FILE = DATA_FOLDER + "base.txt";
    static final String CONFIG_FILE = DATA_FOLDER + "config.txt";
    static final String FACTORY_FOLDER = CORE_FOLDER + "factories/";
    static final String TEMPLATE_FOLDER = CORE_FOLDER + "factoryTemplates/";

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings,
            HashMap<String, Station> stations, String factory, HashSet<String> allMaterials,
            HashSet<String> base_ingredients, Scanner scanner) {
        this.recipes = recipes;
        this.settings = settings;
        this.stations = stations;
        this.factory = factory;
        this.all_materials = allMaterials;
        this.base_ingredients = base_ingredients;
        this.stdin = scanner;
    }

    public static RecipeBrowser initialiseBrowser(Scanner scanInp) {
        try {
            File file = new File(CONFIG_FILE);
            String factory;
            try (Scanner scanner = new Scanner(file))
            {
                factory = scanner.nextLine();
                scanner.close();
            }
            file = new File(RECIPE_FILE);
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            HashSet<String> allMaterials = new HashSet<>();
            while (scanner.hasNextLine()) {
                Recipe recipe = Parser.parseRecipe(scanner.nextLine());
                for (Material input : recipe.inputs) {
                    allMaterials.add(input.name);
                }
                for (Material output : recipe.outputs) {
                    allMaterials.add(output.name);
                }
                recipes.add(recipe);
            }
            scanner.close();

            file = new File(STATION_FILE);
            scanner = new Scanner(file);
            HashMap<String, Station> stations = new HashMap<String, Station>();
            while (scanner.hasNextLine()) {
                Station station = Parser.parseStations(scanner.nextLine());
                stations.put(station.name, station);
            }
            scanner.close();

            file = new File(FACTORY_FOLDER + factory);
            scanner = new Scanner(file);
            HashMap<String, Setting> settings = new HashMap<String, Setting>();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                settings.put(setting.topic, setting);
            }
            scanner.close();

            file = new File(BASE_RESOURCE_FILE);
            scanner = new Scanner(file);
            HashSet<String> base_ingredients = new HashSet<>();
            while (scanner.hasNextLine()) {
                base_ingredients.add(scanner.nextLine());
            }
            scanner.close();
            RecipeBrowser browser = new RecipeBrowser(recipes, settings, stations, factory, allMaterials,
                    base_ingredients, scanInp);
            browser.initCheckCycle();
            return browser;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        } catch (QueryException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public void newFactory(String new_factory) {
        File file = new File(FACTORY_FOLDER + new_factory);
        try (Scanner scanner = new Scanner(file)) {
            HashMap<String, Setting> new_settings = new HashMap<String, Setting>();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                new_settings.put(setting.topic, setting);
            }
            scanner.close();
            this.settings = new_settings;
            this.factory = new_factory;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ArrayList<Recipe> findRecipes(String material) {
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        for (Recipe recipe : this.recipes) {
            if (recipe.hasOutput(material)) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public void getMachinesIn(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        boolean first = true;
        for (Recipe recipe : recipe_order) {
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
                System.out.println(String.format("Using Recipe: %s\nNeed: %.0f %ss", recipe.toStringSpecific(station),
                        machines, station.name));
            }
        }
        reset();
    }

    public <T> void searchQuery(String search_string1, boolean logic_is_and, String search_string2,
            boolean has_second_argument, Iterable<String> searching_list, boolean is_setting) {
        int count = 0;
        for (String material : searching_list) {
            if (material.indexOf(search_string1) != -1) {
                if (has_second_argument && logic_is_and && material.indexOf(search_string2) == -1) {
                    continue;
                }
                if (is_setting) {
                    material = settings.get(material).toString();
                }
                System.out.println(material);
                count++;
            }
            if (has_second_argument && !logic_is_and && material.indexOf(search_string2) != -1) {
                if (is_setting) {
                    material = settings.get(material).toString();
                }
                System.out.println(material);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("None.");
        }
    }

    public void getBasicIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        for (String material : resources.keySet()) {
            double quantity = resources.get(material);
            if (quantity >= 0.001 || quantity <= -0.001) {
                System.out.println(String.format("%s: %.3f", material, quantity));
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
                System.out.println(String.format("%s: %.3f", material, quantity));
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

    private Recipe getLightCrackingRecipe() {
        for (Recipe recipe : recipes) {
            if (recipe.hasOutput("petrol") && recipe.alt_name.equals("cracking")) {
                return recipe;
            }
        }
        System.err.println("Error: No light oil cracking recipe.");
        System.exit(1);
        return null;
    }

    private Recipe getHeavyCrackingRecipe() {
        for (Recipe recipe : recipes) {
            if (recipe.hasOutput("light_oil") && recipe.alt_name.equals("default")) {
                return recipe;
            }
        }
        System.err.println("Error: No heavy oil cracking recipe.");
        System.exit(1);
        return null;
    }

    private void crackOils(int prod_mod_level) {
        double extra_heavy = -resources.getOrDefault("heavy_oil", 0.0);
        double extra_light = -resources.getOrDefault("light_oil", 0.0);
        double extra_petrol = -resources.getOrDefault("petrol", 0.0);
        boolean needed_light = all_resources.getOrDefault("light_oil", 0.0) > 0;
        boolean needed_petrol = all_resources.getOrDefault("petrol", 0.0) > 0;
        Recipe heavy_recipe = getHeavyCrackingRecipe();
        Recipe light_recipe = getLightCrackingRecipe();
        String priority;
        if (needed_petrol && extra_petrol == 0) {
            priority = "petrol";
        } else if (needed_light && extra_light == 0) {
            priority = "light_oil";
        } else {
            return; // No cracking needed (cannot crack for heavy_oil)
        }
        Recipe recipe = null;
        int prio = -1;
        for (Recipe recipe_check : steps.keySet()) {
            if (recipe_check.hasOutput(priority)) {
                if (recipe_check.equals(heavy_recipe) || recipe_check.equals(light_recipe)) {
                    if (prio < 0) {
                        prio = 0;
                        recipe = recipe_check;
                    }
                } else {
                    int num_outputs = recipe_check.outputs.size();
                    if (prio < num_outputs) {
                        prio = num_outputs;
                        recipe = recipe_check;
                    }
                }
            }
        }
        if (recipe == null) {
            return;
        }
        crack(recipe, priority, extra_heavy, extra_light, extra_petrol, prod_mod_level);
        clearUnecessary(priority, prio, recipe, prod_mod_level);
    }

    private void crack(Recipe recipe, String output, double extra_heavy, double extra_light, double extra_petrol,
            int prod_mod_level) {
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        double heavy_out = 0;
        double light_out = 0;
        double petrol_out = 0;
        Recipe light_recipe = getLightCrackingRecipe();
        Station light_station = pickStation(light_recipe);
        double light_prod = getProductivity(light_recipe, light_station, prod_mod_level);
        Recipe heavy_recipe = getHeavyCrackingRecipe();
        Station heavy_station = pickStation(heavy_recipe);
        double heavy_prod = getProductivity(heavy_recipe, heavy_station, prod_mod_level);
        for (Material material : recipe.outputs) {
            double this_amount = material.quantity * productivity;
            if (material.name.equals("petrol")) {
                petrol_out = this_amount;
            } else if (material.name.equals("light_oil")) {
                light_out = this_amount;
            } else if (material.name.equals("heavy_oil")) {
                heavy_out = this_amount;
            }
        }
        double heavy_to_light_out;
        if (light_out != 0) {
            heavy_to_light_out = heavy_out / light_out;
        } else {
            heavy_to_light_out = 0;
        }
        double light_to_petrol_out = light_out / petrol_out;
        double heavy_func = Math.max(
                (extra_heavy * 30 - heavy_to_light_out * 30 * light_to_petrol_out * extra_petrol
                        + light_to_petrol_out * 20 * light_prod * (extra_heavy - heavy_to_light_out * extra_light))
                        / (40 * 30 + light_to_petrol_out * 20 * light_prod
                                * (40 + heavy_to_light_out * 30 * heavy_prod)),
                0);
        double light_func = Math.max((40 * extra_light + extra_heavy * 30 * heavy_prod
                - light_to_petrol_out * extra_petrol * (40 + heavy_to_light_out * 30 * heavy_prod))
                / (40 * 30 + light_to_petrol_out * 20 * light_prod * (40 + heavy_to_light_out * 30 * heavy_prod)),
                0);
        double heavy_cracks = Math
                .max((extra_heavy - heavy_to_light_out * extra_light + heavy_to_light_out * light_func * 30)
                        / (40 + heavy_to_light_out * 30 * heavy_prod), 0);
        double light_cracks = Math
                .max((extra_light + heavy_func * 30 * heavy_prod - light_to_petrol_out * extra_petrol)
                        / (30 + light_to_petrol_out * 20 * light_prod), 0);
        if (!steps.containsKey(heavy_recipe) && heavy_cracks > 0) {
            recipe_order.add(heavy_recipe);
        }
        steps.put(heavy_recipe, steps.getOrDefault(heavy_recipe, 0.0) + heavy_cracks);
        for (Material material : heavy_recipe.outputs) {
            double this_amount = heavy_cracks * material.quantity * light_prod;
            double current_amount = resources.getOrDefault(material.name, 0.0);
            resources.put(material.name, current_amount - this_amount);
        }
        for (Material material : heavy_recipe.inputs) {
            double this_amount = heavy_cracks * material.quantity;
            double current_amount = resources.get(material.name);
            if (current_amount + this_amount > 0 && current_amount < 0) {
                System.out.println("Function failed.");
                return;
            }
            resources.put(material.name, current_amount + this_amount);
            all_resources.put(material.name, all_resources.getOrDefault(material.name, 0.0) + this_amount);
        }
        if (!steps.containsKey(light_recipe) && light_cracks > 0) {
            recipe_order.add(light_recipe);
        }
        steps.put(light_recipe, steps.getOrDefault(light_recipe, 0.0) + light_cracks);
        for (Material material : light_recipe.outputs) {
            double this_amount = light_cracks * material.quantity * light_prod;
            double current_amount = resources.getOrDefault(material.name, 0.0);
            resources.put(material.name, current_amount - this_amount);
        }
        for (Material material : light_recipe.inputs) {
            double this_amount = light_cracks * material.quantity;
            double current_amount = resources.get(material.name);
            if (current_amount + this_amount > 0 && current_amount < 0) {
                System.out.println("Function failed.");
                return;
            }
            resources.put(material.name, current_amount + this_amount);
            all_resources.put(material.name, all_resources.getOrDefault(material.name, 0.0) + this_amount);
        }
    }

    private boolean clearUnecessary(String output, double amount, Recipe recipe, int prod_mod_level) {
        boolean cleared_anything = false;
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        boolean first = true;
        double undos = 0;
        for (Material material : recipe.outputs) {
            double this_amount = -material.quantity * productivity;
            double current_amount = resources.get(material.name);
            if (first) {
                undos = current_amount / this_amount;
                first = false;
            } else {
                undos = Math.max(0, Math.min(undos, current_amount / this_amount));
            }
        }
        double recipe_count = steps.getOrDefault(store_recipe, 0.0);
        if (undos >= recipe_count) {
            undos = recipe_count;
            recipe_order.remove(recipe);
        }
        if (undos > 0) {
            cleared_anything = true;
        }
        steps.put(store_recipe, recipe_count - undos);
        for (Material material : recipe.outputs) {
            double this_amount = undos * material.quantity * productivity;
            double current_amount = resources.get(material.name);
            resources.put(material.name, current_amount + this_amount);
        }
        for (Material material : recipe.inputs) {
            double this_amount = undos * material.quantity;
            double current_amount = resources.get(material.name);
            resources.put(material.name, current_amount - this_amount);
            double all_current = all_resources.getOrDefault(material.name, 0.0);
            all_resources.put(material.name, all_current - this_amount);
            if (current_amount - this_amount < 0) {
                clearUnecessary(material.name, current_amount + this_amount, prod_mod_level);
            }
        }
        return cleared_anything;
    }

    private boolean clearUnecessary(String output, double amount, int prod_mod_level) {
        boolean cleared_anything = false;
        for (Recipe recipe : steps.keySet()) {
            if (!recipe.hasOutput(output) || steps.get(recipe) <= 0) {
                continue;
            }
            cleared_anything = clearUnecessary(output, amount, recipe, prod_mod_level) || cleared_anything;
        }
        return cleared_anything;
    }

    private void printRecipePath(int prod_mod_level) {
        boolean first = true;
        for (Recipe recipe : recipe_order) {
            if (!first) {
                System.out.println();
            } else {
                first = false;
            }
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
        crackOils(prod_mod_level);
    }

    private void addRecipes(String output, double amount, int prod_mod_level) {
        Recipe recipe = pickRecipe(output);
        if (recipe == null || (resources.containsKey(output) && resources.get(output) <= 0)) {
            required_resources.remove(output);
            return;
        }
        if (!steps.containsKey(recipe)) {
            recipe_order.add(recipe);
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        double recipe_completions = amount / (recipe.amountOutput(output) * productivity);
        double recipe_count = steps.getOrDefault(store_recipe, 0.0) + recipe_completions;
        steps.put(store_recipe, recipe_count);
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
        required_resources = new ArrayList<>();
        recipe_order = new ArrayList<>();
    }

    private double getProductivity(Recipe recipe, Station station, int prod_mod_level) {
        if (recipe.can_prod) {
            return station.getProd(prod_mod_level);
        }
        return station.getProd(0);

    }

    private <T> int giveOptions(Iterable<T> list, String material) {
        return giveOptions("Decision concerning '" + material + "' must be made between:", list, material);
    }

    public <T> int giveOptions(String heading, Iterable<T> list, String material) {
        System.out.println(heading);
        int counter = 0;
        for (T element : list) {
            System.out.print(counter + ": ");
            System.out.println(element);
            counter++;
        }
        if (base_ingredients.contains(material)) {
            System.out.print(counter + ": ");
            System.out.println("Basic Resource");
            counter++;
        }
        return counter;
    }

    boolean giveYesNo() {
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 2) == 1;

    }

    public int getUserInt(int min, int max) {
        boolean first = true;
        int userIn = -1;
        do {
            if (!first) {
                System.out.println("Please select a valid option (in the range [" + min + "," + (max - 1) + "]).");
            } else {
                first = false;
            }
            System.out.print("Choice: ");
            try {
                userIn = stdin.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("That is not an integer.");
            }
            stdin.nextLine();
        } while (userIn < min || userIn >= max);
        return userIn;
    }

    private String hasStation(String station) {
        System.out.println("Does this factory use " + station + "?");
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        if (getUserInt(0, 2) == 1) {
            return "yes";
        }
        return "no";
    }

    private int moduleLevel(String moduleType) {
        System.out.println("What level of " + moduleType + " module does this factory have?");
        System.out.println("0: None.");
        System.out.println("1: Level 1.");
        System.out.println("2: Level 2.");
        System.out.println("3: Level 3.");
        return getUserInt(0, 3);
    }

    public void listQuery(String item, boolean verbose) {
        if (item.equals("recipes")) {
            for (Recipe recipe : recipes) {
                System.out.println(recipe);
            }
            return;
        }
        if (item.equals("settings")) {
            ArrayList<Setting> sorted = new ArrayList<>(settings.values());
            Collections.sort(sorted, Comparator.comparing(Setting::getTopic));
            for (Setting setting : sorted) {
                System.out.println(setting);
            }
            return;
        }
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

    private String userChooseRecipe(ArrayList<Recipe> list, String material) {
        int counter = giveOptions(list, material);
        int userIn = getUserInt(0, counter);
        if (userIn == list.size()) {
            return "basic";
        }
        return list.get(userIn).alt_name;
    }

    public Recipe pickRecipe(String output) {
        return pickRecipe(output, findRecipes(output));
    }

    private Recipe pickRecipe(String material, ArrayList<Recipe> recipe_options) {
        if (recipe_options.size() == 0) {
            return null;
        }
        if (recipe_options.size() == 1 && !base_ingredients.contains(material)) {
            return recipe_options.get(0);
        }
        Setting setting = settings.get(material);
        Recipe recipe = null;
        if (setting == null) {
            String alt_name = userChooseRecipe(recipe_options, material);
            setting = new Setting(material, alt_name);
            addNewSetting(setting);
        }
        if (setting.value.equals("basic")) {
            return null;
        }
        for (Recipe r : recipe_options) {
            if (r.alt_name.equals(setting.value) || (r.alt_name == null && setting.value.equals("default"))) {
                recipe = r;
                break;
            }
        }
        return recipe;
    }

    private String getRecipeOrEmpty(String setting_name) {
        if (settings.containsKey(setting_name)) {
            return settings.get(setting_name).value;
        }
        return "";
    }

    public Station pickStation(Recipe recipe) {
        Station station = null;
        int highestPrio = -1;
        @SuppressWarnings("unchecked")
        ArrayList<String> allowedStations = (ArrayList<String>) recipe.stations.clone();
        for (String station_name : allowedStations) {
            Setting setting = settings.get(station_name);
            if (setting == null) {
                setting = new Setting(station_name, hasStation(station_name));
                addNewSetting(setting);
            }
            if (setting.value.equals("yes")) {
                Station search_station = stations.get(station_name);
                if (search_station == null) {
                    reset();
                    throw new StationNotFoundException("Error: Recipe uses station not found in station.txt.",
                            station_name, true);
                }
                if (search_station.priority > highestPrio) {
                    station = search_station;
                    highestPrio = search_station.priority;
                }
            }
        }
        if (station == null) {
            reset();
            throw new StationNotFoundException("Error: Factory does not have any required station.", recipe.toString(),
                    false);
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

    public void query(String line) {
        Query query = Parser.parseQuery(line, toggle_verbose);
        query.query(this);
    }

    public void initCheckCycle() {
        if (checkCycle()) {
            addNewSetting(new Setting("water", "basic"));
            addNewSetting(new Setting("coal", "basic"));
            addNewSetting(new Setting("metallic_chunk", "basic"));
            addNewSetting(new Setting("carbonic_chunk", "basic"));
            addNewSetting(new Setting("oxide_chunk", "basic"));
        }
    }

    private boolean checkCycle() {
        String water = getRecipeOrEmpty("water");
        String steam = getRecipeOrEmpty("steam");
        String acid = getRecipeOrEmpty("sulfuric_acid");
        String coal = getRecipeOrEmpty("coal");
        String carbon = getRecipeOrEmpty("carbon");
        String carbonic_chunk = getRecipeOrEmpty("carbonic_chunk");
        String metallic_chunk = getRecipeOrEmpty("metallic_chunk");
        String oxide_chunk = getRecipeOrEmpty("oxide_chunk");
        boolean cycle = false;
        if (water.equals("default") && steam.equals("boiling")) {
            cycle = true;
        }
        if (water.equals("default") && steam.equals("default") && acid.equals("default")) {
            cycle = true;
        }
        if (coal.equals("default") && carbon.equals("default")) {
            cycle = true;
        }
        if (carbonic_chunk.equals("metallic")) {
            if (metallic_chunk.equals("carbonic")) {
                cycle = true;
            } else if (metallic_chunk.equals("oxide") && oxide_chunk.equals("carbonic")) {
                cycle = true;
            }
        }
        if (carbonic_chunk.equals("oxide")) {
            if (oxide_chunk.equals("carbonic")) {
                cycle = true;
            } else if (oxide_chunk.equals("metallic") && metallic_chunk.equals("carbonic")) {
                cycle = true;
            }
        }
        if (metallic_chunk.equals("oxide")) {
            if (oxide_chunk.equals("metallic")) {
                cycle = true;
            }
        }
        return cycle;
    }

    private void addNewSetting(Setting setting) {
        addNewSetting(setting, true);
    }

    private void addNewSetting(Setting setting, boolean check_cycle) {
        Setting initial = settings.get(setting.topic);
        settings.put(setting.topic, setting);
        if (check_cycle) {
            if (checkCycle()) {
                settings.put(setting.topic, initial);
                throw new CycleException(setting.topic);
            }
        }
        try (FileWriter writer = new FileWriter(FACTORY_FOLDER + factory, true)) {
            writer.write("\n");
            writer.write(setting.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void updateSetting(String setting_name) {
        boolean setting_is_new = !settings.containsKey(setting_name);
        String old_value = "ERROR";
        if (!setting_is_new) {
            old_value = settings.get(setting_name).value;
        }
        Setting new_setting;
        if (all_materials.contains(setting_name)) {
            ArrayList<Recipe> possible_recipes = findRecipes(setting_name);
            new_setting = new Setting(setting_name, userChooseRecipe(possible_recipes, setting_name));
            if (!setting_is_new) {
                changeSetting(new_setting);
            } else {
                addNewSetting(new_setting);
            }
        } else if (stations.containsKey(setting_name)) {
            Station station = stations.get(setting_name);
            new_setting = new Setting(setting_name, hasStation(station.name));
            if (!setting_is_new) {
                changeSetting(new_setting);
            } else {
                addNewSetting(new_setting);
            }
        } else {
            System.err.println("Error: No possible setting fits the name '" + setting_name + "'.");
            return;
        }
        if (setting_is_new) {
            System.out.println("Setting '" + settings.get(setting_name) + "' was added.");
            return;
        } else {
            System.out.println(
                    "Setting '" + setting_name + "' was updated from '" + old_value + "' to '" + new_setting.value
                            + "'.");
        }
    }

    public void changeSetting(Setting setting) {
        Setting old_setting = settings.get(setting.topic);
        settings.put(setting.topic, setting);
        if (checkCycle()) {
            settings.put(setting.topic, old_setting);
            reset();
            throw new CycleException(setting.topic);
        }
        try (FileWriter writer = new FileWriter(FACTORY_FOLDER + factory)) {
            boolean first = true;
            for (Setting set : settings.values()) {
                if (!first) {
                    writer.write("\n");
                } else {
                    first = false;
                }
                writer.write(set.toString());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

class StationNotFoundException extends QueryException {
    String searchStationName;
    boolean not_real_station;

    public StationNotFoundException(String message, String search_station_name, boolean not_real_station) {
        super(message);
        this.searchStationName = search_station_name;
        this.not_real_station = not_real_station;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        if (not_real_station) {
            builder.append("Searched for '" + searchStationName + "'.");
        } else {
            builder.append("\n" + searchStationName);
        }
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
        super("Error: New '" + material + "' setting created a cycle.");
    }

    public String getMessage() {
        String message = super.getMessage();
        message += "\nCyclic recipes include:\n\ncoal <-> carbon\nwater <-> steam (<-> sulfuric_acid)\nmetallic_chunk <-> carbonic_chunk <-> oxide_chunk";
        return message;
    }
}