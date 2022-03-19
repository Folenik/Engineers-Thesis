# Engineers-Thesis
Prototype system desired for checking the completeness of surgical instruments by using an RFID reader.

[![Project presentation](http://img.youtube.com/vi/7z0H7dprxZo/0.jpg)](http://www.youtube.com/watch?v=7z0H7dprxZo "Prezentacja działania SIKICH, projekt inżynierski")

Below diagram shows the system in relation to the schematic diagram. Powered up RFID module enters the standby mode. When tag is brought closer, this module reads ID of the tag, and then sends it to the processing system inside the case, which converts received data to the appropriate type and then sends it to the WiFi module. Then, after establishing connection with the database, this module writes tag ID in it. The mobile application sends a query to the database and as a result receives a list of the scanned transponders.

![System diagram](https://user-images.githubusercontent.com/33907994/159119733-0290b857-1de0-473d-9bac-4e3c5221e6a9.JPG)
