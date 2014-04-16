package gui;

public class HtmlLabel {
	
	final String HTML = "HTML";
	final String LINE_BREAK = "BR";
	final String PARAGRAPH = "P";
	final String DIV = "DIV";
	
	private String text, body;
	
	public HtmlLabel(String text) {
		body = prepareBodyText(text);
		makeText();
	}
	
	public HtmlLabel() {
		this("");
	}
	
	public void addLine(String text) {
		body += prepareBodyText(text);
		makeText();
	}
	
	public void setText(String text) {
		body = prepareBodyText(text);
		makeText();
	}

	public String getText() {
		return text;
	}
	
	private String endOf(String tag) {
		return "/" + tag;
	}
	
	private String makeTag(String tag) {
		return "<" + tag + ">";
	}
	
	private String prepareBodyText(String body) {
		return body + makeTag(LINE_BREAK);
	}
	
	private void makeText() {
		text = makeTag(HTML) + body + makeTag(endOf(HTML));
	}
}
