package core;

import java.util.ArrayList;
import java.util.HashMap;

public class RecipePath {
    HashMap<Recipe, Double> steps;
    ArrayList<Recipe> recipe_order;
    HashMap<String, Double> resources;
    HashMap<String, Double> all_resources;
    ArrayList<String> required_resources;
    RecipeBrowser browser;
    int prod_mod_level;

    enum CompleteType {
        ADD,
        CLEAR,
        CRACK
    }

    public RecipePath() {
        this.steps = new HashMap<>();
        this.recipe_order = new ArrayList<>();
        this.resources = new HashMap<>();
        this.all_resources = new HashMap<>();
        this.required_resources = new ArrayList<>();
    }

    public RecipePath(RecipeBrowser browser, String output, double amount, int prod_mod_level) {
        this();
        this.browser = browser;
        this.prod_mod_level = prod_mod_level;

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

    public double get(String resource) {
        return all_resources.getOrDefault(resource, 0.0);
    }

    public void printRecipePath(int prod_mod_level) {
        boolean first = true;
        for (Recipe recipe : recipe_order) {
            if (!first) {
                System.out.println();
            } else {
                first = false;
            }
            Station station = browser.pickStation(recipe);
            System.out.println(recipe.toStringSpecificVerbose(station, steps.get(recipe), prod_mod_level));
        }
        System.out.println();
    }

    public void printIngredients(String all_or_base) {
        HashMap<String, Double> list = null;
        if (all_or_base.equals("all")) {
            list = all_resources;
        } else if (all_or_base.equals("base")) {
            list = resources;
        } else {
            System.err.println("Error: printIngredients(String all_or_base) called with argument:\n" + all_or_base);
            System.exit(1);
        }
        printOrdered(list);
    }

    private void printOrdered(HashMap<String, Double> list) {
        for (String material : browser.sorted_materials) {
            double quantity = list.getOrDefault(material, 0.0);
            if (quantity >= 0.001 || quantity <= -0.001) {
                System.out.println(String.format("%s: %.3f", material, quantity));
            }
        }
    }

    public void printMachines(boolean verbose) {
        boolean first = true;
        for (Recipe recipe : recipe_order) {
            if (!first) {
                System.out.println();
            } else {
                first = false;
            }
            Station station = browser.pickStation(recipe);
            double crafting_time = recipe.getCraftingTime(station, prod_mod_level);
            double recipe_completions = steps.get(recipe);
            double machines = recipe_completions * crafting_time;
            if (verbose) {
                StringBuilder input_string = new StringBuilder();
                String station_string = recipe.toStringSpecificVerbose(station, recipe_completions, prod_mod_level);
                System.out.println(String.format("%s\nInput: %s\nNeed: %.3f %ss", station_string, input_string.toString(), machines, station.name));
            } else {
                machines = Math.ceil(machines);
                System.out.println(String.format("Using Recipe: %s\nNeed: %.0f %ss", recipe.toStringSpecific(station),
                        machines, station.name));
            }
        }
    }

    private void addRecipes(String output, double amount, int prod_mod_level) {
        Recipe recipe = browser.pickRecipe(output);
        if (recipe == null || (resources.containsKey(output) && resources.get(output) <= 0)) {
            required_resources.remove(output);
            return;
        }
        if (!steps.containsKey(recipe)) {
            recipe_order.add(recipe);
        }
        Station station = browser.pickStation(recipe);
        double productivity = station.getProductivity(recipe, prod_mod_level);
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        double recipe_completions = amount / (recipe.amountOutput(output) * productivity);
        double recipe_count = steps.getOrDefault(store_recipe, 0.0) + recipe_completions;
        steps.put(store_recipe, recipe_count);
        completeRecipe(recipe, productivity, recipe_completions, CompleteType.ADD);
    }

    private void completeRecipe(Recipe recipe, double productivity, double times, CompleteType complete_type) {
        for (Material input : recipe.inputs) {
            double amount = input.quantity * times;
            double current_resource = resources.getOrDefault(input.name, 0.0);
            double all_resource = all_resources.getOrDefault(input.name, 0.0);
            resources.put(input.name, current_resource + amount);
            all_resources.put(input.name, all_resource + amount);
            switch (complete_type) {
                case ADD:
                    if (current_resource <= 0 && current_resource + amount > 0) {
                        required_resources.add(input.name);
                    }
                    break;
                case CLEAR:
                    if (current_resource + amount < 0) {
                        clearUnecessary(input.name, current_resource + amount, prod_mod_level);
                    }
                    break;
                case CRACK:
                    if (current_resource + amount > 0 && current_resource < 0) {
                        System.err.println("Error: Cracking function failed.");
                        System.exit(1);
                    }
                    break;
            }
        }
        for (Material output : recipe.outputs) {
            double amount = output.quantity * times * productivity;
            double current_resource = resources.getOrDefault(output.name, 0.0);
            resources.put(output.name, current_resource - amount);
            if (complete_type == CompleteType.ADD && current_resource - amount <= 0) {
                required_resources.remove(output.name);
            }
        }
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

    private boolean clearUnecessary(String output, double amount, Recipe recipe, int prod_mod_level) {
        boolean cleared_anything = false;
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        Station station = browser.pickStation(recipe);
        double productivity = station.getProductivity(recipe, prod_mod_level);
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
        completeRecipe(recipe, productivity, -undos, CompleteType.CLEAR);
        return cleared_anything;
    }

    private void crackOils(int prod_mod_level) {
        double extra_heavy = -resources.getOrDefault("heavy_oil", 0.0);
        double extra_light = -resources.getOrDefault("light_oil", 0.0);
        double extra_petrol = -resources.getOrDefault("petrol", 0.0);
        boolean needed_light = all_resources.getOrDefault("light_oil", 0.0) > 0;
        boolean needed_petrol = all_resources.getOrDefault("petrol", 0.0) > 0;
        Recipe heavy_recipe = browser.heavy_crack_recipe;
        Recipe light_recipe = browser.light_crack_recipe;
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
        Station station = browser.pickStation(recipe);
        double productivity = station.getProductivity(recipe, prod_mod_level);
        double heavy_out = 0;
        double light_out = 0;
        double petrol_out = 0;
        Recipe light_recipe = browser.light_crack_recipe;
        Station light_station = browser.pickStation(light_recipe);
        double light_prod = light_station.getProductivity(light_recipe, prod_mod_level);
        Recipe heavy_recipe = browser.heavy_crack_recipe;
        Station heavy_station = browser.pickStation(heavy_recipe);
        double heavy_prod = heavy_station.getProductivity(heavy_recipe, prod_mod_level);
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
        double light_to_petrol_out;
        if (petrol_out != 0) {
            light_to_petrol_out = light_out / petrol_out;
        } else {
            light_to_petrol_out = 0;
        }
        double heavy_cracks = calculateHeavyCracks(extra_heavy, extra_light, extra_petrol, heavy_to_light_out,
                light_to_petrol_out, heavy_prod, light_prod);
        double light_cracks = calculateLightCracks(extra_heavy, extra_light, extra_petrol, heavy_to_light_out,
                light_to_petrol_out, heavy_prod, light_prod);
        if (!steps.containsKey(heavy_recipe) && heavy_cracks > 0) {
            recipe_order.add(heavy_recipe);
        }
        steps.put(heavy_recipe, steps.getOrDefault(heavy_recipe, 0.0) + heavy_cracks);
        completeRecipe(heavy_recipe, heavy_prod, heavy_cracks, CompleteType.CRACK);
        if (!steps.containsKey(light_recipe) && light_cracks > 0) {
            recipe_order.add(light_recipe);
        }
        steps.put(light_recipe, steps.getOrDefault(light_recipe, 0.0) + light_cracks);
        completeRecipe(light_recipe, light_prod, light_cracks, CompleteType.CRACK);
    }

    private double calculateHeavyCracks(double extra_heavy, double extra_light, double extra_petrol,
            double heavy_to_light_out, double light_to_petrol_out, double heavy_prod, double light_prod) {
        double light_func = Math.max((40 * extra_light + extra_heavy * 30 * heavy_prod
                - light_to_petrol_out * extra_petrol * (40 + heavy_to_light_out * 30 * heavy_prod))
                / (40 * 30 + light_to_petrol_out * 20 * light_prod * (40 + heavy_to_light_out * 30 * heavy_prod)),
                0);
        double heavy_cracks = Math
                .max((extra_heavy - heavy_to_light_out * extra_light + heavy_to_light_out * light_func * 30)
                        / (40 + heavy_to_light_out * 30 * heavy_prod), 0);
        return heavy_cracks;
    }

    private double calculateLightCracks(double extra_heavy, double extra_light, double extra_petrol,
            double heavy_to_light_out, double light_to_petrol_out, double heavy_prod, double light_prod) {
        double heavy_func = Math.max(
                (extra_heavy * 30 - heavy_to_light_out * 30 * light_to_petrol_out * extra_petrol
                        + light_to_petrol_out * 20 * light_prod * (extra_heavy - heavy_to_light_out * extra_light))
                        / (40 * 30 + light_to_petrol_out * 20 * light_prod
                                * (40 + heavy_to_light_out * 30 * heavy_prod)),
                0);
        double light_cracks = Math
                .max((extra_light + heavy_func * 30 * heavy_prod - light_to_petrol_out * extra_petrol)
                        / (30 + light_to_petrol_out * 20 * light_prod), 0);
        return light_cracks;
    }
}
