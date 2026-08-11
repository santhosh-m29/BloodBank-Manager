# Product Requirements Document (PRD)

## Blood Bank Management System (Java OOP Mini Project)

**Version:** 1.0

**Course:** Object Oriented Programming (Java)

**Project Type:** Console-Based Java Application

**Storage:** File-Based (.txt files)

---

# 1. Project Overview

## Project Title

**Blood Bank Management System**

## Objective

Develop a console-based Blood Bank Management System using Java that automates blood donation, inventory management, blood requests, blood transfers, and reporting while demonstrating Object-Oriented Programming principles including encapsulation, inheritance, abstraction, polymorphism, file handling, and modularity.

---

# 2. Problem Statement

Blood banks currently manage donor information, blood inventory, expiry dates, hospital requests, and blood transfers manually or through disconnected systems. Manual management can result in inaccurate inventory, delayed emergency responses, expired blood units, and poor record maintenance.

The Blood Bank Management System aims to digitize these operations by providing a centralized application that efficiently manages blood inventory, donor records, recipient requests, hospital coordination, and administrative reporting.

---

# 3. Motivation

The healthcare industry depends heavily on timely blood availability.

This application helps:

* Maintain accurate blood inventory.
* Reduce human errors.
* Track blood expiry.
* Process emergency requests faster.
* Improve donor record management.
* Generate useful administrative reports.

---

# 4. Scope

The system supports:

* Admin authentication
* Donor registration
* Recipient registration
* Hospital registration
* Blood donation management
* Blood inventory management
* Blood request processing
* Blood transfer between blood banks
* Blood expiry monitoring
* Low stock alerts
* Report generation
* File-based persistent storage

---

# 5. Limitations

This mini project intentionally excludes:

* Online database
* GUI
* Payment systems
* SMS/Email notifications
* Barcode scanning
* GPS tracking
* Multi-user concurrent access
* Cloud synchronization

---

# 6. Target Users

### Primary Users

* Blood Bank Administrator

### Secondary Users

* Donors
* Recipients
* Hospitals

---

# 7. Functional Requirements

## Module 1 — Authentication

### Purpose

Allows only authorized administrators to access the system.

### Features

* Login
* Logout
* Change Password

---

## Module 2 — Donor Management

### Features

* Register donor
* Update donor
* Delete donor
* Search donor
* View donor details
* Check donation eligibility

---

## Module 3 — Recipient Management

### Features

* Register recipient
* Update recipient
* Delete recipient
* Search recipient
* Track request status

---

## Module 4 — Hospital Management

### Features

* Register hospital
* Update hospital
* Delete hospital
* Search hospital
* View hospital information

---

## Module 5 — Blood Donation

### Features

* Record donation
* Verify donor eligibility
* Add donated blood to inventory
* Generate donation record

---

## Module 6 — Blood Inventory

### Features

* Add blood units
* Remove blood units
* Update quantity
* Search by blood group
* Display complete inventory
* Monitor stock levels

---

## Module 7 — Blood Request

### Features

* Accept hospital request
* Accept recipient request
* Verify stock availability
* Approve request
* Reject request
* Update inventory automatically

---

## Module 8 — Blood Transfer

### Features

* Transfer blood between blood banks
* Record transfer history
* Update inventory

---

## Module 9 — Alert System

### Features

* Low stock alert
* Blood expiry alert
* Emergency request notification

---

## Module 10 — Report Generation

### Features

Generate

* Donation Report
* Inventory Report
* Blood Request Report
* Blood Transfer Report
* Low Stock Report
* Expired Blood Report

---

# 8. Non-Functional Requirements

### Performance

* Response time under 2 seconds.

### Reliability

* Data should remain after application restart.

### Security

* Admin authentication required.

### Maintainability

* Modular package structure.

### Usability

* Menu-driven interface.

### Portability

* Runs on any JVM.

---

# 9. Packages

```
bloodbank.main

bloodbank.models

bloodbank.services

bloodbank.utilities

bloodbank.file

bloodbank.reports
```

---

# 10. System Architecture

```
User

↓

Menu

↓

Login

↓

Services

↓

Models

↓

File Manager

↓

Text Files
```

---

# 11. Class Hierarchy

```
Person (Abstract)
│
├── Admin
├── Donor
└── Recipient

Organization (Abstract)
│
├── BloodBank
└── Hospital

Transaction (Abstract)
│
├── BloodDonation
├── BloodRequest
└── BloodTransfer

Inventory
│
└── BloodUnit

LoginManager
AlertManager
ReportGenerator
Validation
FileManager
Menu
Main
```

---

# 12. Data Model

## Donor

