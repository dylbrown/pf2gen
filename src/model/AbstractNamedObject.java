package model;

import model.util.StringUtils;

import java.util.Objects;

public abstract class AbstractNamedObject implements NamedObject {
    private final String name, description, sourceBook;
    private final int page;

    protected AbstractNamedObject(Builder builder) {
        name = builder.name;
        description = builder.description;
        page = builder.pageNo;
        sourceBook = builder.sourceBook;
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

    public String getSourceBook() {
        return sourceBook;
    }

    public String getSource() {
        if(page == -1) return StringUtils.intialism(sourceBook);
        return StringUtils.intialism(sourceBook) + " pg. " + page;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNamedObject that = (AbstractNamedObject) o;
        return page == that.page &&
                name.equals(that.name) &&
                Objects.equals(description, that.description) &&
                sourceBook.equals(that.sourceBook);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, sourceBook, page);
    }

    public static abstract class Builder {
        private String name = "";
        private String description = "";
        private String sourceBook = "Core Rulebook"; // TODO: Collect this in loader
        private int pageNo = -1;

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

        public void setSourceBook(String sourceBook) {
            this.sourceBook = sourceBook;
        }
    }
}
