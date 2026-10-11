# Blood Bank Inventory Management

## Purpose

This is a small console application for a college Java project. It demonstrates object oriented programming and the Java topics covered in Units III, IV, and V. The code should stay readable for students who are learning these topics.

## Features

- Manage blood banks, hospitals, staff, donors, and patients.
- Record donations, check donated units, and track blood stock by group.
- Submit requests with an urgency level and transfer blood to hospitals.
- Issue blood from hospital inventory.
- Show low stock and expiry alerts and print basic reports.
- Save and load records using comma separated text files.

## Who controls each step

1. A donor checks eligibility, submits a donation, and enters a simulated `PASS` or `FAIL` result immediately after collection. Failed units are rejected. Passed units are added to central stock immediately.
2. Hospital staff creates patients for their hospital and submits blood requests using the patient's blood group. A request is ready immediately if local stock is sufficient.
3. In one admin option, the blood bank administrator approves or rejects requests and dispatches approved requests. Dispatch asks separately for YES/NO confirmation. For a shortage, central stock is checked and only the shortage is transferred.
4. Hospital staff issues the ready request from local inventory, then the request is marked complete.
5. A patient can view their request status and history.

A fresh setup has 0 units in the central inventory. Each hospital starts with 5 available A+ units and 5 available B+ units. Existing saved inventory is loaded as-is.

There is one blood bank admin account and no admin levels.

## Java concepts

Use separate classes in the `bloodbank` package, inheritance for shared person and organization details, `ArrayList` and basic generic collections, loops, conditionals, methods, and `try`/`catch` for file and input errors. Keep methods direct and avoid adding abstractions or features that are not needed by the application.

## Run

```sh
javac -d bin src/bloodbank/Main.java src/bloodbank/model/*.java src/bloodbank/service/*.java src/bloodbank/ui/*.java src/bloodbank/utility/*.java
java -cp bin bloodbank.Main
```
