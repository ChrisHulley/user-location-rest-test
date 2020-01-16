package uk.gov.dwp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.dwp.test.application.LocationRestTestConfiguration;

import uk.gov.dwp.test.application.items.UserRecordItem;
import uk.gov.dwp.test.application.items.ViewItems;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationRestTestResourceTest {
  private WireMockServer downstreamService = new WireMockServer(9898);
  private static final ObjectMapper mapper = new ObjectMapper();

  private static List<UserRecordItem> exampleLondonVicinityUserList;
  private static List<UserRecordItem> exampleLeedsVicinityUserList;
  private static List<UserRecordItem> downstreamReturnList;
  private static String redactedCityRecordOutput;

  @Mock private LocationRestTestConfiguration configuration;

  @BeforeClass
  public static void init() throws IOException {

    downstreamReturnList =
        mapper.readValue(
            FileUtils.readFileToString(new File("src/test/resources/cityOfLondonResidents.json")),
            new TypeReference<>() {});

    exampleLondonVicinityUserList =
        mapper.readValue(
            FileUtils.readFileToString(new File("src/test/resources/londonUserRecords.json")),
            new TypeReference<>() {});

    exampleLeedsVicinityUserList =
        mapper.readValue(
            FileUtils.readFileToString(new File("src/test/resources/yorkUserRecords.json")),
            new TypeReference<>() {});

    redactedCityRecordOutput =
        new ObjectMapper()
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .writerWithView(ViewItems.RedactedUserReturn.class)
            .writeValueAsString(downstreamReturnList);
  }

  @Before
  public void setup() {
    when(configuration.getDownstreamDataSource()).thenReturn("http://localhost:9898");
    when(configuration.getCityRadius()).thenReturn(50);
    downstreamService.start();
  }

  @After
  public void endTest() {
    downstreamService.stop();
  }

  @Test
  public void runSuccessForLocationNoVicinity() throws JsonProcessingException {
    stubForVicinityUserList(exampleLeedsVicinityUserList);
    stubForLondonLondonUsers();

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(response.getEntity().toString(), is(equalTo(redactedCityRecordOutput)));

    List<UserRecordItem> outputList = mapper.readValue(returnBody, new TypeReference<>() {});

    assertThat(outputList.size(), is(equalTo(2)));
    assertThat(
        outputList.get(1).getFirstName(), is(equalTo(downstreamReturnList.get(1).getFirstName())));
    assertThat(
        outputList.get(1).getLastName(), is(equalTo(downstreamReturnList.get(1).getLastName())));
  }

  @Test
  public void runSuccessForLocationWithVicinity() throws JsonProcessingException {
    stubForVicinityUserList(exampleLondonVicinityUserList);
    stubForLondonLondonUsers();

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(returnBody, is(not(equalTo(redactedCityRecordOutput))));

    List<UserRecordItem> outputList = mapper.readValue(returnBody, new TypeReference<>() {});

    assertThat(outputList.size(), is(equalTo(3)));
    assertThat(
        outputList.get(2).getFirstName(), is(equalTo(exampleLondonVicinityUserList.get(0).getFirstName())));
    assertThat(
        outputList.get(2).getLastName(), is(equalTo(exampleLondonVicinityUserList.get(0).getLastName())));
  }

  @Test
  public void runSuccessForLocationDuplicateId() throws JsonProcessingException {
    List<UserRecordItem> duplicatedList = new ArrayList<>();
    duplicatedList.add(exampleLondonVicinityUserList.get(0));
    duplicatedList.add(exampleLondonVicinityUserList.get(0));

    stubForVicinityUserList(duplicatedList);
    stubForLondonLondonUsers();

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(returnBody, is(not(equalTo(redactedCityRecordOutput))));

    List<UserRecordItem> outputList = mapper.readValue(returnBody, new TypeReference<>() {});

    assertThat(outputList.size(), is(equalTo(3)));
    assertThat(
        outputList.get(2).getFirstName(), is(equalTo(exampleLondonVicinityUserList.get(0).getFirstName())));
    assertThat(
        outputList.get(2).getLastName(), is(equalTo(exampleLondonVicinityUserList.get(0).getLastName())));
  }

  @Test
  public void runErrorForUnknownLocation() {
    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  public void runErrorForDownstreamErrorFirst() {
    downstreamService.stubFor(
        get(urlEqualTo("/city/London/users"))
            .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  public void runErrorForDownstreamErrorSecond() throws JsonProcessingException {
    stubForLondonLondonUsers();

    downstreamService.stubFor(
        get(urlEqualTo("/users"))
            .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  private void stubForLondonLondonUsers() throws JsonProcessingException {
    downstreamService.stubFor(
        get(urlEqualTo("/city/London/users"))
            .willReturn(
                aResponse()
                    .withBody(mapper.writeValueAsString(downstreamReturnList))
                    .withStatus(HttpStatus.SC_OK)));
  }

  private void stubForVicinityUserList(List<UserRecordItem> vicinityReturnList)
      throws JsonProcessingException {
    downstreamService.stubFor(
        get(urlEqualTo("/users"))
            .willReturn(
                aResponse()
                    .withBody(mapper.writeValueAsString(vicinityReturnList))
                    .withStatus(HttpStatus.SC_OK)));
  }
}
