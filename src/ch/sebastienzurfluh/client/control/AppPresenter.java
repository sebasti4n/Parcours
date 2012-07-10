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

package ch.sebastienzurfluh.client.control;

import ch.sebastienzurfluh.client.control.eventbus.EventBus;
import ch.sebastienzurfluh.client.control.eventbus.PageRequestEventHandler;
import ch.sebastienzurfluh.client.control.eventbus.events.DataType;
import ch.sebastienzurfluh.client.control.eventbus.events.PageChangeEvent;
import ch.sebastienzurfluh.client.model.Model;
import ch.sebastienzurfluh.client.model.structure.DataReference;
import ch.sebastienzurfluh.client.view.View;

import com.google.gwt.user.client.ui.Panel;

/**
 * This class creates the web app and adds it to the given panel.
 * @author Sebastien Zurfluh
 *
 */
public class AppPresenter {
	private Panel parent;
	private EventBus eventBus;
	private Model model;
	
	public AppPresenter(Panel parent) {
		this.parent = parent;
	}

	/**
	 * Loads and display the application in the panel defined at construction.
	 */
	public void start() {
		eventBus = new EventBus();

		model = ModelFactory.createModel(ModelFactory.Connector.TEST);
		
		
		PageRequestEventHandler pageRequestHandler = new PageRequestEventHandler(eventBus, model);
		
		View view = new View(eventBus, pageRequestHandler, model);
		
		parent.add(view);
		
		// Start the app
//		eventBus.fireEvent(new PageChangeEvent(DataType.SUPER, null));
		
		// test
		eventBus.fireEvent(new PageChangeEvent(DataType.BOOKLET, model.getAssociatedData(new DataReference(DataType.BOOKLET, 1))));
		eventBus.fireEvent(new PageChangeEvent(DataType.CHAPTER, model.getAssociatedData(new DataReference(DataType.CHAPTER, 1))));
		eventBus.fireEvent(new PageChangeEvent(DataType.PAGE, model.getAssociatedData(new DataReference(DataType.PAGE, 1))));
		eventBus.fireEvent(new PageChangeEvent(DataType.SUPER, null));
	}
}