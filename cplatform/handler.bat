@echo off
setlocal

set "HANDLER_DIR=%~dp0"
set "LOG_FILE=%HANDLER_DIR%handler.last.txt"
set "CONTROL_NAME=%~1"
set "SELECTED_LABEL=%~2"
set "SELECTED_VALUE=%~3"

(
    echo [HANDLER] %DATE% %TIME%
    echo control=%CONTROL_NAME%
    echo selected_label=%SELECTED_LABEL%
    echo selected_value=%SELECTED_VALUE%
    echo.
) >> "%LOG_FILE%"

powershell -NoProfile -Command "Add-Type -AssemblyName PresentationFramework; [System.Windows.MessageBox]::Show('Control: %CONTROL_NAME%`nLabel: %SELECTED_LABEL%`nValue: %SELECTED_VALUE%', 'ComboBox Handler')" >nul 2>nul

endlocal
exit /b 0:: monitor-test 2026-08-10T18:00:46.9327122-04:00
:: callback-test 2026-08-10T18:01:39.1067743-04:00
