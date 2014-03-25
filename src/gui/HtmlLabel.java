package gui;

public class HtmlLabel {
	
	final String HTML = "<HTML>";
	final String LINE_BREAK = "<BR>";
	final String PARAGRAPH = "<P>";
	
	private String text, body;
	private int lineNbr;
	
	public HtmlLabel(String text) {
		lineNbr = 0;
		body = prepareBodyText(text);
		makeText();
	}
	
	public HtmlLabel() {
		this("");
	}
	
	public void addLine(String text) {
		lineNbr++;
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
	
	private String prepareBodyText(String body) {
		return lineNbr + ": " + body + LINE_BREAK;
	}
	
	private void makeText() {
		text = HTML + body + endOf(HTML);
	}
}
