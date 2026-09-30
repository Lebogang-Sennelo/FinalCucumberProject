package frameworkfiles.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import frameworkfiles.hooks.BrowserSession;
import frameworkfiles.pages.DashboardPage;
import frameworkfiles.pages.HomePage;
import frameworkfiles.pages.LoginPage;
import frameworkfiles.pages.ProfilePage;
import frameworkfiles.testdata.TestData;
import frameworkfiles.utils.ProfileApiClient;
import org.openqa.selenium.WebDriver;

public class ProfilePictureSteps {
    private WebDriver driver;
    private HomePage homePage;
    private DashboardPage dashboardPage;
    private ProfilePage profilePage;

    @Given("I open the Ndosi Automation website")
    public void openWebsite() {
        driver = BrowserSession.getDriver();
        homePage = new HomePage(driver);
        homePage.open();
    }

    @When("I open the menu and choose the login option")
    public void chooseLoginFromMenu() {
        LoginPage loginPage = homePage.openLogin();
        loginPage.waitUntilLoaded();
    }

    @When("I sign in with the configured account")
    public void signIn() {
        dashboardPage = new LoginPage(driver).signIn(TestData.username(), TestData.password());
        dashboardPage.waitUntilLoaded();
    }

    @When("I open the menu and select My Profile")
    public void openProfile() {
        profilePage = dashboardPage.openMyProfile();
    }

    @When("I edit my profile and upload a new picture")
    public void uploadProfilePicture() throws Exception {
        profilePage.updatePicture(TestData.profilePicture());
    }

    @Then("the new profile picture is displayed and persisted")
    public void verifyProfilePicture() throws Exception {
        String displayedPicture = profilePage.getDisplayedProfilePicture();
        new ProfileApiClient(driver).assertProfilePicturePersisted(displayedPicture);
    }
}
