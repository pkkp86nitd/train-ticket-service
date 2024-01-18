package com.example.trainticketservice.client;

import com.example.trainticketservice.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;



public class TrainTicketClientApplication {

    private final ManagedChannel channel;
    private final TrainTicketServiceGrpc.TrainTicketServiceBlockingStub blockingStub;

    public TrainTicketClientApplication(String host, int port) {
        // Create a gRPC channel
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        // Create a gRPC blocking stub
        this.blockingStub = TrainTicketServiceGrpc.newBlockingStub(channel);
    }





   public Receipt submitPurchase(PurchaseRequest purchaseRequest){
        return blockingStub.submitPurchase(purchaseRequest);
   }

    public Receipt viewReceipt(ViewReceiptRequest viewReceiptRequest){
       return  blockingStub.viewReceipt(viewReceiptRequest);
    }


    public UsersBySectionResponse viewUsersBySection(SectionRequest sectionRequest){
        return blockingStub.viewUsersBySection(sectionRequest);
    }

    public RemoveUserResponse removeUser(UserRequest userRequest){
        return blockingStub.removeUser(userRequest);
    }



   public ModifySeatResponse modifySeat(ModifySeatRequest modifySeatRequest){
        return blockingStub.modifySeat(modifySeatRequest);
   }



    public void shutdown() {
        // Shutdown the channel when done
        channel.shutdown();
    }
}
