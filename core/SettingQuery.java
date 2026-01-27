package core;

public class SettingQuery extends Query {
    String topic;

    public SettingQuery(String topic, boolean verbose) {
        this.topic = topic;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        if (browser.settings.has(topic)) {
            System.out.println(browser.settings.get(topic));
        } else {
            System.out.println("Setting not found.");
        }
    }
}
