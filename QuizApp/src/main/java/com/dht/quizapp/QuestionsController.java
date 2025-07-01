/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.dht.quizapp;

import com.dht.pojo.Category;
import com.dht.pojo.Choice;
import com.dht.pojo.Level;
import com.dht.pojo.Question;
import com.dht.services.CategoryServices;
import com.dht.services.LevelServices;
import com.dht.services.QuestionServices;
import com.dht.utils.MyAlert;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author admin
 */
public class QuestionsController implements Initializable {

    @FXML
    private VBox vboxChoices;
    @FXML
    private ComboBox<Category> cbCates;
    @FXML
    private ComboBox<Level> cbLevels;
    @FXML
    private TextArea txtContent;
    @FXML
    private ToggleGroup toggleChoice = new ToggleGroup();
    @FXML
    private TableView<Question> tbQuestion;
    @FXML
    private TextField txtSearch;
    
    private static final CategoryServices cateService = new CategoryServices();
    private static final LevelServices levelService = new LevelServices();
    private static final QuestionServices questionService = new QuestionServices();

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            this.cbCates.setItems(FXCollections.observableList(cateService.getCates()));
            this.cbLevels.setItems(FXCollections.observableList(levelService.getLevels()));
            this.loadColums();
            this.tbQuestion.setItems(FXCollections.observableList(questionService.getQuestion()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        this.txtSearch.textProperty().addListener((e) -> {
            try {
                this.tbQuestion.setItems(FXCollections.observableList(questionService.getQuestion(this.txtSearch.getText())));
            } catch (SQLException ex) {
                Logger.getLogger(QuestionsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
        });
    }    
    
    public void handleMoreChoice(ActionEvent event) {
        HBox h = new HBox();
        h.getStyleClass().add("Main");
        
        RadioButton r = new RadioButton();
        r.setToggleGroup(toggleChoice);
        
        TextField txt = new TextField();
        txt.getStyleClass().add("Input");
        
        h.getChildren().addAll(r, txt);
        
        this.vboxChoices.getChildren().add(h);
    }
    
    public void handleQuestion(ActionEvent event) {
        try {
            Question.Builder b = new Question.Builder(this.txtContent.getText(),
                    this.cbCates.getSelectionModel().getSelectedItem(),
                    this.cbLevels.getSelectionModel().getSelectedItem());
            
            for (var c : vboxChoices.getChildren()) {
                HBox h = (HBox) c;
                Choice choice = new Choice(((TextField) h.getChildren().get(1)).getText(),
                        ((RadioButton) h.getChildren().get(0)).isSelected());
                
                b.addChoice(choice);
            }
            
            Question q = b.build();
            questionService.addQuestion(q);
            MyAlert.getInstance().showMsg("Thêm câu hỏi thành công!");
        } catch (SQLException ex) {
            MyAlert.getInstance().showMsg("Thêm câu hỏi thất bại!");
        } catch (Exception ex) {
            MyAlert.getInstance().showMsg("Dữ liệu không hợp lệ!");
        }
    }

    private void loadColums() {
        TableColumn colId = new TableColumn("Id");
        TableColumn colCont = new TableColumn("Content");
        colId.setCellValueFactory(new PropertyValueFactory("id"));
        colCont.setCellValueFactory(new PropertyValueFactory("content"));
        colCont.setPrefWidth(300);
        colId.setPrefWidth(150);
        
        TableColumn colAction = new TableColumn();
        colAction.setCellFactory(e->{
        TableCell cell = new TableCell();
        Button btn  = new Button("Xoa");
        btn.setOnAction(event->{
            Optional<ButtonType> type = MyAlert.getInstance().showMsg("m chac chan chua", Alert.AlertType.INFORMATION);
            if(type.isPresent()&& type.get().equals(ButtonType.OK)){
                
            Question q =(Question) cell.getTableRow().getItem();
                try {
                    if(questionService.delQuestion(q.getId())== true)
                    {
                        MyAlert.getInstance().showMsg("XOA thanh cong");
                    }
                    else {
                          MyAlert.getInstance().showMsg("XOA THAT BAI");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(QuestionsController.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        });
        cell.setGraphic(btn);
        return cell;
        });
        this.tbQuestion.getColumns().addAll(colId, colCont,colAction);
    }
}
