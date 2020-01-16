package uk.gov.dwp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;
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

  private static List<UserRecordItem> downstreamReturnList;
  private static UserRecordItem exampleLondonVicinityUser;
  private static UserRecordItem exampleLeedsVicinityUser;
  private static String redactedCityRecordOutput;

  @Mock private LocationRestTestConfiguration configuration;

  @BeforeClass
  public static void init() throws JsonProcessingException {

    downstreamReturnList =
        mapper.readValue(
            "[\n"
                + "  {\n"
                + "    \"id\": 135,\n"
                + "    \"first_name\": \"Mechelle\",\n"
                + "    \"last_name\": \"Boam\",\n"
                + "    \"email\": \"mboam3q@thetimes.co.uk\",\n"
                + "    \"ip_address\": \"113.71.242.187\",\n"
                + "    \"latitude\": -6.5115909,\n"
                + "    \"longitude\": 105.652983\n"
                + "  },\n"
                + "  {\n"
                + "    \"id\": 396,\n"
                + "    \"first_name\": \"Terry\",\n"
                + "    \"last_name\": \"Stowgill\",\n"
                + "    \"email\": \"tstowgillaz@webeden.co.uk\",\n"
                + "    \"ip_address\": \"143.190.50.240\",\n"
                + "    \"latitude\": -6.7098551,\n"
                + "    \"longitude\": 111.3479498\n"
                + "  }\n"
                + "]",
            new TypeReference<>() {});

    exampleLondonVicinityUser =
        mapper.readValue(
            "{\n"
                + "    \"id\": 998,\n"
                + "    \"first_name\": \"John\",\n"
                + "    \"last_name\": \"London\",\n"
                + "    \"email\": \"ch@st-pauls.co.uk\",\n"
                + "    \"ip_address\": \"113.71.240.182\",\n"
                + "    \"latitude\": 51.513870,\n"
                + "    \"longitude\": -0.098362\n"
                + "  }",
            UserRecordItem.class);

    exampleLeedsVicinityUser =
        mapper.readValue(
            "{\n"
                + "    \"id\": 999,\n"
                + "    \"first_name\": \"Chris\",\n"
                + "    \"last_name\": \"Yorky\",\n"
                + "    \"email\": \"ch@york-minster.co.uk\",\n"
                + "    \"ip_address\": \"113.71.242.189\",\n"
                + "    \"latitude\": 53.957162838,\n"
                + "    \"longitude\": -1.07583303\n"
                + "  }",
            UserRecordItem.class);

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
    stubForVicinityUserList(Collections.singletonList(exampleLeedsVicinityUser));
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
    stubForVicinityUserList(Collections.singletonList(exampleLondonVicinityUser));
    stubForLondonLondonUsers();

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(returnBody, is(not(equalTo(redactedCityRecordOutput))));

    List<UserRecordItem> outputList = mapper.readValue(returnBody, new TypeReference<>() {});

    assertThat(outputList.size(), is(equalTo(3)));
    assertThat(
        outputList.get(2).getFirstName(), is(equalTo(exampleLondonVicinityUser.getFirstName())));
    assertThat(
        outputList.get(2).getLastName(), is(equalTo(exampleLondonVicinityUser.getLastName())));
  }

  @Test
  public void runSuccessForLocationDuplicateId() throws JsonProcessingException {
    stubForVicinityUserList(Arrays.asList(exampleLondonVicinityUser, exampleLondonVicinityUser));
    stubForLondonLondonUsers();

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(returnBody, is(not(equalTo(redactedCityRecordOutput))));

    List<UserRecordItem> outputList = mapper.readValue(returnBody, new TypeReference<>() {});

    assertThat(outputList.size(), is(equalTo(3)));
    assertThat(
        outputList.get(2).getFirstName(), is(equalTo(exampleLondonVicinityUser.getFirstName())));
    assertThat(
        outputList.get(2).getLastName(), is(equalTo(exampleLondonVicinityUser.getLastName())));
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

  // duplicate records

  //  @Test
  //  public void runErrorForUnknownLocation() {
  //    LocationRestTestResource instance = new LocationRestTestResource(configuration);
  //    Response response = instance.resolveSingleCityRecords();
  //
  //    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  //  }
  //
  //  @Test
  //  public void runErrorForDownstreamError() {
  //    downstreamService.stubFor(
  //        get(urlEqualTo("/city/London/users"))
  //            .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));
  //
  //    LocationRestTestResource instance = new LocationRestTestResource(configuration);
  //    Response response = instance.resolveSingleCityRecords();
  //
  //    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  //  }
}
