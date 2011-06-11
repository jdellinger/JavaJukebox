Standalone application
startup with -p <port> -dir <music_dir>
  maybe optional to specify path to db with -db

jlGui player libraries
Embedded Jetty server to provide web interface and restful services
Embedded Derby DB to store data

Basic Functionality:
Play, Pause, Resume, Skip playing files in playlist queue
If queue is empty, choose file randomly from available list
Allow marking of file as like, dislike, neutral
Allow marking of file as explicit

Data Storage
Scan directory and store available files
  id, name, checksum, mp3 id info, path_to_file
Queue pointers to available files table
Store like, dislike, explicit flags and play counts, skip counts in related table