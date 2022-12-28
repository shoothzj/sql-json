package io.github.shoothzj.sql.json.core;

import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.Test;

class SqlJsonParserTest {

    private final SqlJsonParser parser = new SqlJsonParser();

    @Test
    public void parseSqlTestCase1() throws SqlParseException {
        parser.parseSql("SELECT * FROM EXAMPLE");
    }

    @Test
    public void parseSqlTestCase2() throws SqlParseException {
        parser.parseSql("SELECT * FROM EXAMPLE WHERE xx.yyy contains hzj");
    }

}
