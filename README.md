 ![Banner](https://i.imgur.com/5D06W87.jpg)
### Become a Music Wiz on MusicQuiz+

# Introduction
MusicQuiz+ is a music quiz game that creates playable multiple choice quizzes from data available on Spotify. It aims to set it self apart from other music quiz games by generating music quizzes from a Spotify playlist or albums the user has saved from an artist.

This project was created as a group capstone project at Full Sail University.

# Features
Here's an overview of MusicQuiz+'s features.
## Browse
The home screen is divided up into three separate screens for browsing content.
### Playlists
The playlists screen is for browsing the playlists you save. From here, you can choose a playlist to take a quiz on.
#### Playlist
The playlist screen shows information about a single playlist. For the data available, it displays the playlists image, name, the username of the person who created it and a list of tracks contained in the playlist. From here, you can listen to track previews, follow or unfollow the playlist, or start the playlist quiz.
### Artists
The artists screen is for browsing the albums you save, categorized by artist. From here, you can choose an artist to take a quiz on.
#### Artist
The artist screen shows an overview of the artist. For the data available, it displays the artists image, name, a truncated bio, external links to social media and Wikipedia, and a list of albums separated by album type. From here, you can save their albums or take a quiz based off those saved albums.
### History
The history screen is for browsing the songs you recently heard in a quiz. From here, you can share the song or view it's page on Spotify.

## User Account
The user account is used to track data about the user, including which albums and playlists have been saved, their level and experience points, earned badges, and to enforce limits on usage. A user without an account is limited to five free searches and can only take quizzes on a distributed list of default playlist quizzes.

## Search
The search screen is used to search Spotify for new data and used to search any previously collected data in the Firebase Realtime Database. It allows the user to search for artists, albums, tracks and playlist by name. From here, the user can filter their search results and find new playlists and albums to take quizzes on.
### Artist Results
Artist search results will take you to an artist screen, as described under browse.
### Album Results
Album search results can be saved directly from the search screen.
### Track Results
Track search results will take you to a track screen. The track screen displays potential matches for albums the track appears on by a single artist.
### Playlist Results
Playlist search results will take you to a playlist screen, as described under browse.

## Music Quiz
The music quiz is made up a quiz screens and a results screen. The quiz screen displays four answers to choose from per answer. The results screen displays the user's score, experience gained and badges earned during the session.


# Technologies
## Developer Tools 
* Android Studio Dolphin 2021.3.1  
* TortoiseGit 2.13.0.1 

## Content Generation Tools: 
* GIMP 2.10.32
* Photopea | Online Photo Editor

## Data Tools 
* Firebase
  * Authentication
  * Firestore
  * Realtime Database
* Local Data Storage 

## 3rd Party Dependencies and APIs 
* Android Min SDK 21, Target SDK 32 
* Spotify API by Glavier 

# Installation
TBA

# Development Setup
Please contact a developer to retrieve a file required in order to connect to Firebase and Spotify API services; intallation instructions will be included.


# License
GNU General Public License v3.0

# Contributors
Charles Holdren
* Back-end developer
* Product Owner
Matthew Vladika
* Front-end developer
* Scrum Master

# Project Status
Alpha
