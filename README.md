# Ndosi profile-picture automation

Automated UI and API coverage for changing the signed-in user's profile picture on the
[Ndosi Test Automation site](https://ndosisimplifiedautomation.vercel.app/). The test uses
Java 21, Selenium WebDriver, Cucumber, and TestNG.

## What the scenario covers

The Cucumber scenario signs in, opens the navigation menu, visits **My Profile**, edits the
profile, uploads the supplied profile picture, and saves the change. It checks the success
confirmation, the profile image displayed by the web app, and the profile returned by a
follow-up API read. The personal photo is intentionally excluded from Git; CI receives it
through an Actions secret.

Browser network responses are checked for every required API operation in the profile flow:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/login` | Authenticate the configured user |
| `GET` | `/profile` | Load the profile and verify the saved profile |
| `PUT` | `/profile` | Save the profile form |
| `POST` | `/profile/image` | Upload the new profile picture |

The API base URL used by the application is `https://www.ndosiautomation.co.za/APIDEV`.
The test checks that each listed operation returns a 2xx response. It also checks the status
codes for all API requests captured during the flow, makes an authenticated `GET /profile`
after the upload, and checks that the returned image URL matches the one displayed by the
application.

## Requirements

- Java 21
- Maven 3.9+
- Google Chrome or Chromium
- A site account authorized to sign in and update its profile

## Run locally

Create `local.credentials.json` in the project root to avoid entering the password on each run:

```json
{
  "SITE_PASSWORD": "your-password"
}
```

The file is ignored by Git and read automatically by both the PowerShell launcher and IntelliJ
test runs. It stores the password in plain text on your machine; do not remove it from `.gitignore`
or commit it. If the file is absent, the launcher securely prompts for the password. The launcher
uses the supplied profile picture from your Downloads folder and runs headed Chrome:

```powershell
.\run-profile-picture-test.ps1
```

For headless mode, run `.\run-profile-picture-test.ps1 -Headless`.
When running the test directly from IntelliJ IDEA, headed Chrome is used by default.

### Run directly from IntelliJ IDEA

Reload the Maven project, then open `src/test/java/frameworkfiles/runner/RunCucumberTest.java`
and click the green run icon beside the class. If you have the Cucumber for Java plugin
installed, you can also run the scenario from `src/test/resources/features/profile-picture.feature`.
If `local.credentials.json` is absent and `SITE_PASSWORD` is not set in IntelliJ's run
configuration, the test opens a masked password dialog. The profile picture is loaded
automatically from `~/Downloads/DC9EBAC7-2F02-4445-944D-C336462CD73A.JPG`; set
`PROFILE_PICTURE_PATH` in the run configuration to use another image.

To run Maven directly instead, set the required environment variables in the same PowerShell
session before starting Maven:

```powershell
$securePassword = Read-Host "Enter SITE_PASSWORD" -AsSecureString
$env:SITE_PASSWORD = (New-Object System.Net.NetworkCredential("", $securePassword)).Password
$env:PROFILE_PICTURE_PATH = "C:\path\to\profile-picture.jpg"
mvn '-Dbrowser.headless=false' test
Remove-Item Env:SITE_PASSWORD
Remove-Item Env:PROFILE_PICTURE_PATH
```

The test defaults to the Ndosi account `laylayt@gmail.com`. Override it with `SITE_USERNAME`
when you need to use another account. In GitHub Actions, the username is configured in
`.github/workflows/cucumber.yml`; keep the account password in the `SITE_PASSWORD` repository
secret.

To watch the scenario in a visible Chrome window when running Maven directly, use:

```powershell
mvn '-Dbrowser.headless=false' test
```

CI runs in headless mode; local test runs use headed Chrome by default. The browser closes after
the scenario finishes.

The default site URL is the production URL above. `PROFILE_PICTURE_PATH` can point to the
provided JPEG (or another supported JPEG, PNG, GIF, or WEBP image). `SITE_URL` and
`API_BASE_URL` can be set to override the application and API URLs. The application default
API URL is `https://www.ndosiautomation.co.za/APIDEV`.

### IntelliJ IDEA cannot resolve Cucumber or Selenium

The test dependencies are declared with Maven's `test` scope, so IntelliJ must load this
project as a Maven project. Open the root `pom.xml` and select **Load Maven Project**, or
click **Reload All Maven Projects** in the Maven tool window. Then select a JDK 21 project
SDK and rebuild. The module configuration marks `src/test/java` as test sources rather than
main sources; do not add these test-only dependencies to production scope.

## Reports and screenshots

After a run, Maven writes:

- `target/cucumber-reports/cucumber.html` — readable Cucumber execution report
- `target/cucumber-reports/cucumber.json` — machine-readable Cucumber results
- `target/cucumber-reports/api-endpoints.txt` — API methods, paths, and HTTP status codes
- `target/screenshots/Upload_and_verify_a_new_profile_picture.png` — scenario screenshot
- `target/surefire-reports/` — TestNG/Maven execution details

The screenshot is also attached to the Cucumber scenario report. In GitHub Actions, these
files are uploaded as the `cucumber-report-and-screenshots` workflow artifact, including
when a run fails.

## GitHub Actions

The workflow at `.github/workflows/cucumber.yml` runs on pushes, pull requests, manual
dispatch, and every day at **00:00 South African Standard Time (SAST)**. GitHub scheduled
workflows use UTC, so the schedule is `22:00 UTC` (`0 22 * * *`).

Add these repository Actions secrets before running the workflow:

| Secret | Value |
| --- | --- |
| `SITE_PASSWORD` | Account password |
| `PROFILE_PICTURE_BASE64` | Base64 of the supplied JPEG image |

The test resizes and JPEG-compresses the supplied image to
`target/test-data/profile-picture.jpg`. Use that optimized image as the value for the GitHub
secret so it fits within the secret-size limit:

```powershell
$env:SITE_PASSWORD = "your-account-password"
$env:PROFILE_PICTURE_PATH = "C:\path\to\profile-picture.jpg"
mvn test
$env:PROFILE_PICTURE_BASE64 = [Convert]::ToBase64String(
    [IO.File]::ReadAllBytes("target\test-data\profile-picture.jpg")
)
```

Add the resulting `$env:PROFILE_PICTURE_BASE64` value as the `PROFILE_PICTURE_BASE64` Actions
secret in repository settings, then remove the local environment variable. Store the account
password and image value as GitHub Actions secrets, never in source control. The workflow
decodes the private image into the ignored `target` directory before running tests.
It uses Java 21 and Chrome, runs `mvn test`, and uploads reports and screenshots for download
from the Actions run artifacts.

## Project layout

```text
src/test/java/org/example/pages/       Home, login, dashboard, and profile page objects
src/test/java/org/example/steps/       Cucumber scenario step definitions
src/test/java/org/example/runner/      TestNG Cucumber runner
src/test/java/org/example/hooks/       Browser lifecycle and screenshot hooks
src/test/java/org/example/utils/       WebDriver factory, API client, and API report
src/test/java/org/example/testdata/    Credentials, URLs, and profile-picture test data
src/test/resources/features/           Gherkin feature
src/test/resources/cucumber.properties Quiet Cucumber report publishing banner
.github/workflows/cucumber.yml         CI workflow and daily schedule
```
