package uk.gov.dwp.test;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import uk.gov.dwp.test.application.LocationRestTestApplication;
import uk.gov.dwp.test.application.LocationRestTestConfiguration;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@RunWith(Cucumber.class)
@SuppressWarnings({
  "squid:S2187",
  "squid:S1118"
}) // deliberately has no tests and no private constructor needed
@CucumberOptions(plugin = "json:target/cucumber-report.json", tags = "@StubbedService")
public class RunStubbedCukesTest {

  @ClassRule
  public static final DropwizardAppRule<LocationRestTestConfiguration> RULE =
      new DropwizardAppRule<>(LocationRestTestApplication.class, resourceFilePath("test.yml"));
}
