public class UpdateSettingQuery extends Query {
    String topic;

    public UpdateSettingQuery(String topic, boolean verbose) {
        this.topic = topic;
    }

    public void query(RecipeBrowser browser) {
        browser.changeSetting(topic);
    }
}
