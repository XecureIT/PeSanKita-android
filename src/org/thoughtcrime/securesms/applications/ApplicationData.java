package org.thoughtcrime.securesms.applications;

import java.util.ArrayList;

/**
 * Created by winardiaris on 14/02/18.
 */

public class ApplicationData {

  public static String[][] data = new String[][] {
      {"1", "application_hkki", "hkki_logo", "HkkiActivity"},
      {"2", "application_kitsia", "kitsia_logo", "KitsiaActivity"},
  };

  public static ArrayList<Application> get() {
    Application application     = null;
    ArrayList<Application> list = new ArrayList<>();

    for (int i = 0; i < data.length; i++) {
      application = new Application();
      application.setId(data[i][0]);
      application.setApplicationName(data[i][1]);
      application.setApplicationIcon(data[i][2]);
      application.setApplicationClass(data[i][3]);
      list.add(application);
    }
    return list;
  }
}