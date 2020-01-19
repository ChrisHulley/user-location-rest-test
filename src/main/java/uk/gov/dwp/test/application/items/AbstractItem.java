package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.Validation;

public interface AbstractItem {

  @JsonIgnore
  public default boolean isContentValid() {
    return Validation.buildDefaultValidatorFactory().getValidator().validate(this).isEmpty();
  }
}
