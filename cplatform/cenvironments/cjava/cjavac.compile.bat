::---------------------------------------------------------------------------------------------------
:: name: cjavac.bat
:: desc: Compiles the entire C3DClasses framework or compiles single files against the framework
:: usage: cjavac [JavaFile.java ...]
::---------------------------------------------------------------------------------------------------
@echo off
set "CJAVACHOME=%CD%"
echo [CALLING] %~nx0

if "%C3DCLASSES_JAVA%"=="" (
	set "C3DCLASSES_JAVA=%~dp0..\..\..\cdata\cmetadata\c3dclasses_java"
)

if "%C3DCLASSES_JAR%"=="" (
	set "C3DCLASSES_JAR=%C3DCLASSES_JAVA%\target\c3dclassessdk-1.0-jar-with-dependencies.jar"
)

if "%C3DCLASSES_CLASSPATH%"=="" (
	if exist "%C3DCLASSES_JAR%" (
		set "C3DCLASSES_CLASSPATH=%C3DCLASSES_JAR%;."
	) else (
		if exist "%C3DCLASSES_JAVA%\target\classes" (
			set "C3DCLASSES_CLASSPATH=%C3DCLASSES_JAVA%\target\classes;."
		) else (
			set "C3DCLASSES_CLASSPATH=."
		)
	)
)

if "%1"=="" goto NOPARAM
echo [MODE] Compile provided Java source file(s)
echo [INFO] Classpath: %C3DCLASSES_CLASSPATH%
javac -classpath "%C3DCLASSES_CLASSPATH%" %*
goto DONE

:NOPARAM
echo [MODE] No file arguments provided - refreshing Java environment
call cjava.update.bat
goto DONE

:DONE
echo [ENDING] %~nx0
cd /d "%CJAVACHOME%"
