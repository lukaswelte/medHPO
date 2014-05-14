package Main.model;

/**
 * Created by lukas on 17.06.13.
 */
public class Term {

    public String name;
    public String tag;
    public String id;

    public int startIndex;
    public int endIndex;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.getName();
    }


    public String jsonDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("start:");
        builder.append(this.getStartIndex());
        builder.append(",end:");
        builder.append(this.getEndIndex());
        builder.append(",id:");
        builder.append(this.getId());
        builder.append("}");
        return builder.toString();
    }
}
