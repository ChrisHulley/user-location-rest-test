package uk.gov.dwp.test.cucumber;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import uk.gov.dwp.test.application.utils.UserRecordReturnItem;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CucumberStepDefs {
  private WireMockServer downstreamService = new WireMockServer(9898);
  private CloseableHttpClient httpClient;
  private HttpResponse response;
  private String payload;

  @Before
  public void init() {
    httpClient = HttpClientBuilder.create().build();
    downstreamService.start();
    payload = null;
  }

  @After
  public void tearDown() {
    downstreamService.stop();
  }

  @When("^I hit \"([^\"]*)\" with a (GET|POST)? request$")
  public void iHitWithAGETRequest(String url, String requestType) throws IOException {
    if (requestType.equals("POST")) {
      response = httpClient.execute(new HttpPost(url));

    } else {
      response = httpClient.execute(new HttpGet(url));
    }

    payload = EntityUtils.toString(response.getEntity());
  }

  @Then("^I should get a (\\d+) response$")
  public void iShouldGetAResponse(int status) {
    assertThat(
        "response codes do not match",
        response.getStatusLine().getStatusCode(),
        is(equalTo(status)));
  }

  @And(
      "^the return payload should be equal to \"([^\"]*)\"$")
  public void thePayloadShouldEqual(String expectedBody) {
    assertThat("payloads do not match", payload, is(equalTo(expectedBody)));
  }

  @And(
      "^the return payload should equal to the redacted version of \"([^\"]*)\"$")
  public void thePayloadShouldEqualTheRedactedContents(String expectedOutputPath) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    List<UserRecordReturnItem> expectedFullList =
        mapper.readValue(
            FileUtils.readFileToString(new File(expectedOutputPath)),
            new TypeReference<>() {});

    assertThat("payloads do not match", payload, is(equalTo(mapper.writeValueAsString(expectedFullList))));
  }

  @Given(
      "^I stub the downstream service with a GET request to \"([^\"]*)\" to return (\\d+) with body \"([^\"]*)\"$")
  public void iStubAGetRequestToToReturnWithBody(String url, int returnCode, String body) {
    downstreamService.stubFor(
        get(urlEqualTo(url)).willReturn(aResponse().withStatus(returnCode).withBody(body)));
    }

  @And("^I stub the downstream service with a GET request to \"([^\"]*)\" to return (\\d+) with body from file \"([^\"]*)\"$")
  public void iStubTheDownstreamServiceWithAGetRequestToToReturnWithBodyFromFile(String url,
      int returnCode, String filePath) throws Throwable {
    downstreamService.stubFor(
        get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(returnCode).withBody(FileUtils.readFileToString(new File(filePath)))));
  }
}
