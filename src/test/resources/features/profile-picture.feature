Feature: Update profile picture
  As an authenticated user
  I want to change my profile picture
  So that my profile displays the new image

  Scenario: Upload and verify a new profile picture
    Given I open the Ndosi Automation website
    When I sign in with the configured account
    And I open the menu and select My Profile
    And I edit my profile and upload a new picture
    Then the new profile picture is displayed and persisted
