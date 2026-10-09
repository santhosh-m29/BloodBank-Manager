# Blood Bank Inventory Management

A console based Java project for managing a central blood bank and its hospitals. It uses classes and inheritance, `ArrayList` collections, simple loops and conditionals, exceptions, and text files for saving data.

## Run

From the project folder, compile and start the application:

```sh
javac -d bin src/bloodbank/Main.java src/bloodbank/model/*.java src/bloodbank/service/*.java src/bloodbank/ui/*.java src/bloodbank/utility/*.java
java -cp bin bloodbank.Main
```

The program creates example records the first time it starts. A new setup starts with 0 central blood units and gives each hospital 5 A+ and 5 B+ units. Existing saved inventory files are kept when the program starts. Example logins:

| Role | Username | Password |
| --- | --- | --- |
| City Blood Center admin | `admin` | `admin123` |
| Northside Blood Bank admin | `admin2` | `admin123` |
| Southside Blood Bank admin | `admin3` | `admin123` |
| Hospital staff | `hosp1` | `hosp123` |
| Donor | `donor` | `donor123` |
| Patient | `patient` | `patient123` |

## Main features

- Register donors, patients, staff, and hospitals.
- Add donated units to central stock immediately after they pass screening.
- Submit blood requests with an urgency level, then approve and transfer blood to a hospital.
- View and update central and hospital inventories.
- Check low stock and expiry alerts and create inventory, donation, and request reports.
- Save records as comma separated text files in `data/`.

## Who controls the workflow

1. **Donor:** checks eligibility, submits a donation, and enters the simulated lab result immediately after collection. A failed unit is rejected. A passed unit is added to central stock immediately.
2. **Hospital staff:** registers patients at their own hospital and submits requests using the patient's blood group. If local inventory has enough stock, the request is ready to issue.
3. **Blood bank admin:** uses one **Approve/Reject Requests & Dispatch Transfer** option. Pending requests ask for YES/NO approval; approved requests ask separately for YES/NO dispatch. The admin transfers only the shortage from central inventory.
4. **Hospital staff:** issues a ready request from local inventory. The request is then marked completed.
5. **Patient:** views their request status and history.

In short: donor eligibility is checked before collection; screening follows collection immediately; passed donations increase central stock; hospital staff requests and issues blood; the admin approves requests and separately dispatches transfers when a hospital needs more. There is one blood bank admin account and no admin levels.

Requests with higher urgency appear first in the admin approval list. Urgency does not make unavailable blood usable.

The source files are in `src/bloodbank/`. Each public class is kept in its own Java file. The course notes in the Unit III, IV, and V folders are the guide for the Java concepts used here.
