package uk.gov.dwp.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dwp.test.application.LocationRestTestConfiguration;
import uk.gov.dwp.test.application.exception.UserLocationException;
import uk.gov.dwp.test.application.items.LocationInputItem;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import uk.gov.dwp.test.application.items.UserRecordItem;
import uk.gov.dwp.test.application.items.ViewItems;
import uk.gov.dwp.test.application.utils.DistanceCalculator;
import uk.gov.dwp.test.application.utils.ServiceConstants;

@Path("/")
public class LocationRestTestResource {
  private static final String DOWNSTREAM_CITY_ENDPOINT = "%s/city/%s/users";
  private static final String DOWNSTREAM_ALL_USERS_ENDPOINT = "%s/users";
  private static final ObjectMapper mapper = new ObjectMapper();

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LocationRestTestResource.class.getName());

  private final LocationRestTestConfiguration configuration;

  public LocationRestTestResource(LocationRestTestConfiguration config) {
    this.configuration = config;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/resolveHomeCityResidents")
  public Response resolveSingleCityRecords() {
    Response response = null;

    try {

      // for the purposes of this example no json is passed and defaults to 'London'
      LocationInputItem inputItem = new LocationInputItem();
      LOGGER.debug("built input item with city = {}", inputItem.getCity());

      List<UserRecordItem> cityUserRecords =
          resolveUserRecords(new HttpGet(buildCityLocationEndpoint(inputItem.getCity())));

      LOGGER.debug(
          "collected {} items from downstream service call for {} residents",
          cityUserRecords.size(),
          inputItem.getCity());

      List<UserRecordItem> allUserRecords =
          resolveUserRecords(new HttpGet(buildAllUsersEndpoint()));

      LOGGER.debug(
          "collected {} items from downstream service call for ALL users", allUserRecords.size());

      response =
          Response.status(HttpStatus.SC_OK)
              .entity(serialiseForOutput(mergeInLocationRecords(cityUserRecords, allUserRecords)))
              .build();

    } catch (UserLocationException | IOException e) {
      response =
          Response.status(HttpStatus.SC_BAD_REQUEST)
              .entity(String.format("%s :: %s", e.getClass().getName(), e.getMessage()))
              .build();
      LOGGER.debug(e.getClass().getName(), e);
      LOGGER.error(e.getMessage());
    }

    return response;
  }

  private List<UserRecordItem> resolveUserRecords(HttpGet httpGet)
      throws IOException, UserLocationException {

    LOGGER.info("call downstream service '{}'", httpGet.getURI());
    HttpResponse downstreamResponse = HttpClientBuilder.create().build().execute(httpGet);

    if (downstreamResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
      throw new UserLocationException(
          String.format(
              "downstream service '%s' returned %d, rejecting",
              configuration.getDownstreamDataSource(),
              downstreamResponse.getStatusLine().getStatusCode()));
    }

    return mapper.readValue(
        EntityUtils.toString(downstreamResponse.getEntity()), new TypeReference<>() {});
  }

  private String buildCityLocationEndpoint(String city) {
    return String.format(DOWNSTREAM_CITY_ENDPOINT, configuration.getDownstreamDataSource(), city);
  }

  private String buildAllUsersEndpoint() {
    return String.format(DOWNSTREAM_ALL_USERS_ENDPOINT, configuration.getDownstreamDataSource());
  }

  private List<Integer> calcCityUserIds(List<UserRecordItem> inputItems) {
    ArrayList<Integer> outList = new ArrayList<>();
    for (UserRecordItem item : inputItems) {
      outList.add(item.getId());
    }

    return outList;
  }

  private List<UserRecordItem> mergeInLocationRecords(
      List<UserRecordItem> inputList, List<UserRecordItem> allUsersList) {

    List<Integer> userListIds = calcCityUserIds(inputList);
    for (UserRecordItem item : allUsersList) {

      if (!userListIds.contains(item.getId())
          && DistanceCalculator.distanceWithinAllowableRadius(
              ServiceConstants.LONDON_LAT,
              ServiceConstants.LONDON_LNG,
              item.getLatitude(),
              item.getLongitude(),
              configuration.getCityRadius())) {

        userListIds.add(item.getId());
        inputList.add(item);
      }
    }

    return inputList;
  }

  private String serialiseForOutput(List<UserRecordItem> fullListItem)
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

    return mapper
        .writerWithView(ViewItems.RedactedUserReturn.class)
        .writeValueAsString(fullListItem);
  }
}
