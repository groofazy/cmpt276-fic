# Group 14 

## Name of Web Application

## Do we have a clear understanding of the problem?

Fraser International College wants an attendance tracker for their classrooms. 

There are three potential views of the web app which include Professors, Students, and Admins

Students will be able to log into the website with their credentials (StudentID, Password) via the auth/login page.
Then, they'll be presented with an interactive map of the classroom, with clickable seats that display if the seat is taken or not (green and red).
After choosing a seat, they will be assessed as present in the class.

Professors will be able to log into the website with their credentials (ProfID, Password) via the auth/login page.
Then, they'll be presented with a map of student seats and see which students are in attendance. 
An additional feature is the ability to adjust the desk setups when needed.

Admins will be able to log into the website with their credentials (AdminID, Password) via the auth/login page. 
Then, they’ll be presented with a map of student seats and other admin features such as removing students.

## How is this problem solved currently (if at all)?
At Fraser International College, classroom attendance is currently recorded manually by professors through methods such as calling out student names, paper sign-in sheets, or marking attendance in spreadsheets both before and after class. While these approaches are common, they are time-consuming, prone to human error, and inefficient for large classrooms. Manual tracking also makes it difficult to monitor real-time seat usage or visualize classroom occupancy. These limitations highlight the need for a centralized digital system that can automate attendance tracking and provide a visual representation of classroom seating.

## How will this project make life better? Is it educational or just for entertainment?
This project will make life better by improving the efficiency, accuracy, and organization of attendance tracking at Fraser International College. At present, attendance is often taken manually, which can be time-consuming, prone to errors, and disruptive to class time. Our attendance tracking app streamlines this process by allowing students to log in, select their seat on the interactive classroom map, and automatically mark themselves as present.This project is educational. It directly supports the academic environment by improving classroom management and ensuring accurate and more efficiently taken attendance records. Attendance plays an important role in student success, engagement, and institutional reporting. By making attendance tracking more reliable and interactive, the app enhances the overall learning experience and supports better academic outcomes.

## Who is the target audience? Who will use your app or play your game?

## What is the scope of your project?
The scope of this project is to design and implement a browser-based classroom attendance management system for Fraser International College. The application will provide secure login functionality for three types of users: students, professors, and administrators. After logging in, users will interact with a visual classroom seating map that displays real-time seat occupancy.

Attendance will be managed through instructor-controlled sessions. Professors will initiate an attendance session at the beginning of class, which activates the system for that specific classroom and time period. While the session is active, students can log in and mark their attendance by selecting their seat on the classroom map. When the professor ends the session, attendance marking is automatically disabled.

Professors will also be able to modify seating layouts and monitor attendance records. Administrators will manage user accounts and perform system-level actions such as updating or removing student records. The project focuses on automating attendance tracking and improving classroom management through a centralized and visual digital system.


## Does this project have many individual features or one main feature (a possibility with many subproblems)? These are the ‘epics’ of your project.
This project is centered around one main core feature, which is a digital classroom attendance tracking system, but it is supported by several substantial sub-features that function as major epics. The primary goal of the application is to automate and improve how attendance is recorded and monitored at Fraser International College. However, to successfully implement this system, the project includes multiple interconnected components such as student seat selection and check-in, professor monitoring and classroom layout management, administrative account control, attendance reporting and analytics, and a secure authentication and role-based access control system. Each of these areas represents a significant development effort on its own, involving frontend design, backend logic, database integration, and security considerations. Therefore, while the project has one central purpose, it clearly contains multiple major features that together form a complete and robust system.


## What are the epics? For example, as a regular user of your site, what general actions/features can I perform? You may choose to start creating some UI mockups on paper. This may help determine your initial features.

The project is structured around five major epics, ensuring that the scope is appropriate for five group members.

The first epic is the **Student Attendance Check-In Module**. This feature allows students to securely log in using their credentials and access an interactive classroom seat map. Students can select their seat to mark themselves present. The system visually indicates seat availability and records attendance in real time within the database.

The second epic is the **Professor Monitoring and Classroom Management Module**. Professors can log in securely and view the live classroom seat map showing attendance status. This module allows instructors to monitor which students are present and manage classroom layouts when necessary. It also provides real-time attendance summaries without requiring manual roll calls.

The third epic is the **Administrative Account and Classroom Management Module**. Administrators can create, update, or remove student and professor accounts. They can also add or remove classrooms, manage seating configurations, and maintain overall system integrity. This module ensures centralized system control.

The fourth epic is the **Attendance Reporting and Analytics Module**. This component generates structured attendance reports based on stored records. It allows administrators and professors to view historical attendance data, identify trends, and export attendance summaries. This introduces a computational aspect to the system beyond simple CRUD functionality.

The fifth epic is the **Authentication and Role-Based Access Control System**. This module ensures secure login functionality and proper authorization based on user roles. It includes session management, credential validation, password handling, and access restrictions to prevent unauthorized actions across different user types.
Each epic represents a substantial development component involving database design, controller logic, frontend rendering, validation rules, and integration testing.


## Is the amount of work required in this proposal sufficient for five group members? A rough rule of thumb is that each group member should have one major feature.

Yes, the workload is sufficient for five group members because each epic is a major feature that includes frontend, backend, database integration, and testing. The work can be divided as:

Member 1 (ArshSaran):  Student Attendance Check-In Module

Member 2: Professor Monitoring and Classroom Management Module

Member 3: Administrative Account and Classroom Management Module

Member 4  Attendance Reporting and Analytics Module

Member 5: Authentication and Role-Based Access Control System

# Deployment

The application is deployed on Render.

Render URL:

