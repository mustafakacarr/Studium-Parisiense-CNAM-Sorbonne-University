package com.le_cnam.studiumParisiense;

import com.le_cnam.studiumParisiense.crud.DynamicCRUD;
import com.le_cnam.studiumParisiense.parser.XmlParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

//import static com.le_cnam.studiumParisiense.parser.XmlParser.parseXmlData;

@SpringBootApplication

public class StudiumParisienseApplication {
    private static DynamicCRUD saver;
    private static XmlParser parser;

    public StudiumParisienseApplication(DynamicCRUD saver, XmlParser parser) {
        this.saver = saver;
        this.parser = parser;
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(StudiumParisienseApplication.class, args);
    String xmlData = "<1a>\t24164\n" +
            "<1b>     MICHAEL Du Quesnay\n" +
            "<1c>     $Michael DU=QUESNAY$\n" +
            "<r>      CUP: IV, 123 (n° 1799).\n" +
            "<1d>     Bachelier ès arts\n" +
            "<1f>     %1403-1403%\n" +
            "<1g>      %1403%\n" +
            "<1k>     Gradué\n" +
            "<2a>     *Normandie ?\n" +
            "<2b>     Diocèse de £Coutances ;\n" +
            "<r>      CUP: IV, 123 (n° 1799).\n" +
            "<5b>     *Paris (Nation de £Normandie) %1403% ;\n" +
            "<5c>     Bachelier ès arts (*Paris) %1403% ;\n" +
            "<r>      CUP: IV, 123 (n° 1799).\n" +
            "<6a>     Clerc du diocèse de £Coutances ;\n" +
            "<r>      CUP: IV, 123 (n° 1799).\n" +
            "<6b>     Candidat à un bénéfice sur le &Magnus Rotulus& (&rotulus& des bacheliers de la Nation de £Normandie) de l’Université de £Paris daté de %1403% (21 octobre) et présenté à $BENOÎT XIII$.\n" +
            "<r>      CUP: IV, 123 (n° 1799).\n" +
            "<19>   Pas d’œuvre connue.\n" +
            "C      BIBLIOGRAPHIE \n" +
            "<99a>   CUP: IV, 123 (n° 1799).\n" +
            "<99c>\tSTUDIUM : http://lamop-vs3.univ-paris1.fr/studium/ : Rédaction, Jean-Philippe Genet, 28/09/2023.\n";


        parser.parseXmlData(xmlData);
/*
        JsonProcessor jsonProcessor = new JsonProcessor();
        String path = "/Users/mustafa/Desktop/CNAM WorkSpace/studiumParisiense/src/main/java/com/le_cnam/studiumParisiense/prosopography.json";
        int a = 0;
        for (String s : jsonProcessor.processJsonFile(path)) {
            if (a == 10)
                break;
            parser.parseXmlData(s);
            System.out.println("--");
            a++;
        }

*/
    }


}
