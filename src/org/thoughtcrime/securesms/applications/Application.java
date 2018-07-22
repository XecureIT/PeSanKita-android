package org.thoughtcrime.securesms.applications;

/**
 * Created by winardiaris on 14/02/18.
 */

public class Application {
  private String id;
  private String applicationName;
  private String applicationIcon;
  private String applicationClass;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getApplicationIcon() {
    return applicationIcon;
  }

  public void setApplicationIcon(String applicationIcon) {
    this.applicationIcon = applicationIcon;
  }

  public String getApplicationClass() {
    return applicationClass;
  }

  public void setApplicationClass(String applicationClass) {
    this.applicationClass = applicationClass;
  }
}