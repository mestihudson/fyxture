package com.fyxture;

import org.junit.Test;
import org.junit.Assert;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;

public class FyxtureTest {
  private static Logger logger = Logger.getLogger(FyxtureTest.class);

  @Test public void init() throws Throwable {
	//logger.info(Fyxture.init().entity("pessoa", "id_pessoa:1"));
	logger.info(Fyxture.init().entity("pessoa", "id_pessoa:1").insert());
	logger.info(Fyxture.init().entity("pessoa", "id_pessoa:2").insert());
    //Assert.assertThat(Fyxture.init().entity("pessoa"), Matchers.not(Matchers.equalTo(null)));
  }
}
