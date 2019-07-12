@echo off
set /p testname="Enter the test name: "
if not ["%testname%"]==[""] (
	mkdir %testname%
	echo package com.test;>>"%testname%\%testname%.javapp"
	echo.>>"%testname%\%testname%.javapp"
	echo public class %testname% {>>"%testname%\%testname%.javapp"
	echo.	>>"%testname%\%testname%.javapp"
	echo }>>"%testname%\%testname%.javapp"
	
	echo package com.test;>>"%testname%\%testname%.java"
	echo.>>"%testname%\%testname%.java"
	echo public class %testname% {>>"%testname%\%testname%.java"
	echo.	>>"%testname%\%testname%.java"
	echo }>>"%testname%\%testname%.java"
	
	notepad++ "%testname%\%testname%.java"
	notepad++ "%testname%\%testname%.javapp"
)