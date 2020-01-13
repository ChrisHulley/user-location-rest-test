Feature: test the application against a mocked server

  Scenario: get request to an invalid endpoint
    When I hit "http://localhost:9044/sausage" with a POST request
    Then I should get a 404 response
