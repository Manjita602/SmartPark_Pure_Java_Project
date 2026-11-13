# SmartPark – Pure Java Project

## Topic selected
IoT-based Smart Parking System with Real-Time Slot Detection (software simulation).

## Requirements
- JDK 8 or later
- Any Java IDE (IntelliJ IDEA, Eclipse, NetBeans, or VS Code with Java extensions)

## Run from terminal
Open a terminal in the `SmartPark_Java_Project` folder and execute:

```bash
javac src/SmartParkingApp.java
java -cp src SmartParkingApp
```

The application creates `smartpark-data.ser` in the working directory to save active parking slots and completed parking history.

## Features
- Live dashboard with total, occupied, available slots and collected revenue
- 12 car slots and 8 bike slots
- Automatic allocation of the first available slot for the selected vehicle type
- Duplicate vehicle-entry prevention
- Vehicle exit lookup by registration number
- Parking fee calculation (first hour: car ₹30 / bike ₹15; each additional started hour: car ₹20 / bike ₹10)
- Parking session history
- Persistent local file storage using Java object serialization

## How the sensor simulation works
Each parking slot behaves like a virtual sensor. On vehicle entry, the selected slot changes from AVAILABLE to OCCUPIED. On exit, it changes back to AVAILABLE. This demonstrates the application logic without requiring physical IoT hardware.

## Suggested demonstration
1. Start the application and show the dashboard.
2. Register a car and a bike; show automatic slot assignment.
3. Try entering the same vehicle number again to demonstrate duplicate checking.
4. Try to park more vehicles than the available slots of a category.
5. Exit a vehicle and show the generated fee and released slot.
6. Open Parking History and restart the app to demonstrate saved data.

## Project structure
```text
SmartPark_Java_Project/
├── src/
│   └── SmartParkingApp.java
└── README.md
```

## Important note
This is a pure Java desktop application. The real-time sensor behavior is simulated in software; no external libraries, database, web technology, or physical sensor is needed.
