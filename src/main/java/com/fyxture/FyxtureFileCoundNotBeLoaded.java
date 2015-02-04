package com.fyxture;

public class FyxtureFileCoundNotBeLoaded extends Exception {
  public FyxtureFileCoundNotBeLoaded(String filename, String motive) {
    super(Utils.fmt("Fyxtures file [%s] could not be loaded.\n%s", filename, motive));
  }
}
