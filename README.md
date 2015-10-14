# Model-Checker
Streader's Raiders SWEN302 Group Project

## Overview
Automata Concocter is developed using the [Polymer 1.0](https://www.polymer-project.org/1.0/) framework. Polymer uses predefined elements in a composite pattern 
to create web based applications in HTML, CSS and Javascript. All elements used to create this application can be found on
the Polymer website in the element catalogue. The Polymer framework requires Bower to manage third party libraries. 
Information on Bower and Bower installation can be found at their website.

Bower Home:		http://bower.io/  
Bower Install:		http://bower.io/#install-bower  
Polymer 1.0 Home:	https://www.polymer-project.org/1.0/  
Polymer 1.0 Catalogue:	https://elements.polymer-project.org/   
Polymer 1.0 Dev Guide:	https://www.polymer-project.org/1.0/docs/devguide/feature-overview.html   

-----------------------
###MAIN
-----------------------

Note: The root directory of Automata Concocter is 'Model-Checker' and for the remainder of this document will be reffered
to as the root directory.

The main index.html file is located in 'Model-Checker/app/index.html'. Within this file all elements which are used are
shown including all the styling of the given elements.

To do...

###CUSTOM ELEMENTS
-----------------------

All custom elements are located within there corosponding directories in 'Model-Checker/elements/' and include the index
file and corrsponding html file of the same name as the given directory that includes the script utlizied by the element. 

  * **text-editor**
  
    The text-editor custom element is used to describe Automata by the user and has a live compiling option which can be set
    to on or off in the toolbar. Assuming the live compiling option is off, definitions are retrieved from the text editor 
    using the private getCode function outlined in text-editor.html when the compile button is used. SetCode is used here when
    the user opts to load a txt file of their choosing and updates the corrosponding field within this element. If live compilation
    is on, the text given by the user is updated on each key press by firing a text-editor-change event. These events are limited
    to be a second away from each other at least. This value is given as the changeEventDebounceTime defined in text-editor.html.

  * **automata-walker**
   
     This element outlines the walker in its entirety. By updating the Automata dropdown menu on each compilation the user can
    then select a given automata and a corrosponding edge based on the current Node. The _walk funtion is invoked onClick of
    the walk button. The automata-visalization is then updated to highlight the current node the user has navigated through.

  * **Automata-Visualisation**
   
    This element is responsible for the visualisatising of automata. It interacts directly
    with the DagreD3 library and renders the graphs on screen in the appropriate place in
    the SVG group.  
