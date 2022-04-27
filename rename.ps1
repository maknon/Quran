# Remove ___Hafs39__DM / ___Warsh39__DM from file names
get-childitem pages-hafs/*.png | foreach { rename-item $_ $_.Name.Replace("___Hafs39__DM", "") }
get-childitem pages-warsh/*.png | foreach { rename-item $_ $_.Name.Replace("___Warsh39__DM", "") }
get-childitem pages-qalon/*.png | foreach { rename-item $_ $_.Name.Replace("___qaloun__DM", "") }
get-childitem pages-shubah/*.png | foreach { rename-item $_ $_.Name.Replace("___shuba__DM", "") }
get-childitem pages-douri/*.png | foreach { rename-item $_ $_.Name.Replace("___douri__DM", "") }

# Remove leading zeros from file names
Get-ChildItem . -File -Filter "pages-hafs/0*.*" | ForEach-Object { Rename-Item $_.FullName $($_.Name -replace '0+(?=\d)', '') }
Get-ChildItem . -File -Filter "pages-warsh/0*.*" | ForEach-Object { Rename-Item $_.FullName $($_.Name -replace '0+(?=\d)', '') }
Get-ChildItem . -File -Filter "pages-qalon/0*.*" | ForEach-Object { Rename-Item $_.FullName $($_.Name -replace '0+(?=\d)', '') }
Get-ChildItem . -File -Filter "pages-shubah/0*.*" | ForEach-Object { Rename-Item $_.FullName $($_.Name -replace '0+(?=\d)', '') }
Get-ChildItem . -File -Filter "pages-douri/0*.*" | ForEach-Object { Rename-Item $_.FullName $($_.Name -replace '0+(?=\d)', '') }