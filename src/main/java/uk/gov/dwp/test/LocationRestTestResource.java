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

@Path("/")
public class LocationRestTestResource {
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
    ObjectMapper mapper = new ObjectMapper();
    List<UserRecordItem> returnList;
    Response response;

    try {

      HttpResponse downstreamResponse = captureCityResidents();

      if (downstreamResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        throw new UserLocationException(
            String.format(
                "downstream service '%s' returned %d, rejecting",
                configuration.getDownstreamDataSource(),
                downstreamResponse.getStatusLine().getStatusCode()));

      } else {

        returnList =
            mapper.readValue(
                EntityUtils.toString(downstreamResponse.getEntity()), new TypeReference<>() {});

        LOGGER.debug("read {} items from downstream service call for {} residents", returnList.size(), configuration.getHomeCity());
        List<Integer> returnIds = outputListUserIds(returnList);




        response =
            Response.status(downstreamResponse.getStatusLine().getStatusCode())
                .entity(serialiseForOutput(returnList))
                .build();
      }

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

  private String buildLocationEndpoint(LocationInputItem item) {
    return String.format(
        "%s/city/%s/users", configuration.getDownstreamDataSource(), item.getCity());
  }

  private HttpResponse captureCityResidents() throws UserLocationException, IOException {

    // normally the 'location' would be a parameter but for this example it is fixed from config
    LOGGER.debug("building location input item from config -> city = {}", configuration.getHomeCity());
    LocationInputItem inputItem = new LocationInputItem();
    inputItem.setCity(configuration.getHomeCity());

    if (!inputItem.isContentValid()) {
      throw new UserLocationException(
          "'city' is null or empty, in this implementation it 'should' be impossible :-)");
    }

    HttpGet httpGet = new HttpGet(buildLocationEndpoint(inputItem));

    LOGGER.info("call downstream service '{}' for city residents", buildLocationEndpoint(inputItem));
    return HttpClientBuilder.create().build().execute(httpGet);
  }

  private List<Integer> outputListUserIds(List<UserRecordItem> inputItems) {
    ArrayList<Integer> outList = new ArrayList<>();
    for (UserRecordItem item : inputItems) {
      outList.add(item.getId());
    }

    return outList;
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
