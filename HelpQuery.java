public class HelpQuery extends Query {
    public HelpQuery(boolean verbose) {
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        System.out.println("Some Commands:");
        System.out.println();
        System.out.println("[verbose] get <material1> in <material2> [prod <productivity_module_level>]");
        System.out.println("    Returns the amount of material1 in material2.");
        System.out.println("    Prints the construction path if verbose.");
        System.out.println("    Uses productivity modules if prod <integer> is included.");
        System.out.println();
        System.out.println("list <material>");
        System.out.println("    Lists all recipes involving material.");
        System.out.println();
        System.out.println("setting <setting_topic>");
        System.out.println("    Prints the setting.");
        System.out.println();
        System.out.println("update <setting_topic> = <setting_value>");
        System.out.println("    Updates the setting file.");
        System.out.println("    Adds a new setting if the topic has none yet.");
        System.out.println();
        System.out.println("help");
        System.out.println("    Lists all commands and usage with syntax.");
        System.out.println();
        System.out.println("exit");
        System.out.println("    Ends the process.");
    }
}
