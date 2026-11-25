# Parking Lot Manager - Java DSA Practical Project

A menu-driven Java console application for managing parking slots, a FIFO waiting line,
vehicle search, checkout billing, and recent exit history.

## DSA concepts used

| Data structure | Java implementation | Purpose |
|---|---|---|
| Array | `Vehicle[] slots` | Each index represents one parking slot |
| Queue | `Queue<Vehicle>` using `ArrayDeque` | FIFO waiting line when the lot is full |
| Stack | `Stack<ExitRecord>` | Keeps recent checkout history; newest exit is shown first |
| Hashing | `HashMap<String, Integer>` | Maps registration number to slot index for average O(1) lookup |
| Hashing | `HashSet<String>` | Prevents duplicate active vehicle registrations |

## Features

1. Park a vehicle in the first available slot.
2. Add a vehicle to the waiting queue if all slots are occupied.
3. Check out a vehicle and calculate the parking fee.
4. Automatically admit the next waiting vehicle after checkout.
5. Search parked and waiting vehicles by registration number.
6. Display parking status, waiting queue, and exit history.

## Requirements

- JDK 8 or later
- Any Java IDE (IntelliJ IDEA, Eclipse, NetBeans, or VS Code) or terminal

## Compile and run

Open a terminal in this folder:

```bash
javac ParkingLotManager.java
java ParkingLotManager
```

## Sample demonstration

Set capacity to 2:
- Park CAR-101 and BIKE-202: both occupy the array slots.
- Park CAR-303: it enters the waiting queue.
- Check out CAR-101: CAR-303 is automatically moved into the free slot.
- Search BIKE-202: the HashMap finds its slot.
- Display exit history: the Stack shows the latest checkout first.

## Complexity (average case unless noted)

| Operation | Complexity |
|---|---|
| Find first free slot | O(n) |
| Park in a free slot | O(1) average hash insertion |
| Search parked vehicle | O(1) average using HashMap |
| Add to waiting queue | O(1) |
| Remove from waiting queue | O(1) |
| Display parking status | O(n) |
| Display waiting queue | O(w) |
| Display exit history | O(h) |

`n` = number of parking slots, `w` = waiting vehicles, `h` = exit records.

## Billing rule

The program charges at least one hour. Any started hour is rounded up to a full hour.
For example, 1 hour and 5 minutes is billed as 2 hours.

## Limitations and extensions

- Data is stored in memory and resets when the application closes.
- Possible extensions: file/database persistence, different slot types, admin login,
  GUI using Java Swing/JavaFX, and daily/monthly parking passes.
