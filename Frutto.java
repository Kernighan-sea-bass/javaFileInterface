import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.JAXBException;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;

import com.itextpdf.text.DocumentException;

@XmlRootElement(name = "Frutto")

class Frutto{

  private String nome = null;
  private Stagionalita stagione = Stagionalita.DEFAULT;
  private int costo = 0;
  
  public Frutto(){}

  public Frutto(String nome, Stagionalita stagione, int costo){

    this.nome = nome;
    this.stagione = stagione;
    this.costo = costo;
   }

  public Frutto(String line){    //costruttore con linea csv

    String[] fields = line.split("[;,]"); //regex per indicare che il separatore puà essere ';' o ','
    this.nome = fields[0];
    this.stagione = Stagionalita.valueOf(fields[1]);
    this.costo = Integer.parseInt(fields[2]);   //metodo statico per parsare da String a int
  }

  public String getNome(){

    return nome;
  }

  public Stagionalita getStagionalita(){

    return stagione;
  }

  public int getCosto(){

    return costo;
  }

  @XmlElement
  public void setNome(String nome){

    this.nome = nome;
  }

  @XmlAttribute
  public void setStagionalita(Stagionalita stagione){

    this.stagione = stagione;
  }

  @XmlAttribute
  public void setCosto(int costo){

    this.costo = costo;
  }

  public String toString(){

    return "Frutto: " + "\nnome: " + nome + "\nstagione: " + stagione + "\ncosto: " + costo;
  }

  public String toLine(){ //stringa formattata per csv

    return nome + ";" + stagione + ";" + costo;
  }

  public String[] toRow(){  //dato un oggetto ritorna un array di stringhe con gli attributi

    String[] ret = new String[3];
    ret[0] = this.nome;
    ret[1] = this.stagione.toString();
    ret[2] = this.costo + "";

    return ret;
  }

  public static void main(String[] args) {
    
    try{  //contiene codice che potrebbe generare eccezioni
    //scrittura/lettura su csv
     //FruttoCsv fruttoCsv = new FruttoCsv();    //uguale a riga successiva
      
      IFruttoFile fruttoCsv = new FruttoCsv(); //tramite binding dinamico sfrutta automaticamente FruttoCsv(), aggiungo a interfaccia
                                                //tutte le classi che lavorano su diversi file
                                                //se ci fosse stato 'FruttoJson()' chiamerebbe in automatico la classe che implementa i json 
      //String fileName = args[0];  //non c'è nome programma come args[0]

      ArrayList<Frutto> frutti = fruttoCsv.read("frutto.csv");
      
      System.out.println("\nprint della collezione da csv\n");
      for(Frutto f: frutti)
        System.out.println(f);
      fruttoCsv.write(frutti, "frutto.csv"); //riscrive su file i dati della collezione (non li modifica perchè non ho voglia di farlo)
      //
      //lettura/scrittura su json
      IFruttoFile fruttoJson = new FruttoJson();
      fruttoJson.write(frutti, "frutto.json");
      frutti = null;
      frutti = new ArrayList<Frutto>();
      frutti = fruttoJson.read("frutto.json");

      System.out.println("\nprint della collezione da json\n");
      for(Frutto f : frutti)
        System.out.println(f);
      //
      //lettura/scrittura xml
      IFruttoFile fruttoXml = new FruttoXml();
      fruttoXml.write(frutti, "frutto.xml");

      System.out.println("prima di read()");
      frutti = fruttoXml.read("frutto.xml");
      System.out.println("\nprint della collezione da xml\n");
      System.out.println(frutti);
      //
      //lettura/scrittura xlsx
      IFruttoFile fruttoXls = new FruttoXls();
      fruttoXls.write(frutti, "frutto.xls");
      frutti = fruttoXls.read("frutto.xls");
      System.out.println("\nprint della collezione da xls\n");
      System.out.println(frutti);
      //
      //lettura/scrittura ods
      IFruttoFile fruttoOds = new FruttoOds();
      fruttoOds.write(frutti, "frutto.ods");
      frutti = fruttoOds.read("frutto.ods");
      System.out.println("\nprint della collezione da ods\n");
      System.out.println(frutti);
      //
      //scrittura pdf
      IFruttoFile fruttoPdf = new FruttoPdf();
      fruttoPdf.write(frutti, "frutto.pdf");
      //
    } catch(IOException | JAXBException | DocumentException ex){    //FileNotFoundException è una sottoclasse di IOException -> uso solo IOException

      ex.printStackTrace();
    }       //usare Exception (generico) -> antipattern Pokèmon Exception = gotta catch them all
            //in stdout stampa la traccia dello stack
  }
}
