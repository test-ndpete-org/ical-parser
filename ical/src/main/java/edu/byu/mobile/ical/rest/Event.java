package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.component.VEvent;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an event.
 * @author jmooreoa
 */
public class Event {
    private String uid;
    private String summary;
    private Date startDate;
    private Date endDate;
    private List<TimeSpan> occurrences;
    private String location;
    private String repeatRule;
    private String description;
    private String repeatExceptionDate;
    private Date modified;
    private String url;

    public Event() {
    }

    @SuppressWarnings({"unchecked"})
    public Event(VEvent event, PeriodList occurrences) {
        if (event.getSummary() != null)
            this.summary = event.getSummary().getValue();
        if (event.getUid() != null)
            this.uid = event.getUid().getValue();
        if (event.getStartDate() != null)
            this.startDate = event.getStartDate().getDate();
        if (event.getEndDate(true) != null)
            this.endDate = event.getEndDate(event.getDuration() != null && event.getDuration().getValue() != null).getDate();
        if (occurrences != null)
            this.occurrences = TimeSpan.fromIcal(occurrences);
        if (event.getLocation() != null)
            this.location = event.getLocation().getValue();
        if (event.getProperty("RRULE") != null)
            this.repeatRule = event.getProperty("RRULE").getValue();
        if (event.getDescription() != null)
            this.description = event.getDescription().getValue();
        if (event.getProperty("EXDATE") != null)
            this.repeatExceptionDate = event.getProperty("EXDATE").getValue();
        if (event.getLastModified() != null)
            this.modified = event.getLastModified().getDateTime();
        if (event.getUrl() != null) {
            this.url = event.getUrl().getValue();
        }
    }

    public Event(Event source, Date from, Date to) {
        this.uid = source.getUid();
        this.summary = source.getSummary();
        this.startDate = source.getStartDate();
        this.endDate = source.getEndDate();
        this.location = source.getLocation();
        this.repeatRule = source.getRepeatRule();
        this.description = source.getDescription();
        this.repeatExceptionDate = source.getRepeatExceptionDate();
        this.modified = source.getModified();
        this.url = source.getUrl();

        this.occurrences = new LinkedList<TimeSpan>();
        for (TimeSpan each : source.getOccurrences()) {
            if (each.fits(from, to)) {
                this.occurrences.add(each);
            }
        }
    }

    public boolean isAllDay() {
        return !occurrences.isEmpty() && occurrences.get(0).isAllDay();
    }

    public List<TimeSpan> getOccurrences() {
        if (occurrences == null) {
            occurrences = new LinkedList<TimeSpan>();
        }
        return occurrences;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRepeatRule() {
        return repeatRule;
    }

    public void setRepeatRule(String repeatRule) {
        this.repeatRule = repeatRule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepeatExceptionDate() {
        return repeatExceptionDate;
    }

    public void setRepeatExceptionDate(String repeatExceptionDate) {
        this.repeatExceptionDate = repeatExceptionDate;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Event");
        sb.append("{\nuid='").append(uid).append('\'');
        sb.append(",\nsummary='").append(summary).append('\'');
        sb.append(",\nstartDate=").append(startDate);
        sb.append(",\nendDate=").append(endDate);
        sb.append(",\noccurrences=").append(occurrences);
        sb.append(",\nlocation='").append(location).append('\'');
        sb.append(",\nrepeatRule='").append(repeatRule).append('\'');
        sb.append(",\ndescription='").append(description).append('\'');
        sb.append(",\nrepeatExceptionDate='").append(repeatExceptionDate).append('\'');
        sb.append(",\nmodified=").append(modified);
        sb.append(",\nurl='").append(url).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
