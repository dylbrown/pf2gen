package model;

import model.data_managers.sources.Source;
import model.util.StringUtils;

import java.util.Objects;

public abstract class AbstractNamedObject implements NamedObject {
    private final String name, description;
    private final Source source;
    private final int page;

    protected AbstractNamedObject(Builder builder) {
        name = builder.name;
        description = builder.description;
        page = builder.pageNo;
        source = builder.source;
    }

    public String getName() {
        return name;
    }

    /**
    * Allows for getName to be overridden, while still maintaining access to the original value
    * @return the stored name string
    * */
    public String getRawName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getDescription() {
        return description;
    }

    /**
     * Allows for getDescription to be overridden, while still maintaining access to the original value
     * @return the stored description string
     * */
    public String getRawDescription() {
        return description;
    }

    public int getPage() {
        return page;
    }

    public Source getSourceBook() {
        return source;
    }

    public String getSource() {
        if(source == null) return "N/A";
        if(page == -1) return StringUtils.intialism(source.getName());
        return StringUtils.intialism(source.getName()) + " pg. " + page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNamedObject that = (AbstractNamedObject) o;
        return page == that.page &&
                name.equals(that.name) &&
                Objects.equals(description, that.description) &&
                source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, source, page);
    }

    public static abstract class Builder {
        private String name = "";
        private String description = "";
        protected final Source source;
        private int pageNo = -1;

        public Builder(Source source) {
            this.source = source;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPage(int pageNo) {
            this.pageNo = pageNo;
        }
    }
}
