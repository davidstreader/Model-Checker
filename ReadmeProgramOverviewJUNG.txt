Start
UserInterfaceApplication
  FX   start(primaryStage) method
       Parent root = FXMLLoader.load(getClass().getResource("/clientres/UserInterface.fxml"));
       UserInterfaceController.java
        Calls ModelView
         Text plane
           sets up list of automata, syntax completion dictionary,
                popupSelection ?
                text processing - autocompletion box
                save text if modified  (file handling,  open save, ..)
         Graphics Plane
              clear, add model, addAll, freeze
              Options button FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientres/SettingsInterface.fxml"));

        Sets how nodes and edges are to appear (can bas this on details of the model)
        but may have to extend EdgeShape and GraphNode

        How this actually gets rendered is a mystery

        DoubleClickHandler
            seems to handle both double and single clicks!
   BasicVisualizationServer<V,E>
       renderGraph(Graphics2D g2d) the Graphics2d is an awt component for displaying graphics content - build the "graph" on g2d then render it.
        CAN not be changed but calls methods that can be customized.

       calls methods to set up rendering of nodes and edges




   GraphNoe dose not contain location
