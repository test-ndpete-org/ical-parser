package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an event.
 *
 * @author jmooreoa
 */
public class Event {
	private String uid;
	private String summary;
	private Date startDate;
	private Date endDate;
	private List<TimeSpan> occurrences;
	private String location;
	private String description;
	private Date modified;
	private String url;
	private boolean repeats;
	private RepeatRule repeatRule;

	public Event() {
	}

	@SuppressWarnings({"unchecked"})
	public Event(VEvent event, PeriodList occurrences) {
		if (event.getSummary() != null)
			this.summary = event.getSummary().getValue();
		if (event.getUid() != null)
			this.uid = event.getUid().getValue();

		final Date eventStartDate = event.getStartDate().getDate();
		final Date eventEndDate = event.getEndDate(event.getDuration() != null && event.getDuration().getValue() != null).getDate();

		if (occurrences != null && !occurrences.isEmpty()) {
			this.occurrences = TimeSpan.fromIcal(occurrences);
		} else {
			this.occurrences = Collections.emptyList();
		}

		if (this.occurrences.isEmpty()) {
			this.startDate = eventStartDate;
			this.endDate = eventEndDate;
		} else {
			Collections.sort(this.occurrences);
			final TimeSpan first = this.occurrences.get(0);
			this.startDate = first.getStart();
			this.endDate = first.getEnd();
		}

		if (event.getLocation() != null)
			this.location = event.getLocation().getValue();

		final Property rrule = event.getProperty("RRULE");
		this.repeats = rrule != null && rrule.getValue() != null;

		if (this.repeats)
			this.repeatRule = new RepeatRule(event);
		if (event.getDescription() != null)
			this.description = event.getDescription().getValue();
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

	public boolean isRepeats() {
		return repeats;
	}

	public void setRepeats(boolean repeats) {
		this.repeats = repeats;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public RepeatRule getRepeatRule() {
		return repeatRule;
	}

	public void setRepeatRule(RepeatRule repeatRule) {
		this.repeatRule = repeatRule;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Event");
		sb.append("{uid='").append(uid).append('\'');
		sb.append(", summary='").append(summary).append('\'');
		sb.append(", startDate=").append(startDate);
		sb.append(", endDate=").append(endDate);
		sb.append(", occurrences=").append(occurrences);
		sb.append(", location='").append(location).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", modified=").append(modified);
		sb.append(", url='").append(url).append('\'');
		sb.append(", repeatRule=").append(repeatRule);
		sb.append('}');
		return sb.toString();
	}
}
