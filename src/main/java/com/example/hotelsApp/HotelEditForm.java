package com.example.hotelsApp;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

import com.vaadin.data.Binder;
import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValidationException;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Notification.Type;

public class HotelEditForm extends FormLayout {
    private HotelUI ui;
    private HotelService service = HotelService.getInstance();
    private Hotel hotel;
    private Binder<Hotel> binder = new Binder<>(Hotel.class);
    private TextField name = new TextField("Name");
    private TextField address = new TextField("Address");
    private TextField rating = new TextField("Rating");
    private DateField operatesFrom = new DateField("Date");
    private NativeSelect<String> category = new NativeSelect<>("Category");
    private TextArea description = new TextArea("Description");
    private TextField url = new TextField("URL");

    private Button save = new Button("Save");
    private Button close = new Button("Close");
    private HorizontalLayout buttons = new HorizontalLayout();

    public HotelEditForm (HotelUI ui) {
        this.ui = ui;
        this.setVisible(false);

        String required = "Please enter a ";
        binder.forField(name).asRequired(required + "name").bind(Hotel::getName, Hotel::setName);
        binder.forField(address).asRequired(required + "address").withValidator(adressValidator()).bind(Hotel::getAddress, Hotel::setAddress);
        binder.forField(rating).asRequired(required + "rating").withConverter(ratingConverter()).bind(Hotel::getRating, Hotel::setRating);
        binder.forField(operatesFrom).asRequired(required + "opening date").withConverter(dateConverter()).bind(Hotel::getOperatesFrom, Hotel::setOperatesFrom);
        binder.forField(category).asRequired(required + "category").bind(Hotel::getCategory, Hotel::setCategory);
        binder.forField(description).bind(Hotel::getDescription, Hotel::setDescription);
        binder.forField(url).asRequired(required + "url").bind(Hotel::getUrl, Hotel::setUrl);
        
        name.setDescription("Hotel name");
        address.setDescription("Hotel address");

        buttons.addComponents(save, close);

        save.addClickListener(e -> save());
        close.addClickListener(e -> exit());

        category.setItems(service.getCategories());

        addComponents(name, address, rating, operatesFrom, category, description, url, buttons);
        binder.bindInstanceFields(this);
    }


    private Converter<String, Integer> ratingConverter () {
        return new Converter<String, Integer>() {
            @Override
            public Result<Integer> convertToModel (String value, ValueContext context) {
                int parseInt = -1;
                try {
                    parseInt = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    Result.error("Wrong number");
                }
                if (!(parseInt >= 0 && parseInt < 6)) return Result.error("value must be beetween 0 and 5 inclusive");
                return Result.ok(parseInt);
            }

            @Override
            public String convertToPresentation (Integer value, ValueContext context) {
                if (value == null) return "";
                return value + "";
            }
        };
    }

    private Converter<LocalDate, Long> dateConverter () {
        return new Converter<LocalDate, Long>() {

            @Override
            public Result<Long> convertToModel (LocalDate value, ValueContext context) {
                if (!value.isBefore(LocalDate.now())) return Result.error("Wrong date. Should be until the current moment");;
                return Result.ok(value.getLong(ChronoField.EPOCH_DAY));
            }

            @Override
            public LocalDate convertToPresentation (Long value, ValueContext context) {
                if (value == null) return null;
                return LocalDate.ofEpochDay(value);
            }
        };
    }

    private Validator<String> adressValidator () {
        Validator<String> adressValidator = new Validator<String>() {
            @Override
            public ValidationResult apply (String value, ValueContext context) {
                if (value.length() < 5) return ValidationResult.error("The adress is too short");
                return ValidationResult.ok();
            }
        };
        return adressValidator;
    }

    private void exit () {
        setVisible(false);
        hotel = null;
    }

    private void save () {
        if (binder.isValid()) {
            try {
                binder.writeBean(hotel);
            } catch (ValidationException e) {
                Notification.show("Unable to save! " + e.getMessage(), Type.ERROR_MESSAGE);
            }
            service.save(hotel);
            ui.updateList();
            Notification.show("Hotel " + hotel.getName() + " saved", Type.TRAY_NOTIFICATION);
            exit();
        } else Notification.show("Unable to save! please review errors and fill in all the required fields", Type.ERROR_MESSAGE);
    }

    public Hotel getHotel () {
        return hotel;
    }

    public void setHotel (Hotel hotel) {
        this.hotel = hotel;
        binder.readBean(this.hotel);
        setVisible(true);
    }
}

