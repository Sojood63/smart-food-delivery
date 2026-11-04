# Smart Food Delivery Management System

A console food delivery system for restaurants near An-Najah National University, Nablus.
Built with Java 17, Maven, JUnit 5, Mockito and JaCoCo, checked by GitHub Actions and SonarCloud.

## Run

```
mvn clean test          # run all tests and build the coverage report (target/site/jacoco/index.html)
```

Then run `edu.najah.stu.Main`. On the first run the program creates a `data/` folder with
the restaurants, menus and accounts. Changes are saved there and kept between runs.
Delete the `data/` folder to start again from the bundled data.

## Demo accounts

| Username    | Password      | Role                                 |
|-------------|---------------|--------------------------------------|
| `sojood`    | `Manager123`  | Manager of Khamees (`manual-63`)     |
| `customer1` | `Customer123` | Customer                             |

New customers can create their own account from the first screen.
Only password hashes (PBKDF2) are stored, never the passwords themselves.
After 3 wrong passwords in a row an account is locked for 15 minutes.

## Email

New customers get a welcome email, and users get an email when their account is locked.
Emails are sent over SMTP (for example [Brevo](https://www.brevo.com), free for 300 emails a day).
The settings are read from a `mail.properties` file next to `pom.xml`
(copy `mail.properties.example` and fill it in; Git ignores the real file),
or from environment variables with the same names, which win over the file:

| Variable        | Value                                              | Default                  |
|-----------------|----------------------------------------------------|--------------------------|
| `MAIL_USERNAME` | SMTP login (Brevo: the login shown next to the key) | required                 |
| `MAIL_PASSWORD` | SMTP key (Brevo) or App Password (Gmail)           | required                 |
| `MAIL_FROM`     | sender address, verified with the mail service     | `MAIL_USERNAME`          |
| `MAIL_HOST`     | SMTP server                                        | `smtp-relay.brevo.com`   |
| `MAIL_PORT`     | SMTP port (STARTTLS)                               | `587`                    |

The password is never written in the code or committed to Git. Without `MAIL_USERNAME` and
`MAIL_PASSWORD` the program prints the emails in the console instead, so it runs anywhere.
When it starts, the program says which of the two it is doing.
Tests never send real emails: they use a Mockito mock or a recording sender.

## Data

Restaurant names and opening hours were collected by the team. Locations and some
opening hours come from OpenStreetMap.

Map data (c) [OpenStreetMap contributors](https://www.openstreetmap.org/copyright), available under the Open Database License.
