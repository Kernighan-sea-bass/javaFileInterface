import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.JAXBException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import org.jopendocument.dom.spreadsheet.SpreadSheet; 
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.MutableCell;
import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Text;
import com.itextpdf.text.Font;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.*;

interface IFruttoFile{     //'I' indica un interfaccia

  public ArrayList<Frutto> read(String filename) throws IOException, JAXBException;
  
  public void write(ArrayList<Frutto> frutti, String filename) throws IOException, JAXBException, DocumentException;
}

class FruttoCsv implements IFruttoFile{   //implements può essere multiplo non come ereditarietà

  public ArrayList<Frutto> read(String fileName) throws IOException, JAXBException{

    ArrayList<Frutto> frutti = new ArrayList<Frutto>();
    FileReader fileRead = new FileReader(fileName);
    
    //apertura stream di lettura
    BufferedReader buff = new BufferedReader(fileRead);

    String line = buff.readLine();    //lettura fuori ciclo per inizializzare line
    while(line != null){

      //aggiunta dei frutti alla collezione
      frutti.add(new Frutto(line));
      line = buff.readLine();
    }

    buff.close(); //chiusura file in lettura (non obbligatorio)

    return frutti;
  }

  public void write(ArrayList<Frutto> frutti, String fileName) throws IOException, JAXBException, DocumentException{

     PrintWriter fileWrite = new PrintWriter(fileName);  //istanza di una delle classi per leggere da file

    for(Frutto f: frutti)
      fileWrite.println(f.toLine());  //scrive linea su file e manda a capo

    fileWrite.close();  //chiusura file in scrittura
  }
}

class FruttoJson implements IFruttoFile{

  public ArrayList<Frutto> read(String fileName) throws IOException, JAXBException{

    FileReader fr = new FileReader(fileName);
    ArrayList<Frutto> frutti = new ArrayList<Frutto>();
    Gson gson = new Gson();
    Type fruttiType = new TypeToken<ArrayList<Frutto>>(){}.getType();   //passaggio intermedio per ottenere ArrayList
    
    frutti = gson.fromJson(fr, fruttiType);

    return frutti;
  }

  public void write(ArrayList<Frutto> frutti, String fileName) throws IOException, JAXBException, DocumentException{

    Gson gson = new Gson(); //Gson = creata da Google
    
    String jsonStr = gson.toJson(frutti);   //crea json basandosi automaticamente sull'oggetto passato per parametro

    PrintWriter printfWrite = new PrintWriter(fileName);
    printfWrite.println(jsonStr);
    printfWrite.close();
  }

}

class FruttoXml implements IFruttoFile{

  public ArrayList<Frutto> read(String fileName) throws IOException, JAXBException{

    JAXBContext context = JAXBContext.newInstance(Frutti.class);   //factory method != costruttore 
    Unmarshaller unmarsh = context.createUnmarshaller();

    Frutti f = (Frutti) unmarsh.unmarshal(new FileInputStream(fileName));   //ritorna maniglia a Object quindi casting
    ArrayList<Frutto> frutti = f.getFrutti();
    return frutti;
  }

  public void write(ArrayList<Frutto> frutti, String fileName) throws IOException, JAXBException, DocumentException{

    PrintWriter fw = new PrintWriter(fileName); //non funziona con true come parametro -- chiedi

    JAXBContext context = JAXBContext.newInstance(Frutti.class);   //factory method != costruttore 
                                                                  //a differemza do costruttore non ritorna eccezione se c'è un errore ma magari ritorna null
    Marshaller marsh = context.createMarshaller();  //classe JAXBContext contiene factory method di Marshaller
    marsh.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); //classe Marshall contiene proprietà final -> classe non estensibile
    
    Frutti ret = new Frutti();
    ret.setFrutti(frutti);
    marsh.marshal(ret, fw);

    fw.close();
  }
}

class FruttoXls implements IFruttoFile{ 

  public ArrayList<Frutto> read(String fileName) throws IOException, JAXBException{

    ArrayList<Frutto> frutti = new ArrayList<Frutto>();
    FileInputStream fp = new FileInputStream(fileName);

    Workbook wb = new HSSFWorkbook(fp);
    org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);   //a getSheetAt() passiamo il numero del foglio partendo da 0

    int rowNum = sheet.getLastRowNum();   //restituisce numero di righe valorizzate
    
    String nome = null;
    Stagionalita stagionalita = Stagionalita.DEFAULT;
    int costo = 0;

