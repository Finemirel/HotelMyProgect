package com.example.hotelsApp;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class HotelUI extends UI {
	
	final VerticalLayout layout = new VerticalLayout();
	final HotelService hotelService = HotelService.getInstance();
	final Grid<Hotel> grid = new Grid<>();
	final TextField filterName = new TextField("Name");
	final TextField filterAddress = new TextField("Address");
	final Button addHotel = new Button("Add hotel");
    final Button deleteHotel = new Button("Delete hotel");
    private HotelEditForm form = new HotelEditForm(this);

    @Override
    protected void init(VaadinRequest vaadinRequest) {
    	final HorizontalLayout controller = new HorizontalLayout();
    	controller.addComponents(filterName, filterAddress, addHotel, deleteHotel);
    	deleteHotel.setEnabled(false);
    	
    	
        final VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        Column<Hotel, String> name = grid.addColumn(Hotel::getName);
        name.setCaption("Name");
        Column<Hotel, String> reiting = grid.addColumn(Hotel::getRating);
        reiting.setCaption("Reiting");
        Column<Hotel, String> address = grid.addColumn(Hotel::getAddress);
        address.setCaption("Address");
        Column<Hotel, HotelCategory> category = grid.addColumn(Hotel::getCategory);
        category.setCaption("Category");
        Column<Hotel, String> url = grid.addColumn(Hotel -> "<a href='" + Hotel.getUrl() + "' target='_top'>link</a>",
        	      new HtmlRenderer());
        url.setCaption("Url");
        Column<Hotel, Long> id = grid.addColumn(Hotel::getId);
        id.setCaption("Id");
        Column<Hotel, LocalDate> date = grid.addColumn(Hotel::getOperatesFrom);
        date.setCaption("Date");
        Column<Hotel, String> description = grid.addColumn(Hotel::getDescription);
        description.setCaption("Description");

        //gdhsgsg
        grid.setWidth(800, Unit.PIXELS);
        
        grid.setVisible(true);
        final HorizontalLayout content = new HorizontalLayout();       
        layout.addComponents(controller, content);
        content.addComponents(grid, form);
        form.setVisible(false);
        
        grid.asSingleSelect().addValueChangeListener(e -> {
        	if(e.getValue()!=null){
        		deleteHotel.setEnabled(true);
        		form.setHotel(e.getValue());
        	}       	
        });
        
        filterName.addValueChangeListener(e -> updateListForName());
        filterName.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        filterAddress.addValueChangeListener(e -> updateListForAddress());
        filterAddress.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        addHotel.addClickListener(e -> form.setHotel(new Hotel()));
        
        deleteHotel.addClickListener(e -> {
        	Hotel delCandidate = grid.getSelectedItems().iterator().next();
        	hotelService.delete(delCandidate);
        	deleteHotel.setEnabled(false);
        	updateList();
        	form.setVisible(false);
        });
        
        
    }
    
    public void updateListForName() {
    	deleteHotel.setEnabled(false);
    	List<Hotel> hotelList = hotelService.findName(filterName.getValue());
    	grid.setItems(hotelList);
    }
    
    public void updateListForAddress() {
    	deleteHotel.setEnabled(false);
    	List<Hotel> hotelList = hotelService.findAddress(filterAddress.getValue());
    	grid.setItems(hotelList);
    }
    
    public void updateList() {
		grid.setItems(hotelService.findAll());	
	}

    @WebServlet(urlPatterns = "/*", name = "HotelUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = HotelUI.class, productionMode = false)
    public static class HotelUIServlet extends VaadinServlet {
    }
}
