#!/bin/bash
mvn compile exec:java -Dexec.mainClass="com.mycompany.automatedferryticketingsystem.view.WelcomeScreen" \
                      -DDB_URL=jdbc:mariadb://127.0.0.1:3306/ferry_ticketing_db \
                      -DDB_USER=ferry_user \
                      -DDB_PASS=michaelvalles123
