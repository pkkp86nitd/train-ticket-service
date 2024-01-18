package com.example.trainticketservice.service;


import com.example.trainticketservice.*;
import com.example.trainticketservice.client.TrainTicketClientApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;



@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class TrainTicketServiceApplicationTest {
    private TrainTicketClientApplication client;

    @BeforeEach
    void setUp() {

        client = new TrainTicketClientApplication("localhost", 6565);
    }

    @AfterEach
    void tearDown() {
        // Shutdown the client after each test
        client.shutdown();
    }

    @Test
    void testSubmitPurchase_Successful() {
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();

        Receipt receipt = client.submitPurchase(purchaseRequest);
        assertNotNull(receipt);
        assertEquals("Pankaj", receipt.getUserFirstName());
        assertEquals("Kumar", receipt.getUserLastName());
        assertEquals("pk@gmail.com", receipt.getUserEmail());
    }

    @Test
    void testViewReceipt_Successful() {
        // Updated purchase details
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();
        client.submitPurchase(purchaseRequest);

        ViewReceiptRequest viewReceiptRequest = ViewReceiptRequest.newBuilder()
                .setUserEmail("pk@gmail.com")
                .build();

        Receipt receipt = client.viewReceipt(viewReceiptRequest);
        assertNotNull(receipt);
        assertEquals("pk@gmail.com", receipt.getUserEmail());
    }

    @Test
    void testViewReceipt_FailureUserNotFound() {
        ViewReceiptRequest viewReceiptRequest = ViewReceiptRequest.newBuilder()
                .setUserEmail("nonexistent.user@example.com")
                .build();

        assertThrows(RuntimeException.class, () -> client.viewReceipt(viewReceiptRequest));
    }

    @Test
    void testViewUsersBySection_Successful() {
        // Updated purchase details
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();
        client.submitPurchase(purchaseRequest);

        SectionRequest sectionRequest = SectionRequest.newBuilder()
                .setSection("A")
                .build();

        UsersBySectionResponse response = client.viewUsersBySection(sectionRequest);
        assertNotNull(response);
        assertEquals(1, response.getUserSeatsCount());
        assertEquals("pk@gmail.com", response.getUserSeats(0).getUserEmail());
    }

    @Test
    void testRemoveUser_Successful() {
        // Updated purchase details
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();
        client.submitPurchase(purchaseRequest);

        UserRequest userRequest = UserRequest.newBuilder()
                .setUserEmail("pk@gmail.com")
                .build();

        RemoveUserResponse response = client.removeUser(userRequest);
        assertTrue(response.getSuccess());
    }

    @Test
    void testRemoveUser_FailureUserNotFound() {
        UserRequest userRequest = UserRequest.newBuilder()
                .setUserEmail("nonexistent.user@example.com")
                .build();

        assertThrows(RuntimeException.class, () -> client.removeUser(userRequest));
    }

    @Test
    void testModifySeat_Successful() {
        // Updated purchase details
        PurchaseRequest purchaseRequest = PurchaseRequest.newBuilder()
                .setFrom("Delhi")
                .setTo("BLR")
                .setUserFirstName("Pankaj")
                .setUserLastName("Kumar")
                .setUserEmail("pk@gmail.com")
                .build();
        client.submitPurchase(purchaseRequest);

        ModifySeatRequest modifySeatRequest = ModifySeatRequest.newBuilder()
                .setUserEmail("pk@gmail.com")
                .setNewSection("B")
                .build();

        ModifySeatResponse response = client.modifySeat(modifySeatRequest);
        assertTrue(response.getSuccess());
    }

    @Test
    void testModifySeat_FailureUserNotFound() {
        ModifySeatRequest modifySeatRequest = ModifySeatRequest.newBuilder()
                .setUserEmail("nonexistent.user@example.com")
                .setNewSection("B")
                .build();

        assertThrows(RuntimeException.class, () -> client.modifySeat(modifySeatRequest));
    }

}