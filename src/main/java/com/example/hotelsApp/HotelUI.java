package com.example.hotelsApp;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;

/**
* This UI is the application entry point. A UI may either represent a browser
* window (or tab) or some part of an HTML page where a Vaadin application is
* embedded.
* <p>
* The UI is initialized using {@link #init(VaadinRequest)}. This method is
* intended to be overridden to add component to the user interface and
* initialize non-component functionality.
*/
@Theme("mytheme")
public class HotelUI extends UI {
	
	final VerticalLayout layout = new VerticalLayout();
	final HotelService hotelService = HotelService.getInstance();
	final Grid<Hotel> hotelGrid = new Grid<>();
	final TextField filterName = new TextField("Name");
	final TextField filterAddress = new TextField("Address");
	final Button addHotel = new Button("Add hotel");
	final MenuBar menu = new MenuBar();
	final Label status = new Label();
    final Button deleteHotel = new Button("Delete hotel");
    final Button editHotel = new Button("Edit hotel");
    final Button editCategory = new Button("Edit category");
    private HotelEditForm form = new HotelEditForm(this);
    final HorizontalLayout controller = new HorizontalLayout();
    final HorizontalLayout content = new HorizontalLayout();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	deleteHotel.setEnabled(false);
    	MenuBar.Command command = new MenuBar.Command() {
            MenuItem previous = null;
            
            public void menuSelected (MenuItem selectedItem) {
                String notification = "You click: " + selectedItem.getText();
                status.setValue(notification);
                
                if (previous != null) {
                	previous.setStyleName(null);
                }
                selectedItem.setStyleName("hi");
                
                previous = selectedItem;
            }
        };
        
        MenuItem hotelItem = menu.addItem("Hotel", VaadinIcons.BUILDING, command);
        MenuItem categoryItem = menu.addItem("Category", VaadinIcons.RECORDS, command);
        
        command.menuSelected(hotelItem);
        
        menu.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
        Column<Hotel, String> name = hotelGrid.addColumn(Hotel::getName);
        name.setCaption("Name");
        Column<Hotel, Integer> reiting = hotelGrid.addColumn(Hotel::getRating);
        reiting.setCaption("Reiting");
        Column<Hotel, String> address = hotelGrid.addColumn(Hotel::getAddress);
        address.setCaption("Address");
        Column<Hotel, String> category = hotelGrid.addColumn(Hotel::getCategory);
        category.setCaption("Category");
        Column<Hotel, String> url = hotelGrid.addColumn(Hotel -> "<a href='" + Hotel.getUrl() + "' target='_blank'>link</a>",
        	      new HtmlRenderer());
        url.setCaption("Url");
        Column<Hotel, Long> id = hotelGrid.addColumn(Hotel::getId);
        id.setCaption("Id");
        Column<Hotel, LocalDate> date = hotelGrid.addColumn(Hotel -> LocalDate.ofEpochDay(Hotel.getOperatesFrom()));
        date.setCaption("Date");
        Column<Hotel, String> description = hotelGrid.addColumn(Hotel::getDescription);
        description.setCaption("Description");
        
        hotelGrid.setHeightUndefined();

        hotelGrid.setWidth(95, Unit.PERCENTAGE);
        hotelGrid.setHeight(540, Unit.PIXELS);
        
        hotelGrid.setVisible(true);
        controller(controller);
		layout.addComponents(menu, status, controller, content);
		setContent(layout);
        content.addComponents(hotelGrid, form);
        content.setWidth(100, Unit.PERCENTAGE);
        content.setComponentAlignment(form, Alignment.TOP_RIGHT);

        form.setVisible(false);
        form.setCaption("Edit / add hotel");
        
        filterName.addValueChangeListener(e -> updateList());
        filterName.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        filterAddress.addValueChangeListener(e -> updateList());
        filterAddress.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        addHotel.addClickListener(e -> {
        	form.setHotel(new Hotel());
        	});
       
        deleteHotel.addClickListener(e -> {
			Iterator<Hotel> candidates = hotelGrid.getSelectedItems().iterator();
        	while (candidates.hasNext()) {
        		hotelService.delete(candidates.next());
        	}        	
        	deleteHotel.setEnabled(false);
        	updateList();
        });
        
        editHotel.setEnabled(false);
		editHotel.addClickListener(e -> {
        	Hotel candidate = hotelGrid.getSelectedItems().iterator().next();
        	form.setHotel(candidate);
        });
		
		editCategory.setEnabled(false);
		editCategory.addClickListener(e -> {
        	Hotel candidate = hotelGrid.getSelectedItems().iterator().next();
        	form.setHotel(candidate);
        	updateList();
        });
        
        hotelGrid.asSingleSelect().addValueChangeListener(e -> {
        	if(e.getValue()!=null){
        		deleteHotel.setEnabled(true);
        		form.setHotel(e.getValue());
        	}       	
        }); 
        
        hotelGrid.setSelectionMode(SelectionMode.MULTI);
        hotelGrid.asMultiSelect().addSelectionListener(listener());
    }

	
	private MultiSelectionListener<Hotel> listener () {
        return e -> {
            Set<Hotel> value = e.getValue();
            
            if (value.size() == 0) {
                deleteHotel.setEnabled(false);
                editHotel.setEnabled(false);
                addHotel.setEnabled(true);
            }
            if (value.size() == 1) {
                editHotel.setEnabled(true);
                deleteHotel.setEnabled(true);
                addHotel.setEnabled(false);
            }
            if (value.size() > 1) {
                editHotel.setEnabled(false);
                deleteHotel.setEnabled(true);
                addHotel.setEnabled(false);
            }
        };
    }

	private void menu() {
		
	}

	private void controller(final HorizontalLayout controller) {
		controller.addComponents(filterName, filterAddress, addHotel, deleteHotel, editHotel, editCategory);
		controller.setComponentAlignment(addHotel, Alignment.BOTTOM_LEFT);
    	controller.setComponentAlignment(deleteHotel, Alignment.BOTTOM_LEFT);
    	controller.setComponentAlignment(editHotel, Alignment.BOTTOM_LEFT);
    	controller.setComponentAlignment(editCategory, Alignment.BOTTOM_LEFT);
	}
    
    public void updateList() {
    	deleteHotel.setEnabled(false);
    	List<Hotel> hotelList = hotelService.findAll(filterName.getValue(), filterAddress.getValue());
    	hotelGrid.setItems(hotelList);
    }
    

    @WebServlet(urlPatterns = "/*", name = "HotelUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = HotelUI.class, productionMode = false)
    public static class HotelUIServlet extends VaadinServlet {
    }
}