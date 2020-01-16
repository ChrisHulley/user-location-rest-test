package uk.gov.dwp.test.application;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.dwp.test.LocationRestTestResource;

public class LocationRestTestApplication extends Application<LocationRestTestConfiguration> {

  @Override
  public void initialize(Bootstrap<LocationRestTestConfiguration> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
  }

  @Override
  public void run(LocationRestTestConfiguration configuration, Environment environment) {
    final LocationRestTestResource instance = new LocationRestTestResource(configuration);
    environment.jersey().register(instance);
  }

  public static void main(String[] args) throws Exception {
    new LocationRestTestApplication().run(args);
  }
}
