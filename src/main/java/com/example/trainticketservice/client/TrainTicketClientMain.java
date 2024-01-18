package com.example.trainticketservice.client;

import com.example.trainticketservice.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class TrainTicketClientMain {

    public static void main(String[] args) {
        TrainTicketClientApplication clientApplication = new TrainTicketClientApplication("localhost", 6565);
        runClient(clientApplication);
    }

    public static  void runClient(TrainTicketClientApplication clientApplication) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                displayMenu();
                String choice = reader.readLine();

                switch (choice) {
                    case "1":
                        submitPurchase(clientApplication,reader);
                        break;
                    case "2":
                        viewReceipt(clientApplication,reader);
                        break;
                    case "3":
                        viewUsersBySection(clientApplication,reader);
                        break;
                    case "4":
                        removeUser(clientApplication,reader);
                        break;
                    case "5":
                        modifySeat(clientApplication,reader);
                        break;
                    case "0":
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Shutdown the channel
            clientApplication.shutdown();
        }
    }
    private static void displayMenu() {
        System.out.println("Choose an action:");
        System.out.println("1. Submit Purchase");
        System.out.println("2. View Receipt");
        System.out.println("3. View Users By Section");
        System.out.println("4. Remove User");
        System.out.println("5. Modify Seat");
        System.out.println("0. Exit");
        System.out.print("Enter your choice (0-5): ");
    }

    private static  void submitPurchase(TrainTicketClientApplication clientApplication,BufferedReader reader) throws IOException {
        System.out.println("Submitting Purchase:");
        System.out.print("From: ");
        String from = reader.readLine().trim();
        System.out.print("To: ");
        String to = reader.readLine().trim();
        System.out.print("User First Name: ");
        String firstName = reader.readLine().trim();
        System.out.print("User Last Name: ");
        String lastName = reader.readLine().trim();
        System.out.print("User Email: ");
        String email = reader.readLine().trim();

        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom(from)
                .setTo(to)
                .setUserFirstName(firstName)
                .setUserLastName(lastName)
                .setUserEmail(email)
                .build();

        Receipt receipt =  clientApplication.submitPurchase(purchaseRequest);
        System.out.println("Purchase submitted successfully. Receipt: " + receipt);
        // Add a delay of 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static  void viewReceipt(TrainTicketClientApplication clientApplication,BufferedReader reader) throws IOException {
        System.out.println("Viewing Receipt:");
        System.out.print("User Email: ");
        String email = reader.readLine().trim();

        ViewReceiptRequest viewReceiptRequest = ViewReceiptRequest.newBuilder()
                .setUserEmail(email)
                .build();

        Receipt receipt = clientApplication.viewReceipt(viewReceiptRequest);
        System.out.println("View receipt response: " + receipt);
        // Add a delay of 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void viewUsersBySection(TrainTicketClientApplication clientApplication,BufferedReader reader) throws IOException {
        System.out.println("Viewing Users By Section:");
        System.out.print("Section: ");
        String section = reader.readLine().trim();

        SectionRequest sectionRequest = SectionRequest.newBuilder()
                .setSection(section)
                .build();

        UsersBySectionResponse usersBySectionResponse = clientApplication.viewUsersBySection(sectionRequest);
        System.out.println("View users by section response: " + usersBySectionResponse);
        // Add a delay of 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void removeUser(TrainTicketClientApplication clientApplication,BufferedReader reader) throws IOException {
        System.out.println("Removing User:");
        System.out.print("User Email: ");
        String email = reader.readLine().trim();

        UserRequest userRequest = UserRequest.newBuilder()
                .setUserEmail(email)
                .build();

        RemoveUserResponse removeUserResponse = clientApplication.removeUser(userRequest);
        System.out.println("Remove user response: " + removeUserResponse);
        // Add a delay of 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void modifySeat(TrainTicketClientApplication clientApplication,BufferedReader reader) throws IOException {
        System.out.println("Modifying Seat:");
        System.out.print("User Email: ");
        String email = reader.readLine().trim();
        System.out.print("New Section: ");
        String newSection = reader.readLine().trim();

        ModifySeatRequest modifySeatRequest = ModifySeatRequest.newBuilder()
                .setUserEmail(email)
                .setNewSection(newSection)
                .build();

        ModifySeatResponse modifySeatResponse = clientApplication.modifySeat(modifySeatRequest);
        System.out.println("Modify seat response: " + modifySeatResponse);
        // Add a delay of 1 second
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
