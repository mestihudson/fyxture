package com.fyxture;

import static com.fyxture.Fyxture.cols;
import static com.fyxture.Fyxture.pair;
import static com.fyxture.Fyxture.where;
import static com.fyxture.Utils.l;
import static com.fyxture.Utils.fmt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public abstract class FyxtureTest {
  private static Logger logger = Logger.getLogger(FyxtureTest.class);

  protected String DRIVER;
  protected String URL;
  protected String USER;
  protected String PASSWORD;
  protected String DATASOURCE;

  protected Connection connection = null;
  protected Statement statement = null;

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

  private void clear_db() throws Throwable {
    Flyway flyway = new Flyway();
    flyway.setDataSource(URL, USER, PASSWORD);
    flyway.setSqlMigrationSuffix("." + DATASOURCE + ".sql");
    flyway.clean();
    flyway.migrate();
    flyway.validate();
    if(!DATASOURCE.equals("h2")){
      logger.info("esperando...");
      Thread.sleep(5 * 1000);
    }
  }

  @Test public void clear() throws Throwable {
    logger.debug("");
    clear_db();
    create();
    Fyxture.clear();
    assert_cleaned();
  }

  @Test public void count() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Assert.assertThat(Fyxture.count("LIVRO"), Matchers.equalTo(0));
    Fyxture.clear();
    create();
    Assert.assertThat(Fyxture.count("LIVRO"), Matchers.equalTo(1));
  }

  @Test public void insert() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("LIVRO");
    assert_have();
    assert_current_value_of_sequence_is(1);
  }

  @Test public void named_insert() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("LIVRO", "festa-no-ceu");
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test public void extended_insert() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("AUTOR", "segundo-extend");
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test(expected=FyxtureIllegalFormatFail.class)
  @Ignore
  public void expected_extended_insert() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("AUTOR", "segundo-extend");
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test public void insert_with_replacement() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("LIVRO", pair("id", null), pair("titulo", "O Senhor dos Anéis"));
    assert_have();
    assert_current_value_of_sequence_is(1);
  }

  @Test public void named_insert_with_replacement() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.insert("LIVRO", "festa-no-ceu", pair("titulo", "Game of Thrones"), pair("ano", 1996));
    assert_have();
    assert_current_value_of_sequence_is(2);
  }

  @Test public void select() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    create();
    Map<String, Object> o = Fyxture.select("LIVRO").get(0);
    Assert.assertEquals(l(1l), l(o.get("ID").toString()));
    Assert.assertEquals("Dom Casmurro", o.get("TITULO"));
    Assert.assertEquals(l(1885), l(o.get("ANO").toString()));
    Assert.assertEquals(l(0), l(o.get("VERSION").toString()));
  }

  @Test public void select_with_selected_cols() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    create();
    Map<String, Object> o = Fyxture.select("LIVRO", cols("ANO", "TITULO", "ID")).get(0);
    Assert.assertEquals(l(1l), l(o.get("ID").toString()));
    Assert.assertEquals("Dom Casmurro", o.get("TITULO"));
    Assert.assertEquals(l(1885), l(o.get("ANO").toString()));
    Assert.assertNull(o.get("VERSION"));
  }

  @Test public void select_with_selected_cols_and_conditions() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    create();
    create(2);
    List<Map<String, Object>> l = Fyxture.select("LIVRO", where("id = 1"));
    Assert.assertEquals(1L, l.size());
  }

  @Test public void select_with_selected_cols_and_conditions_like() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    create(1, 0, 1948, "1984");
    create(2, 0, 1880, "Dom Casmurro");
    create(3, 0, 1890, "Câmara Cascudo");
    List<Map<String, Object>> l = Fyxture.select("LIVRO", where("titulo like '%Cas%'"));
    Assert.assertEquals(2, l.size());
  }

  @Test public void load() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.load("inicial");
    assert_have(1, "LIVRO");
    assert_have(1, "AUTOR");
  }

  @Test public void load_object() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.load("outra-carga");
    assert_have(1, "LIVRO");
    assert_have(3, "AUTOR");
    assert_have(3, "AUTOR_LIVRO");
  }

  @Test public void verify() throws Throwable {
    logger.debug("");
    Fyxture.clear();
    Fyxture.load("inicial");
    Fyxture.verify("todos");
  }

  protected void create(Integer id, Integer version, Integer ano, String titulo) throws Throwable {
    logger.debug(fmt("%d %d %d %s", id, version, ano, titulo));
    statement.execute(fmt("INSERT INTO LIVRO (ID, VERSION, ANO, TITULO) VALUES (%d, %d, %d, '%s')", id, version, ano, titulo));
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

  private void assert_have(Integer quantity, String table) throws Throwable {
    logger.debug("");
    ResultSet rs = statement.executeQuery(fmt("SELECT COUNT(*) FROM %s", table));
    rs.next();
    Assert.assertThat(rs.getInt(1), Matchers.equalTo(quantity));
  }

  private void assert_have() throws Throwable {
    logger.debug("");
    Assert.assertThat(statement.executeQuery("SELECT * FROM LIVRO").next(), Matchers.equalTo(true));
  }

  protected void execute(String command) throws Throwable {
    logger.debug(command);
    statement.execute(command);
  }

  protected ResultSet query(String command) throws Throwable {
    return statement.executeQuery(command);
  }

  protected void assert_current_value_of_sequence_is(Integer value) throws Throwable {
    ResultSet rs = query(get_command_for_assert_current_value_of_sequence_is());
    rs.next();
    Assert.assertThat(value, Matchers.equalTo(rs.getInt(1)));
  }

  protected abstract String get_command_for_assert_current_value_of_sequence_is() throws Throwable;
}
