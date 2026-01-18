public class HelpQuery extends Query {
    String command;

    public HelpQuery(String command, boolean verbose) {
        this.command = command;
        this.verbose = verbose;
    }

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
                printVerbose();
                printHelp();
                printExit();
        }
    }

    private void printGet() {
        System.out.println("[verbose] get <material1> in [amount] <material2> [prod <productivity_module_level>]");
        System.out.println("    Returns the amount of material1 in [amount *] material2.");
        System.out.println("    Prints the construction path if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println("    Using input 'base' returns all base ingredients.");
        System.out.println("    Using input 'all' returns all ingredients.");
        System.out.println();
    }

    private void printMachines() {
        System.out.println("[verbose] machines in [quantity] <material> [prod <productivity_module_level>]");
        System.out.println(
                "    Returns the minimum number of machines needed to achieve\n    quantity (defaults to 1) material per second.");
        System.out.println("    Prints the recipes used and shows decimal places for machine number if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println();
    }

    private void printList() {
        System.out.println("list <material>");
        System.out.println("    Lists all recipes involving material.");
        System.out.println("    Using material 'recipes' returns all recipes.");
        System.out.println("    Using material 'settings' returns all settings.");
        System.out.println();
    }

    private void printSetting() {
        System.out.println("setting <setting_topic>");
        System.out.println("    Prints the setting.");
        System.out.println();
    }

    private void printTime() {
        System.out.println("[verbose] time <material> [prod <productivity_module_level>]");
        System.out.println("    Returns the amount of time it takes for the chosen recipe to complete.");
        System.out.println("    Prints the recipe details if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println();
    }

    private void printUpdate() {
        System.out.println("update <setting_topic> = <setting_value>");
        System.out.println("    Updates the setting file.");
        System.out.println("    Adds a new setting if the topic has none yet.");
        System.out.println();
    }

    private void printSearch() {
        System.out.println("search <word1> [<logical_operator> <word2>]");
        System.out.println("    Prints a list of materials containing word1.");
        System.out.println("    logical_operator can either be 'and' or 'or'.");
        System.out.println("    If the second argument is supplied, prints materials based on both words and logical_operator.");
        System.out.println();
    }

    private void printMath() {
        System.out.println("<number1> <operation> <number2>");
        System.out.println("    Prints the result of basic mathematical operations.");
        System.out.println();
    }

    private void printVerbose() {
        System.out.println("verbose");
        System.out.println("    Toggles verbose on/off.");
        System.out.println();
    }

    private void printHelp() {
        System.out.println("help [command]");
        System.out.println("    Lists all commands and usage with syntax.");
        System.out.println("    Lists specific command's usage and syntax if a command is included.");
        System.out.println();
    }

    private void printExit() {
        System.out.println("exit");
        System.out.println("    Ends the process.");
    }

}
