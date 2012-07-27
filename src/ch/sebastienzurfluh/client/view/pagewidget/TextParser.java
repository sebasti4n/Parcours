package ch.sebastienzurfluh.client.view.pagewidget;

import ch.sebastienzurfluh.client.control.eventbus.events.DataType;
import ch.sebastienzurfluh.client.model.Model;
import ch.sebastienzurfluh.client.model.structure.DataReference;
import ch.sebastienzurfluh.client.model.structure.ResourceData;

public class TextParser {
	private static final String IMG_BALISE_START = "[img]";
	private static final String IMG_BALISE_END = "[/img]";
	Model model;
	
	public TextParser(Model model) {
		this.model = model;
	}
	
	public String parse(String text) {
		StringBuilder parsed = new StringBuilder(text);
		
		parsed = parseImages(parsed);
		
		
		return parsed.toString();
	}
	
	private StringBuilder parseImages(StringBuilder textToParse) {
		String parsing = textToParse.toString();
		StringBuilder parsed = new StringBuilder();
		
		while (!parsing.isEmpty()) {
			int imgDefBegin = parsing.indexOf(IMG_BALISE_START);
			int imgDefEnd = parsing.indexOf(IMG_BALISE_END);
			
			if(imgDefBegin == -1 || imgDefEnd == -1) {
				parsed.append(parsing);
				
				break;
			} else {
				parsed.append(parsing.substring(0, imgDefBegin));
				
				parsed.append(getImageHTML(
						new DataReference(
								DataType.RESOURCE,
								Integer.parseInt(
										parsing.substring(
												imgDefBegin + IMG_BALISE_START.length(),
												imgDefEnd)))));
				
				parsing = parsing.substring(imgDefEnd + IMG_BALISE_END.length());
			}
		}
		return parsed;
	}
	
	private String primaryStyle = "pageWidget-image";
	private String containerExtension = "-container";
	private String titleExtension = "-title";
	private String detailsExtension = "-details";
	private String imageExtension = "-image";
	
	private String getImageHTML(DataReference reference) {
		ResourceData resource = model.getResourceData(reference);
		
		return 
			"<div class=\"" + primaryStyle+containerExtension + "\"> " +
				"<img class=\"" + primaryStyle+imageExtension + "\"src=\"" + 
				resource.getURL() + "\" alt=\"" + resource.getTitle() + "\"></img>" +
				"<div class=\"" + primaryStyle+titleExtension + "\">" +
					resource.getTitle() +
				"</div>" +
				"<div class=\"" + primaryStyle+detailsExtension + "\">" +
					resource.getDetails() +
				"</div>" +
			"</div>";
	}
}