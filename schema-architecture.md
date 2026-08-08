# Smart Clinic Management System - Architecture Design

## Section 1: Architecture Summary

The Smart Clinic Management System is a Spring Boot application that uses both Spring MVC and REST controllers. Thymeleaf templates are used to provide server-rendered dashboards for administrators and doctors, while REST APIs are used by modules such as appointments, patient dashboards, and patient records. The application follows a three-tier architecture consisting of the presentation layer, application layer, and data layer.

The application uses two databases. MySQL stores structured information such as patients, doctors, appointments, and administrators using Spring Data JPA. MongoDB stores flexible document-based information such as prescriptions using Spring Data MongoDB. Controllers receive requests and pass them to the service layer, where business rules and validations are handled. The service layer then communicates with the appropriate repositories to retrieve or store data.

## Section 2: Numbered Flow of Data and Control

1. Users access the Smart Clinic application through Thymeleaf-based dashboards such as the Admin Dashboard and Doctor Dashboard, or through REST API clients for modules such as appointments and patient records.

2. The user's request is sent to the appropriate controller. Thymeleaf controllers handle requests for web pages, while REST controllers handle API requests and return JSON responses.

3. The controller passes the request to the service layer. The service layer contains the application's business logic, validations, and workflow processing.

4. The service layer communicates with the appropriate repository to access or modify the required data.

5. MySQL repositories use Spring Data JPA to manage structured data such as patients, doctors, appointments, and administrator information. MongoDB repositories manage flexible document-based data such as prescriptions.

6. The retrieved database information is mapped to Java model classes. MySQL records are represented as JPA entities, while MongoDB records are represented as document models.

7. The processed models are returned to the presentation layer. For MVC requests, the data is passed to Thymeleaf templates and rendered as HTML. For REST requests, the data is converted to JSON and returned to the client.
