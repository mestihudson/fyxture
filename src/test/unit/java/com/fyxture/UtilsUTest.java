package com.fyxture;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import static com.fyxture.Utils.*;
import org.apache.log4j.Logger;

public class UtilsUTest {
  private static Logger logger = Logger.getLogger(UtilsUTest.class);

  @Test
  public void quote() {
    Assert.assertThat(Utils.quote(new Integer(1)), Matchers.equalTo("1"));
    Assert.assertThat(Utils.quote("a"), Matchers.equalTo("'a'"));
  }

  @Test
  public void comma() {
    Assert.assertThat(Utils.comma(null), Matchers.equalTo(""));
    Assert.assertThat(Utils.comma(""), Matchers.equalTo(""));
    Assert.assertThat(Utils.comma("1"), Matchers.equalTo(", "));
  }

  @Test
  public void literal() {
    Assert.assertThat(Utils.literal("newid()"), Matchers.equalTo("'newid()'"));
    Assert.assertThat(Utils.literal("$newid()"), Matchers.equalTo("newid()"));
    Assert.assertThat(Utils.literal("\\$newid()"), Matchers.equalTo("'$newid()'"));
    Assert.assertThat(Utils.literal("'$newid()'"), Matchers.equalTo("'''$newid()'''"));
  }
}