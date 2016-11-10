<b>LogDruid</b>: Generate Charts and Reports using data gathered in log files

Download on https://sourceforge.net/projects/logdruid/

<h2> Overview </h2>
An application to gather, aggregate, chart and report information originating from any log files in a given folder.
Templates can be easily created to gather information for any type of log files.
It uses regular expressions that are constructed graphically and can be verified in the application against data samples.

![Alt text](doc/LD-charts.png?raw=true "screenshot")

The regular expressions are then used both to group files depending on any path/filename criteria (such as PID) and to mine data series from the files.
The information is aggregated for each groups and displayed in time series chart. it is possible to toggle the display of any recordings and to zoom or go to a specific time period for all the charts at the same time.
Once configured for a specific type of logs set, the gathering and display of the chart for a new files set can be done in just one click. The application is multi-threaded at its core and can handle quickly significant amounts of data.   

![Alt text](doc/LD-regexp.png?raw=true "screenshot")

<h2>Glossary</h2>
- <b>Source</b> : identifies the type of log file. permit collecting list of files matching each source  
- <b>Recording</b> : A sort of advanced regular expressions used to match a log line . Each Recording can be linked to any Source.	There are 3 types of recordings: 
	- <b>File Grouping</b> : used to group files (eg. regex to locate the Process PID in the filename)
	- <b>Series</b> : gathering any Time Series information available in the logs
	- <b>Event</b> : gathering occurrence of specific log entry (such as errors) 
		- <b>occurrence</b> will appear as a bar in the chart of 100 or 101 value: 100 for 1 hit, 101, for one or more.
		- <b>duration</b> will draw a bar of the length of the value
	- <b>Report</b> : dynamic reports displayed in a table. 
		- <b>histogram</b> : table with distinct combination of columns and count  (eg. number of request per user in Apache; count of logging categorized by loglevel and class)
		- <b>sum</b> : sum of a value through the matches (eg. sum of request durations for each Apache users)
		- <b>top100</b> : top 100 values for given field. Other items can get captured.


- <b>Recording Item</b>: one for each of the captured groups in a Recording. Existing fields: Name, before, after, type, active. Name is important as it is the name of the series in the charts.


<h2>Installation</h2>

Download and unzip the latest release. Launch the application using start.sh or start.bat depending on the OS. A Java JRE must be present in the system path for this to work otherwise it can be hardcoded in the files.


<h2>Planned improvements</h2>
- <del>ability to see which file as the information selected in any charts</del>.DONE
- <del>addition of progress bar</del> DONE
- filter in chart view
- make it easier to configure the software for a new logs set through
	- templates selection
	- automatic sampling of the data
	- visual feedback on what is not matching
	- easy access and use of list of records that failed to match 
	- way to quickly disable the capture of data(RecordingItem) which is not available in data source 
	- mass fixing when feasible (eg. Date Formats)
- add column sorting and filtering to tables
- allow combination in charts of recording item from different sources
- <del>rework of multi-threaded data mining</del>
- ability to export the representation of all the charts as shown to one PNG 
- graphical design (icon, some buttons text replaced wit pictures)
- One key functionality of the application is that Recording Items are being aggregated through different Recordings if they have the same name. This is useful when there are several version of a recording as it allow charting them in one graph. It is however adding confusion as the information end up as being redundant in such case. One significant improvement will be to allow several sets of RecordingItems for a given Recording.


Other ideas:
- <del>allow opening external text editor on selected file</del> DONE
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
- visualize logs extract corresponding to selection
