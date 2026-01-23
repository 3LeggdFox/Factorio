package core;

public class ToggleVerboseQuery extends Query {
    
    public ToggleVerboseQuery() {}

    public void query(RecipeBrowser browser)
    {
        browser.toggle_verbose = !browser.toggle_verbose;
        if (browser.toggle_verbose)
        {
            System.out.println("Verbose toggled: ON");
        } else 
        {
            System.out.println("Verbose toggled: OFF");
        }
    }
}
