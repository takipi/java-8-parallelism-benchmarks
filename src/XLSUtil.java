import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class XLSUtil {
	public static Workbook createWorkbook() throws IOException {
		Workbook wb = new HSSFWorkbook();
		return wb;
	}

	public static void createColumn(Sheet sheet) {
		// Create a row and put some cells in it. Rows are 0 based.
		Row row = sheet.createRow((short) 0);
		row.setRowStyle(getColumnsStyle((HSSFWorkbook) sheet.getWorkbook()));
		// Create a cell and put a value in it.
		row.createCell(0).setCellValue("Seq Sort ");
		row.createCell(1).setCellValue("Seq reduce ");
		row.createCell(2).setCellValue("Seq filter ");
		row.createCell(3).setCellValue("Parallel Sort");
		row.createCell(4).setCellValue("Parallel reduce ");
		row.createCell(5).setCellValue("Parallel filter");
		
	}

	public static void writeToXls(Workbook wb, String filePath) throws IOException {

		FileOutputStream fileOut = new FileOutputStream(filePath);
		wb.write(fileOut);
		fileOut.close();
	}
	
	public static void autoSizeColumn(Sheet sheet,int numOfColumn)
	{
		for(int i=0;i<numOfColumn;i++)
		sheet.autoSizeColumn(i);
	}
	
	
	public static synchronized void setCell(Row row, int index, long duration)
	{	
		Cell cell = row.getCell(index);
		
		if (cell != null)
		{
			double value = cell.getNumericCellValue();
			cell.setCellValue(value + duration);
			System.out.println("GFDHFDGHDFGHFGHFG");
		}
		else
		{
			row.createCell(index).setCellValue(duration);
		}
	}
	
	public static Row createRow(Sheet sheet,int row)
	{
		if (sheet.getRow(row) != null)
		{
			return sheet.getRow(row);
		}

		return sheet.createRow((short) row);
	}
	public static HSSFCellStyle getColumnsStyle(HSSFWorkbook wrkbk) {
	    HSSFCellStyle style = wrkbk.createCellStyle();
	    style.setWrapText(true);        
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	    HSSFFont timesBoldFont = wrkbk.createFont();
	    timesBoldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	    timesBoldFont.setFontName("Arial");
	    style.setFont(timesBoldFont);
	    return style;
	}
}
