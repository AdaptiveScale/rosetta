package com.adaptivescale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import queryhelper.service.AIService;


public class AIServiceTest {

    @Test
    void testIsSelectStatementGoodCase() {
        String goodQuery = "SELECT * FROM table_name;";
        assertTrue(AIService.isSelectStatement(goodQuery));
    }

    @Test
    void testIsSelectStatementBadCase() {
        String badQuery = "UPDATE table_name SET column_name = value WHERE condition;";
        assertFalse(AIService.isSelectStatement(badQuery));
    }

}
