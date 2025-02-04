
## User Database and Registration System: 
- **Users** table in the database 
- **User** entity in Spring Boot
- **UserRepository** to interact with the database
- **UserServices** to handle business logic, such as checking if the username or email is already taken and encoding the password before saving the user to the database.
  
- **UserController** with a **/resigster** endpoint so users can sign up. follows the principle of MVC (Model-View-Controller) architecture, where the controller handles HTTP requests, interacts with services, and prepares the response to send back.
   -  a /login endpoint where users enter credentials.
   -  a /register endpoint where new users can be registered. 

 

