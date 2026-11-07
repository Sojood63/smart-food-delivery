# Smart Food Delivery Management System

[![CI](https://github.com/Sojood63/smart-food-delivery/actions/workflows/ci.yml/badge.svg)](https://github.com/Sojood63/smart-food-delivery/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sojood63_smart-food-delivery&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Sojood63_smart-food-delivery)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Sojood63_smart-food-delivery&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Sojood63_smart-food-delivery)

A console food delivery system for restaurants near An-Najah National University, Nablus.
Built with Java 17, Maven, JUnit 5, Mockito and JaCoCo, checked by GitHub Actions and SonarCloud.

Software Engineering (10636313) course project, An-Najah National University, first semester 2026/2027.

## Team

| Member | GitHub | Work |
|--------|--------|------|
| Sojood Saed| [@Sojood63](https://github.com/Sojood63) | Team lead and integrator: project setup, CI and SonarCloud, architecture, login, file storage, manager panel, emails |
| Zeina Suwwan |[@ZeinaSuwwan](https://github.com/ZeinaSuwwan)| Restaurant opening hours, restaurant data collection |
| Mira Hussain | [@mirahussain11](https://github.com/mirahussain11) | Login roles and demo accounts idea, menu data collection |

## Design

| Layer | Package | Examples |
|-------|---------|----------|
| Presentation | `presentation` | `LoginConsole`, `CustomerConsole`, `ManagerConsole` |
| Service | `service`, `pricing`, `delivery`, `payment` | `OrderService`, `PricingService`, `DriverAssignmentService`, `CashPaymentService` |
| Domain | `domain` | `Restaurant`, `MenuItem`, `WeeklySchedule`, `User` |
| Persistence | `repository` | `FileRestaurantRepository`, `FileUserRepository` (text files in `data/`) |

Design patterns: **Strategy** (`HoursStrategy` for opening hours, `PricingStrategy` for
prices, `DriverSelectionStrategy` for drivers) and **Observer** (`EventBus` and
`EmailEventListener`: an email for every event). Polymorphism: order types and driver types.
Services depend on interfaces (`RestaurantRepository`, `EventPublisher`, `TimeProvider`,
`MailSender`), so tests replace them with Mockito mocks.

## Sprint 1 features

| User story | What the program does |
|------------|-----------------------|
| US1.1 Customer/Manager login | Log in with hashed passwords; a wrong login shows an error; 3 wrong passwords lock the account for 15 minutes; customers can sign up |
| US1.2 View available restaurants | Only open restaurants are shown, nearest first from each An-Najah campus, with distance, walking time and a "closing soon" warning |
| US1.3 View restaurant menu | A customer picks an open restaurant and sees only the items that can be ordered now, with prices |
| Extra: manager panel | A manager changes their own restaurant's hours, location and menu; every change is saved and emailed |

## Sprints 2-5 features

| User story | What the program does |
|------------|-----------------------|
| US2.1 Place an order | Customer picks items and quantities; the order is saved as Pending with food, delivery fee and total |
| US2.2 Invalid orders | Unavailable items, items not on the menu, empty orders and quantities outside 1-20 are rejected |
| US2.3 Delivery policies | Restaurant must be within 5 km of the address, food at least 20 ILS, address inside Nablus |
| US2.4 Cancel an order | Only the customer's own Pending order can be cancelled; it becomes Cancelled |
| US3.1 Assign a driver | Only available drivers whose vehicle fits the order; the driver becomes Busy and the order records the driver |
| US3.2 Smart selection | Strategy: `NearestDriverStrategy` or `FastestDriverStrategy`; rejected when no driver is eligible |
| US3.3 / US3.4 Notifications | Every status change, driver assignment, out for delivery and delivery sends an email (Observer) |
| US4.1 Order status | Pending → Confirmed → Preparing → Ready → Out for delivery → Delivered; other steps are rejected |
| US4.2 Delivery cost | 5 ILS + 2 ILS per km, included in the bill |
| US4.3 Dynamic pricing | Strategy: Normal, Peak hour (12-14, 19-21: fee x1.5), Express (+10 ILS), Promotion (`NAJAH10`, `WELCOME20`) |
| US4.4 Cash payment | Cash on delivery; less than the total is rejected; the payment status is recorded |
| US5.1 / US5.2 Order types | `StandardOrder`, `ExpressOrder` (extra fee), `ScheduledOrder` (needs a future time) |
| US5.3 / US5.4 Driver types | `BicycleDriver` (3 km, 5 items), `MotorcycleDriver` (8 km), `CarDriver` (large orders), `ElectricVehicleDriver` (needs battery range) |

Customers order from menu 3 and see or cancel orders from menu 4. Managers move their
restaurant's orders, assign drivers and take cash from "Orders of my restaurant".
Orders and the five demo drivers are kept in memory while the program runs.

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

Every important event sends an email to the person it concerns:

| Event                                   | Email goes to |
|-----------------------------------------|---------------|
| A customer creates an account           | the customer  |
| An account is locked (3 wrong passwords) | the user      |
| Opening hours changed                   | the manager   |
| Restaurant location changed             | the manager   |
| Menu item added / removed               | the manager   |
| Menu item price changed                 | the manager   |
| Menu item made available / unavailable  | the manager   |

Services only publish events (`edu.najah.stu.event`); an `EmailEventListener` subscribed to
the `EventBus` turns each one into an email (Observer pattern). A new kind of email only
needs a new factory method in `AccountEvent` or `RestaurantEvent`.

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
