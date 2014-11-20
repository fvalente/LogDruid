LogDruid: chart statistics and events retrieved in logs files through configurable regular expressions
URL : https://github.com/fvalente/LogDruid

LogDruid is an application to gather, aggregate and chart information originating from any log files.
Templates can be easily created to gather information in any type of log files.
It uses regular expressions that are constructed graphically and can be verified in the application against data samples.
This facility is at the core of the application and is used both to group files depending on any path/filename criteria (such as PID) and to mine data items from the files.
The information is aggregated for each groups and displayed in time series chart. it is possible to toggle the display of any recordings and to zoom or go to a specific time period for all the charts at the same time.
Once configured for a specific type of logs set, the gathering and display of the information can be done in one click. The application is multi-threaded at its core and can handle quickly significant amounts of data.   

![Alt text](logdruid-charts.png?raw=true "screenshot")

Glossary:
Source: identifies the type of log file. permit collecting list of files matching each source  
Recording: A sort of advanced regular expressions used to match a log line . Each Recording can be linked to any Source.
	There are 3 types of recordings: 
	- Identification: used to group files (eg. regex to locate the Process PID in the filename)
	- Statistic: gathering any statistics available in the logs
	- Event: gathering occurrence of specific log entry (such as errors) and computed result (top list of number of occurrence and duration)
Recording Item: one for each of the captured groups in a Recording. Existing fields: Name, before, after, type, active. Name is important as it is the name of the series in the charts.


Planned improvements:
- make it easier to configure the software for a new logs set through
	- templates selection
	- automatic sampling of the data
	- visual feedback on what is not matching
	- easy access and use of list of records that failed to match 
	- way to quickly disable the capture of data(RecordingItem) which is not available in data source 
	- mass fixing when feasible (eg. Date Formats)
- ability to see which file as the information selected in any charts and possibly to visualize it (or open it with external text editor)
- add column sorting and filtering to tables
- One key functionality of the application is that Recording Items are being aggregated through different Recordings if they have the same name. This is useful when there are several version of a recording as it allow charting them in one graph.
	It is however adding confusion as the information end up as being redundant in such case. One significant improvement will be to allow several sets of RecordingItems for a given Recording.
- rework of multi-threaded data mining
- addition of progress bar
- ability to export the representation of all the charts as shown to one PNG 
- graphical design (icon, some buttons text replaced wit pictures)


Other ideas:
- export of data to csv (currently data to xml only)
- export of chart data for light transport
- record template exceptions (spot quickly what was custom for a log set - could be useful to merge templates for instance) 
- percentage of file mapping
- records of patterns to ignore
- recordings discovery helper (listing of unmapped recordings)  
- Mass Recordings auto discovery
- exporting data to standard formats
- report builder (reuse?)
- automatic validation of DateFormat / regular expression
- interface to get data from other sources 
- ability to access data in archives
- template compression
- 
