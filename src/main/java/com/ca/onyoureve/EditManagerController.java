package com.ca.onyoureve;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EditManagerController {

    @FXML private JFXTextField address;
    @FXML private JFXTextField fname;
    @FXML private JFXTextField lname;
    @FXML private JFXTextField nid;
    @FXML private DatePicker dob;
    @FXML private JFXTextField accountno;
    @FXML private JFXTextField newpass;
    @FXML private JFXTextField curpass;
    @FXML private JFXTextField email;
    @FXML private JFXTextField emp_type;
    @FXML private JFXTextField salary_rate;
    @FXML private JFXTextField mgrid;
    @FXML private JFXTextField contact;

    //////////// BUTTONS /////////

    @FXML private JFXButton exit_btn;
    @FXML private JFXButton save_btn;

    //////////////////////////////////////////////////////////////////////////

    public void init() {
        System.out.println("Initialising edit info variables");

        Employee emp = LoggedUser.getEmp();

        // set values
        address.setText(emp.getAddress());
        nid.setText(emp.getNid());
        emp_type.setText(emp.getEmp_type());
        salary_rate.setText(Integer.toString(emp.getSalary_rate()));
        mgrid.setText(emp.getMgr_id());
        contact.setText(emp.getPhone_no());

        // get values
        String firstName = "", lastName = "";
        String fullName = emp.getName();
        String[] tokens = fullName.split(" ", 2);
        firstName = tokens[0];
        lastName = tokens[1];

        String date = emp.getDob();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        try {
            dob.setValue(localDate);
        }

        catch (NullPointerException e) {
            System.out.println("Date not picked up");
        }

        // set values
        fname.setText(firstName);
        lname.setText(lastName);

        accountno.setText(emp.getAcc_no());
        email.setText(emp.getEmail());

        // disable editing
        dob.setEditable(false);
        dob.setDisable(true);
        dob.setOpacity(0.7);
        mgrid.setEditable(false);
    }

    public boolean checkInputs() throws IOException, LineUnavailableException, UnsupportedAudioFileException {

        // check for null values
        if (fname == null || nid == null || accountno == null || email == null) {
            openPopup("Invalid Input", "Please fill all the fields.");
            return false;
        }

        // check for blank values
        if (fname.getText().isEmpty()|| nid.getText().isEmpty() || accountno.getText().isEmpty() || email.getText().isEmpty()) {
            openPopup("Invalid Input", "Please fill all the fields.");
            return false;
        }

        // null values or blank values
        if (contact == null || contact.getText().isEmpty()) {
            openPopup("Missing Input", "Contact number should not be blank.");
            return false;
        }

        // check if cnic has any letters
        if (nid.getText().matches(".*[a-zA-Z]+.*")) {
            openPopup("Invalid Input", "CNIC should contain only numbers and dashes.");
            return false;
        }

        // check if cnic has any letters
        if (accountno.getText().matches(".*[a-zA-Z]+.*")) {
            openPopup("Invalid Input", "Account number should contain only numbers.");
            return false;
        }

        // invalid email address
        if (email.getText().indexOf('@') == -1) {
            openPopup("Invalid Email", "Please enter a valid email.");
            return false;
        }

        return true;
    }

    //////////////////////////////////////////////////////

    public void handleExitButton(ActionEvent actionEvent) throws IOException {
        System.out.println("Exit button pressed.");
        goToMgrMenu();
    }

    public void handleSaveButton(ActionEvent actionEvent) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        System.out.println("Save button pressed");

        if (checkInputs() == false) {
            System.out.println("Input check failed");
            return;
        }

        // getting all the data entered
        String emp_name = fname.getText() + " " + lname.getText();
        String emp_nid = nid.getText();
        String emp_account = accountno.getText();
        String emp_email = email.getText();
        String emp_contact = contact.getText();

        // setting values
        Employee emp = new Employee(LoggedUser.getEmp());
        emp.setName(emp_name);
        emp.setNid(emp_nid);
        emp.setAcc_no(emp_account);
        emp.setEmail(emp_email);
        emp.setPhone_no(emp_contact);

        // updating record in database
        emp.editEmployeeAccountField(emp.getEmployee_id(),"name",emp_name, false);
        emp.editEmployeeAccountField(emp.getEmployee_id(),"nid",emp_nid, false);
        emp.editEmployeeAccountField(emp.getEmployee_id(),"email",emp_email, false);
        emp.editEmployeeAccountField(emp.getAcc_no(),"acc_no",emp_account, false);
        emp.editEmployeeAccountField(emp.getEmployee_id(), "phone_no", emp_contact, false);

        // changing password (if entered)
        if (curpass != null && newpass != null) {
            String currPass = curpass.getText();
            String newPass = newpass.getText();

            if (!currPass.isEmpty() && !newPass.isEmpty()){

                // password changed successfully
                if (emp.changePassword(currPass, newPass)) {
                    System.out.println("Password changed");

                    SendMail.sendEmail("Password changed.", "Your password has been changed successfully.", emp.getEmail());
                }

                // password not changed
                else {
                    openPopup("Warning","Password not changed.");
                }
            }
        }

        // refresh data
        LoggedUser.setEmp(emp);

        // refresh screen
        goToMgrEdit();
    }

    //////////////// SCENE SWITCHING ///////////////////

    public void goToMgrMenu() throws IOException {
        System.out.println("Loading manager menu window");

        //Load next
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mgr_menu.fxml"));
        Parent root = loader.load();

        //Get controller of menu scene
        mgr_menu_Controller controller = loader.getController();
        controller.setName(LoggedUser.getEmp().getName());

        // close current window
        Stage window = (Stage) exit_btn.getScene().getWindow();
        window.close();

        // start new window for menu scene
        window = new Stage();
        window.setScene(new Scene(root, 900, 600));

        Font.loadFont(getClass().getResourceAsStream("Fonts/Alifiyah.otf"), 10);
        Font.loadFont(getClass().getResourceAsStream("Fonts/Honeymoon Avenue Script Demo.ttf"), 10);

        Font.loadFont(getClass().getResourceAsStream("Fonts/ArchivoNarrow-Regular.ttf"), 10);
        Font.loadFont(getClass().getResourceAsStream("Fonts/JuliusSansOne-Regular.ttf"), 10);

        window.setTitle("Manager Menu");
        window.show();
    }

    public void goToMgrEdit() throws IOException {
        System.out.println("Loading manager edit window");

        //Load next
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EditManager.fxml"));
        Parent root = loader.load();

        //Get controller of manager edit scene
        EditManagerController controller = loader.getController();
        controller.init();

        // close current window
        Stage window = (Stage) save_btn.getScene().getWindow();
        window.close();

        // start new window for edit scene
        window = new Stage();
        window.setScene(new Scene(root, 900, 600));

        Font.loadFont(getClass().getResourceAsStream("Fonts/Alifiyah.otf"), 10);
        Font.loadFont(getClass().getResourceAsStream("Fonts/Honeymoon Avenue Script Demo.ttf"), 10);

        Font.loadFont(getClass().getResourceAsStream("Fonts/ArchivoNarrow-Regular.ttf"), 10);
        Font.loadFont(getClass().getResourceAsStream("Fonts/JuliusSansOne-Regular.ttf"), 10);

        window.setTitle("Edit Your Account");
        window.show();
    }

    // open popup
    public void openPopup(String heading, String text) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        new WelcomeController().toPopUp(heading, text);
    }
}
