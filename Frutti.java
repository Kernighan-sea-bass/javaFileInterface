import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;

import java.util.ArrayList;

@XmlRootElement(name = "Frutti")
class Frutti{     //wrapper di frutto per fromattare bene .xml -> altrimenti scrive sempre header

  private ArrayList<Frutto> fruttaLista = null;

  public Frutti(){}

  public Frutti(ArrayList<Frutto> fruttaLista){
    this.fruttaLista = fruttaLista;
  }

  public ArrayList<Frutto> getFrutti(){
    return this.fruttaLista;
  }

  @XmlElement(name = "Frutto")
  public void setFrutti(ArrayList<Frutto> fruttaLista){

    this.fruttaLista = fruttaLista;
  }
}
