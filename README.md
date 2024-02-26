
# RunTracker
By : Benhard Simanullang

## About Application
Embark on a personalized running journey with RunTracker, your all-in-one application for logging and enhancing your running activities. This app empowers you to effortlessly record your runs, and set ambitious goals over time.

RunTracker is constructed with the Ratpack library for the back-end, featuring multiple endpoints that users can leverage for their individual development (for more details, click here). Utilizing Firebase for the database, users are required to set up their own accounts for database usage. To facilitate swift deployment, the application also provides a straightforward user interface, allowing users to promptly utilize it upon execution.

### Key Features of RunTracker:

#### Simple Activity Management:
Easily add, edit, or delete your daily running sessions with RunTracker, making it straightforward to maintain your exercise log.

#### Clear Activity History:
View a straightforward history of all your running activities, providing a quick overview of your progress and achievements.

#### Insightful Activity Summary:
Get a summarized view of your running efforts, including calories burned, steps taken, and total duration, for a quick understanding of your overall performance.

#### Easy Goal Setup:
Set achievable running goals within RunTracker to tailor your fitness journey according to your preferences and capabilities.

#### Efficient Goal Tracking:
Track your running goals efficiently in real-time, helping you stay motivated and aware of your progress without unnecessary complexity.



## How to start
### Pre-requisite
- Make Sure you have Graddle and JDK Installed in your device
- This Application is using Firebase as the database, make sure you have your account setup
  go to the console and create your project : https://firebase.google.com/docs/database/
### Running the application
#### Make your database and download your serviceAccountKey.json from your firebase console
1. go to your firebase console 
2. select your project 
3. go to build, choose firestore database
4. click create database
5. choose start in test mode, and create
6. go to user and premission
7. go to service account 
8. click the Generate new private key

#### seting up the RunTracker application
1. clone the github repository

```
git clone https://github.com/BenhardSim/RunTracker.git
```
2. change dirrectory
```
cd RunTracker
```  
3. Make sure you have serviceAccountKey.json from the firebase console
4. Make sure you rename it into serviceAccountKey.json
5. go to the specified location, create a dirrectory and file.
```
mkdir src/main/java/org/example/firebaseConfig/key
touch src/main/java/org/example/firebaseConfig/key/serviceAccountKey.json
```
6. copy and paste the key you got from your firebase console to the serviceAccountKey.json that you just created.
7. after that execute the command bellow to build and run the application
```
gradle wrapper
./gradlew build
./gradlew run
```
8. when you see the application running on Localhost:5050 you can access the app via browser by typing it to access the simple user interface Utilizing the REST API endpoint.


    





