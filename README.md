# Dynamic Reputation Prototype

This application pulls messages from the GCP Pub/Sub topic and updates the FHIR Store accordingly.

## Environment Setup (Without Docker)

1.) Install maven 3.6.0 and Java JDK 11. JDK located [here](https://openjdk.java.net/install/) or use the Maven wrapper
that's a part of this repository.

```bash
brew install maven
```

2.) Clone down the repository from GitLab

```bash
git clone git@git.carekinesis.net:integration-services/dynamic-reputation-prototype.git
```

3.) Contact [John Ravan](mailto:jravan@trhc.com) in order to get the values for the `application.properties`

4.) After populating the properties build the project

```bash
./mvnw clean install
```

5.) Run the worker.

```bash
./mvnw spring-boot:run
```

The project is configured with Spring dev tools which allows for hot reloads without restarting the application. Simply
rebuild the project after making a change (In IntelliJ use `Build -> Build Project` or `CMD + F9`) and the application will
auto re-deploy.

6.) Success!

## Docker Setup

1.) Install Docker located [here](https://docs.docker.com/docker-for-mac/install/).

2.) Clone down the repository from GitLab

```bash
git clone git@git.carekinesis.net:integration-services/dynamic-reputation-prototype.git
```

3.) Contact [John Ravan](mailto:jravan@trhc.com) in order to get the values for the `application.properties`

4.) From the root directory run the following command to build the project.

```bash
docker-compose build
```

5.) To run the worker

```bash
docker-compose up
```

6.) Success!