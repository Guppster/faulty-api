This Bugzilla bug Retriever uses the XML-RPC API of the Bugzilla project


Class Description:

	XML_Bugzilla.java
	This class contains the main() method which is doing all everything.
	Connects to the XMLRPC endpoint of the Bugzilla site and retrieves the necessary bugs 
	afterwards it creates instances of all relevant classes in order to process the results
	of the XML-RPC and gradually add them to a database.
	
	BugDataRepresentation.java
	This class contains methods and instance variables aiming to store in an 
	organized manner the data corresponding to a bug report.
	There exist fields for every piece of information within a bug report and 
	also a toString method is provided which creates Json object compatible 
	string which when used with the JSONObject.JSONObject() constructor creates a
	JSON object suitable for storing in a MongoDB Database.
	
	BugCommentRepresentation.java
	This class contains methods and instance variables aiming to store in an 
	organised manner the data corresponding to a bug report's comments.
	There exist fields for ever piece of information within a bug report and also
	a toString method is provided which creates Json object compatible string 
	which when used with the JSONObject.JSONObject() constructor creates a JSON
	object suitable for storing in a mongoDB Database.
	
	BugDataCommentMerger.java
	This class takes the JSONObject compatible Strings from BugDataRepresentation
	and BugCommentRepresentation and merges them into one JSONObject compatible 
	string again suitable for use and loading in a mongoDB Database. 
	 