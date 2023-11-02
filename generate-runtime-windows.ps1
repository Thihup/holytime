
$Dependencies = ConvertFrom-StringData (Get-Content .\dependencies.properties -raw)

# Create an in-memory module so $ScriptBlock doesn't run in new scope
$null = New-Module {
    function Silence {
        [CmdletBinding()]
        param (
            [Parameter(Mandatory)] [scriptblock] $ScriptBlock
        )

        # Save current progress preference and hide the progress
        $prevProgressPreference = $global:ProgressPreference
        $global:ProgressPreference = 'SilentlyContinue'

        try {
            # Run the script block in the scope of the caller of this module function
            . $ScriptBlock
        }
        finally {
            # Restore the original behavior
            $global:ProgressPreference = $prevProgressPreference
        }
    }
}

function silentMkdir { $sink = mkdir $args }

silentMkdir mods
cd mods

echo "Downloading dependencies"


Silence {
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/openjdk/nashorn/nashorn-core/$($Dependencies.nashorn_version)/nashorn-core-$($Dependencies.nashorn_version).jar -OutFile nashorn-core-$($Dependencies.nashorn_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/ow2/asm/asm/$($Dependencies.asm_version)/asm-$($Dependencies.asm_version).jar -OutFile asm-$($Dependencies.asm_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/$($Dependencies.asm_version)/asm-commons-$($Dependencies.asm_version).jar -OutFile asm-commons-$($Dependencies.asm_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/$($Dependencies.asm_version)/asm-tree-$($Dependencies.asm_version).jar -OutFile asm-tree-$($Dependencies.asm_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/$($Dependencies.asm_version)/asm-analysis-$($Dependencies.asm_version).jar -OutFile asm-analysis-$($Dependencies.asm_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/org/ow2/asm/asm-util/$($Dependencies.asm_version)/asm-util-$($Dependencies.asm_version).jar -OutFile asm-util-$($Dependencies.asm_version).jar
    Invoke-WebRequest -Uri https://repo1.maven.org/maven2/com/sun/xml/ws/jaxws-ri/$($Dependencies.jaxws_version)/jaxws-ri-$($Dependencies.jaxws_version).zip -OutFile jaxws-ri.zip
    Invoke-WebRequest -Uri https://download2.gluonhq.com/openjfx/$($Dependencies.openjfx_version)/openjfx-$($Dependencies.openjfx_version)_windows-x64_bin-jmods.zip -OutFile openjfx_windows-x64_bin-jmods.zip
}

echo "Extracting"
Silence {
    Expand-Archive -DestinationPath . -Path jaxws-ri.zip
    Expand-Archive -DestinationPath . -Path openjfx_windows-x64_bin-jmods.zip
}

echo "Adding missing module-info to jakarta.annotation-api.jar"

jdeps --generate-module-info tmp ./jaxws-ri/lib/jakarta.annotation-api.jar
javac --patch-module=java.annotation=jaxws-ri/lib/jakarta.annotation-api.jar ./tmp/java.annotation/module-info.java
jar uf ./jaxws-ri/lib/jakarta.annotation-api.jar -C ./tmp/java.annotation/ module-info.class

echo "Generating runtime"
cd ..
jlink --module-path "mods;mods/javafx-jmods-$($Dependencies.openjfx_version);mods/jaxws-ri/lib" --add-modules $($Dependencies.modules) --output holytime --compress zip-6 --generate-cds-archive --strip-debug

Silence {
    Compress-Archive -Path holytime -DestinationPath holytime-windows && echo "Generated runtime --> $(PWD)\holytime-windows.zip"
}
