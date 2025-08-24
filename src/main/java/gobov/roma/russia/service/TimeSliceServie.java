package gobov.roma.russia.service;

import gobov.roma.reserch.TimeSlice;
import org.springframework.stereotype.Service;

@Service
public class TimeSliceServie {
    TimeSlice timeSlice;
    public TimeSliceServie(TimeSlice timeSlice) {
        this.timeSlice = timeSlice;
    }

    public void addTimeSlice(String key, Object value) {

    }
}

