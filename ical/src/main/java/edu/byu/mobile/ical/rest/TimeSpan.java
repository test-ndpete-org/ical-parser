package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Period;

import java.util.*;

/**
 * @author jmooreoa
 */
public class TimeSpan implements Comparable<TimeSpan> {
    private final DateTime start;
    private final DateTime end;
    private final boolean allDay;

    public TimeSpan(Date start, Date end, boolean allDay) {
        this.start = new DateTime(start);
        this.end = new DateTime(end);
        this.allDay = allDay;
    }

    public TimeSpan(final Period period) {
        start = period.getStart() == null ? null : new DateTime(period.getStart().getTime());
        end = period.getEnd() == null ? null : new DateTime(period.getEnd().getTime());
        final Dur dur = new Dur(start,end);
        allDay = dur.getDays() == 0 && dur.getHours() == 23 && dur.getMinutes() == 59 && dur.getSeconds() == 59;
    }

    public static List<TimeSpan> fromIcal(final Set<Period> occurrences) {
        final List<TimeSpan> result = new ArrayList<TimeSpan>(occurrences.size());

        for (final Period each : occurrences) {
            result.add(new TimeSpan(each));
        }
        Collections.sort(result);
        return result;
    }

    public Date getStart() {
        return start;
    }

    public Date getEnd() {
        return end;
    }

    public boolean isAllDay() {
        return allDay;
    }

    @Override
    public int compareTo(final TimeSpan that) {
        final int fromStart = this.start.compareTo(that.start);
        if (fromStart != 0) return fromStart;
        if (this.allDay && !that.allDay) return -1;
        if (!this.allDay && that.allDay) return 1;
        return this.end.compareTo(that.end);
    }

    public boolean fits(Date from, Date to) {
        return !to.before(this.start) && !from.after(this.start) && !(this.end != null && this.end.after(to) && !this.isAllDay());
    }

    private static boolean sameOrAfter(Date compare, Date compareTo) {
        return compare.after(compareTo) || compare.compareTo(compareTo) == 0;
    }

    private static boolean sameOrBefore(Date compare, Date compareTo) {
        return compare.before(compareTo) || compare.compareTo(compareTo) == 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TimeSpan");
        sb.append("{start=").append(start);
        sb.append(", end=").append(end);
        sb.append(", allDay=").append(allDay);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpan timeSpan = (TimeSpan) o;

        if (allDay != timeSpan.allDay) return false;
        if (end != null ? !end.equals(timeSpan.end) : timeSpan.end != null) return false;
        if (start != null ? !start.equals(timeSpan.start) : timeSpan.start != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (allDay ? 1 : 0);
        return result;
    }
}
