package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    TicketDAO ticketDAO = new TicketDAO();

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        // Some tests are failing here. Need to check if this logic is correct
        // got the original in and out time and retreived the duration in minutes
        long durationInMinutes = (outTime - inTime) / 60000;
        durationInMinutes = reducedDurationOnFisrt30Minutes(durationInMinutes);
        boolean isRecurrent = isRecurrentPark(ticket.getVehicleRegNumber());
        if (isRecurrent) {
            durationInMinutes = reducedDurationAsDiscount(durationInMinutes, 5);
        }

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(durationInMinutes * Fare.CAR_RATE_PER_HOUR / 60);
                break;
            }
            case BIKE: {
                ticket.setPrice(durationInMinutes * Fare.BIKE_RATE_PER_HOUR / 60);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    /**
     * reduce 30 minutes of parking duration if the original parking duration
     * exceeds 30 minutes else keep it the same
     */
    private long reducedDurationOnFisrt30Minutes(long originalDuration) {
        return originalDuration > 30 ? originalDuration - 30 : originalDuration;
    }

    private long reducedDurationAsDiscount(long originalDuration, int discountRate) {
        return (long) (originalDuration * (1.0 - discountRate / 100.0));
    }

    private boolean isRecurrentPark(String regNumber) {
        long numberOfRecurrentParksByVehivle = ticketDAO.getNumberOfRecurrentParksByVehivle(regNumber);
        return numberOfRecurrentParksByVehivle > 1 ? true : false;
    }
}
