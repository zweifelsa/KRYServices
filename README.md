# KRYServices

## Backend
The backend offers a simple API (on port 1111) to view the state of different services:
* GET: /services

   returns a list of observerd services with name, url, status and the time of the last check in JSON.

* POST: /services

   Expects an new service, with name and URL (JSON). The new service is added to the list.

* DELETE: /services/:id
  
   The service with :id is removed from the observed services.
  
Each of the services in the list is pinged every minuted and result of the response stored. All data about the services is stored in a file called "services.json", in the working directory.

### Build and Run
The project is based on the build system gradle. The gradle job "jar" builds a jar file with everything containing that is necessary. It can be run with the command "java -jar services-backend.jar"

## Android Client
The Android app provides a user interface to view, add and delete services. On startup the current observed services are loaded. All available info of each service is displayed, a color code visualizes the status of the services. With the action button (+) new services can be added. The user input is not checked yet, invalid URLs will be accepted but the services will maintain the status INVALID. With a click on the trash can services can be removed, it requires a confirmation to delete. While adding and removing services the UI is adapted immediately without awaiting the confirmation of the server, if a call failes the original state is restored.

### Build and Run
The app was implemented, built and run with Android Studio, please refer to their [website](https://developer.android.com/studio/index.html) for more information. The app is compatible with Lollypop and newer versions of Android. It was only tested on a Nexus 5X with Nougat (7.1.2).

#### Reach Backend
The URL to the backend is hardcoded in the class ServiceRequest, please adjust this URL to reach your backend.
