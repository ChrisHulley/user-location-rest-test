package uk.gov.dwp.test.application.items;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UserRecordItemTest {

  @Test
  public void testSerialisationOk() throws JsonProcessingException {
    String inputString = formatRecordForTest(1, "Maurise", "34.003135", "-117.7228641");
    UserRecordItem item = new ObjectMapper().readValue(inputString, UserRecordItem.class);
    assertTrue(item.isContentValid());

    assertThat("id mismatch", item.getId(), is(equalTo(1)));
    assertThat("first name mismatch", item.getFirstName(), is(equalTo("Maurise")));
    assertThat("last name mismatch", item.getLastName(), is(equalTo("Shieldon")));
    assertThat("email mismatch", item.getEmailAddress(), is(equalTo("mshieldon0@squidoo.com")));
    assertThat("ip mismatch", item.getIpAddress(), is(equalTo("192.57.232.111")));
    assertThat("latitude mismatch", item.getLatitude(), is(equalTo(34.003135)));
    assertThat("longitude mismatch", item.getLongitude(), is(equalTo(-117.7228641)));
  }

  @Test
  public void testMissingItemRejects() throws JsonProcessingException {
    String inputString = formatRecordForTest(1, null, "34.003135", "-117.7228641");
    UserRecordItem item = new ObjectMapper().readValue(inputString, UserRecordItem.class);
    assertFalse(item.isContentValid());
  }

  @Test(expected = InvalidFormatException.class)
  public void testBadDataTypeFails() throws JsonProcessingException {
    String inputString = "{\"id\": 1, \"latitude\": \"bad bad\"}";
    new ObjectMapper().readValue(inputString, UserRecordItem.class);
  }

  private String formatRecordForTest(int id, String firstName, String latitude, String longitude) {
    return String.format(
        "{\"id\":%d,\"first_name\":%s,\"last_name\":\"Shieldon\",\"email\":\"mshieldon0@squidoo.com\",\"ip_address\":\"192.57.232.111\",\"latitude\":%s,\"longitude\":%s}",
        id, firstName != null ? String.format("\"%s\"", firstName) : null, latitude, longitude);
  }
}
