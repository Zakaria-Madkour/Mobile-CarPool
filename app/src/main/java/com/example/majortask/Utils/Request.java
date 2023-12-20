package com.example.majortask.Utils;

public class Request {
    private String status;
    private String rideId;
    private String riderId;
    private String requestId;


    public Request(String status, String rideId, String riderId, String requestId) {
        this.status = status;
        this.rideId = rideId;
        this.riderId = riderId;
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public String getRideId() {
        return rideId;
    }

    public String getRiderId() {
        return riderId;
    }

    public String getRequestId() {
        return requestId;
    }
}
