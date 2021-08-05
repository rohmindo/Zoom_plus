<p align="center">
  <img width="260" alt="캡처" src="https://user-images.githubusercontent.com/49011398/120544261-2d23f800-c428-11eb-864c-4d0adb5fd1e8.png">
</p>
<p align="center">
  <b>LMS focused on interaction between participants</b>
</p>

## Table of Content
1. Introduction
2. Features
3. Screenshots
4. Setup
5. Technologies
6. Sources

## 1. Introduction
### Background
Recently, due to the advent of the untact era due to COVID-19, many educational institutions (elementary, middle, and higher educational institutions, universities, and private institutes) are replacing face-to-face classes with non-face-to-face classes. According to data conducted by the national educational statistics service KESS on 332 universities across the country, as of September 7, 2020, 196 universities (59.0%) across the country were taking ‘full face-to-face’ classes.
Educational institutions are using existing video conferencing platforms rather than building their own non-face-to-face teaching platforms to convert classes to non-face-to-face. However, since the existing video conferencing platform is not made for educational purposes, it is inconvenient for educational institutions to use it for non-face-to-face classes. In addition, as the face-to-face classes are converted to non-face-to-face classes, the quality of classes deteriorates or student participation is low. Among the students who experienced the non-face-to-face class, the response that they were satisfied with the non-face-to-face class was low at 21.4%. On the other hand, 40.2% answered that they were average and 38.4% answered that they were dissatisfied.
Therefore, educational institutions need an online video conferencing platform for educational purposes, and as they switch to non-face-to-face classes, they need to compensate for side effects such as lowering the quality of classes or lowering participation.
### Goal
As mentioned earlier, as the classes of educational institutions are converted to non-face-to-face lectures, problems such as a lack of video conferencing platforms for educational purposes, deterioration of class quality, and reduced student participation occur.
Because non-face-to-face classes are conducted online by professors and students, it is difficult to check the level of understanding or responses of students compared to face-to-face classes. Since most students do not turn on their cams, the professor cannot gauge the students' expressions or level of understanding, which leads to a decrease in the quality of the class, and the students become passive in class, which leads to a decrease in participation. For this reason, in most cases, the professor is unilaterally teaching without knowing the reaction of the students.
In addition, although Zoom and Google Meet are currently widely used video conferencing platforms in educational institutions, these are also platforms for 'video conferencing streaming', not 'classes'. A function to check this is not provided properly.
Therefore, in order to solve the above problems, we aim to improve the quality of classes and improve student participation by building a video conferencing platform for educational purposes that increases communication between students and professors during class and provides functions to actively interact with each other. want to In addition, we intend to establish an integrated education service by linking the video conferencing platform focused on education and interaction with the education management system (LMS).
 Specifically, functions such as quizzes, comprehension assessment, and responses will be provided during real-time classes to increase the interaction between professors and students. In addition, we intend to contribute to improving the quality of classes by providing services that can store, manage, and visualize the data obtained during real-time classes in LMS.
## 2. Features
### During Class
- anonymous chat, Question
- Provide real time Speaker's speech
- Auto Q&A through Machine Reading
- Comprehension survey & real time evaluation
- Provide Student's status through 3D Modeling 
### Other
- Lecture note
- Notice
- Assignment
- Community
- auto attendance excel file
- Visualized interaction analyzed page
## 3. Screenshots
<img width="960" alt="1" src="https://user-images.githubusercontent.com/49011398/120563274-29519f00-c443-11eb-93e5-1e2258c311ec.PNG">
<p align="left">
  <b>Main Page</b>
</p>
<img width="948" alt="2" src="https://user-images.githubusercontent.com/49011398/120563277-29519f00-c443-11eb-8bf1-a72ea0aaa225.PNG">
<p align="left">
  <b>Lecture Analysis Page</b>
</p>
<img width="948" alt="3" src="https://user-images.githubusercontent.com/49011398/120563275-29519f00-c443-11eb-9f0a-2198115143c7.PNG">
<p align="left">
  <b>Attendance Page</b>
</p>
<img width="954" alt="4" src="https://user-images.githubusercontent.com/49011398/120563273-29519f00-c443-11eb-8e9c-233c54410218.png">
<p align="left">
  <b>During Class Page</b>
</p>
<img width="948" alt="5" src="https://user-images.githubusercontent.com/49011398/120563276-29519f00-c443-11eb-95b1-d3c5ab2cad36.PNG">
<p align="left">
  <b>Assignment Page</b>
</p>

## 4. Setup
### Our domain
Visit our Servie Domain https://disboard13.kro.kr/ to fully experience our system!
### Local
~~~
cd Web-Frontend
npm install
npm link ../my_modules/zoomus/instantsdk
npm start
~~~
Our server related source code is not intended to work locally.
Run only Front-end related code, and rest will be done by EC2 server.
~~~
npm build
~~~
For those who like to build app.
## 5. Technologies
### Front-End
- React.js
- Redux Thunk
- styled-component
- javascript / Typescript
- HTML / CSS
- HTML5 Canvas
- Web Speech API
### Back-End
- Node.js + Express.js
- mongoDB + mongoose
- swagger( for API Docs )
- docker ( for Deployment )
- AWS ec2 / S3
- Nginx( for Reverse Proxy Server)
- Jenkins( for CI/CD)
### Mobile App
- Android Studio
- Kotlin / Java
- JET Pack
### AI
- flask

## 6. Sources
- [Zoom Web Video SDK](https://marketplace.zoom.us/docs/sdk)
- [Chart.js](https://github.com/reactchartjs/react-chartjs-2)
- [Three.js](https://threejs.org/)
- [CKEditor](https://ckeditor.com/)
- [antd](https://ant.design)
- [socket-io](https://socket.io/)
