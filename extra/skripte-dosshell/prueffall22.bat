@echo off

call ..\..\..\skripte-dosshell\einstellungen.bat

set cp=..\..\de.bsvrz.sys.funclib.bitctrl\de.bsvrz.sys.funclib.bitctrl-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.progglaette-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.progglaette-test.jar
set cp=%cp%;..\..\junit-4.1.jar

title Pruefungen SE4 - DUA, SWE 4.14

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.14
echo #
echo #  Testet die  Publikation der Datensätze bei fehlenden Eingabedaten
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.progglaette.progglaette.GlaetteWarnungUndPrognoseTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.14
echo #
echo #  Testet den Flussdiagram
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.progglaette.progglaette.EntscheidungsBaumTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.14
echo #
echo #  Testet die Prognoseberechnung - Trendinterpolation
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.progglaette.progglaette.PrognoseZustandTest
pause