    for(int i = 0; i <= rowNum; i++){

      Row row = sheet.getRow(i);
     
      if(row != null){

        int j = 0;
        Cell cellNome = row.getCell(j);
        Cell cellStagionalita = row.getCell(++j);
        Cell cellCosto = row.getCell(++j);
         
        nome = null;
        stagionalita = Stagionalita.DEFAULT;
        costo = 0;

        if(cellNome != null)
          nome = cellNome.getStringCellValue();
        if(cellStagionalita != null)
          stagionalita = Stagionalita.valueOf(cellStagionalita.getStringCellValue());
        if(cellCosto != null){
          costo = Integer.parseInt(cellCosto.getStringCellValue());

        }
      }

      frutti.add(new Frutto(nome, stagionalita, costo));
    }

    return frutti;
  } 

  public void write(ArrayList<Frutto> frutti, String fileName) throws IOException, JAXBException, DocumentException{

    Workbook wb = new HSSFWorkbook();   //HSSFWorkbook è l'implementazione della classe astrtatta Workbook
    org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("frutti"); //nome del foglio di calcolo
    
    int nRow = 0;
    for(Frutto f: frutti){

      int nCol = 0;
      Row row = sheet.createRow(nRow);   //createRow() richiede numero della riga
      
      String nome = f.getNome();
      String stagionalita = f.getStagionalita().toString();
      String costo = f.getCosto() + "";
      
      Cell cell = row.createCell(nCol);
      cell.setCellValue(nome);

      cell = row.createCell(++nCol);

      cell.setCellValue(stagionalita);

      cell = row.createCell(++nCol);
      cell.setCellValue(costo);

      nRow++;
    }
    sheet.setColumnWidth(1, (256 * 15));//colonna 1 prende dimensione di 15 caratteri 
                                          //per qualche motivo la funzione richiede il 256esimo di un carattere come unità
    wb.write(new FileOutputStream(fileName)); 
  }
}

class FruttoOds implements IFruttoFile{

  public ArrayList<Frutto> read(String fileName) throws IOException, JAXBException{

    ArrayList<Frutto> frutti = new ArrayList<Frutto>();
    
    SpreadSheet spreadsheet = SpreadSheet.createFromFile(new File(fileName));
    org.jopendocument.dom.spreadsheet.Sheet sheet = spreadsheet.getSheet(0);

    int nRow = sheet.getRowCount();
    int nCol = sheet.getColumnCount();

    MutableCell cellNome = null;
    MutableCell cellStagionalita = null;
    MutableCell cellCosto = null;

    for(int i = 1; i < nRow; i++){
      
      String nome = ""; Stagionalita stagione = Stagionalita.DEFAULT;
      int costo = 0;

      cellNome = sheet.getCellAt(nCol - 3, i);   //prima la colonna poi la riga

      if(cellNome != null)
        nome = cellNome.getValue() + "";

      cellStagionalita = sheet.getCellAt(nCol - 2, i);

      if(cellStagionalita != null)
        stagione = Stagionalita.valueOf(cellStagionalita.getValue() + "");

      cellCosto = sheet.getCellAt(nCol - 1, i);

      if(cellCosto != null)
        costo = Integer.parseInt(cellCosto.getValue() + "");

      frutti.add(new Frutto(nome, stagione, costo));
    }

    return frutti;
  }

  public void write(ArrayList<Frutto> frutti, String fileName) throws IOException, JAXBException, DocumentException{

    DefaultTableModel model = new DefaultTableModel();  //crea un modello per come dovrà essere l'ods
    String[] cols = {"nome", "stagionalita", "costo"};

    for(String col: cols)
      model.addColumn(col);

    for(Frutto f: frutti)
      model.addRow(f.toRow());

    SpreadSheet.createEmpty(model).saveAs(new File(fileName));
  }
}

class FruttoPdf implements IFruttoFile{   //pdf formato vettoriale, uguale per ogni formato, font

   public ArrayList<Frutto> read(String filename) throws IOException, JAXBException{

     return null;
   }
  
  public void write(ArrayList<Frutto> frutti, String filename) throws IOException, JAXBException, DocumentException{

    Document document = new Document();
    PdfWriter.getInstance(document, new FileOutputStream(filename));  //singoletto->getInstance() ritorna sempre la stessa istanza di PdfWriter 
    document.open();                                                      //solo un instanza per processo, fin quando non viene eliminata
    document.addTitle("lista frutti");                                    
    document.addAuthor("Marco Garro");

    Font fontTitle = new Font(Font.FontFamily.TIMES_ROMAN, 20, Font.BOLD);
    Paragraph phTitle = new Paragraph("---LISTA FRUTTI---", fontTitle);
    phTitle.setSpacingAfter(30);   //in pixel
    document.add(phTitle);

    Font fontText = new Font(Font.FontFamily.TIMES_ROMAN, 13);

    for(Frutto f: frutti){

      document.add(new Paragraph("nome: " + f.getNome(), fontText));
      document.add(new Paragraph("stagione: " + f.getStagionalita().toString(), fontText));
      document.add(new Paragraph("costo: " + f.getCosto(), fontText));
    }

    document.close();
  }
}
