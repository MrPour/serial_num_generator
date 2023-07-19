# serial_num_generator
a serial number generator depends on Java and redis<br/>

General<br/>

use BusinessEnum to define the parameters like business id, length, dateformat, beginning, the min size and the max size ;<br/>

create serial code like pool：[QQ2023071900001, QQ2023071900002, QQ2023071900003,... <br/>

fetch one serial code at one time from the beginning <br/>

ex: fetch the first one，left：[QQ2023071900002, QQ2023071900003, QQ2023071900004,...<br/>

point 1:<br/>
if the size is less than the min size threshold, then it will create new serial codes to feed up the min size after this fetch<br/>

ex:   fetch the first one and crate QQ2023071900101, QQ2023071900102] in the end;

point 2:<br/>

when you fetch the code at first of a new day, the codes of yesterday will be cleared<br/>