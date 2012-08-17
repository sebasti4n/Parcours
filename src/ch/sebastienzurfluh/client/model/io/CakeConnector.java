/*
 * Copyright 2012 Sebastien Zurfluh
 * 
 * This file is part of "Parcours".
 * 
 * "Parcours" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * "Parcours" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with "Parcours".  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.sebastienzurfluh.client.model.io;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import ch.sebastienzurfluh.client.control.ModelAsyncPlug;
import ch.sebastienzurfluh.client.control.eventbus.events.DataType;
import ch.sebastienzurfluh.client.control.eventbus.events.ResourceType;
import ch.sebastienzurfluh.client.model.structure.Data;
import ch.sebastienzurfluh.client.model.structure.DataReference;
import ch.sebastienzurfluh.client.model.structure.MenuData;
import ch.sebastienzurfluh.client.model.structure.ResourceData;

/**
 * Handles communication with CakePHP which in turn handles the ADBMS.
 * @author Sebastien Zurfluh
 *
 */
public class CakeConnector implements IOConnector {
	private final static String CAKE_PATH = "http://www.sebastienzurfluh.ch/swissmuseumbooklets/cakePHP/index.php/";
	private final static String CAKE_SUFFIX = ".json";
	private final static String CAKE_ARGS_SEPARATOR = "/";

	public CakeConnector() {
	}
	
	private void asyncRequest(
			final Requests request,
			final int referenceId,
			final DataType expectedReturnDataType, 
			final ResourceType expectedReturnResourceType,
			String args,
			final ModelAsyncPlug<?> asyncPlug) {
		String url = CAKE_PATH + request.getURL();
		
		if (referenceId != -1) {
			if(args != null && !args.isEmpty()) {
				url += args + CAKE_ARGS_SEPARATOR + referenceId;
			} else {
				url += referenceId;
			}
		}
		url += CAKE_SUFFIX;
		
		System.out.println("Making async request on "+url);
		
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request httpRequest, Throwable exception) {
					System.out.println("JSON request failure. " + exception.getLocalizedMessage());
				}

