# Ndosi profile-picture automation

Automated UI and API coverage for changing the signed-in user's profile picture on the
[Ndosi Test Automation site](https://ndosisimplifiedautomation.vercel.app/). The test uses
Java 21, Selenium WebDriver, Cucumber, and TestNG.

## What the scenario covers

The Cucumber scenario signs in, opens the navigation menu, visits **My Profile**, edits the
profile, uploads a generated PNG, and saves the change. It checks the success confirmation,
the profile image URL stored by the web app, and the profile returned by a follow-up API read.

Browser network responses are checked for every required API operation in the profile flow:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/login` | Authenticate the configured user |
| `GET` | `/profile` | Load the profile and verify the saved profile |
| `PUT` | `/profile` | Save the profile form |
| `POST` | `/profile/image` | Upload the new profile picture |

The API base URL used by the application is `https://www.ndosiautomation.co.za/APIDEV`.
The test checks that each listed operation returns a 2xx response. It also makes an
authenticated `GET /profile` after the upload and checks that the returned image URL matches
the one displayed by the application.

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
mvn test
```

The default site URL is the production URL above. To test another deployment, pass
`-Dsite.url=https://your-test-site/`.

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

The workflow uses Java 21 and Chrome, runs `mvn test`, and uploads the reports and screenshots
for download from the Actions run's artifacts.

## Project layout

```text
src/test/java/org/example/hooks/       Browser lifecycle, screenshots, API response report
src/test/java/org/example/steps/       Cucumber UI and API verification steps
src/test/java/org/example/runner/      TestNG Cucumber runner
src/test/resources/features/           Gherkin scenario
.github/workflows/cucumber.yml         CI and daily schedule
```
