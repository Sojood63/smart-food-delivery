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

## Data

Restaurant names and opening hours were collected by the team. Locations and some
opening hours come from OpenStreetMap.

Map data (c) [OpenStreetMap contributors](https://www.openstreetmap.org/copyright), available under the Open Database License.
