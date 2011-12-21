package edu.byu.mobile.ical.rest;

import java.util.Date;

/**
 * @author jmooreoa
 */
public class OccurrencesRequest {
	private String start;
	private TimePeriod show;
	private String until;
	private String ruleStart;
	private String ruleEnd;
	private String rule;
	private String exceptions;

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public TimePeriod getShow() {
		return show;
	}

	public void setShow(TimePeriod show) {
		this.show = show;
	}

	public String getUntil() {
		return until;
	}

	public void setUntil(String until) {
		this.until = until;
	}

	public String getRuleStart() {
		return ruleStart;
	}

	public void setRuleStart(String ruleStart) {
		this.ruleStart = ruleStart;
	}

	public String getRuleEnd() {
		return ruleEnd;
	}

	public void setRuleEnd(String ruleEnd) {
		this.ruleEnd = ruleEnd;
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
