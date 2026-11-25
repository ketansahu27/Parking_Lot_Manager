import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

public class ParkingLotManager {
    private static class Vehicle {
        String number;
        String owner;
        String type;
        LocalDateTime entryTime;

        Vehicle(String number, String owner, String type) {
            this.number = number.trim().toUpperCase();
            this.owner = owner.trim();
            this.type = type.trim();
            this.entryTime = LocalDateTime.now();
        }
    }

    private static class ExitRecord {
        Vehicle vehicle;
        LocalDateTime exitTime;
        double fee;

        ExitRecord(Vehicle vehicle, LocalDateTime exitTime, double fee) {
            this.vehicle = vehicle;
            this.exitTime = exitTime;
            this.fee = fee;
        }
    }

    // ARRAY: each index represents one parking slot.
    private final Vehicle[] slots;
    // QUEUE: vehicles wait in FIFO order when the lot is full.
    private final Queue<Vehicle> waitingLine = new ArrayDeque<>();
    // STACK: newest vehicle checkout is shown first.
    private final Stack<ExitRecord> exitHistory = new Stack<>();
    // HASHING: registration number -> parking slot index.
    private final Map<String, Integer> parkedIndex = new HashMap<>();
    // HASH SET: prevents duplicate active registrations.
    private final Set<String> activeVehicles = new HashSet<>();

    private final double hourlyRate;
    private final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a");

    public ParkingLotManager(int capacity, double hourlyRate) {
        slots = new Vehicle[capacity];
        this.hourlyRate = hourlyRate;
    }

