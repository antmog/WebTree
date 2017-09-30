package tree.mvc.model;

import javax.json.Json;
import java.util.ArrayList;
import java.util.List;

public class CustomTreeNode {
    private String id;
    private String parent;
    private String text;
    private boolean children; // (haschildren?)

    public boolean getChildren() {
        return children;
    }

    public void setChildren(boolean children) {
        this.children = children;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        String json = Json.createObjectBuilder()
                .add("id", "" + this.id)
                .add("parent", this.parent)
                .add("text", this.text)
                .add("children", this.children)
                .build()
                .toString();
        return json;
    }
}
