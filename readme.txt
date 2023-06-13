**************************************
*****     UrlSearcher readme     *****
**************************************

======================================================================
What is UrlSearcher and what is it intended to be used for
======================================================================

In a nutshell UrlSearcher is a Java application that takes as input a list of names and 
for each of them queries a search engine and write a txt file on disk containing the first 
10 urls provided by the search engine as response to the query.

I developed and used this program in order to acquire for an enterprise name a list of websites, 
the assumption is that: if an enterprise has an official website, this must be found within the 
first 10 result of a search engine. It will be up to us to decide which one of the collected 
website is the official one for that particular enterprise.

======================================================================
How is the project folder made
======================================================================

The RootJuice folder is an Eclipse project ready to be run or modified in the IDE (you just have to import the project "as an existing project" and optionally change some configuration parameters in the code).

Ignoring the hidden directories and the hidden files, under the main directory you can find 4 subdirectories :
5) src => contains the source code of the program
1) bin => contains the compiled version of the source file
3) lib => contains the jar file (libraries) that the program needs
4) sandbox => contains the executable jar file that you have to use in order to launch the program, the configuration file that you have to pass as parameter and some test input files that you can modify on the basis of your needs

As you probably already know it is definitely not a good practice to put all this stuff into a downloadable project folder, but i decided to break the rules in order to facilitate your job. Having all the stuff that will be necessary in just one location and by following the instructions you should be able to test the whole environment in 5-10 minutes.

======================================================================
How to execute the program on your PC by using the executable jar file
======================================================================

If you have Java already installed on your PC you just have to apply the following instruction points:

1) create a folder on your filesystem (let's say "myDir")

2) copy the content of the sandbox directory into "myDir"

3) customize the parameters inside the urlSearcherConf.properties file :
        
        If you are behind a proxy simply uncomment and customize the 2 parameters under the "proxy section" by removing the initial # character
        
        Change the value of the parameters under the "paths section" according with the position of the files and folders on your filesystem.

4) open a terminal, go into the myDir directory, type and execute the following command:
        java -jar UrlSearcher.jar urlSearcherConf.properties

5) at the end of execution you should find 9 txt files inside the directory myDir/txtFiles and a file called seed_[dateTime].txt in the myDir directory

======================================================================
LINUX			
======================================================================

If you are using a Linux based operating system open a terminal and type on a single line :

java -jar 
-Xmx_AmountOfRamMemoryInMB_m
/path_of_the_directory_containing_the_jar/UrlSearcher.jar 
/path_of_the_directory_containing_the_conf_file/urlSearcherConf.properties

eg:

java -jar -Xmx1024m UrlSearcher.jar urlSearcherConf.properties

java -jar -Xmx1024m /home/summa/workspace/UrlSearcher/sandbox/UrlSearcher.jar /home/summa/workspace/UrlSearcher/sandbox/urlSearcherConf.properties


======================================================================
WINDOWS			
======================================================================

If you are using a Windows based operating system you just have to do exactly the same, the only difference is that you have to substitute the slashes "/" with the backslashes "\" in the filepaths.

eg:

java -jar -Xmx1536m C:\workspace2\UrlSearcher\sandbox\UrlSearcher.jar C:\workspace2\UrlSearcher\sandbox\urlSearcherConf.properties


======================================================================
LICENSING
======================================================================

This software is released under the European Union Public License v. 1.2
A copy of the license is included in the project folder.


======================================================================
Considerations
======================================================================

This program is still a work in progress so be patient if it is not completely fault tolerant; in any case feel free to contact me (donato.summa@istat.it) if you have any questions or comments.
