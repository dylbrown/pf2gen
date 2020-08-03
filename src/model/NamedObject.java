package model;

public abstract class NamedObject {
    private final String name, description;
    private final int pageNo;

    protected NamedObject(Builder builder) {
        name = builder.name;
        description = builder.description;
        pageNo = builder.pageNo;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPageNo() {
        return pageNo;
    }

    public static abstract class Builder {
        private String name = "";
        private String description = "";
        private int pageNo = -1;

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPageNo(int pageNo) {
            this.pageNo = pageNo;
        }
    }
}
