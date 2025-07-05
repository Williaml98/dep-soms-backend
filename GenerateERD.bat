@echo off
echo Generating ERD for your database...

REM Set the project directory
set PROJECT_DIR=%~dp0

REM Download PostgreSQL JDBC driver if needed
if not exist "%PROJECT_DIR%\postgresql-42.6.0.jar" (
    echo Downloading PostgreSQL JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://jdbc.postgresql.org/download/postgresql-42.6.0.jar' -OutFile '%PROJECT_DIR%\postgresql-42.6.0.jar'"
)

REM Compile the Java file
javac -cp ".;%PROJECT_DIR%\postgresql-42.6.0.jar;%PROJECT_DIR%\target\classes" %PROJECT_DIR%\src\main\java\tools\schemaexporter\CompleteERDGenerator.java

REM Run the generator
java -cp ".;%PROJECT_DIR%\postgresql-42.6.0.jar;%PROJECT_DIR%\target\classes;%PROJECT_DIR%\src\main\java" tools.schemaexporter.CompleteERDGenerator

echo Done!
pause
