#target illustrator

var sourceFolder = Folder.selectDialog( "Select the folder", "~" );

var distFolder = new Folder( sourceFolder.absoluteURI + "-svg" );
distFolder.create();

var files = sourceFolder.getFiles( "*.ai" );

for ( i = 0; i < files.length; i++ )
{
	app.open(files[i]);

	app.activeDocument.selectObjectsOnActiveArtboard();
	app.activeDocument.fitArtboardToSelectedArt(0);
	
	var file_name = app.activeDocument.name.toString().replace(".ai", "");

	var options = new ExportOptionsSVG();
	//options.compressed = true;
    //options.rasterImageLocation = RasterImageLocation.EMBED;
	options.cssProperties = SVGCSSPropertyLocation.PRESENTATIONATTRIBUTES;
	//options.DTD = SVGDTDVersion.SVGTINY1_1;
	options.coordinatePrecision = 2;
	//options.fontType = SVGFontType.SVGFONT;
	options.fontSubsetting = SVGFontSubsetting.None;
	options.embedAllFonts = false
	app.activeDocument.exportFile( new File(distFolder + "/" + file_name), ExportType.SVG, options );

	/*
	var options = new ExportOptionsWebOptimizedSVG();
	options.svgMinify = true;
	options.coordinatePrecision = 2;
	options.svgResponsive = false;
	options.svgId = SVGIdType.SVGIDMINIMAL;
	app.activeDocument.exportFile( new File(distFolder + "/" + file_name), ExportType.WOSVG, options );
	*/
	app.activeDocument.close(SaveOptions.DONOTSAVECHANGES);
}