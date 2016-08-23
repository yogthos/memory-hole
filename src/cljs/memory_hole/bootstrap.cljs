(ns memory-hole.bootstrap
  (:require
    [reagent.core :as r]
    cljsjs.react-bootstrap
    cljsjs.react-bootstrap-datetimepicker
    cljsjs.react-select))

;;;
;;; REACT-BOOTSTRAP ELEMENTS
;;;

(def Button (r/adapt-react-class js/ReactBootstrap.Button))
(def ButtonGroup (r/adapt-react-class js/ReactBootstrap.ButtonGroup))
(def ButtonToolbar (r/adapt-react-class js/ReactBootstrap.ButtonToolbar))
(def DropdownButton (r/adapt-react-class js/ReactBootstrap.DropdownButton))
(def SplitButton (r/adapt-react-class js/ReactBootstrap.SplitButton))
(def MenuItem (r/adapt-react-class js/ReactBootstrap.MenuItem))

(def Modal (r/adapt-react-class js/ReactBootstrap.Modal))
(def Modal.Dialog (r/adapt-react-class js/ReactBootstrap.Modal.Dialog))
(def Modal.Header (r/adapt-react-class js/ReactBootstrap.Modal.Header))
(def Modal.Title (r/adapt-react-class js/ReactBootstrap.Modal.Title))
(def Modal.Body (r/adapt-react-class js/ReactBootstrap.Modal.Body))
(def Modal.Footer (r/adapt-react-class js/ReactBootstrap.Modal.Footer))

(def OverlayTrigger (r/adapt-react-class js/ReactBootstrap.OverlayTrigger))
(def Popover (r/adapt-react-class js/ReactBootstrap.Popover))
(def Tooltip (r/adapt-react-class js/ReactBootstrap.Tooltip))

(def Nav (r/adapt-react-class js/ReactBootstrap.Nav))
(def NavItem (r/adapt-react-class js/ReactBootstrap.NavItem))
(def NavDropdown (r/adapt-react-class js/ReactBootstrap.NavDropdown))

(def Navbar (r/adapt-react-class js/ReactBootstrap.Navbar))
(def Navbar.Header (r/adapt-react-class js/ReactBootstrap.Navbar.Header))
(def Navbar.Brand (r/adapt-react-class js/ReactBootstrap.Navbar.Brand))
(def Navbar.Toggle (r/adapt-react-class js/ReactBootstrap.Navbar.Toggle))
(def Navbar.Collapse (r/adapt-react-class js/ReactBootstrap.Navbar.Collapse))
(def Navbar.Form (r/adapt-react-class js/ReactBootstrap.Navbar.Form))
(def Navbar.Text (r/adapt-react-class js/ReactBootstrap.Navbar.Text))

(def Breadcrumb (r/adapt-react-class js/ReactBootstrap.Breadcrumb))
(def Breadcrumb.Item (r/adapt-react-class js/ReactBootstrap.Breadcrumb.Item))

(def Tabs (r/adapt-react-class js/ReactBootstrap.Tabs))
(def Tab (r/adapt-react-class js/ReactBootstrap.Tab))
(def Tab.Container (r/adapt-react-class js/ReactBootstrap.Tab.Container))
(def Tab.Pane (r/adapt-react-class js/ReactBootstrap.Tab.Pane))

(def Pagination (r/adapt-react-class js/ReactBootstrap.Pagination))
(def Pager (r/adapt-react-class js/ReactBootstrap.Pager))
(def PageItem (r/adapt-react-class js/ReactBootstrap.PageItem))

(def Grid (r/adapt-react-class js/ReactBootstrap.Grid))
(def Row (r/adapt-react-class js/ReactBootstrap.Row))
(def Col (r/adapt-react-class js/ReactBootstrap.Col))

(def Jumbotron (r/adapt-react-class js/ReactBootstrap.Jumbotron))

(def PageHeader (r/adapt-react-class js/ReactBootstrap.PageHeader))

