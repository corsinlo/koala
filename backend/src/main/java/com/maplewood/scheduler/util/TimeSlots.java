package com.maplewood.scheduler.util;

import java.util.ArrayList;
import java.util.List;

public class TimeSlots {
    // 9:00-17:00 with lunch 12:00-13:00 excluded
    public static final String[] SLOTS = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00"};

    public static List<Integer> days() {
        // 1..5
        List<Integer> d = new ArrayList<>();
        for (int i = 1; i <= 5; i++) d.add(i);
        return d;
    }
}
