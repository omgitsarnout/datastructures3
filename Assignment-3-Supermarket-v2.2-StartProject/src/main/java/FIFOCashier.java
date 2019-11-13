import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.PriorityQueue;
import static java.time.temporal.ChronoUnit.SECONDS;

import static java.lang.Math.toIntExact;


public class FIFOCashier extends Cashier {

    public FIFOCashier(String name) {
        super(name);
        waitingQueue = new PriorityQueue<>();
    }

    /**
     * calculate the expected nett checkout time of a customer with a given number of items
     * this may be different for different types of Cashiers
     * @param numberOfItems
     * @return
     */

    @Override
    public int expectedCheckOutTime(int numberOfItems) {
        int time;
        time = (numberOfItems * 2) + 20;
        if (numberOfItems == 0) {
            time = 0;
        }
        return time;
    }

    @Override
    public void reStart(LocalTime currentTime) {
        this.waitingQueue.clear();
        this.currentTime = currentTime;
        this.totalIdleTime = 0;
        this.maxQueueLength = 0;
        // TODO: you may need to override this method in sub-classes
    }

    @Override
    public int expectedWaitingTime(Customer customer) {
        int time = 0;
        for (Customer c : waitingQueue) {
            if (c == customer) {
                break;
            }
            if (c.getNumberOfItems() != 0) {
                time += c.getNumberOfItems() * 2 + 20;
            }
        }
        time += timeLeftWithCurrentCustomer;

        return time;
    }

    @Override
    public void doTheWorkUntil(LocalTime targetTime) {
        //de cashier moet werken tot targettime is bereikt
        while(this.currentTime.isBefore(targetTime)) {
            if (waitingQueue.isEmpty()) {
                this.currentTime = this.currentTime.plusSeconds(1);
                this.totalIdleTime += 1;
            } else {
                //hij moet eerst kijken of de tijd nog af te trekken is van de vorige customer. Vanaf die tijd op 0 is, moet hij de volgende customer pollen.
                Duration duration = Duration.between(currentTime, targetTime);
                int durationSecs = (int) duration.getSeconds();
                //als er niet genoeg tijd is om de huidige customer af te werken, trek dan gewoon de tijd ervan af
                if ((int) duration.getSeconds() < timeLeftWithCurrentCustomer) {
                    timeLeftWithCurrentCustomer -= duration.getSeconds();
                    this.currentTime = targetTime;
                }
                //als er wel genoeg tijd is om de vorige klant af te werken en aan een nieuwe te beginnen
                else if (timeLeftWithCurrentCustomer != 0 && (int) duration.getSeconds() > timeLeftWithCurrentCustomer) {
                    this.currentTime = this.currentTime.plusSeconds(timeLeftWithCurrentCustomer);
                    timeLeftWithCurrentCustomer = 0;
                    currentCustomer = waitingQueue.poll();
                    currentCustomer.setActualWaitingTime((int) Duration.between(currentCustomer.getQueuedAt(), currentTime).toSeconds());
                    if (currentCustomer.getActualWaitingTime() > this.maxWaitingTimeCustomer) {
                        this.maxWaitingTimeCustomer = currentCustomer.getActualWaitingTime();
                    }
                    int waitingTimeCustomer = this.expectedCheckOutTime(currentCustomer.getNumberOfItems());
                    if (waitingTimeCustomer > durationSecs) {
                        timeLeftWithCurrentCustomer = waitingTimeCustomer - durationSecs;
                        this.currentTime = targetTime;
                    } else {
                        this.currentTime = currentTime.plusSeconds(waitingTimeCustomer);
                    }
                }
                //als timeleftwithcurrentcustomer 0 is pollen we gewoon een nieuwe customer
                else {
                    currentCustomer = waitingQueue.poll();
                    currentCustomer.setActualWaitingTime((int) Duration.between(currentCustomer.getQueuedAt(), currentTime).toSeconds());
                    if (currentCustomer.getActualWaitingTime() > this.maxWaitingTimeCustomer) {
                        this.maxWaitingTimeCustomer = currentCustomer.getActualWaitingTime();
                    }
                    int waitingTimeCustomer = this.expectedCheckOutTime(currentCustomer.getNumberOfItems());
                    if (waitingTimeCustomer > durationSecs) {
                        timeLeftWithCurrentCustomer = waitingTimeCustomer - durationSecs;
                        this.currentTime = targetTime;
                    } else {
                        this.currentTime = currentTime.plusSeconds(waitingTimeCustomer);
                    }
                }
                //this.currentTime = targetTime;
            }
        }
    }

    @Override
    public void add(Customer customer) {
        // TODO add the customer to the queue of the cashier (if check-out is required)
        this.totalCustomers += 1;

        if (customer.getNumberOfItems() == 0) {
            return;
        }
        waitingQueue.add(customer);

        //maxQueueLength
        int extraCustomer = 0;
        if (timeLeftWithCurrentCustomer > 0) {
            extraCustomer = 1;
        }
        if (maxQueueLength < waitingQueue.size() + extraCustomer) {
            maxQueueLength = waitingQueue.size() + extraCustomer;
        }
    }

    public int checkoutTimePerCustomer() {
        return 0;
    }

    public int checkoutTimePerItem() {
        return 0;
    }
}
