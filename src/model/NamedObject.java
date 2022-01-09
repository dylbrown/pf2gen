package model;

import model.data_managers.sources.Source;

public interface NamedObject {
    String getName();

    /**
     * Allows for getName to be overridden, while still maintaining access to the original value
     *
     * @return the stored name string
     */
    String getRawName();

    String getDescription();

    /**
     * Allows for getDescription to be overridden, while still maintaining access to the original value
     *
     * @return the stored description string
     */
    String getRawDescription();

    int getPage();

    Source getSourceBook();

    String getSource();
}
