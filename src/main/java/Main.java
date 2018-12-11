
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.cell.ColorGridCell;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ForkJoinPool;
import java.net.InetAddress;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class Main extends Application {

    private static Board displayBoard;
    private static Board boardToSolve;
    private static int numMoves;

    private static Pane root;
    private static GridView<Color> grid;
    private static ObservableList<Color> colours;
    private static Button redButton;
    private static Button yellowButton;
    private static Button blueButton;
    private static Button greenButton;
    private static Button purpleButton;
    private static Button orangeButton;
    private static Label numMovesLabel;
    private static VBox vbox;
    static ArrayList<Socket> clients = new ArrayList();
    static ArrayList<Board> clientBoards = new ArrayList();

    static final int portNumber = 2697;
    static String hostName;

    public static void main(String[] args) {

        Scanner kb = new Scanner(System.in);
        int role;
        System.out.println("Hello. \nPlease type 1 to accept connections (server role)\nPress 2 to connect to another computer");
        role = Integer.parseInt(kb.nextLine());
        int requiredComputers = 0;
        int numConnections = 0;

        if (role == 1) {
            displayBoard = Board.generateRandomBoard(10, 10, 6);
            boardToSolve = new Board(displayBoard);
            boardToSolve.printBoard();
            clientBoards.addAll(boardToSolve.getNextBoards());

            //do the server stuff
            System.out.println("Please enter the number of computers you wish to connect");
            requiredComputers = Integer.parseInt(kb.nextLine());

            ServerSocket ss = null;
            try {
                ss = new ServerSocket(portNumber);

                while (numConnections < requiredComputers) {
                    Socket socket = ss.accept();

                    ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
                    System.out.println("Object to be written = ");
                    clientBoards.get(numConnections).printBoard();

                    outputStream.writeObject(clientBoards.get(numConnections));

                    if (socket != null) {
                        numConnections++;
                        System.out.println("Found a client!" + numConnections + " / " + requiredComputers);
                    }

                    socket.close();

                }


                //launch(args);
                Platform.runLater(() -> {
                    launch(args);
                });

                System.out.println("\nWaiting for clients to find a result...");

                while (true) {
                    Socket solutionSocket = ss.accept();
                    ObjectInputStream inputStream = new ObjectInputStream(solutionSocket.getInputStream());
                    System.out.println("Found the Solution!");

                    ArrayList<String> solutionFromClient = (ArrayList<String>) inputStream.readObject();

                    for (String step : solutionFromClient) {
                        System.out.println(step);
                    }

                }

            } catch (IOException io) {
                System.out.println("Something went wrong in client");

            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        } else if (role == 2) {
            //do the "client" stuff

            System.out.println("Type in host IP");

            hostName = kb.nextLine();
            boolean connected = false;
            try {
                while (!connected) {
                    Socket socket = new Socket(hostName, portNumber);

                    connected = true;
                    System.out.println("Creating socket to '" + hostName + "' on port " + portNumber);

                    ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

                    boardToSolve = (Board) inStream.readObject();
                    boardToSolve.printBoard();

                    ForkJoinPool childBoardSolver = new ForkJoinPool();
                    childBoardSolver.invoke(boardToSolve);

                    socket.close();

                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.println("Typed in invalid command.... Please relaunch");
        }

    }

    public void start(Stage primaryStage) throws Exception {

        vbox = new VBox(5);

        numMoves = 0;

        colours = FXCollections.observableArrayList();
        for (int i = 0; i < displayBoard.getSpaces().length; i++) {
            for (int j = 0; j < displayBoard.getSpaces()[0].length; j++) {
                colours.add(Board.getColour(displayBoard.getSpaces()[i][j].getColour()));
            }
        }

        grid = new GridView<Color>(colours);

        grid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
            public GridCell<Color> call(GridView<Color> param) {
                ColorGridCell cell = new ColorGridCell();

                cell.setBorder(new Border(new BorderStroke(Color.BLACK,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

                return cell;
            }
        });

        grid.setCellHeight(45);
        grid.setCellWidth(45);
        grid.setHorizontalCellSpacing(0);
        grid.setVerticalCellSpacing(0);

        redButton = new Button("   ");
        redButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        redButton.setStyle("-fx-background-color: red;");
        redButton.setLayoutX(100);
        redButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 0 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(0);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();
                }
            }
        });

        blueButton = new Button("   ");
        blueButton.setStyle("-fx-background-color: blue;");
        blueButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        blueButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 1 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(1);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();
                }
            }
        });
        yellowButton = new Button("   ");
        yellowButton.setStyle("-fx-background-color: yellow;");
        yellowButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        yellowButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 2 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(2);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();
                }
            }
        });

        greenButton = new Button("   ");
        greenButton.setStyle("-fx-background-color: green;");
        greenButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        greenButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 3 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(3);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();
                }
            }
        });

        purpleButton = new Button("   ");
        purpleButton.setStyle("-fx-background-color: purple;");
        purpleButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        purpleButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 4 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(4);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();

                }
            }
        });

        orangeButton = new Button("   ");
        orangeButton.setStyle("-fx-background-color: orange;");
        orangeButton.setBorder(new Border(new BorderStroke(Color.BLACK,
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        orangeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if ((displayBoard.getSpaces()[0][0].getColour() != 5 || numMoves == 0) && !displayBoard.isDoneFlooding()) {
                    numMoves++;
                    displayBoard.changeColour(5);
                    changeGrid();
                    System.out.println();
                    displayBoard.printBoard();

                }
            }
        });

        numMovesLabel = new Label("Number of moves: " + numMoves);
        numMovesLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        vbox.setAlignment(Pos.BOTTOM_CENTER);

        vbox.getChildren().addAll(grid, numMovesLabel, yellowButton, blueButton, redButton, greenButton, purpleButton, orangeButton);

        root = new StackPane();
        StackPane.setMargin(grid, new Insets(8, 8, 8, 8));

        root.getChildren().addAll(vbox);

        Scene scene = new Scene(root, 485, 725);

        primaryStage.setTitle("Flood It - Solver");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public void changeGrid() {
        vbox.getChildren().remove(grid);
        vbox.getChildren().remove(yellowButton);
        vbox.getChildren().remove(blueButton);
        vbox.getChildren().remove(redButton);
        vbox.getChildren().remove(greenButton);
        vbox.getChildren().remove(purpleButton);
        vbox.getChildren().remove(orangeButton);
        vbox.getChildren().remove(numMovesLabel);

        ObservableList<Color> colours = FXCollections.observableArrayList();
        for (int i = 0; i < displayBoard.getSpaces().length; i++) {
            for (int j = 0; j < displayBoard.getSpaces()[0].length; j++) {
                colours.add(Board.getColour(displayBoard.getSpaces()[i][j].getColour()));
            }
        }

        grid = new GridView<Color>(colours);

        grid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
            public GridCell<Color> call(GridView<Color> param) {

                ColorGridCell cell = new ColorGridCell();

                cell.setBorder(new Border(new BorderStroke(Color.BLACK,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

                return cell;
            }
        });

        numMovesLabel.setText("Number of moves: " + numMoves + "/17");
        Label finishedLabel = new Label();
        if (numMoves <= 17) {
            finishedLabel.setText("You win!");
        } else {
            finishedLabel.setText("You lose!");
        }

        grid.setCellHeight(45);
        grid.setCellWidth(45);
        grid.setHorizontalCellSpacing(0);
        grid.setVerticalCellSpacing(0);

        //root = new StackPane();
        //root.setAlignment(Pos.BOTTOM_RIGHT);
        StackPane.setMargin(grid, new Insets(8, 8, 8, 8));
        vbox.getChildren().addAll(grid, numMovesLabel, yellowButton, blueButton, redButton, greenButton, purpleButton, orangeButton);
        if (displayBoard.isDoneFlooding()) {
            vbox.getChildren().add(finishedLabel);
        }

    }

}
