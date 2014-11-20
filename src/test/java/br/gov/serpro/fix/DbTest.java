package br.gov.serpro.fix;

import org.junit.Test;
import org.junit.Assert;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;

public class DbTest {
  private static Logger logger = Logger.getLogger(DbTest.class);

  @Test public void init() throws Throwable {
	//logger.info(Db.init().entity("pessoa", "id_pessoa:1"));
	logger.info(Db.init().entity("pessoa", "id_pessoa:1").insert());
	logger.info(Db.init().entity("pessoa", "id_pessoa:2").insert());
    //Assert.assertThat(Db.init().entity("pessoa"), Matchers.not(Matchers.equalTo(null)));
  }
}
