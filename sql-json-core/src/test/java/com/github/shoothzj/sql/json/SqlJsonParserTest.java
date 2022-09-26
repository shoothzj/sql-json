package com.github.shoothzj.sql.json;

import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.Test;

class SqlJsonParserTest {

    private final SqlJsonParser parser = new SqlJsonParser();

    @Test
    public void parseSqlTestCase1() throws SqlParseException {
        parser.parseSql("SELECT * FROM EXAMPLE");
    }

}
