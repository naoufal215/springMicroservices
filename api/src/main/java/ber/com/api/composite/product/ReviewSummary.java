package ber.com.api.composite.product;

public class ReviewSummary {
	
	private  int reviewId;
	private  String author;
	private  String subject;
	private  String content;
	
	
	
	
	public ReviewSummary() {
		this.reviewId = 0;
		this.author = null;
		this.subject = null;
		this.content = null;
	}


	public ReviewSummary(int reviewId, String author, String subject, String content) {
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
	}


	public int getReviewId() {
		return reviewId;
	}


	public String getAuthor() {
		return author;
	}


	public String getSubject() {
		return subject;
	}
	
	public String getContent() {
		return content;
	}
	
	


	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public void setSubject(String subject) {
		this.subject = subject;
	}


	public void setContent(String content) {
		this.content = content;
	}


	@Override
	public String toString() {
		return "ReviewSummary [reviewId=" + reviewId + ", author=" + author + ", subject=" + subject + ", content="
				+ content + "]";
	}
	
	
	
	

}
