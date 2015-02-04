package com.fyxture;

public class FyxtureFileNotFound extends Exception {
  public FyxtureFileNotFound(String filename) {
    super(Utils.fmt("Fyxtures file [%s] not found", filename));
  }
}
