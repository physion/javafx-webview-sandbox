package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.net.URL;

public class JavaFX_Browser extends Application {

  private Scene scene;
  MyBrowser myBrowser;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("javafx test");

    myBrowser = new MyBrowser();
    scene = new Scene(myBrowser, 640, 480);
//    scene.getStylesheets().add("sample/stylesheet.css");

    primaryStage.setScene(scene);
    primaryStage.show();
  }

  class MyBrowser extends Region {

    HBox toolbar;

    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();

    public MyBrowser() {

      webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
        @Override
        public void handle(WebEvent<String> arg0) {
          System.out.println("There was an alert: " + arg0.toString());
        }
      });

      final URL urlHello = getClass().getResource("test.html");
      webEngine.load(urlHello.toExternalForm());

      final TextField textField = new TextField();
      textField.setPromptText("Hello! Who are?");

      Button buttonEnter = new Button("Enter");
      buttonEnter.setOnAction(new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent arg0) {
          webEngine.executeScript("doAlert('abcdef')");
        }
      });

      Button buttonClear = new Button("Clear");
      buttonClear.setOnAction(new EventHandler<ActionEvent>() {

        @Override
        public void handle(ActionEvent arg0) {
          webEngine.executeScript("clearHello()");
        }
      });

      webEngine.setJavaScriptEnabled(true);
      webEngine.getLoadWorker().stateProperty().addListener(
          new ChangeListener<Worker.State>() {
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
              if (newState == Worker.State.SUCCEEDED) {
                JSObject jso = (JSObject) webEngine.executeScript("window");
                jso.setMember("app", new JavaApplication());
              }
            }
          });

      toolbar = new HBox();
      toolbar.setPadding(new Insets(10, 10, 10, 10));
      toolbar.setSpacing(10);
      toolbar.setStyle("-fx-background-color: #336699");
      toolbar.getChildren().addAll(textField, buttonEnter, buttonClear);

      getChildren().add(toolbar);
      getChildren().add(webView);
    }

    @Override
    protected void layoutChildren() {
      double w = getWidth();
      double h = getHeight();
      double toolbarHeight = toolbar.prefHeight(w);
      layoutInArea(webView, 0, 0, w, h - toolbarHeight, 0, HPos.CENTER, VPos.CENTER);
      layoutInArea(toolbar, 0, h - toolbarHeight, w, toolbarHeight, 0, HPos.CENTER, VPos.CENTER);
    }

  }

}