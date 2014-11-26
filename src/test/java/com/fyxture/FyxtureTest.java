package com.fyxture;

import org.junit.Test;
import org.junit.Assert;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.After;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import org.flywaydb.core.Flyway;

public class FyxtureTest {
  private static Logger logger = Logger.getLogger(FyxtureTest.class);
  private Connection connection = null;
  private Statement statement = null;
  private static Flyway flyway = null;

  @Before public void init() throws Throwable {
    reset();
    Class.forName("org.h2.Driver").newInstance();
    connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/blog", "sa", null);
    statement = connection.createStatement();
  }

  private void reset() throws Throwable {
    if(flyway == null){
      flyway = new Flyway();
      flyway.setDataSource("jdbc:h2:tcp://localhost/~/blog", "sa", null);
    }
    flyway.clean();
    flyway.migrate();
  }

  @After public void close() throws Throwable {
    try{
      statement.close();
      connection.close();
    }catch(Throwable t){
      statement = null;
      connection = null;
    }
  }

  @Test public void clear() throws Throwable {
    create();
    Fyxture.clear();
    assertCleaned();
  }

  @Test public void count() throws Throwable {
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(0));
    create();
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(1));
  }

  @Test public void insert() throws Throwable {
    Fyxture.insert("livro");
    assertHave();
  }

  private void create() throws Throwable {
    statement.executeUpdate("INSERT INTO LIVRO (ID, VERSION, ANO, TITULO) VALUES (1, 0, 1885, 'Dom Casmurro')");
  }

  private void assertCleaned() throws Throwable {
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(false));
  }

  private void assertHave() throws Throwable {
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(true));
  }
}
