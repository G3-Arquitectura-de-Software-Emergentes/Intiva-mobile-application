# Intiva Mobile Application

This repository contains the mobile application for the Intiva Platform, which is a platform for managing expenses, savings and incomes.
The mobile application is built using Kotlin and Jetpack Compose and provides a user-friendly interface for users to interact with the features.
It also uses Room for local data storage and Retrofit for network communication with the backend API.

## Summary

This application is a mobile application for the Intiva Platform. It includes the following features:

- Transaction management: Register and monitor all transactions, including expenses and incomes.
- Categorization: Classify transactions into different categories for better organization and analysis.
- Budget management: Set and track budgets for different categories of expenses and for different periods of time.
- Savings management: Set and track savings goals, and monitor progress towards those goals.
- Reminders and notifications: Set up reminders for upcoming bills, budget limits, and savings goals, and receive notifications to stay on track.
- Family group management: Create and manage family groups to share expenses, budgets, and savings goals among family members.
- Reporting and analytics: Generate reports and visualizations to analyze spending patterns, savings progress, and overall financial health.
- User management: Manage user accounts, including registration, authentication, and authorization.

## Technologies Used

For the development of this API, the following technologies were used:

### Main Stack

- Kotlin: The programming language used for the development of the mobile application.
- Jetpack Compose: A modern toolkit for building native Android UI, used to create the user interface of the mobile application.
- Room: A persistence library for Android, used for local data storage in the mobile application.
- Retrofit: A type-safe HTTP client for Android, used for network communication with the backend API.
- Coroutines: A concurrency framework for Kotlin, used for asynchronous programming in the mobile application.
- Dagger Hilt: A dependency injection framework for Android, used for managing dependencies in the mobile application.
- Navigation Component: A framework for handling navigation in Android applications, used for managing navigation between different screens in the mobile application.
- ViewModel: A component of the Android Architecture Components, used for managing UI-related data in a lifecycle-conscious way in the mobile application.

### Approaches and Patterns

- MVVC (Model-View-ViewModel): The application follows the MVVC architectural pattern, which separates the user interface (View) from the business logic (ViewModel) and the data (Model).
- Clean Architecture: The application is structured following the principles of Clean Architecture, which promotes separation of concerns and makes the codebase more maintainable and testable.
- Feature-based module organization: The codebase is organized into modules based on features, which helps to keep the codebase organized and makes it easier to navigate and maintain.

## Documentation

The project includes basic documentation and is available in the `docs` folder. It includes:

- C4 Model Software Architecture Diagrams: includes context, container and component level diagrams to provide an overview of the system architecture and its components.
- Class Diagrams: includes class diagrams to illustrate the structure of the codebase and the relationships between classes.
## PoC: plan mensual con IA

La app consume las rutas de la PoC a través del Gateway. En el emulador, el build debug usa `http://10.0.2.2:8080/api/v1/`. Se puede cambiar por `-PintivaDebugGateway=...`; para release configurar `-PintivaGateway=https://.../api/v1/` o la variable de entorno `INTIVA_GATEWAY_URL`. Firebase App Distribution toma el valor de la variable del repositorio del mismo nombre.

En Inicio, abrir **Agente · Plan mensual**. La app distingue borrador, aplicación, confirmación y error. El botón de consulta recupera el estado y el aviso del plan. En el formulario de gastos personales, **Sugerir categoría con IA** ofrece una categoría que se puede aceptar o cambiar; el botón de guardar sigue siendo la única acción que registra el movimiento.
