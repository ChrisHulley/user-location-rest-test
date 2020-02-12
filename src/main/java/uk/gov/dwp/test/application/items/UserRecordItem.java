package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import uk.gov.dwp.test.application.items.ViewItems.RedactedUserReturn;

public class UserRecordItem implements AbstractItem {

  @NotNull
  @JsonProperty("id")
  private int id;

  @NotNull
  @JsonView(RedactedUserReturn.class)
  @JsonProperty("first_name")
  private String firstName;

  @NotNull
  @JsonView(RedactedUserReturn.class)
  @JsonProperty("last_name")
  private String lastName;

  @NotNull
  @JsonProperty("email")
  private String emailAddress;

  @NotNull
  @JsonProperty("ip_address")
  private String ipAddress;

  @NotNull
  @JsonProperty("latitude")
  private double latitude;

  @NotNull
  @JsonProperty("longitude")
  private double longitude;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  @Override
  @JsonIgnore
  public int hashCode() {
    return Objects.hash(this.getId(), this.getFirstName(), this.getLastName());
  }

  @Override
  @JsonIgnore
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    return this.getId() == ((UserRecordItem) o).getId();
  }
}
