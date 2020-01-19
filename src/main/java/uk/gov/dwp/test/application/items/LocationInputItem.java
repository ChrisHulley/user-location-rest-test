package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class LocationInputItem implements AbstractItem {

  @NotNull
  @JsonProperty("city")
  private String city = "London"; // spoof default for this exercise

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
