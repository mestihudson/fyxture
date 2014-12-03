package com.fyxture;

import static com.fyxture.Fyxture.cols;
import static com.fyxture.Fyxture.pair;
import static com.fyxture.Fyxture.where;
import static com.fyxture.Utils.cat;
import static com.fyxture.Utils.fmt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class FyxtureTest {
  private static Logger logger = Logger.getLogger(FyxtureTest.class);

  private String DRIVER;
  private String URL;
  private String USER;
  private String PASSWORD;
  private String DATASOURCE;

  private Connection connection = null;
  private Statement statement = null;

  public FyxtureTest(String driver, String url, String user, String password, String datasource) {
    DRIVER = driver;
    URL = url;
    USER = user;
    PASSWORD = password;
    DATASOURCE = datasource;
  }

  @Before public void init() throws Throwable {
    Class.forName(DRIVER).newInstance();
    connection = DriverManager.getConnection(URL, USER, PASSWORD);
    statement = connection.createStatement();
    logger.debug(DRIVER);
    logger.debug(connection);
    Fyxture.init(DATASOURCE);
  }

  @After public void close() throws Throwable {
    logger.debug("");
    try{
      statement.close();
      connection.close();
    }catch(Throwable t){
      statement = null;
      connection = null;
    }
  }

  @Test public void clear() throws Throwable {
    logger.debug("");
    create();
    Fyxture.clear();
    assert_cleaned();
  }

  @Test public void count() throws Throwable {
    logger.debug("");
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(0));
    create();
    Assert.assertThat(Fyxture.count("livro"), Matchers.equalTo(1));
  }

  @Test public void insert() throws Throwable {
    logger.debug("");
    Fyxture.insert("livro");
    assert_have();
    assert_current_value_of_sequence_is(1);
  }

  @Test public void named_insert() throws Throwable {
    logger.debug("");
    Fyxture.insert("livro", "festa-no-ceu");
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test public void insert_with_replacement() throws Throwable {
    logger.debug("");
    Fyxture.insert("livro", pair("id", 3), pair("titulo", "O Senhor dos Anéis"));
    assert_have();
    assert_current_value_of_sequence_is(3);
  }

  @Test public void named_insert_with_replacement() throws Throwable {
    logger.debug("");
    Fyxture.insert("livro", "festa-no-ceu", pair("titulo", "Game of Thrones"), pair("ano", 1996));
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test public void select() throws Throwable {
    logger.debug("");
    create();
    Map<String, Object> o = Fyxture.select("livro").get(0);
    Assert.assertEquals(1L, o.get("ID"));
    Assert.assertEquals("Dom Casmurro", o.get("TITULO"));
    Assert.assertEquals(1885, o.get("ANO"));
    Assert.assertEquals(0L, o.get("VERSION"));
  }

  @Test public void select_with_selected_cols() throws Throwable {
    logger.debug("");
    create();
    Map<String, Object> o = Fyxture.select("livro", cols("ano", "titulo", "id")).get(0);
    Assert.assertEquals(1L, o.get("ID"));
    Assert.assertEquals("Dom Casmurro", o.get("TITULO"));
    Assert.assertEquals(1885, o.get("ANO"));
    Assert.assertNull(o.get("VERSION"));
  }

  @Test public void select_with_selected_cols_and_conditions() throws Throwable {
    logger.debug("");
    create();
    create(2);
    List<Map<String, Object>> l = Fyxture.select("livro", where("id = 1"));
    Assert.assertEquals(1L, l.size());
  }

  @Test public void select_with_selected_cols_and_conditions_like() throws Throwable {
    logger.debug("");
    create(1, 0, 1948, "1984");
    create(2, 0, 1880, "Dom Casmurro");
    create(3, 0, 1890, "Câmara Cascudo");
    List<Map<String, Object>> l = Fyxture.select("livro", where("titulo like '%Cas%'"));
    Assert.assertEquals(2, l.size());
  }

  private void create(Integer id, Integer version, Integer ano, String titulo) throws Throwable {
    logger.debug(String.format("%d %d %d %s", id, version, ano, titulo));
    statement.execute(String.format("INSERT INTO LIVRO (ID, VERSION, ANO, TITULO) VALUES (%d, %d, %d, '%s')", id, version, ano, titulo));
  }

  private void create(Integer id) throws Throwable {
    logger.debug(id);
    create(id, 0, 1885, "Dom Casmurro");
  }

  private void create() throws Throwable {
    logger.debug("");
    create(1);
  }

  private void assert_cleaned() throws Throwable {
    logger.debug("");
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(false));
  }

  private void assert_have() throws Throwable {
    logger.debug("");
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(true));
  }

  private void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    ResultSet rs = statement.executeQuery(get_command_for_assert_current_value_of_sequence_is());
    rs.next();
    Assert.assertThat(rs.getInt(1), Matchers.equalTo(value));
  }

  protected abstract String get_command_for_assert_current_value_of_sequence_is() throws Throwable;
}
