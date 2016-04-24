# File Systen Checker (Operating System)
# Language: Java

## Synopsis

Design a file system checker for our file system.  You should call it csefsck.  It will have to do the following, correcting errors whenever possible, and reporting everything it does to the user:<br>
<br>1.)	The DeviceID is correct (20)<br>
2.)	All times are in the past, nothing in the future<br>
3.)	Validate that the free block list is accurate this includes<br>
  - Making sure the free block list contains ALL of the free blocks<br>
  - Make sure than there are no files/directories stored on items listed in the free block list<br>
  
4.)	Each directory contains . and .. and their block numbers are correct<br>
5.)	If indirect is 1, that the data in the block pointed to by location pointer is an array<br>
6.)	That the size is valid for the number of block pointers in the location array. The three possibilities are:<br>
  - size < blocksize  should have indirect=0 and size>0<br>
  - if indirect!=0, size should be less than (blocksize*length of location array)<br>
  - if indirect!=0, size should be greater than (blocksize*length of location array-1)<br>


## Configuration

Fork this project in your GitHub account, then clone your repository:

  Run the following query on terminal.
  ```
  git clone http://github.com/vaibhavagg12393/Interview_GetGrist.git
  ```

Instructions to import java-json.jar file to Eclipse
  
   A.) Download the ZIP file from this ['URL'](http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip) and extract it to get the Jar in the cloned directory.<br>
       
   B.) Add the Jar to your build path.<br><br>
  	   To Add this Jar to your build path, follow these steps:
  	   
```
        Right click the Project > Build Path > Configure build path > Select Libraries tab > 
        Click Add External Libraries > Select the Jar file Download
       
```
        
   C.) Compile and run the code in any IDE ( I used Eclipse)<br>
   D.) Comments have been provided throughout the code for a better understanding.<br>

## Additional
Same project have been code using Python. Click ['here'] (https://github.com/vaibhavagg12393/File-System-Checker---Python.git) to open Python code.

