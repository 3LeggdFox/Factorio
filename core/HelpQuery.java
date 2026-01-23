package core;

/**
 * HelpQuery
 * Class to manage queries about the commands of the interface
 * 
 * @version 1.0
 */
public class HelpQuery extends Query {
    String command;

    /**
     * Constructor
     * 
     * @param command Specifies which command to detail. Can be null (details all
     *                commands)
     */
    public HelpQuery(String command) {
        this.command = command;
    }

    /**
     * Details the specified command usage (or all if command == null)
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        if (command == null) {
            command = "default";
        }
        switch (command) {
            case "get":
                printGet();
                break;
            case "machines":
                printMachines();
                break;
            case "list":
                printList();
                break;
            case "setting":
                printSetting();
                break;
            case "time":
                printTime();
                break;
            case "update":
                printUpdate();
                break;
            case "search":
                printSearch();
                break;
            case "math":
                printMath();
                break;
            case "factory":
                printFactory();
                break;
            case "verbose":
                printVerbose();
                break;
            case "help":
                printHelp();
                break;
            case "exit":
                printExit();
                System.out.println();
                break;
            default:
                System.out.println("Some Commands:");
                System.out.println();
                printGet();
                printMachines();
                printList();
                printSetting();
                printTime();
                printUpdate();
                printSearch();
                printMath();
                printFactory();
                printVerbose();
                printHelp();
                printExit();
        }
    }

    /**
     * Helper function to print details of the 'get' command
     */
    private void printGet() {
        System.out.println("[verbose] get <material1> in [amount] <material2> [prod <productivity_module_level>]");
        System.out.println("    Returns the amount of material1 in [amount *] material2.");
        System.out.println("    Prints the construction path if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println("    Using input 'base' returns all base ingredients.");
        System.out.println("    Using input 'all' returns all ingredients.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'machines' command
     */
    private void printMachines() {
        System.out.println("[verbose] machines in [<quantity>] <material> [prod <productivity_module_level>]");
        System.out.println(
                "    Returns the minimum number of machines needed to achieve\n    quantity (defaults to 1) material per second.");
        System.out.println("    Prints the recipes used and shows decimal places for machine number if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'list' command
     */
    private void printList() {
        System.out.println("list <material>");
        System.out.println("    Lists all recipes involving material.");
        System.out.println("    Using material 'recipes' returns all recipes.");
        System.out.println("    Using material 'settings' returns all settings.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'setting' command
     */
    private void printSetting() {
        System.out.println("setting <setting_topic>");
        System.out.println("    Prints the setting.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'time' command
     */
    private void printTime() {
        System.out.println("[verbose] time for <material> [prod <productivity_module_level>]");
        System.out.println("    Returns the amount of time it takes for the chosen recipe to complete.");
        System.out.println("    Prints the recipe details if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'update' command
     */
    private void printUpdate() {
        System.out.println("update <setting_topic>");
        System.out.println("    Prompts the user to choose the setting for the given topic.");
        System.out.println("    Will replace any previous setting for the topic.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'search' command
     */
    private void printSearch() {
        System.out.println("search <word1> [<logical_operator> <word2>]");
        System.out.println("    Prints a list of materials containing word1.");
        System.out.println("    logical_operator can either be 'and' or 'or'.");
        System.out.println(
                "    If the second argument is supplied, prints materials based on both words and logical_operator.");
        System.out.println();
    }

    /**
     * Helper function to print details of the math command
     */
    private void printMath() {
        System.out.println("<number1> <operation> <number2>");
        System.out.println("    Prints the result of basic mathematical operations.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'factory' command
     */
    private void printFactory() {
        System.out.println("factory [<command> <target*>]");
        System.out.println("    Prints the name of the factory.");
        System.out.println("    Using a command can perform various functions as listed below.");
        System.out.println();
        System.out.println("        new <new_factory_name>");
        System.out.println("            Makes a new factory with the given name.");
        System.out.println();
        System.out.println("        change <new_factory_name>");
        System.out.println("            Moves to the given factory.");
        System.out.println();
        System.out.println("        rename <new_factory_name>");
        System.out.println("            Renames the current factory to the given name.");
        System.out.println();
        System.out.println("        copy <new_factory_name>");
        System.out.println("            Copies the current factory to a new one with the given name.");
        System.out.println();
        System.out.println("        list [templates]");
        System.out.println("            *target is not required.");
        System.out.println("            Prints the names of all factories.");
        System.out.println("            Prints the names of all templates if 'templates' is appended to command.");
        System.out.println();
        System.out.println("        template <new_template_name>");
        System.out
                .println("            Creates a new template under the given name with the current factory settings.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'verbose' command
     */
    private void printVerbose() {
        System.out.println("verbose");
        System.out.println("    Toggles verbose on/off.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'help' command
     */
    private void printHelp() {
        System.out.println("help [<command>]");
        System.out.println("    Lists all commands and usage with syntax.");
        System.out.println("    Lists specific command's usage and syntax if a command is included.");
        System.out.println();
    }

    /**
     * Helper function to print details of the 'exit' command
     */
    private void printExit() {
        System.out.println("exit");
        System.out.println("    Ends the process.");
    }
}