    private int findFreeSlot() {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    private void parkInSlot(Vehicle vehicle, int index) {
        slots[index] = vehicle;
        parkedIndex.put(vehicle.number, index);
        System.out.println("Vehicle " + vehicle.number
                + " parked successfully in slot " + (index + 1) + ".");
    }

    public void parkVehicle() {
        System.out.print("\nEnter vehicle registration number: ");
        String number = scanner.nextLine().trim().toUpperCase();

        if (number.isEmpty()) {
            System.out.println("Registration number cannot be empty.");
            return;
        }
        if (activeVehicles.contains(number)) {
            System.out.println("This vehicle is already parked or waiting.");
            return;
        }

        System.out.print("Enter owner's name: ");
        String owner = scanner.nextLine();
        System.out.print("Enter vehicle type (Car/Bike/Other): ");
        String type = scanner.nextLine();

        Vehicle vehicle = new Vehicle(number, owner, type);
        activeVehicles.add(vehicle.number);

        int freeSlot = findFreeSlot();
        if (freeSlot != -1) {
            parkInSlot(vehicle, freeSlot);
        } else {
            waitingLine.offer(vehicle);
            System.out.println("Parking lot is full. Vehicle " + number
                    + " added to waiting queue at position " + waitingLine.size() + ".");
        }
    }

    public void removeVehicle() {
        System.out.print("\nEnter vehicle registration number to check out: ");
        String number = scanner.nextLine().trim().toUpperCase();

        Integer index = parkedIndex.get(number);
        if (index == null) {
            System.out.println("Vehicle not found in the parking lot.");
            return;
        }

        Vehicle vehicle = slots[index];
        LocalDateTime now = LocalDateTime.now();
        long seconds = Math.max(0, Duration.between(vehicle.entryTime, now).getSeconds());
        long billableHours = Math.max(1, (seconds + 3599) / 3600);
        double fee = billableHours * hourlyRate;

        System.out.println("\n---------- PARKING BILL ----------");
        System.out.println("Vehicle number : " + vehicle.number);
        System.out.println("Owner          : " + vehicle.owner);
        System.out.println("Vehicle type   : " + vehicle.type);
        System.out.println("Slot number    : " + (index + 1));
        System.out.println("Entry time     : " + vehicle.entryTime.format(FORMAT));
        System.out.println("Exit time      : " + now.format(FORMAT));
        System.out.println("Billable hours : " + billableHours);
        System.out.printf("Hourly rate    : Rs. %.2f%n", hourlyRate);
        System.out.printf("Total amount   : Rs. %.2f%n", fee);
        System.out.println("----------------------------------");

        exitHistory.push(new ExitRecord(vehicle, now, fee));
        parkedIndex.remove(number);
        activeVehicles.remove(number);
        slots[index] = null;

        System.out.println("Vehicle checked out successfully.");
        admitWaitingVehicle();
    }

    private void admitWaitingVehicle() {
        if (!waitingLine.isEmpty()) {
            int freeSlot = findFreeSlot();
            if (freeSlot != -1) {
                Vehicle next = waitingLine.poll();
                parkInSlot(next, freeSlot);
                System.out.println("The next vehicle in the waiting queue has been admitted.");
            }
        }
    }

    public void searchVehicle() {
        System.out.print("\nEnter registration number to search: ");
        String number = scanner.nextLine().trim().toUpperCase();

        Integer index = parkedIndex.get(number);
        if (index != null) {
            Vehicle v = slots[index];
            System.out.println("\nVehicle found!");
            System.out.println("Registration : " + v.number);
            System.out.println("Owner        : " + v.owner);
            System.out.println("Type         : " + v.type);
            System.out.println("Slot         : " + (index + 1));
            System.out.println("Entry time   : " + v.entryTime.format(FORMAT));
            return;
        }

        int position = 1;
        for (Vehicle v : waitingLine) {
            if (v.number.equals(number)) {
                System.out.println("Vehicle is in the waiting queue at position " + position + ".");
                return;
            }
            position++;
        }
        System.out.println("Vehicle is neither parked nor waiting.");
    }

    public void displayParkingStatus() {
        System.out.println("\n========== PARKING LOT STATUS ==========");
        int occupied = 0;
        for (int i = 0; i < slots.length; i++) {
            System.out.printf("Slot %2d : ", i + 1);
            if (slots[i] != null) {
                occupied++;
                System.out.println("OCCUPIED | " + slots[i].number + " | " + slots[i].owner);
            } else {
                System.out.println("AVAILABLE");
            }
        }
        System.out.println("----------------------------------------");
        System.out.println("Occupied slots : " + occupied + "/" + slots.length);
        System.out.println("Available      : " + (slots.length - occupied));
        System.out.println("Waiting queue  : " + waitingLine.size());
        System.out.println("========================================");
    }

    public void displayWaitingQueue() {
        System.out.println("\n========== WAITING QUEUE (FIFO) ==========");
        if (waitingLine.isEmpty()) {
            System.out.println("No vehicles are waiting.");
            return;
        }

        int position = 1;
        for (Vehicle v : waitingLine) {
            System.out.println(position++ + ". " + v.number + " | " + v.owner + " | " + v.type);
        }
        System.out.println("Next vehicle admitted: " + waitingLine.peek().number);
    }

    public void displayExitHistory() {
        System.out.println("\n========== RECENT EXIT HISTORY (STACK) ==========");
        if (exitHistory.empty()) {
            System.out.println("No vehicle has exited yet.");
            return;
        }

        Stack<ExitRecord> copy = (Stack<ExitRecord>) exitHistory.clone();
        int count = 1;
        while (!copy.empty()) {
            ExitRecord record = copy.pop();
            System.out.printf("%d. %s | %s | Rs. %.2f | %s%n",
                    count++, record.vehicle.number, record.vehicle.owner,
                    record.fee, record.exitTime.format(FORMAT));
        }
        System.out.println("(Newest exit is displayed first because this is a stack.)");
    }

    public void run() {
        while (true) {
            System.out.println("\n\n====================================");
            System.out.println("       PARKING LOT MANAGER");
            System.out.println("====================================");
            System.out.println("1. Park a vehicle");
            System.out.println("2. Check out vehicle / Generate bill");
            System.out.println("3. Search vehicle");
            System.out.println("4. Display parking status");
            System.out.println("5. Display waiting queue");
            System.out.println("6. Display recent exit history");
            System.out.println("0. Exit");
            System.out.println("------------------------------------");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1: parkVehicle(); break;
                case 2: removeVehicle(); break;
                case 3: searchVehicle(); break;
                case 4: displayParkingStatus(); break;
                case 5: displayWaitingQueue(); break;
                case 6: displayExitHistory(); break;
                case 0:
                    System.out.println("Thank you for using Parking Lot Manager!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void main(String[] args) {
        Scanner setupScanner = new Scanner(System.in);
        int capacity;
        double rate;

        System.out.println("===== PARKING LOT MANAGER SETUP =====");
        while (true) {
            System.out.print("Enter total number of parking slots: ");
            try {
                capacity = Integer.parseInt(setupScanner.nextLine().trim());
                if (capacity > 0) break;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a positive whole number.");
        }

        while (true) {
            System.out.print("Enter hourly parking rate (Rs.): ");
            try {
                rate = Double.parseDouble(setupScanner.nextLine().trim());
                if (rate >= 0) break;
            } catch (NumberFormatException ignored) { }
            System.out.println("Please enter a valid non-negative rate.");
        }

        new ParkingLotManager(capacity, rate).run();
    }
}
