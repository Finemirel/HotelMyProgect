package com.example.hotelsApp;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
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
	final Grid<Hotel> hotelGrid = new Grid<>();
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
    	controller.setComponentAlignment(addHotel, Alignment.BOTTOM_CENTER);
    	controller.setComponentAlignment(deleteHotel, Alignment.BOTTOM_LEFT);
    	controller.setWidth(60, Unit.PERCENTAGE);
    	
    	
        final VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        Column<Hotel, String> name = hotelGrid.addColumn(Hotel::getName);
        name.setCaption("Name");
        Column<Hotel, String> reiting = hotelGrid.addColumn(Hotel::getRating);
        reiting.setCaption("Reiting");
        Column<Hotel, String> address = hotelGrid.addColumn(Hotel::getAddress);
        address.setCaption("Address");
        Column<Hotel, HotelCategory> category = hotelGrid.addColumn(Hotel::getCategory);
        category.setCaption("Category");
        Column<Hotel, String> url = hotelGrid.addColumn(Hotel -> "<a href='" + Hotel.getUrl() + "' target='_blank'>link</a>",
        	      new HtmlRenderer());
        url.setCaption("Url");
        Column<Hotel, Long> id = hotelGrid.addColumn(Hotel::getId);
        id.setCaption("Id");
        Column<Hotel, LocalDate> date = hotelGrid.addColumn(Hotel::getOperatesFrom);
        date.setCaption("Date");
        Column<Hotel, String> description = hotelGrid.addColumn(Hotel::getDescription);
        description.setCaption("Description");
        
        hotelGrid.setHeightUndefined();

        hotelGrid.setWidth(95, Unit.PERCENTAGE);
        
        hotelGrid.setVisible(true);
        final HorizontalLayout content = new HorizontalLayout();       
        layout.addComponents(controller, content);
        content.addComponents(hotelGrid, form);
        content.setWidth(100, Unit.PERCENTAGE);
        content.setComponentAlignment(form, Alignment.TOP_RIGHT);
        form.setVisible(false);
        form.setCaption("Edit / add hotel");
        
        hotelGrid.asSingleSelect().addValueChangeListener(e -> {
        	if(e.getValue()!=null){
        		deleteHotel.setEnabled(true);
        		form.setHotel(e.getValue());
        	}       	
        });
        
        filterName.addValueChangeListener(e -> updateList());
        filterName.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        filterAddress.addValueChangeListener(e -> updateList());
        filterAddress.setValueChangeMode(ValueChangeMode.LAZY);
        updateList();
        
        addHotel.addClickListener(e -> {form.setHotel(new Hotel());
            deleteHotel.setEnabled(false);
        });
        
        deleteHotel.addClickListener(e -> {
        	Hotel delCandidate = hotelGrid.getSelectedItems().iterator().next();
        	hotelService.delete(delCandidate);
        	deleteHotel.setEnabled(false);
        	updateList();
        	form.setVisible(false);
        });
        
        
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
