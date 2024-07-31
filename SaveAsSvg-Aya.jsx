#target illustrator

var sourceFolder = Folder.selectDialog( "Select the folder", "~" );

var distFolder = new Folder( sourceFolder.absoluteURI + "-svg-transparent" );
distFolder.create();

var files = sourceFolder.getFiles( "*.ai" );

for ( i = 0; i < files.length; i++ )
{
	app.open(files[i]);

	var file_name = app.activeDocument.name.toString().replace(".ai", "");

	var options = new ExportOptionsSVG();
	options.cssProperties = SVGCSSPropertyLocation.PRESENTATIONATTRIBUTES;
	options.coordinatePrecision = 2;
	options.fontSubsetting = SVGFontSubsetting.None;
	options.embedAllFonts = false
	app.activeDocument.exportFile( new File(distFolder + "/" + file_name), ExportType.SVG, options );

	app.activeDocument.close(SaveOptions.DONOTSAVECHANGES);
}