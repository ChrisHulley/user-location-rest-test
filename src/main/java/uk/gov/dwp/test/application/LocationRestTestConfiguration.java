package uk.gov.dwp.test.application;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class LocationRestTestConfiguration extends Configuration {

  @NotNull
  @JsonProperty("downstreamDataSource")
  private String downstreamDataSource;

  @NotNull
  @JsonProperty("cityRadius")
  private int cityRadius;

  public String getDownstreamDataSource() {
    return downstreamDataSource;
  }

  public int getCityRadius() {
    return cityRadius;
  }
}
