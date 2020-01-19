Feature: test the application against a mocked server

  @StubbedService
  Scenario: get request to an invalid endpoint yields 404
    When I hit "http://localhost:9044/sausage" with a GET request
      Then I should get a 404 response

  @StubbedService
  Scenario: post request to the correct endpoint yields 405
    When I hit "http://localhost:9044/londonCityAndVicinityUsers" with a POST request
    Then I should get a 405 response

  @StubbedService
  Scenario: get request where first downstream endpoint is good but 500 for second endpoint should fail with 400
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body from file "src/test/resources/cityOfLondonResidents.json"
    And I stub the downstream service with a GET request to "/users" to return 500 with body "[]"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 400 response

  @StubbedService
  Scenario: get request returns failure for offline downstream service
    Given I bring down the remote service
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 400 response

  @StubbedService
  Scenario: get request returns success with NO London residents and LONDON vicinity users
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body "[]"
    And I stub the downstream service with a GET request to "/users" to return 200 with body from file "src/test/resources/londonUserRecords.json"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 200 response
    And the return payload should equal to the redacted version of "src/test/resources/londonUserRecords.json"

  @StubbedService
  Scenario: get request returns success with NO London residents and YORK vicinity users
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body "[]"
    And I stub the downstream service with a GET request to "/users" to return 200 with body from file "src/test/resources/yorkUserRecords.json"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 200 response
    And the return payload should be equal to "[]"

  @StubbedService
  Scenario: get request returns success with London and ZERO vicinity users
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body from file "src/test/resources/cityOfLondonResidents.json"
      And I stub the downstream service with a GET request to "/users" to return 200 with body "[]"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
      And I should get a 200 response
      And the return payload should equal to the redacted version of "src/test/resources/cityOfLondonResidents.json"

  @StubbedService
  Scenario: get request returns success with London and with YORK vicinity user
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body from file "src/test/resources/cityOfLondonResidents.json"
    And I stub the downstream service with a GET request to "/users" to return 200 with body from file "src/test/resources/yorkUserRecords.json"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 200 response
    And the return payload should equal to the redacted version of "src/test/resources/cityOfLondonResidents.json"

  @StubbedService
  Scenario: get request returns success with London and with London vicinity user
    Given I stub the downstream service with a GET request to "/city/London/users" to return 200 with body from file "src/test/resources/cityOfLondonResidents.json"
    And I stub the downstream service with a GET request to "/users" to return 200 with body from file "src/test/resources/londonUserRecords.json"
    Then I hit "http://localhost:9044/londonCityAndVicinityUsers" with a GET request
    And I should get a 200 response
    And the return payload should equal to the redacted version of "src/test/resources/cityAndLondonVicinityResidents.json"

