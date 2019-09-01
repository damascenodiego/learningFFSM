package uk.le.ac;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ovgu.featureide.fm.core.base.IFeatureModel;
import de.ovgu.featureide.fm.core.io.manager.FeatureModelManager;
import uk.le.ac.fts.Fts;
import uk.le.ac.fts.FtsTransition;

public class FsmFromFTS {
	
	public static void main(String[] args) {
		try {
			String spl_name = "minepump";
			File f_fm = new File("Benchmark_SPL/"+spl_name+"/model.xml");
			File f_fts = new File("./Benchmark_SPL/"+spl_name+ "/fts/"+spl_name+".fts");
			
			IFeatureModel fm = FeatureModelManager.load(f_fm.toPath()).getObject();
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(f_fts);
            doc.getDocumentElement().normalize();

            Node nodeStart = doc.getElementsByTagName("start").item(0).getChildNodes().item(0);
            Fts fts = new Fts(nodeStart.getTextContent());
            
            NodeList nodeList = doc.getElementsByTagName("state");
            for (int i = 0; i < nodeList.getLength(); i++) {
            	Node node = nodeList.item(i);
            	if (node.getNodeName().equals("state") && node.hasAttributes()) {
            		Node originNd = node.getAttributes().getNamedItem("id");
            		if (node.hasChildNodes()) {
            			for (int j = 0; j < node.getChildNodes().getLength(); j++) {
            				Node childNode = node.getChildNodes().item(j);
            				
            				if (childNode.getNodeName().equals("transition")) {
            					Node targetNd= childNode.getAttributes().getNamedItem("target");
            					Node fexpreNd = childNode.getAttributes().getNamedItem("fexpression");
            					Node actionNd = childNode.getAttributes().getNamedItem("action");
            					
            					FtsTransition tr = new FtsTransition(originNd, fexpreNd, actionNd, targetNd);
            					fts.getTransitions().add(tr);
            				}
            			}
            		}
            	}
            }
            System.out.println(fts);

		}catch (Exception e) {
			e.printStackTrace();
		}

		
	}

}
