package org.DreamTeam;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InterfaceComplete extends Parent {

    /**
     * Interface Message, pour la partie droite de la fenêtre
     */
    InterfaceMessage interMsg;

    /**
     * Interface Discussion, pour la partie gauche de la fenêtre
     */
    InterfaceDiscussion interDisc;

    /**
     * Dimension de l'interface globale = dimension de la fenêtre
     */
    double height, width;

    /**
     * Pourcentage de place occupée par l'interface Discussion dans la partie gauche de la fenetre
     */
    double pourcentageSeparation=30.0f/100.0f;

    /**
     * <p>Menu contextuel pour les options lors du clic droit</p>
     */
    final ContextMenu contactContextMenu, mainContextMenu;

    /**
     * <p>Methode permettant de changer la dimension de l'interface, dit "InterfaceComplete"
     * </p>
     * @param height hauteur que doit occuper le nouvel interface
     * @param width longueur que doit occuper le nouvel interface
     */
    public void changeSize(double height, double width){
        this.height=height;
        this.width=width;
        interMsg.resize(getHeight(),(1-pourcentageSeparation)*getWidth());
        interMsg.setTranslateX(pourcentageSeparation*width);
        interDisc.resize(getHeight(),pourcentageSeparation*getWidth());

    }

    /**
     * <p>Constructeur, créer l'interface
     * </p>
     * @param height hauteur que doit occuper l'interface
     * @param width longueur que doit occuper l'interface
     */
    public InterfaceComplete(double height, double width){
        this.height=height;
        this.width=width;

        interMsg = new InterfaceMessage(getHeight(),(1-pourcentageSeparation)*getWidth());
        interMsg.setTranslateX(pourcentageSeparation*width);
        interDisc = new InterfaceDiscussion(getHeight(),pourcentageSeparation*getWidth());

        Discussion discussion;
        try(Stream<Path> walk = Files.walk(Paths.get("src\\Discussions"))){
            List<Path> paths = walk.filter(Files::isRegularFile).collect(Collectors.toList());
            for(Path path : paths){
                discussion = new Discussion();
                discussion.addObserver(interDisc);
                discussion.importFromJSON(path);
            }
        } catch(IOException e){
            e.printStackTrace();
        }

        this.getChildren().addAll(interDisc, interMsg);

        
        contactContextMenu = new ContextMenu();
        mainContextMenu = new ContextMenu();
    }


    /**
     * <p>Methode permettant de récupérer la hauteur de l'interface
     * </p>
     * @return la hauteur de l'interface
     */
    public double getHeight() {
        return height;
    }

    /**
     * <p>Methode permettant de récupérer la longueur de l'interface
     * </p>
     * @return la longueur de l'interface
     */
    public double getWidth() {
        return width;
    }

    /**
     * <h2>createContextMenu</h2>
     * <p>Fonction qui crée le menu contextuel, les options de ce menu et les actions associées.</p>
     */
    public void createContextMenu() {
        final InterfaceContact[] contacts = {null};
        EventHandler<MouseEvent> eventHandler = event -> {
            contacts[0] = (InterfaceContact) event.getSource();
            if(event.isPrimaryButtonDown()){
                //interMsg.setColor(); TODO REMPLACER PAR UN APPEL SHOW MESSAGE
                for (InterfaceContact ic:interDisc.getInterfaceContactArrayList()) {
                    ic.unselectedContact();
                }
                contacts[0].selectedContact();
                interMsg = new InterfaceMessage(getHeight(),(1-pourcentageSeparation)*getWidth(), interDisc.getListeDiscussion().get(contacts[0].getDiscussionId()-1));
            } else if(event.isSecondaryButtonDown()){
                contactContextMenu.show(event.getPickResult().getIntersectedNode(), Side.BOTTOM, 0, 0);
            }
        };
        updateBehavior(eventHandler);
        MenuItem item1 = new MenuItem("Delete message");
        item1.setOnAction(e -> {
            System.out.println(e.toString());
            interDisc.deleteDiscussion(contacts[0]);
        });
        MenuItem item2 = new MenuItem("Create discussion");
        item2.setOnAction(event -> {
            interDisc.createDiscussion();
            updateBehavior(eventHandler);
        });
        MenuItem item3 = new MenuItem("Add member");
        //item3.setOnAction(event -> interDisc.addMemberToDiscussion(contacts[0]));
        item3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                Button button = new Button("Validate");
                TextField pseudo  = new TextField();
                dialog.initModality(Modality.APPLICATION_MODAL);
                VBox dialogVbox = new VBox(10);
                EventHandler<ActionEvent> ev = new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent e)
                    {
                        interDisc.addMemberToDiscussion(contacts[0]);
                    }
                };
                button.setOnAction(ev);
                pseudo.setOnAction(ev);
                dialogVbox.getChildren().add(new Text("Pseudo : "));
                dialogVbox.getChildren().add(pseudo);
                dialogVbox.getChildren().add(button);
                Scene dialogScene = new Scene(dialogVbox, 200, 100);
                dialog.setScene(dialogScene);
                dialog.show();
            }
        });

        contactContextMenu.getItems().addAll(item1, item3);
        mainContextMenu.getItems().add(item2);

        interDisc.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                if(!contactContextMenu.isShowing()) {
                    mainContextMenu.show(interDisc.getScene().getWindow(), interDisc.getScene().getWindow().getX()+event.getX(), interDisc.getScene().getWindow().getY()+event.getY());
                }
            }
        });

    }

    /**
     * <h2>updateBehavior</h2>
     * <p>Fonction qui met à jour le comportement des nouveaux nodes créés lors de l'ajout d'une nouvelle discussion.</p>
     * @param eventHandler eventHandler à utiliser pour le comportement
     */
    public void updateBehavior(EventHandler<MouseEvent> eventHandler){
        for(Node node : interDisc.getInterfaceContactArrayList()){
            node.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        }
    }
}