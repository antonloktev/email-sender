package com.netcracker.edu.sender;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Parser {
    public static List<Person> readFromExcel(String filename) throws IOException {
        File myFile = new File(filename);
        FileInputStream fis = new FileInputStream(myFile);

        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);  // Finds the workbook instance for XLSX file
        XSSFSheet mySheet = myWorkBook.getSheetAt(0); // Return first sheet from the XLSX workbook
        Iterator<Row> rowIterator = mySheet.iterator(); // Get iterator to all the rows in current sheet

        List<Person> persons = new ArrayList<>();

        while (rowIterator.hasNext()) { // Traversing over each row of XLSX file
            Row row = rowIterator.next();
            Person person = new Person();
            Iterator<Cell> cellIterator = row.cellIterator(); // // For each row, iterate through each columns
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (row.getRowNum() > 0) {  // To filter column headings
                    // filtering persons with empty values
                    if (cell.getColumnIndex() == 0 && !cell.getCellType().equals(CellType.BLANK)) { // name
                        String name = cell.getStringCellValue();
                        person.setName(name);
                    } else if (cell.getColumnIndex() == 2 && !cell.getCellType().equals(CellType.BLANK)) { //email
                        String email = cell.getStringCellValue();
                        if (person.getName() != null) {
                            person.setEmail(email);
                            persons.add(person);
                        }
                    }
                }
            }
        }
        return persons;
    }
}
