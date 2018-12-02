public class Item {
    protected String name;
    protected String id;
    protected String uri;

    public Item() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Item(String name, String id, String uri) {
        this.name = name;
        this.id = id;
        this.uri = uri;
    }
}
