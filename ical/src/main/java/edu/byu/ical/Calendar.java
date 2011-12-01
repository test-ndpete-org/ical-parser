package edu.byu.ical;

/**
 * User: wct5
 * Date: 11/11/11
 * Time: 12:10 PM
 */
public class Calendar{

	private String name;
	private String desc;
	private String rssUrl;

	/**
	 *
	 */
	public Calendar() {
	}

	/**
	 * @param desc
	 * @param name
	 * @param rssUrl
	 */
	public Calendar(final String desc, final String name, final String rssUrl) {
		this.desc = desc;
		this.name = name;
		this.rssUrl = rssUrl;
	}

	/** @return result */
	public String getDesc() {
		return desc;
	}

	/** @param desc */
	public void setDesc(final String desc) {
		this.desc = desc;
	}

	/** @return result */
	public String getName() {
		return name;
	}

	/** @param name */
	public void setName(final String name) {
		this.name = name;
	}

	/** @return result */
	public String getRssUrl() {
		return rssUrl;
	}

	/** @param rssUrl */
	public void setRssUrl(final String rssUrl) {
		this.rssUrl = rssUrl;
	}

	@Override
	public String toString() {
		return "Calendar{" + "name='" + name + '\'' + ", desc='" + desc + '\'' + ", rssUrl='" + rssUrl + '\'' + '}';
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Calendar)) return false;

		Calendar calendar = (Calendar) o;

		if (name != null ? !name.equals(calendar.name) : calendar.name != null) return false;
		if (rssUrl != null ? !rssUrl.equals(calendar.rssUrl) : calendar.rssUrl != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (rssUrl != null ? rssUrl.hashCode() : 0);
		return result;
	}

}
