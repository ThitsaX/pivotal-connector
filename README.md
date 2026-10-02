# Pivotal Connector

The **Pivotal Connector** is the core integration framework for connecting Digital Financial Service Providers (DFSPs) to the Pivotal platform.

This repository contains the common connector implementation and shared business logic that are reused across all DFSP integrations. It is built as a Maven library (JAR), which is consumed by individual DFSP connector projects.

The Pivotal Connector is responsible for listening to the Hub's FSPIOP APIs, processing incoming requests, coordinating with the DFSP-specific connector implementation, and publishing the corresponding responses and transaction updates to the Pivotal platform.

Each DFSP, for example Bank A, MFI B, or Wallet C, maintains its own dedicated connector repository. These repositories import the JAR generated from this project and implement DFSP-specific integrations such as CBS/API communication, message transformation, authentication, and institution-specific business rules while leveraging the common framework provided by this repository.

## Architecture

```text
                    +----------------------+
                    |  Pivotal Connector   |
                    |   (Core Framework)   |
                    |      Generates JAR   |
                    +----------+-----------+
                               |
          ---------------------------------------------
          |                    |                       |
          ▼                    ▼                       ▼
 +----------------+   +----------------+   +----------------+
 | Bank A         |   | MFI B          |   | Wallet C       |
 | Connector Repo |   | Connector Repo |   | Connector Repo |
 +----------------+   +----------------+   +----------------+
          |                    |                       |
          +--------------------+-----------------------+
                               |
                        DFSP Systems / CBS

Hub (FSPIOP APIs)
        │
        ▼
Pivotal Connector (Common Framework)
        │
        ├── Listens to Hub FSPIOP API requests
        ├── Delegates processing to DFSP Connector
        ├── Receives DFSP responses
        └── Publishes responses/events to Pivotal
```

## Key Features

- Listens to the Hub's FSPIOP API requests
- Publishes transaction responses and events to the Pivotal platform
- Shared connector framework for all DFSP integrations
- Packaged as a reusable Maven JAR
- Common transaction processing and routing logic
- Standardized request/response handling
- Configurable connector behavior
- Common exception handling and logging
- Extensible architecture for onboarding new DFSPs
- Reduced code duplication across connector implementations

## Repository Responsibilities

This repository is responsible for:

- Listening to the Hub's FSPIOP API callbacks and requests
- Routing transactions to the appropriate DFSP connector implementation
- Providing the common integration framework
- Defining extension points for DFSP-specific implementations
- Maintaining shared models, services, utilities, and configurations
- Publishing responses and transaction updates to the Pivotal platform
- Delivering versioned JAR releases for downstream connector projects

Individual DFSP repositories are responsible for:

- Implementing institution-specific APIs or CBS integrations
- Processing requests received from the Pivotal Connector
- Returning standardized responses to the common framework
- Managing DFSP-specific configurations
- Importing and using the JAR published from this repository

## Technology Stack

- Java
- Spring Boot
- Maven

## Build

Install everything locally:

```bash
mvn clean install
```

Run the full test suite:

```bash
mvn test
```

Build a packaged application:

```bash
mvn clean package
```

## Project Layout

- `pom.xml` - parent Maven build
- `implementation/mod_fspiop_interface` - generated FSPIOP API client
- `implementation/mod_component` - shared connector library
- `implementation/mod_pivotal_connector_api` - Spring Boot application

## License

Copyright © 2026 ThitsaWorks.

Licensed under the Apache License, Version 2.0.
