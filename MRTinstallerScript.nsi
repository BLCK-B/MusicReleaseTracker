; UI settings
!include "MUI2.nsh"
!define VERSION "4.2"
!define MUI_ABORTWARNING
!define MUI_ICON "MRTicon.ico"
!insertmacro MUI_PAGE_LICENSE "license.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

Var JDKPath

Function OpenLink
    ExecShell "open" "https://www.oracle.com/java/technologies/downloads/#jdk20-windows"
FunctionEnd

; Function to search for JDK
Function SearchForJDK
    StrCpy $JDKPath ""
    StrCpy $JDKPath "C:\Program Files\Java\jdk-20"
    IfFileExists "$JDKPath\bin\java.exe" JDKFound
    
    JDKNotFound:
    MessageBox MB_ICONEXCLAMATION|MB_OK "JDK not found. The solution is in the repository readme at github.com/BLCK-B/MusicReleaseTracker"
    Quit
    
    JDKFound:
FunctionEnd

Section "Uninstall"

    Delete "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk"
    Delete "$INSTDIR\MusicReleaseTracker.exe"
    Delete "$INSTDIR\Uninstall.exe"
    RMDir "$INSTDIR"
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker"

SectionEnd

; Installer section
Section
    ; Search for JDK
    Call SearchForJDK

    ; Remove the old version if it's installed
    ReadRegStr $R0 HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayVersion"
    StrCmp $R0 "${VERSION}" NoUninstallOldVersion
    
    ; Uninstall old version
    ExecWait '"$INSTDIR\Uninstall.exe" /S' ; Silent uninstall
    
    NoUninstallOldVersion:

    ; Create the installation directory
    SetOutPath "$INSTDIR"

    ; Install the new version
    File "MusicReleaseTracker.exe"
    
    ; Create Start Menu shortcut
    CreateDirectory "$SMPrograms\MusicReleaseTracker"
    CreateShortCut "$SMPrograms\MusicReleaseTracker\MusicReleaseTracker.lnk" "$INSTDIR\MusicReleaseTracker.exe"

    ; Create uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"
    
    ; Register the uninstaller in the registry
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayName" "MusicReleaseTracker"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayVersion" "${VERSION}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "Publisher" "BLCK"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\MusicReleaseTracker" "DisplayIcon" "$INSTDIR\MusicReleaseTracker.exe,0"

SectionEnd

; Installer details
Name "MusicReleaseTracker"
Outfile "MRT-${VERSION}-win.exe"
Icon "MRTicon.ico"
InstallDir $PROGRAMFILES\MusicReleaseTracker
ShowInstDetails show

; Uninstaller details
UninstallCaption "Uninstall MusicReleaseTracker"
UninstallIcon "MRTicon.ico"

; This line is important to include for creating the uninstaller executable
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES