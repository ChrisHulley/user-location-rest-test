package uk.gov.dwp.test.cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CucumberStepDefs {
  private WireMockServer integrationService = new WireMockServer(9898);
  private CloseableHttpClient httpClient;
  private HttpResponse response;
  private String payload;

  @Before
  public void init() {
    httpClient = HttpClientBuilder.create().build();
    integrationService.start();
    payload = null;
  }

  @After
  public void tearDown() {
    integrationService.stop();
  }

  @When("^I hit \"([^\"]*)\" with a POST request$")
  public void iHitWithAGETRequest(String url) throws IOException {
    HttpPost postMethod = new HttpPost(url);

    response = httpClient.execute(postMethod);
    payload = EntityUtils.toString(response.getEntity());
  }

  @Then("^I hit \"([^\"]*)\" with a POST request with \"([^\"]*)\" as the body$")
  public void iHitWithAPOSTRequestWithAsTheBody(String url, String data) throws IOException {
    HttpPost postMethod = new HttpPost(url);
    postMethod.setEntity(new StringEntity(data));

    response = httpClient.execute(postMethod);
    payload = EntityUtils.toString(response.getEntity());
  }

  @Then("^I should get a (\\d+) response$")
  public void iShouldGetAResponse(int status) {
    assertThat(
        "response codes do not match",
        response.getStatusLine().getStatusCode(),
        is(equalTo(status)));
  }

  @And("^the payload should equal \"([^\"]*)\"$")
  public void thePayloadShouldEqual(String expectedPayload) {
    assertThat("payloads do not match", payload, is(equalTo(expectedPayload)));
  }

  @Given(
      "^I stub the integration service with a GET request to \"([^\"]*)\" to return (\\d+) with body \"([^\"]*)\"$")
  public void iStubAGetRequestToToReturnWithBody(String url, int returnCode, String body) {
    integrationService.stubFor(
        get(urlEqualTo(url)).willReturn(aResponse().withStatus(returnCode).withBody(body)));
  }
}
