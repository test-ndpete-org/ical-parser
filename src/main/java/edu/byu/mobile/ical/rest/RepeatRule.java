package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * @author jmooreoa
 */
public class RepeatRule {
	private String firstOccurrenceStart;
	private String firstOccurrenceEnd;
	private String rule;
	private String exceptions;

	public RepeatRule(VEvent event) {
		this.firstOccurrenceStart = event.getStartDate().getValue();
		this.firstOccurrenceEnd = event.getEndDate(event.getDuration() != null && event.getDuration().getValue() != null).getValue();
		if (event.getProperty("RRULE") != null) {
			this.rule = event.getProperty("RRULE").getValue();
		}
		if (event.getProperty("EXDATE") != null) {
			this.exceptions = event.getProperty("EXDATE").getValue();
		}
	}

	public String getFirstOccurrenceStart() {
		return firstOccurrenceStart;
	}

	public void setFirstOccurrenceStart(String firstOccurrenceStart) {
		this.firstOccurrenceStart = firstOccurrenceStart;
	}

	public String getFirstOccurrenceEnd() {
		return firstOccurrenceEnd;
	}

	public void setFirstOccurrenceEnd(String firstOccurrenceEnd) {
		this.firstOccurrenceEnd = firstOccurrenceEnd;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getExceptions() {
		return exceptions;
	}

	public void setExceptions(String exceptions) {
		this.exceptions = exceptions;
	}
}
