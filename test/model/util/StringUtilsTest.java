package model.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void camelCaseValid() {
        assertEquals("Test", StringUtils.capitalize("tEsT"));
        assertEquals("Test Multi Word", StringUtils.capitalize("tEST Multi WorD"));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Test  Test", StringUtils.capitalize("tEST  tesT"));
    }

    @Test
    void camelCaseWord() {
        assertEquals("Test", StringUtils.camelCaseWord("tEsT"));
        assertEquals("Test Multi Word", StringUtils.camelCaseWord("tEST Multi WorD"));
        assertEquals("", StringUtils.camelCaseWord(""));
    }
}