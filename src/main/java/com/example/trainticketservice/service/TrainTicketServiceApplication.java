package com.example.trainticketservice.service;

import com.example.trainticketservice.*;
import com.example.trainticketservice.ModifySeatRequest;
import com.example.trainticketservice.ModifySeatResponse;
import com.example.trainticketservice.PurchaseRequest;
import com.example.trainticketservice.Receipt;
import com.example.trainticketservice.RemoveUserResponse;
import com.example.trainticketservice.SectionRequest;
import com.example.trainticketservice.UserRequest;
import com.example.trainticketservice.UserSeat;
import com.example.trainticketservice.UsersBySectionResponse;
import com.example.trainticketservice.ViewReceiptRequest;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@GRpcService
public class TrainTicketServiceApplication extends TrainTicketServiceGrpc.TrainTicketServiceImplBase {
    public static final int NUM_SECTIONS = 2;
    public static final int SEATS_PER_SECTION = 10;

    private final Map<String, Receipt> tickets = new HashMap<>();
    private final Map<String, Integer> seatOccupied = new HashMap<>();

    @Override
    public void submitPurchase(PurchaseRequest request, StreamObserver<Receipt> responseObserver) {

        synchronized (this) {
            System.out.printf("Received SubmitPurchase request for user: %s%n", request.getUserEmail());

            for (String section : new String[]{"A", "B"}) {
                for (int i = 0; i < SEATS_PER_SECTION; i++) {
                    String seatSection = String.format("%s%d", section, i + 1);
                    if (!isSeatOccupied(seatSection)) {
                        double price = 20.0;
                        try {
                            Integer discount = parseDiscount(request.getDiscount());
                            price -=  discount;
                        }catch ( Exception ex){
                            System.out.println("Invalid Discount, No Discount applied");
                        }


                        Receipt receipt = Receipt.newBuilder()
                                .setFrom(request.getFrom())
                                .setTo(request.getTo())
                                .setUserFirstName(request.getUserFirstName())
                                .setUserLastName(request.getUserLastName())
                                .setUserEmail(request.getUserEmail())
                                .setSeatSection(section)
                                .setSeatNumber(i + 1)
                                .setPricePaid(price)
                                .build();

                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        // Update the seat occupancy
                        seatOccupied.put(seatSection, seatOccupied.getOrDefault(seatSection, 0) + 1);
                        tickets.put(request.getUserEmail(), receipt);

                        System.out.printf("Purchase successful for user: %s, Seat Section: %s, Seat Number: %d%n",
                                request.getUserEmail(), section, i + 1);
                        responseObserver.onNext(receipt);
                        responseObserver.onCompleted();
                        return;
                    }
                }
            }

            System.out.printf("Purchase failed for user: %s, all sections are full%n", request.getUserEmail());
            responseObserver.onError(new RuntimeException("All sections are full"));
        }
    }

