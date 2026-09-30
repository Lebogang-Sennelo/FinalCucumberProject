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

Set credentials as environment variables; do not place account credentials in source control:

```powershell
$env:SITE_USERNAME = "your-account-email"
$env:SITE_PASSWORD = "your-account-password"
$env:PROFILE_PICTURE_PATH = "C:\path\to\profile-picture.jpg"
mvn test
```

To watch the scenario in a visible Chrome window, run:

```powershell
mvn '-Dbrowser.headless=false' test
```

Headless mode remains enabled by default for CI. The browser closes after the scenario finishes.

The default site URL is the production URL above. `PROFILE_PICTURE_PATH` must point to the
provided JPEG (or another supported JPEG, PNG, GIF, or WEBP image). `SITE_URL` and
`API_BASE_URL` can be set to override the application and API URLs. The application default
API URL is `https://www.ndosiautomation.co.za/APIDEV`.

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
| `SITE_USERNAME` | Authorized Ndosi site account email |
| `SITE_PASSWORD` | Account password |
| `PROFILE_PICTURE_BASE64` | Base64 of the supplied JPEG image |

The test resizes and JPEG-compresses the supplied image to
`target/test-data/profile-picture.jpg`. Use that optimized image as the value for the GitHub
secret so it fits within the secret-size limit:

```powershell
$env:SITE_USERNAME = "your-account-email"
$env:SITE_PASSWORD = "your-account-password"
$env:PROFILE_PICTURE_PATH = "C:\path\to\profile-picture.jpg"
mvn test
$env:PROFILE_PICTURE_BASE64 = [Convert]::ToBase64String(
    [IO.File]::ReadAllBytes("target\test-data\profile-picture.jpg")
)
```

Add the resulting `$env:PROFILE_PICTURE_BASE64` value as the `PROFILE_PICTURE_BASE64` Actions
secret in repository settings, then remove the local environment variable. Store the username,
password, and image value as GitHub Actions secrets, never in source control. The workflow
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
