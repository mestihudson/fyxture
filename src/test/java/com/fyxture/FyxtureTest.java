package com.fyxture;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.junit.After;

import org.apache.log4j.Logger;

import org.hamcrest.Matchers;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;

import org.flywaydb.core.Flyway;

public class FyxtureTest {
  private static Logger logger = Logger.getLogger(FyxtureTest.class);
  private static final String DRIVER = "org.h2.Driver";
  private static final String URL = "jdbc:h2:target/blog";
  private static final String USER = "sa";
  private static final String PASSWORD = null;
  private Connection connection = null;
  private Statement statement = null;
  private static Flyway flyway = null;

  @Before public void init() throws Throwable {
    reset();
    Class.forName(DRIVER).newInstance();
    connection = DriverManager.getConnection(URL, USER, PASSWORD);
    statement = connection.createStatement();
  }

  private void reset() throws Throwable {
    if(flyway == null){
      flyway = new Flyway();
      flyway.setDataSource(URL, USER, PASSWORD);
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
    assert_cleaned();
  }

  @Test public void count() throws Throwable {
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(0));
    create();
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(1));
  }

  @Test public void insert() throws Throwable {
    Fyxture.insert("livro");
<<<<<<< HEAD
    assert_have();
    assert_current_value_of_sequence_is(1);
=======
    assertHave();
    assertCurrentValueOfSequenceIs(1);
>>>>>>> 152b59ee8f69c3e21e46142a5fc448a1ad782ed9
  }

  @Test public void named_insert() throws Throwable {
    Fyxture.insert("livro", "festa-no-ceu");
<<<<<<< HEAD
    assert_have();
    assert_current_value_of_sequence_is(2);
=======
    assertHave();
    assertCurrentValueOfSequenceIs(2);
>>>>>>> 152b59ee8f69c3e21e46142a5fc448a1ad782ed9
  }

  @Test public void insert_with_replacement() throws Throwable {
    Fyxture.insert("livro", Fyxture.pair("id", 3), Fyxture.pair("titulo", "O Senhor dos Anéis"));
<<<<<<< HEAD
    assert_have();
    assert_current_value_of_sequence_is(3);
=======
    assertHave();
    assertCurrentValueOfSequenceIs(3);
>>>>>>> 152b59ee8f69c3e21e46142a5fc448a1ad782ed9
  }

  private void create() throws Throwable {
    statement.executeUpdate("INSERT INTO LIVRO (ID, VERSION, ANO, TITULO) VALUES (1, 0, 1885, 'Dom Casmurro')");
  }

  private void assert_cleaned() throws Throwable {
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(false));
  }

  private void assert_have() throws Throwable {
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(true));
  }

<<<<<<< HEAD
  private void assert_current_value_of_sequence_is(Integer value) throws Throwable {
=======
  private void assertCurrentValueOfSequenceIs(Integer value) throws Throwable {
>>>>>>> 152b59ee8f69c3e21e46142a5fc448a1ad782ed9
    ResultSet rs = statement.executeQuery("SELECT CURRVAL('SQ_ID_LIVRO')");
    rs.next();
    Assert.assertThat(rs.getInt(1), Matchers.equalTo(value));
  }
}