    @Override
    public void viewReceipt(ViewReceiptRequest request, StreamObserver<Receipt> responseObserver) {
        System.out.printf("Received ViewReceipt request for user: %s%n", request.getUserEmail());

        if (tickets.containsKey(request.getUserEmail())) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Receipt receipt = tickets.get(request.getUserEmail());
            System.out.printf("Returning receipt for user: %s%n", request.getUserEmail());
            responseObserver.onNext(receipt);
            responseObserver.onCompleted();
        } else {
            System.out.printf("Receipt not found for user: %s%n", request.getUserEmail());
            responseObserver.onError(new RuntimeException("Receipt not found"));
        }
    }

    @Override
    public void viewUsersBySection(SectionRequest request, StreamObserver<UsersBySectionResponse> responseObserver) {
        System.out.printf("Received ViewUsersBySection request for section: %s%n", request.getSection());

        UsersBySectionResponse.Builder responseBuilder = UsersBySectionResponse.newBuilder();
        for (Map.Entry<String, Receipt> entry : tickets.entrySet()) {
            if (entry.getValue().getSeatSection().equals(request.getSection())) {
                System.out.printf("Adding user to result: UserEmail=%s, SeatSection=%s, SeatNumber=%d%n",
                        entry.getKey(), entry.getValue().getSeatSection(), entry.getValue().getSeatNumber());
                responseBuilder.addUserSeats(UserSeat.newBuilder()
                        .setUserEmail(entry.getKey())
                        .setSeatSection(entry.getValue().getSeatSection())
                        .setSeatNumber(entry.getValue().getSeatNumber())
                        .build());
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf("Returning %d users for section: %s%n", responseBuilder.getUserSeatsCount(), request.getSection());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeUser(UserRequest request, StreamObserver<RemoveUserResponse> responseObserver) {
        System.out.printf("Received RemoveUser request for user: %s%n", request.getUserEmail());

        synchronized (this) {

            if (tickets.containsKey(request.getUserEmail())) {
                Receipt receipt = tickets.get(request.getUserEmail());
                // Update the seat occupancy
                seatOccupied.put(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()),
                        seatOccupied.getOrDefault(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()), 0) - 1);
                tickets.remove(request.getUserEmail());

                // Introduce a 1-second delay
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.printf("User removed successfully: %s%n", request.getUserEmail());
                responseObserver.onNext(RemoveUserResponse.newBuilder().setSuccess(true).build());
                responseObserver.onCompleted();
            } else {
                System.out.printf("User not found for removal: %s%n", request.getUserEmail());
                responseObserver.onNext(RemoveUserResponse.newBuilder().setSuccess(false).build());
                responseObserver.onError(new RuntimeException("User not found for removal"));
            }
        }
    }

    @Override
    public void modifySeat(ModifySeatRequest request, StreamObserver<ModifySeatResponse> responseObserver) {
        System.out.printf("Received ModifySeat request for user: %s%n", request.getUserEmail());

        synchronized (this) {
            if (tickets.containsKey(request.getUserEmail())) {
                Receipt receipt = tickets.get(request.getUserEmail());
                // Attempt to update the seat to the specified new section
                String newSeatSection = String.format("%s%d", request.getNewSection(), receipt.getSeatNumber());
                if (!isSeatOccupied(newSeatSection)) {
                    // Seat in the new section is available, update the seat
                    seatOccupied.put(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()),
                            seatOccupied.getOrDefault(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()), 0) - 1);
                    seatOccupied.put(newSeatSection, seatOccupied.getOrDefault(newSeatSection, 0) + 1);
                    receipt = receipt.toBuilder().setSeatSection(request.getNewSection()).build();
                    tickets.put(request.getUserEmail(),receipt);

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.printf("Seat modified successfully for user: %s, New Section: %s%n", request.getUserEmail(), request.getNewSection());
                    responseObserver.onNext(ModifySeatResponse.newBuilder().setSuccess(true).build());
                    responseObserver.onCompleted();
                } else {
                    // If the seat in the new section is not available, find an available seat in another section
                    for (int i = 0; i < NUM_SECTIONS; i++) {
                        String alternativeSeatSection = String.format("%s%d", request.getNewSection(), i + 1);
                        if (!isSeatOccupied(alternativeSeatSection)) {
                            // Found an available seat in another section, update the seat
                            seatOccupied.put(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()),
                                    seatOccupied.getOrDefault(String.format("%s%d", receipt.getSeatSection(), receipt.getSeatNumber()), 0) - 1);
                            seatOccupied.put(alternativeSeatSection, seatOccupied.getOrDefault(alternativeSeatSection, 0) + 1);
                            receipt = receipt.toBuilder().setSeatSection(alternativeSeatSection).build();
                            tickets.put(request.getUserEmail(),receipt);

                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }

                            System.out.printf("Seat modified successfully for user: %s, New Section: %s%n", request.getUserEmail(), alternativeSeatSection);
                            responseObserver.onNext(ModifySeatResponse.newBuilder().setSuccess(true).build());
                            responseObserver.onCompleted();
                            return;
                        }
                    }

                    System.out.printf("No available seats found for user: %s in the new section or alternative section%n", request.getUserEmail());
                    responseObserver.onNext(ModifySeatResponse.newBuilder().setSuccess(false).build());
                    responseObserver.onError(new RuntimeException("No available seats found"));
                }
            } else {
                System.out.printf("User not found for seat modification: %s%n", request.getUserEmail());
                responseObserver.onNext(ModifySeatResponse.newBuilder().setSuccess(false).build());
                responseObserver.onError(new RuntimeException("User not found for seat modification"));
            }
        }

    }

    private boolean isSeatOccupied(String seatSection) {
        return seatOccupied.getOrDefault(seatSection, 0) > 0;
    }

    public Map<String, Receipt> getTickets() {
        return tickets;
    }

    public Map<String, Integer> getSeatOccupied() {
        return seatOccupied;
    }

    private Integer parseDiscount(String discount) throws Exception{
        try{
            if(!Discount.isValid(discount))
                throw new CustomException("Invalid Discount");
            return Discount.valueOf(discount).getPercentage();
        } catch (CustomException ex){
            System.out.println("Custom Exception Error :" + ex.getLocalizedMessage());
            throw new CustomException("Discount Parse Error ");
        } catch (Exception ex){
            System.out.println("New Exception Error :" + ex.getLocalizedMessage());
            throw new CustomException("parse error : " + ex.getLocalizedMessage());
        }

    }
}
