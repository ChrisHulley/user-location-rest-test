package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.Validation;

public interface AbstractItem {

  @JsonIgnore
  default boolean isContentValid() {
    return Validation.buildDefaultValidatorFactory().getValidator().validate(this).isEmpty();
  }
}
