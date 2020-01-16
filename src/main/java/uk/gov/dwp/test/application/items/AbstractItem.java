package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.Validation;

public abstract class AbstractItem {

  @JsonIgnore
  public boolean isContentValid() {
    return Validation.buildDefaultValidatorFactory().getValidator().validate(this).isEmpty();
  }
}
