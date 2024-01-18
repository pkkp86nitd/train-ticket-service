package com.example.trainticketservice.service;


import com.example.trainticketservice.*;
import com.example.trainticketservice.client.TrainTicketClientApplication;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;


@SpringBootTest
class TrainTicketServiceApplicationTest {


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitPurchase_Successful() {

        TrainTicketServiceApplication service = new TrainTicketServiceApplication();

        StreamObserver<Receipt> purchaseResponseObserver = mock(StreamObserver.class);
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();


        service.submitPurchase(purchaseRequest, purchaseResponseObserver);
        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);

        verify(purchaseResponseObserver, times(1)).onNext(receiptCaptor.capture());

        List<Receipt> capturedReceipts = receiptCaptor.getAllValues();

        assertEquals(1, capturedReceipts.size());

        Receipt actualReceipt = capturedReceipts.get(0);

        assertNotNull(actualReceipt);
        assertEquals("Pankaj", actualReceipt.getUserFirstName());
        assertEquals("Kumar", actualReceipt.getUserLastName());
        assertEquals("pk@gmail.com", actualReceipt.getUserEmail());

    }

    @Test
    void testViewReceipt() {
        TrainTicketServiceApplication service = new TrainTicketServiceApplication();

        StreamObserver<Receipt> responseObserver = mock(StreamObserver.class);

        Receipt mockReceipt = Receipt.newBuilder()
                .setUserEmail("pk@example.com")
                // ... set other receipt fields ...
                .build();
        service.getTickets().put("pk@example.com", mockReceipt);

        // Create a request
        ViewReceiptRequest request = ViewReceiptRequest.newBuilder()
                .setUserEmail("pk@example.com")
                .build();


        service.viewReceipt(request, responseObserver);

        ArgumentCaptor<Receipt> receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
        verify(responseObserver).onNext(receiptCaptor.capture());

        Receipt response = receiptCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getUserEmail()).isEqualTo("pk@example.com");
    }


    @Test
    void testViewUsersBySection() {
        TrainTicketServiceApplication service = new TrainTicketServiceApplication();

        StreamObserver<UsersBySectionResponse> responseObserver = mock(StreamObserver.class);

        String userEmail = "pk@example.com";
        String section = "A";
        Receipt mockReceipt = Receipt.newBuilder()
                .setUserEmail(userEmail)
                .setSeatSection(section)
                .build();
        service.getTickets().put(userEmail, mockReceipt);


        SectionRequest request = SectionRequest.newBuilder()
                .setSection(section)
                .build();


        service.viewUsersBySection(request, responseObserver);

        ArgumentCaptor<UsersBySectionResponse> responseCaptor = ArgumentCaptor.forClass(UsersBySectionResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());

        UsersBySectionResponse response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getUserSeatsList()).hasSize(1);
        assertThat(response.getUserSeatsList().get(0).getUserEmail()).isEqualTo(userEmail);
        assertThat(response.getUserSeatsList().get(0).getSeatSection()).isEqualTo(section);
        // ... add other assertions for receipt fields ...
    }


    @Test
    void testRemoveUser() {
        TrainTicketServiceApplication service = new TrainTicketServiceApplication();

        StreamObserver<RemoveUserResponse> responseObserver = mock(StreamObserver.class);

        String userEmail = "pk@example.com";
        String section = "A";
        int seatNumber = 1;
        Receipt mockReceipt = Receipt.newBuilder()
                .setUserEmail(userEmail)
                .setSeatSection(section)
                .setSeatNumber(seatNumber)
                .build();
        service.getTickets().put(userEmail, mockReceipt);
        service.getSeatOccupied().put(section + seatNumber, 1);

        UserRequest request = UserRequest.newBuilder()
                .setUserEmail(userEmail)
                .build();

        service.removeUser(request, responseObserver);

        ArgumentCaptor<RemoveUserResponse> responseCaptor = ArgumentCaptor.forClass(RemoveUserResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());

        RemoveUserResponse response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(service.getSeatOccupied().get(section + seatNumber)).isEqualTo(0);
        assertThat(service.getTickets().get(userEmail)).isNull();
    }



    @Test
    void testModifySeat() {
        TrainTicketServiceApplication service = new TrainTicketServiceApplication();

        StreamObserver<ModifySeatResponse> responseObserver = mock(StreamObserver.class);


        String userEmail = "pk@example.com";
        String section = "A";
        int seatNumber = 1;
        Receipt mockReceipt = Receipt.newBuilder()
                .setUserEmail(userEmail)
                .setSeatSection(section)
                .setSeatNumber(seatNumber)
                // ... set other receipt fields ...
                .build();
        service.getTickets().put(userEmail, mockReceipt);
        service.getSeatOccupied().put(section + seatNumber, 1);

        ModifySeatRequest request = ModifySeatRequest.newBuilder()
                .setUserEmail(userEmail)
                .setNewSection("B")
                .build();

        service.modifySeat(request, responseObserver);

        // Capture the argument passed to onNext
        ArgumentCaptor<ModifySeatResponse> responseCaptor = ArgumentCaptor.forClass(ModifySeatResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());

        ModifySeatResponse response = responseCaptor.getValue();

        assertThat(response).isNotNull();
        assertThat(response.getSuccess()).isTrue();
        assertThat(service.getSeatOccupied().get(section + seatNumber)).isEqualTo(0);
        assertThat(service.getSeatOccupied().get("B" + seatNumber)).isEqualTo(1);
        assertThat(service.getTickets().get(userEmail).getSeatSection()).isEqualTo("B");
    }


}