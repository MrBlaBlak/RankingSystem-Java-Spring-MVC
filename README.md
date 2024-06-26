# Titanfall 2 Balanced Teams Ranking System
## Introduction
Welcome to the Titanfall 2 Balanced Teams Ranking System! This project aims to create a balanced team selection and ranking system for the first-person shooter (FPS) game Titanfall 2. The system focuses on ensuring fair and competitive matches by intelligently forming teams based on players' Matchmaking Ratings (MMR) and implementing a comprehensive ranking algorithm.

In Titanfall 2's "Capture the Flag" mode, two teams of five players each compete to capture more flags than their opponents. The game features powerful titans, colossal machines that can be summoned onto the battlefield periodically, adding an extra layer of strategy to the gameplay.

## Features
### Team Formation
The ranking system initiates by selecting two teams based on the MMR of individual players. The allocation of players to teams is done in a way that minimizes the MMR difference, ensuring teams are as balanced as possible.

### Ranking Algorithm
After each match, the system collects player statistics, determines the winner, and assigns or deducts points accordingly. The ranking algorithm takes into account the following factors:

* **Match Outcome:** The greater the difference in flags captured between the winning and losing teams, the more points are awarded or deducted.

* **Winstreak:** Players receive additional points based on the percentage of victories in their last 10 matches, encouraging consistent performance.

* **Match Resolution:** Points are influenced by whether the match ended within the regulation time or resulted in a Sudden Death situation.

### Player Statistics Tracking
The system continuously monitors player statistics, allowing users to explore various insightful summaries, including:

* **Eliminations and Flag Captures:** Detailed information on the number of eliminations and flag captures on specific maps, along with average and best performances.

* **Titan Effectiveness:** Insights into players' efficiency when using different titans.

* **Best Wingman:** Frequency of wins and losses when playing on the same team with specific players.

* **Worst Nemesis:** Frequency of wins and losses when facing off against particular players.

## More information
For more explanation you can watch video below

https://www.youtube.com/watch?v=UoY1Vs-6aJ4

## How to run

### Docker
#### Configure Persistence Settings:
Open the persistence.xml file located in the src/main/resources/META-INF directory of your project.
Verify that the javax.persistence.jdbc.user and javax.persistence.jdbc.password properties match your MySQL configuration.

Build war and docker image  
`mvn clean package && docker build -t mrblablak-ranking .`

Run app with docker-compose  
`docker-compose up`

Stop app    
`docker-compose down`

Open a web browser and go to http://localhost:8080/rankingSystemJavaSpringMVC-1

### Tomcat

#### Configure Persistence Settings:
Open the persistence.xml file located in the src/main/resources/META-INF directory of your project.
Verify that the javax.persistence.jdbc.url, javax.persistence.jdbc.user, and javax.persistence.jdbc.password properties match your MySQL configuration.

#### Build the project using Maven:

`mvn clean install`

#### Deploy the WAR file to Apache Tomcat:

Copy the generated WAR file (usually located in the target directory) to the webapps directory of your Tomcat installation.

#### Start Tomcat:

Navigate to the Tomcat installation directory.
Start Tomcat using the startup script:

`./bin/startup.sh`     # For Unix-based systems

`./bin/startup.bat`    # For Windows

#### Access the application:
Open a web browser and go to http://localhost:8080/rankingSystemJavaSpringMVC-1