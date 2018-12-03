public class Item {
    protected String name;
    protected String id;
    protected String uri;

    public Item() {
    }

    public Item(String name, String id, String uri) {
        this.name = name;
        this.id = id;
        this.uri = uri;
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof Artist) {
            Artist a = (Artist) o;
            if (this.name.equals(a.getName()) && this.uri.equals(a.getUri())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
