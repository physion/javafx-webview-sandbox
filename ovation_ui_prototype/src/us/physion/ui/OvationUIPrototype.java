package us.physion.ui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Screen;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import us.physion.ovation.DataStoreCoordinator;
import us.physion.ovation.api.Ovation;
import us.physion.ovation.domain.*;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class OvationUIPrototype extends Application {

  private Scene scene;
  HTMLView htmlview;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Ovation");

    Rectangle2D rect = Screen.getPrimary().getVisualBounds();

    htmlview = new HTMLView();
//    scene = new Scene(htmlview, rect.getWidth(), rect.getHeight());
    scene = new Scene(htmlview, 800, 600);
//    scene.getStylesheets().add("com/stylesheet.css");

    primaryStage.setScene(scene);
    primaryStage.show();

    Runnable r = new Runnable() {
      public void run() {
        DataStoreCoordinator dsc = Ovation.newDataStoreCoordinator();
        try {
          boolean success = dsc.authenticateUser("adam.conroy@gmail.com", "newpassword".toCharArray(), false).get();
          if (success) {
            System.out.println("success!");
            htmlview.loggedIn(dsc);
          }else{
            System.out.println("Invalid Login");
          }
        }
        catch (Exception ex) {
          System.out.println(ex.getLocalizedMessage());
          return;
        }
      }
    };
    r.run();
  }

  class HTMLView extends Region {
    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();

    DataStoreCoordinator dsc;

    LinkedList<Project> projects;
    LinkedList<LinkedList<Measurement>> projectMeasurements;

    public HTMLView() {

      webEngine.setOnAlert(new EventHandler<WebEvent<String>>() {
        @Override
        public void handle(WebEvent<String> arg0) {
          System.out.println("There was an alert: " + arg0.toString());
        }
      });

      final URL urlHello = getClass().getResource("/viewer.html");
      webEngine.load(urlHello.toExternalForm());
      webEngine.setJavaScriptEnabled(true);

      getChildren().add(webView);
    }

    public void loggedIn(DataStoreCoordinator dsc) {
      this.dsc = dsc;

//      try {
//        dsc.sync().get();
//      } catch (Exception e) {
//        System.out.println("exception: " + e.getLocalizedMessage());
//      }

      Iterator<Project> iter = dsc.getContext().getProjects().iterator();

      projects = new LinkedList<Project>();
      projectMeasurements = new LinkedList<LinkedList<Measurement>>();

      while (iter.hasNext()) {
        Project proj = iter.next();
        projects.add(proj);

        // for each project let's just drill down and grab each measurement
        LinkedList<Measurement> measurements = new LinkedList<Measurement>();
        for (Experiment experiment : proj.getExperiments()) {
          for (EpochGroup epochGroup : experiment.getEpochGroups()) {
            for (Epoch epoch : epochGroup.getEpochs()) {
              for (Measurement measurement : epoch.getMeasurements()) {
                measurements.add(measurement);
              }
            }
          }
        }

        projectMeasurements.add(measurements);
      }


      populateJSProjects();
    }

    private void populateJSProjects() {
      if (webEngine.getLoadWorker().stateProperty().getValue() == Worker.State.SUCCEEDED) {
        showJSProjects();
      } else {
        webEngine.getLoadWorker().stateProperty().addListener(
          new ChangeListener<Worker.State>() {
            public void changed(ObservableValue ov, Worker.State oldState, Worker.State newState) {
              if (newState == Worker.State.SUCCEEDED) {
                if (projects != null) {
                  showJSProjects();
                }
              }
            }
          }
        );
      }
    }

    private void showJSProjects() {
      JSObject jso = (JSObject) webEngine.executeScript("window");
      jso.setMember("projects", projects);
      jso.setMember("projectMeasurements", projectMeasurements);
      jso.setMember("javafx_controller", this);

      webEngine.executeScript("showJSProjects()");
    }

    public Object[] getSourceNameArray(int proj, int measurement){
      Measurement m = projectMeasurements.get(proj).get(measurement);
      return m.getSourceNames().toArray();
    }
  }

}
