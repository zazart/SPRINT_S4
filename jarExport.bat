@echo off
@REM example of using this script >jarExport.bat "." "." "wilk" "C:\Program Files\Apache Software Foundation\Tomcat 10.1_Tomcat10.1.7\lib\servlet-api.jar"

set "working_dir=%~1"
set "local=%~2"
set "name=%~3"
@REM set "servletLink=%~4"
set "servletLink=C:\Program Files\Apache Software Foundation\Tomcat 10.1_Tomcat10.1.7\lib\servlet-api.jar"

if "%working_dir%"=="" (
    echo Le lien vers le répertoire de travail est vide.
    goto :EOF
)
if "%local%"=="" (
    echo Le lien vers le dossier temporaire est vide.
    goto :EOF
)
if "%name%"=="" (
    echo Le nom est vide.
    goto :EOF
)
if "%name%"=="" (
    echo Le lien vers le servlet-api.jar est vide 
    goto :EOF
)


if exist "%local%\out" (
    rmdir /s /q "%local%\out"
)
mkdir "%local%\out"


echo Copie de tout les fichiers java vers un dossier temporaire "out" 
for /r "%working_dir%\src" %%f in (*.java) do copy "%%f" "%local%\out"
echo Copie avec succes

echo Compilation de toute ces fichiers
javac -cp "%servletLink%" -d "%local%" "%local%\out\*.java"

jar -cf "%name%.jar" %local%\mg

rmdir /s /q "%local%\mg"
rmdir /s /q "%local%\out"

