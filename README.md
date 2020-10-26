# Music-Genre-Identifier App
A collaboration with Jerin Joseph, Daniel Mata, Madeline Ben, and Annie Liu at the University of North Texas
Last updated 2020-Oct-26

# Abstract
With an increase in the quantity of music via platforms like Spotify, Apple Music, and Pandora, grouping together different types of music in order to better handle data is a task that has become increasingly daunting. Whether it’s database management, searching for a song, or even just storing a song, being able to classify music into different genres can help ease the process of working with such large amounts of data. This inspired us to create a better user experience for both clientele and developers.
Fortunately, we have material from a previous group project to build off of. We intend to use it as both a resource and to further extend what has been accomplished. One of the key features we would like to implement is the ability for a user to record a song through their phone, which can then be classified based on their genre. 

# Overview
Model - 
There are two separate models we are taking into consideration for implementation. The first is the original CNN model. If we choose to maintain the CNN model, we would focus on improving accuracy. The second option is to start from scratch using a KNN model. Both make use of the mfcc of a wav audio in order to determine the genre of music. We may use Recorder.js for this purpose.

Android Studio - 
A user interface will be created to visually present how the program will work. We will use Android Studio to develop the app. Audio recordings will be submitted through the app. The backend, which contains the KNN model, will then guess what genre it is and inform the user.

