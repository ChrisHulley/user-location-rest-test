package uk.gov.dwp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationRestTestResourceTest {
  private WireMockServer downstreamService = new WireMockServer(9898);
  private static final ObjectMapper mapper = new ObjectMapper();
  private static List<UserRecordItem> downstreamReturnList;
  private static String expectedOutput;

  @Mock private LocationRestTestConfiguration configuration;

  @BeforeClass
  public static void init() throws JsonProcessingException {

    String testReturn =
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
            + "]";

    expectedOutput =
        "[{\"id\":135,\"first_name\":\"Mechelle\",\"last_name\":\"Boam\"},"
            + "{\"id\":396,\"first_name\":\"Terry\",\"last_name\":\"Stowgill\"}]";

    downstreamReturnList = mapper.readValue(testReturn, new TypeReference<>() {});
  }

  @Before
  public void setup() {
    when(configuration.getDownstreamDataSource()).thenReturn("http://localhost:9898");
    when(configuration.getHomeCity()).thenReturn("London");
    when(configuration.getCityRadius()).thenReturn(50);
    downstreamService.start();
  }

  @After
  public void endTest() {
    downstreamService.stop();
  }

  @Test
  public void runSuccessForSingleLocation() throws JsonProcessingException {
    downstreamService.stubFor(
        get(urlEqualTo("/city/London/users"))
            .willReturn(
                aResponse()
                    .withBody(mapper.writeValueAsString(downstreamReturnList))
                    .withStatus(HttpStatus.SC_OK)));

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_OK)));

    String returnBody = response.getEntity().toString();
    assertThat(returnBody, is(equalTo(expectedOutput)));

    List<UserRecordItem> returnList =
        new ObjectMapper().readValue(returnBody, new TypeReference<>() {});

    assertThat(returnList.size(), is(equalTo(2)));
    assertThat(returnList.get(0).getId(), is(equalTo(downstreamReturnList.get(0).getId())));
  }

  @Test
  public void runErrorForUnknownLocation() {
    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }

  @Test
  public void runErrorForDownstreamError() {
    downstreamService.stubFor(
        get(urlEqualTo("/city/London/users"))
            .willReturn(aResponse().withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)));

    LocationRestTestResource instance = new LocationRestTestResource(configuration);
    Response response = instance.resolveSingleCityRecords();

    assertThat(response.getStatus(), is(equalTo(HttpStatus.SC_BAD_REQUEST)));
  }
}
