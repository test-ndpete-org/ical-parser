package edu.byu.mobile.ical.rest;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;

/**
 * @author jmooreoa
 */
@JsonAutoDetect(value = JsonMethod.FIELD)
public class OccurrencesRequest {
	private String start;
	private TimePeriod offset;
	private TimePeriod show;
	private String until;
	private String ruleStart;
	private String ruleEnd;
	private String rule;
	private String exceptions;

	public TimePeriod getOffset() {
		return offset;
	}

	public void setOffset(TimePeriod offset) {
		this.offset = offset;
	}

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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("OccurrencesRequest");
		sb.append("{start='").append(start).append('\'');
		sb.append(", offset=").append(offset);
		sb.append(", show=").append(show);
		sb.append(", until='").append(until).append('\'');
		sb.append(", ruleStart='").append(ruleStart).append('\'');
		sb.append(", ruleEnd='").append(ruleEnd).append('\'');
		sb.append(", rule='").append(rule).append('\'');
		sb.append(", exceptions='").append(exceptions).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
