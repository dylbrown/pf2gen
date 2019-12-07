package model.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void camelCaseValid() {
        assertEquals("Test", StringUtils.camelCase("tEsT"));
        assertEquals("Test Multi Word", StringUtils.camelCase("tEST Multi WorD"));
        assertEquals("", StringUtils.camelCase(""));
        assertEquals("Test  Test", StringUtils.camelCase("tEST  tesT"));
    }

    @Test
    void camelCaseWord() {
        assertEquals("Test", StringUtils.camelCaseWord("tEsT"));
        assertEquals("Test multi word", StringUtils.camelCaseWord("tEST Multi WorD"));
        assertEquals("", StringUtils.camelCaseWord(""));
    }
}