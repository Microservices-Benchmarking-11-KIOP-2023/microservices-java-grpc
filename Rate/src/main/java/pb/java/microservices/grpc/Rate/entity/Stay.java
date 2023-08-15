package pb.java.microservices.grpc.Rate.entity;

import java.util.Objects;

public class Stay {
    private String hotelId;
    private String inDate;
    private String outDate;

    public Stay(String hotelId, String inDate, String outDate) {
        this.hotelId = hotelId;
        this.inDate = inDate;
        this.outDate = outDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stay stay = (Stay) o;
        return Objects.equals(hotelId, stay.hotelId) && Objects.equals(inDate, stay.inDate) && Objects.equals(outDate, stay.outDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hotelId, inDate, outDate);
    }

    public String getHotelId() {
        return hotelId;
    }

    public void setHotelId(String hotelId) {
        this.hotelId = hotelId;
    }

}