* Donor ID
* Name
* Age
* Gender
* Blood Group
* Phone
* Address
* Weight
* Hemoglobin
* Last Donation Date

---

## Recipient

* Recipient ID
* Name
* Age
* Blood Group
* Disease
* Doctor
* Hospital ID
* Units Required

---

## Hospital

* Hospital ID
* Name
* Address
* Contact Number
* Emergency Contact

---

## Blood Unit

* Blood Unit ID
* Blood Group
* Quantity
* Collection Date
* Expiry Date

---

## Blood Request

* Request ID
* Recipient ID
* Hospital ID
* Blood Group
* Units Required
* Request Date
* Status

---

## Blood Transfer

* Transfer ID
* Source Blood Bank
* Destination Blood Bank
* Blood Group
* Units
* Transfer Date

---

# 13. File Storage

```
admin.txt

donors.txt

recipients.txt

hospitals.txt

bloodunits.txt

requests.txt

donations.txt

transfers.txt
```

---

# 14. Main Workflow

```
Application Starts

↓

Admin Login

↓

Main Menu

↓

Choose Module

↓

Perform Operation

↓

Update File

↓

Display Result

↓

Return Menu

↓

Exit
```

---

# 15. Inventory Workflow

```
Donation Recorded

↓

Blood Added

↓

Inventory Updated

↓

Expiry Checked

↓

Low Stock Checked

↓

Alert Generated
```

---

# 16. Blood Request Workflow

```
Request Received

↓

Check Blood Availability

↓

Enough Stock?

YES ---------------- NO

Approve             Reject

↓

Update Inventory

↓

Generate Transaction
```

---

# 17. Blood Transfer Workflow

```
Transfer Requested

↓

Check Source Stock

↓

Approve

↓

Remove Source Stock

↓

Add Destination Stock

↓

Record Transfer
```

---

# 18. OOP Concepts Used

## Encapsulation

All fields are private/protected with getters and setters.

Example

```
private String donorId;
```

---

## Inheritance

```
Person

↓

Admin

Donor

Recipient
```

```
Organization

↓

BloodBank

Hospital
```

```
Transaction

↓

BloodDonation

BloodRequest

BloodTransfer
```

---

## Abstraction

Abstract Classes

* Person
* Organization
* Transaction

---

## Polymorphism

Override methods such as

```
displayDetails()

displayTransaction()
```

---

## Composition

```
Inventory

◆────── BloodUnit
```

Inventory owns multiple BloodUnit objects.

---

## Association

```
BloodDonation ---- Donor

BloodRequest ---- Recipient

BloodRequest ---- Hospital

BloodTransfer ---- BloodBank
```

---

# 19. Validation Rules

## Donor

* Age ≥ 18
* Age ≤ 60
* Weight ≥ 50 kg
* Hemoglobin ≥ 12.5
* Valid Blood Group

---

## Recipient

* Positive blood units required
* Valid hospital

---

## Blood Unit

* Quantity > 0
* Expiry Date > Collection Date

---

## Login

* Username exists
* Password matches

---

# 20. Reports

The administrator can generate:

* Current Inventory Report
* Blood Group Availability Report
* Donation History
* Recipient History
* Hospital Request Report
* Blood Transfer Report
* Low Stock Report
* Expired Blood Report

---

# 21. Test Cases

### Authentication

* Valid Login
* Invalid Login
* Wrong Password

### Donor

* Register donor
* Duplicate donor
* Invalid age
* Invalid blood group

### Inventory

* Add blood
* Remove blood
* Search blood
* Low stock detection
* Expired blood detection

### Requests

* Request available blood
* Request unavailable blood
* Emergency request

### Transfer

* Successful transfer
* Insufficient stock

---

# 22. Future Enhancements

* Java Swing/JavaFX graphical interface
* MySQL database integration
* QR code for blood units
* Barcode scanner support
* SMS/Email alerts
* Mobile application
* Online donor registration
* Hospital portal
* Blood compatibility matching (ABO and Rh factor)
* Analytics dashboard

---

# 23. Success Criteria

The project will be considered successful if it:

* Implements all required modules.
* Demonstrates encapsulation, inheritance, abstraction, polymorphism, composition, and association.
* Stores and retrieves data using file handling.
* Provides a complete menu-driven console interface.
* Correctly manages blood inventory, donations, requests, and transfers.
* Generates accurate reports and alerts.
* Follows modular Java package organization.
* Passes all planned test cases.
* Includes UML class diagrams, documentation, and screenshots for presentation.

This PRD is intentionally scoped to match a semester-level Java OOP mini project while remaining extensible for future enhancements.
