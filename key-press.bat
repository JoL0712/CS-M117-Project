@if (@CodeSection == @Batch) @then


@echo off

set SendKeys=CScript //nologo //E:JScript "%~F0"

%SendKeys% %*

goto :EOF


@end


// JScript section

var WshShell = WScript.CreateObject("WScript.Shell");
WshShell.SendKeys(WScript.Arguments(0));