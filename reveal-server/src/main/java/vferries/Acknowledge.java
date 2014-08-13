package vferries;

public class Acknowledge {
	private String message;
	private int slideNumber;
	private int totalSlides;

	public Acknowledge(String message, int slideNumber, int totalSlides) {
		super();
		this.message = message;
		this.slideNumber = slideNumber;
		this.totalSlides = totalSlides;
	}

	public int getTotalSlides() {
		return totalSlides;
	}

	public void setTotalSlides(int totalSlides) {
		this.totalSlides = totalSlides;
	}

	public int getSlideNumber() {
		return slideNumber;
	}

	public void setSlideNumber(int slideNumber) {
		this.slideNumber = slideNumber;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
