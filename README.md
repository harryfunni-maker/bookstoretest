
# BookstoreTest

This project demonstrates a hybrid end-to-end test for the DemoQA Bookstore application, combining API and UI automation using Java, Maven, Selenium, RestAssured, and TestNG.

## Hybrid End-to-End Test Overview
The main scenario is implemented in `HybridEndToEndTest.java` and covers the following steps:

1. **Create User and Generate Token (API):**
	- Registers a new user via API.
	- Generates an authentication token for the user.

2. **Add Book to User (API):**
	- Fetches the list of available books via API.
	- Adds the first book to the user's collection using the API.

3. **List User's Books (API):**
	- Retrieves the user's profile and validates the book is present via API.

4. **Login and Validate Book in Profile (UI):**
	- Logs in to the DemoQA Bookstore UI with the created user.
	- Waits for the profile page and checks that the added book appears in the user's book list (by ISBN in the link).

5. **Delete Book and Validate Removal (API + UI):**
	- Deletes the book from the user's collection via API.
	- Refreshes the UI and verifies the book is no longer listed in the user's profile.

## What This Test Validates
- End-to-end integration of API and UI for user and book management.
- Data consistency between backend (API) and frontend (UI).
- Robustness of login, book addition, and deletion flows.
- Use of advanced waits and debug logging for reliable UI validation.

## How to Run
1. Install dependencies: `mvn clean install`
2. Run tests: `mvn test`
3. View reports in `target/surefire-reports/`

---
See `src/test/java/hybrid/HybridEndToEndTest.java` for full implementation details.