(def ListGroup (r/adapt-react-class js/ReactBootstrap.ListGroup))
(def ListGroupItem (r/adapt-react-class js/ReactBootstrap.ListGroupItem))

(def Table (r/adapt-react-class js/ReactBootstrap.Table))

(def Panel (r/adapt-react-class js/ReactBootstrap.Panel))
(def PanelGroup (r/adapt-react-class js/ReactBootstrap.PanelGroup))
(def Accordion (r/adapt-react-class js/ReactBootstrap.Accordion))

(def Well (r/adapt-react-class js/ReactBootstrap.Well))

(def Form (r/adapt-react-class js/ReactBootstrap.Form))
(def FormGroup (r/adapt-react-class js/ReactBootstrap.FormGroup))
(def FormControl (r/adapt-react-class js/ReactBootstrap.FormControl))
(def FormControl.Feedback (r/adapt-react-class js/ReactBootstrap.FormControl.Feedback))
(def FormControl.Static (r/adapt-react-class js/ReactBootstrap.FormControl.Static))
(def ControlLabel (r/adapt-react-class js/ReactBootstrap.ControlLabel))
(def HelpBlock (r/adapt-react-class js/ReactBootstrap.HelpBlock))
(def Checkbox (r/adapt-react-class js/ReactBootstrap.Checkbox))
(def Radio (r/adapt-react-class js/ReactBootstrap.Radio))
(def InputGroup (r/adapt-react-class js/ReactBootstrap.InputGroup))
(def InputGroup.Addon (r/adapt-react-class js/ReactBootstrap.InputGroup.Addon))
(def InputGroup.Button (r/adapt-react-class js/ReactBootstrap.InputGroup.Button))

(def Image (r/adapt-react-class js/ReactBootstrap.Image))
(def Thumbnail (r/adapt-react-class js/ReactBootstrap.Thumbnail))

(def ResponsiveEmbed (r/adapt-react-class js/ReactBootstrap.ResponsiveEmbed))

(def Carousel (r/adapt-react-class js/ReactBootstrap.Carousel))
(def Carousel.Item (r/adapt-react-class js/ReactBootstrap.Carousel.Item))
(def Carousel.Caption (r/adapt-react-class js/ReactBootstrap.Carousel.Caption))

(def Media (r/adapt-react-class js/ReactBootstrap.Media))
(def Media.Body (r/adapt-react-class js/ReactBootstrap.Media.Body))
(def Media.Left (r/adapt-react-class js/ReactBootstrap.Media.Left))
(def Media.Right (r/adapt-react-class js/ReactBootstrap.Media.Right))
(def Media.Heading (r/adapt-react-class js/ReactBootstrap.Media.Heading))
(def Media.List (r/adapt-react-class js/ReactBootstrap.Media.List))
(def Media.ListItem (r/adapt-react-class js/ReactBootstrap.Media.ListItem))

(def Glyphicon (r/adapt-react-class js/ReactBootstrap.Glyphicon))

(def Label (r/adapt-react-class js/ReactBootstrap.Label))

(def Badge (r/adapt-react-class js/ReactBootstrap.Badge))

(def Alert (r/adapt-react-class js/ReactBootstrap.Alert))

(def ProgressBar (r/adapt-react-class js/ReactBootstrap.ProgressBar))

(def Collapse (r/adapt-react-class js/ReactBootstrap.Collapse))
(def Fade (r/adapt-react-class js/ReactBootstrap.Fade))

(def Clearfix (r/adapt-react-class js/ReactBootstrap.Clearfix))

;;;
;;; REACT-BOOTSTRAP-DATETIMEPICKER ELEMENTS
;;;

(def DateTimeField (r/adapt-react-class js/ReactBootstrapDatetimepicker))

;;;
;;; REACT-SELECT ELEMENTS
;;;

(def Select (r/adapt-react-class js/Select))
(def Select.Async (r/adapt-react-class js/Select.Async))