				@SuppressWarnings("unchecked")
				public void onResponseReceived(Request httpRequest, Response response) {
					if (200 == response.getStatusCode()) {
						System.out.println("Got answer from async request.");
						JsArray<Entry> entries = convertToOverlayedJava(response.getText());
						
						switch(request) {
						case GETBOOKLETDATAOF:
						case GETCHAPTERDATAOF:
						case GETPAGEDATAOF:
						case GETPARENTOF:
							((ModelAsyncPlug<Data>)asyncPlug).update(parseData(entries.get(0), referenceId, expectedReturnDataType));
							break;
						case GETRESOURCEDATAOF:
							((ModelAsyncPlug<ResourceData>)asyncPlug).update(
									parseResourceData(entries.get(0), referenceId, expectedReturnResourceType));
							break;
						case GETALLBOOKLETMENUS:
						case GETSUBMENUOFBOOKLET:
						case GETSUBMENUOFCHAPTER:
						case GETSUBMENUOFPAGE:
							LinkedList<MenuData> dataList = new LinkedList<MenuData>();
							for (int i = 0; i < entries.length(); i++) {
								Entry entry = entries.get(i);
								dataList.add(parseMenuData(entry, referenceId, expectedReturnDataType));
							}
							((ModelAsyncPlug<Collection<MenuData>>)asyncPlug).update(dataList);
							break;
						default:
							throw new Error("Yo, the request is wrong.");
						}
					} else {
						System.out.println("JSON request failure. Status " + response.getStatusCode() + ".");
					}
				}
			});
		} catch (RequestException e) {
			System.out.println("JSON request failure. Request exception.");
		}
		
	}
	
	/**
	 * Converts json data into java objects.
	 * @param json
	 * @return a java converted
	 */
	private final native JsArray<Entry> convertToOverlayedJava(String json) /*-{
    	return eval(json);
  	}-*/;
	
	private Data parseData(Entry entry, int referenceId, DataType expectedDataType) {
		// Create data reference
		
		
		
		return new Data(
				getDataReference(expectedDataType, entry),
				Integer.parseInt(entry.getMenuPriorityNumber()), 
				entry.getPageTitle(),
				entry.getPageContentHeader(),
				entry.getPageContentBody(),
				entry.getMenuTitle(),
				entry.getMenuDescription(),
				entry.getMenuTileImgURL(),
				entry.getMenuSliderImgURL());
	}
	
	private MenuData parseMenuData(Entry entry, int referenceId, DataType expectedDataType) {
		return new MenuData(
				getDataReference(expectedDataType, entry),
				Integer.parseInt(entry.getMenuPriorityNumber()),
				entry.getMenuTitle(),
				entry.getMenuDescription(),
				entry.getMenuTileImgURL(),
				entry.getMenuSliderImgURL());
	}
	
	private ResourceData parseResourceData(Entry entry, int referenceId, ResourceType expectedResourceType) {
		return new ResourceData(
				new DataReference(DataType.RESOURCE, referenceId),
				expectedResourceType,
				entry.getResourceTitles(),
				entry.getResourceDetails(),
				entry.getResourceURL());
	}
	
	private DataReference getDataReference(DataType type, Entry entry) {
		switch(type) {
		case BOOKLET:
			return new DataReference(type, Integer.parseInt(entry.getBookletReference()));
		case CHAPTER:
			return new DataReference(type, Integer.parseInt(entry.getChapterReference()));
		case PAGE:
			return new DataReference(type, Integer.parseInt(entry.getPageReference()));
		default:
			return null;
		}
	}


	/**
	 *  list all possible requests and their command (url) in cake
	 */
	private enum Requests {
		GETALLBOOKLETMENUS("booklets/listMenus"),
		GETBOOKLETDATAOF("booklets/getData/"),
		GETCHAPTERDATAOF("chapters/getData/"),
		GETPAGEDATAOF("page_elements/getData/"),
		GETRESOURCEDATAOF("resources/getData/"),
		GETSUBMENUOFBOOKLET("chapters/listAttachedToBooklet/"),
		GETSUBMENUOFCHAPTER("page_elements/listAttachedToChapter/"),
		GETSUBMENUOFPAGE("resources/listAttachedToPage/"),
		GETPARENTOF("page_datas/getParentOf/");


		String request;
		Requests(String request) {
			this.request = request;
		}

		public String getURL() {
			return request;
		}
	}


	@Override
	public void getAllBookletMenus(ModelAsyncPlug<Collection<MenuData>> asyncPlug) {
		System.out.println("Async request made: getAllBookletMenus.");
		asyncRequest(
				Requests.GETALLBOOKLETMENUS,
				-1,
				DataType.BOOKLET,
				null,
				"",
				asyncPlug);
	}

	@Override
	public void getBookletDataOf(ModelAsyncPlug<Data> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETBOOKLETDATAOF,
				referenceId,
				DataType.BOOKLET,
				null, 
				"",
				asyncPlug);
	}

	@Override
	public void getChapterDataOf(ModelAsyncPlug<Data> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETCHAPTERDATAOF,
				referenceId,
				DataType.CHAPTER,
				null, 
				"",
				asyncPlug);
	}

	@Override
	public void getPageDataOf(ModelAsyncPlug<Data> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETPAGEDATAOF,
				referenceId,
				DataType.PAGE,
				null, 
				"",
				asyncPlug);
	}

	@Override
	public void getRessourceDataOf(ModelAsyncPlug<ResourceData> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETRESOURCEDATAOF,
				referenceId,
				DataType.RESOURCE,
				ResourceType.UNDEFINED, 
				"",
				asyncPlug);
	}

	@Override
	public void getSubMenusOfBooklet(ModelAsyncPlug<Collection<MenuData>> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETSUBMENUOFBOOKLET,
				referenceId,
				DataType.CHAPTER,
				null, 
				"",
				asyncPlug);
	}

	@Override
	public void getSubMenusOfChapter(ModelAsyncPlug<Collection<MenuData>> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETSUBMENUOFCHAPTER,
				referenceId,
				DataType.PAGE,
				null, 
				"",
				asyncPlug);
	}

	@Override
	public void getSubMenusOfPage(ModelAsyncPlug<Collection<MenuData>> asyncPlug, int referenceId) {
		asyncRequest(
				Requests.GETSUBMENUOFPAGE,
				referenceId,
				DataType.RESOURCE,
				null,
				"",
				asyncPlug);
	}

	@Override
	public void getParentOf(ModelAsyncPlug<Data> asyncPlug,
			DataReference childReference) {
		DataType parentType = null;
		String parentTypeString = null;
		switch(childReference.getType()) {
		case RESOURCE:
			parentType = DataType.PAGE;
			parentTypeString = "pages";
			break;
		case PAGE:
			parentType = DataType.CHAPTER;
			parentTypeString = "chapters";
			break;
		case CHAPTER:
			parentType = DataType.BOOKLET;
			parentTypeString = "booklets";
			break;
		case BOOKLET:
			parentType = DataType.SUPER;
			break;
		case SUPER:
			break;
		}
		asyncRequest(
				Requests.GETPARENTOF,
				childReference.getReferenceId(),
				parentType,
				null,
				parentTypeString,
				asyncPlug);
	}
}

class Entry extends JavaScriptObject {
	protected Entry() {}
	
	// Reference ids
	public final native String getBookletReference() /*-{
		return this.booklets.id;
	}-*/;
	
	public final native String getChapterReference() /*-{
		return this.chapters.id;
	}-*/;
	
	public final native String getPageReference() /*-{
		return this.pages.id;
	}-*/;
	
	// Page
	public final native String getPageTitle() /*-{
		return this.page_datas.title;
	}-*/;
	
	public final native String getPageContentHeader() /*-{
		return this.page_datas.subtitle;
	}-*/;
	
	public final native String getPageContentBody() /*-{
		return this.page_datas.content;
	}-*/;
	
	// Menu
	public final native String getMenuTitle() /*-{
		return this.menus.title;
	}-*/;
	
	public final native String getMenuDescription() /*-{
		return this.menus.description;
	}-*/;
	
	public final native String getMenuPriorityNumber() /*-{
		return this.menus.priority_number;
	}-*/;
	
	public final native String getMenuTileImgURL() /*-{
		return this.menus.tile_url_img;
	}-*/;
	
	public final native String getMenuSliderImgURL() /*-{
		return this.menus.slider_url_img;
	}-*/;
	
	// Resource
	public final native String getResourceTitles() /*-{
    	return this.resources.title;
	}-*/;
	
	public final native String getResourceDetails() /*-{
    	return this.resources.details;
	}-*/;
	
	public final native String getResourceURL() /*-{
	   	return this.resources.url;
	}-*/;
}