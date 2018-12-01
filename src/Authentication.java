import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author amukh
 */
public class UI extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Button btnSearch = new Button();
        Label lblSearch = new Label("Search");
        btnSearch.setText("Search");
        btnSearch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        
        StackPane root = new StackPane();
        root.getChildren().add(btnSearch);
        root.getChildren().add(lblSearch);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Spotify Matcher");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}