package visualizer;
/**
 * 
 */


/**
 * This class contains some static string constants that are 
 * used throughout the program. 
 * 
 * 
 * @author Rene Wegener
 *
 */
public class StringConstants 
{
	public static String RowLayout = "Zeile fuer Zeile",
		ColumnLayout = "Spalte fuer Spalte",
		ZLayout = "Z-Kurve",
		MyZLayout = "angepasste Z-Kurve",
		HilbertLayout = "Hilbert Kurve",
		TableLayout = "Tabelle",
		OptimizedTableLayout = "Sortierte Tabelle",
		JigsawLayout = "Gitter-Anordnung",
		MDSLayout = "MDS-Anordnung", 
		FatRowLayout = "Schlangenlinien",
		MatrixLayout = "Matrix",
		GrayScale = "Grauwertskala",
		HeatScale = "Temperaturskala",
		GlyphBorders = "Glyphenumrandung",
		GlyphSpaces = "Glyphenabstand", 
		ColorsInverted = "Invertierte Farbskalen",
		BackgroundColor = "Hintergrundfarbe",
		BackWhite = "Weiss",
		BackGray = "Grau",
		BackBlue = "Blau",
		AuthorFilter = "Daten filtern",
		ColorChooser = "Farben waehlen",
		Data2D = "Tabelle",
		Data3D = "Wuerfel",
		RowText = "<html>Anordnung Zeile für Zeile ohne jegliche Beschriftung. <br>Die Daten behalten ihre ursprüngliche Reihenfolge bei.</html>",
		TableText = "<html>Tabellarische Anordnung mit Beschriftung. <br>Die Reihenfolge entspricht derjenigen der Zeile-für-Zeile-Darstellung.<br>Haben Sie eine drei-dimensionale Datenstruktur <br>geladen, so stimmen X- und Y-Achse der Tabelle<br>mit den Achsen der ursprünglichen Datenstruktur überein. <br> Die Z-Achse wird auf die einzelnen Pixel in den Glyphen abgebildet.</html>",
		OptimizedTableText = "<html>Tabelle, bei der die Glyphen anhand ihrer Mittelwerte absteigend<br> sortiert werden. Haben Sie eine zwei-dimensionale Datenstruktur geladen, erfolgt zusätzlich eine Optimierung<br>auf Basis der Ähnlichkeiten der Glyphen.</html>",
		MDSText = "<html>Die Glyphen werden anhand ihrer Ähnlichkeit über den Raum verteilt.<br>So lassen sich evtl. einzelne Gruppen erkennen, allerdings<br>überlappen sich Glyphen mitunter. Sollte die Übersicht<br>zu gering sein, wählen Sie die " + JigsawLayout + ".</html>",
		JigsawText = "<html>Wie bei der " + MDSLayout + "erfolgt hier eine Anordnung nach <br>Ähnlichkeit, jedoch in Form eines Gitters. Gruppierungen sind so evtl. schlechter<br> zu erkennen, dafür überlappen sich keine Glyphen.</html>",
		pixelRowText = "<html>Verteilung in Zeilen untereinander. Die Breite der Zeilen<br> ist wählbar. Sie können so dafür sorgen, dass die Glyphen bestimmte<br>Maße haben oder bspw. die Werte desselben Monats immer untereinander stehen.</html>",
		pixelColumnText = "<html>Analog zur Zeilendarstellung, allerdings in Spalten von links nach rechts.</html>",
		pixelZText = "<html>Die Z-Kurve ordnet die Pixel rekursiv z-förmig an.<br>Auf diese Weise können Muster manchmal besser identifiziert werden.<br>Weicht die Anzahl der Pixel pro Glyph jedoch stark von einer Viererpotenz ab,<br>wirkt diese Anordnung oft unästhetisch.<br>Wählen Sie dann besser die " + MyZLayout + "</html>",
		pixelMyZText ="<html>Im Gegensatz zur Z-Kurve viertelt diese Kurve die<br>Glyphenflächen nicht exakt. Dadurch entstehen kleine Pixelblöcke von leicht<br>unterschiedlicher Größe, der Gesamteindruck ist jedoch oft ästhetischer<br>als bei der " + ZLayout + ".</html>",
		pixelHilbertText = "<html>Diese Kurve ist komplexer und schwieriger <br>nachzuvollziehen als die Z-Kurven, erreicht jedoch eine Anordnung,<br>welcher der ursprünglichen ein-dimensionalen Reihenfolge der Pixel am nächsten kommt.<br>Dadurch sind Muster mitunter gut zu erkennen.</html>",
		Nothing = "";
}
