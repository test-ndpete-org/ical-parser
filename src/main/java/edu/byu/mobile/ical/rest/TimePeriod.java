package edu.byu.mobile.ical.rest;

import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;

/**
 * @author jmooreoa
 */
public class TimePeriod {
    private static final Logger LOG = Logger.getLogger(TimePeriod.class);

    private static final MessageFormat EXTERNAL_FORM = new MessageFormat("{0,number,integer}{1}");

    private final int duration;
    private final TimeUnit unit;

    public TimePeriod(int duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public int getDuration() {
        return duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return EXTERNAL_FORM.format(new Object[]{duration, unit.externalValue});
    }

    public static final TimePeriod ONE_DAY = new TimePeriod(1, TimeUnit.DAY);
    public static final TimePeriod ONE_WEEK = new TimePeriod(1, TimeUnit.WEEK);
    public static final TimePeriod ONE_MONTH = new TimePeriod(1, TimeUnit.MONTH);
    public static final TimePeriod ONE_YEAR = new TimePeriod(1, TimeUnit.YEAR);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePeriod that = (TimePeriod) o;

        return duration == that.duration && unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = duration;
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    public static TimePeriod valueOf(String value) {
        Object[] parsed;
        try {
            parsed = EXTERNAL_FORM.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse TimePeriod " + value, e);
        }

        final int duration = ((Number) parsed[0]).intValue();
        final TimeUnit unit = TimeUnit.fromExternalValue(String.valueOf(parsed[1]));

        return new TimePeriod(duration, unit);
    }

    public static enum TimeUnit {
        DAY("D", Calendar.DAY_OF_YEAR),
        WEEK("WK", Calendar.WEEK_OF_YEAR),
        MONTH("MO", Calendar.MONTH),
        YEAR("Y", Calendar.YEAR);
        private final String externalValue;
        private final int calendarField;

        TimeUnit(String externalValue, int calendarField) {
            this.externalValue = externalValue;
            this.calendarField = calendarField;
        }

        public static TimeUnit fromExternalValue(String externalValue) {
            if (externalValue == null) {
                throw new NullPointerException("externalValue cannot be null");
            }
            for (final TimeUnit each : TimeUnit.values()) {
                if (each.externalValue.equalsIgnoreCase(externalValue) || each.name().equalsIgnoreCase(externalValue)) {
                    return each;
                }
            }
            throw new IllegalArgumentException("No such TimeUnit: " + externalValue);
        }

        public int getCalendarField() {
            return calendarField;
        }
    }
}
