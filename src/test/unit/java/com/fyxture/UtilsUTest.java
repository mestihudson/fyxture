package com.fyxture;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import static com.fyxture.Utils.*;

public class UtilsUTest {
  @Test
  public void literal() {
    Object value = "$newid()";
    literal(value);
  }
}