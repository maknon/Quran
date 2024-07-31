#target illustrator

var sourceFolder = Folder.selectDialog( "Select the folder", "~" );

var distFolder = new Folder( sourceFolder.absoluteURI + "-png-transparent" );
distFolder.create();

var files = sourceFolder.getFiles( "*.ai" );

for ( i = 0; i < files.length; i++ )
{
	app.open(files[i]);

	var file_name = app.activeDocument.name.toString().replace(".ai", "");

	var options = new ExportOptionsPNG8();
	options.transparency = true;
	options.artBoardClipping = true;
	//options.colorCount = 8; // Not good for transparent pages but reduce 20% of size
	options.horizontalScale = 320;
	options.verticalScale = 320;

	app.activeDocument.exportFile( new File(distFolder + "/" + file_name), ExportType.PNG8, options );
	
	app.activeDocument.close(SaveOptions.DONOTSAVECHANGES);
}