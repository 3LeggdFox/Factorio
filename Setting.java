public class Setting {
    String topic;
    String value;

    public Setting(String topic, String setting) {
        this.topic = topic;
        this.value = setting;
    }

    public String toString() {
        return topic + " = " + value;
    }
}
