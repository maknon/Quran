#target illustrator

var sourceFolder = Folder.selectDialog( "Select the folder", "~" );

var distFolder = new Folder( sourceFolder.absoluteURI + "-png" );
distFolder.create();

var files = sourceFolder.getFiles( "*.ai" );

for ( i = 0; i < files.length; i++ )
{
	app.open(files[i]);

	//alert( files[i].name, "Script Alert", true);
	app.activeDocument.selectObjectsOnActiveArtboard();
	app.activeDocument.fitArtboardToSelectedArt(0);
	
	var mast = app.activeDocument.layers.add();

	mast.name = 'background';

	var r = app.activeDocument.artboards[0].artboardRect;
	var background = app.activeDocument.pathItems.rectangle(r[1], r[0], r[2]-r[0], r[1]-r[3]);
	background.strokeWidth = 0;
	background.stroked = false;

	var col = new CMYKColor();

	col.cyan = 0;
	col.magenta = 0;
	col.yellow = 5;
	col.black = 0;

	//background.filled = true;
	background.fillColor = col;
	//background.strokeColor = col;

	//redraw();
	
	for ( var  k = 1; k < app.activeDocument.layers.length; k++ )
		mast.move(  app.activeDocument.layers[k], ElementPlacement.PLACEATEND );
	
	var file_name = app.activeDocument.name.toString().replace(".ai", "");

	var options = new ExportOptionsPNG8();
	options.antiAliasing = true;
	options.transparency = false;
	//options.colorCount = 8; // Use it for PDF since it reduces the size of 20% and no visual difference when used with background in the PDF
	options.horizontalScale = 320;
	options.verticalScale = 320;

	app.activeDocument.exportFile( new File(distFolder + "/" + file_name), ExportType.PNG8, options );
	
	app.activeDocument.close(SaveOptions.DONOTSAVECHANGES);
}