package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.w3c.dom.Element;

public class Main extends Application {

  private Scene scene;

  @Override
  public void start(Stage primaryStage) throws Exception {
    WebView webview = new WebView();
//    webview.getEngine().setJavaScriptEnabled(true);

    webview.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
      @Override
      public void handle(WebEvent<String> arg0) {
        System.out.println("There was an alert: " + arg0.toString());
      }
    });

    webview.getEngine().load(Main.class.getResource("hello.html").toExternalForm());
    primaryStage.setScene(new Scene(webview));

//    JSObject window = (JSObject) webview.getEngine().executeScript("window");
//    window.setMember("app", new JavaApplication());

    primaryStage.show();

//    Element p = (Element) webview.getEngine().executeScript("$('#draggable').text('java code');");
//    Element p = (Element) webview.getEngine().executeScript("document.getElementById('draggable')");
//    p.setAttribute("style", "font-weight: bold");


    webview.getEngine().executeScript("updateHello('abcdef')");
  }

  public static void main(String[] args) {
    launch(args);
  }
}