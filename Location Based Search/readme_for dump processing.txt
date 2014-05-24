1.Unprocessed Wikipedia XML file is read by ReadXML.java

2.The title,text,username,timestamp and parent-id from each page with good titles is sent to WriteXML.java 

3.WriteXML class writes seperate XML files for each page in wikidump containing text,title,timestamp,username.

4.dumpProcessing class of WriteXML also sends the title to getLocation method of LocationExtraction class.

5.getLocations methods of LocationExtraction Class extracts the locations from title using OpenNLP and stores the location names in a processedloc.txt file.

6.The duplicates locations from processedloc.txt file is removed.

7.CreateLocationsXML.java is the run.

8.This class reads the processedloc.txt file, sends the location name to GeoNames class.

9.GeoNames class extracts the latitude longitude of the location and creates different XML files for all the location names containing latlng and country information of the location.

10.We now have seperate XML files for each page tag of wikidump and each location.

11.Use the Combine class to cobine all the sperate wiki news xmls files in one XML.

12.Use the Combine class to combine all the locations xml files in one XML.

13.We now have two XML files:
     1.Wikinews.xml
     2.Locations.xml

14.The text from wikinews.XML is then parsed using WikipediaParser and Parser, summary is added in the XML.



