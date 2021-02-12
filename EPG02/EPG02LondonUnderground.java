// Teoria dos Grafos - UFCG

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.jgrapht.GraphTests;
import org.jgrapht.Graphs;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.YenKShortestPath;
import org.jgrapht.alg.shortestpath.YenShortestPathIterator;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.GraphWalk;
import org.jgrapht.graph.WeightedMultigraph;

import util.DefaultVertex;
import util.ImportUtil;
import util.RelationshipWeightedEdge;
import util.VertexEdgeUtil;

public class EPG02LondonUnderground {
	
	//private static final String NL = System.getProperty("line.separator");
	private static final String sep = System.getProperty("file.separator");
	// path do folder onde os grafos a serem carregados estão armazenados
	private static final String graphpathname = "graphs" + sep;
	private static final String datapathname = "datasets" + sep;
	
	Graph<DefaultVertex, RelationshipWeightedEdge> graph;
  Graph<DefaultVertex, RelationshipWeightedEdge> graphTest;
	Set <DefaultVertex> V;
  //Set <DefaultVertex> V2;
	GraphPath <DefaultVertex, RelationshipWeightedEdge> emptyPath;	
	HashMap <String, DefaultVertex> attractions; 
	Set <RelationshipWeightedEdge> E;

	///////////////////////////////////////
	// Constructor
	public EPG02LondonUnderground () {
		graph = new WeightedMultigraph <> (VertexEdgeUtil.createDefaultVertexSupplier(), 
	            VertexEdgeUtil.createRelationshipWeightedEdgeSupplier());
		// Data from http://markdunne.github.io/2016/04/10/The-London-Tube-as-a-Graph/
		ImportUtil.importGraphMultipleCSV(graph, 
				graphpathname + "london.stations.csv","id","name",
				graphpathname + "london.connections.csv","station1", "station2", "time", false, true);
    
		V = graph.vertexSet();
    
		E = graph.edgeSet();
		emptyPath = new GraphWalk <> (graph,new ArrayList <DefaultVertex> (), 0.0);	
		readAttractions();
	}
	
	public void readAttractions () {
        String csvFile = datapathname + "london-attractions.csv";
        String line = "";
        String cvsSplitBy = ",";  
        attractions = new HashMap <> ();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] aline = line.split(cvsSplitBy);
                attractions.put(aline[0], VertexEdgeUtil.getVertexfromLabel(V, aline[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
		Iterator <String> it = attractions.keySet().iterator();
        while (it.hasNext()) {
        	String n = it.next();
        	if (attractions.get(n) == null) {
        		System.out.println(n + "," + attractions.get(n));
        	}
        }
        
	}
	
	///////////////////////////////////////
	// get methods
	public Graph<DefaultVertex, RelationshipWeightedEdge> getGraph () {
		return graph;
	}
	
	public DefaultVertex getStation (String attraction) {
		return attractions.get(attraction);
	}
	
	public Set<String> getLines (GraphPath <DefaultVertex, RelationshipWeightedEdge> path) {
		Set<String> lines = new HashSet <> ();
		Iterator <RelationshipWeightedEdge> it = path.getEdgeList().iterator();
		while (it.hasNext()) {
			RelationshipWeightedEdge e = it.next();
			lines.add(e.getAtt("line").toString());
		}
		return lines;
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// Métodos do EPG01 utilizados nos testes desta classe. Acrescente seu código
	
	// Menor Trajeto de Trem entre duas Estações
	public GraphPath <DefaultVertex, RelationshipWeightedEdge> shortestPath (String source, String sink) {
		DefaultVertex v1 = VertexEdgeUtil.getVertexfromLabel(V, source);
    DefaultVertex v2 = VertexEdgeUtil.getVertexfromLabel(V, sink);

    if (!graph.vertexSet().contains(v1) || !graph.vertexSet().contains(v2)) {

      return emptyPath;

    }
    else {
      YenKShortestPath <DefaultVertex, RelationshipWeightedEdge> yenk = new YenKShortestPath <>(graph);

      return yenk.getPaths(v1,v2,1).get(0);
    }
		
	}
	
	// Troca de Linhas em um Trajeto
	public List <Pair<String,RelationshipWeightedEdge>> changeofLines 
						(GraphPath <DefaultVertex, RelationshipWeightedEdge> path) {
		List<RelationshipWeightedEdge> edges = path.getEdgeList();

    List<Pair<String, RelationshipWeightedEdge>> list = new ArrayList<>();

    String linha = "";
    
		for (int i = 0; i < edges.size(); i++) {
		  if (!linha.equals(edges.get(i).getAtt("line").toString())) {
		    Pair<String, RelationshipWeightedEdge> a = Pair.of(edges.get(i).getAtt("line").toString(), edges.get(i));
        linha = edges.get(i).getAtt("line").toString();
		    list.add(a);
		    }
    }
		return list;
	}
	
	/////////////////////////////////////////////////
	// Métodos a serem implementados no EPG02
	// Tempo Total Estimado de um Trajeto
	public double estimatedTime (GraphPath <DefaultVertex, RelationshipWeightedEdge> p, double t) {
    return p.getWeight() + t*changeofLines(p).size();
  }

    /*Iterator<RelationshipWeightedEdge> it = p.getEdgeList().iterator();

		double timeWithoutChangeOfLines = 0;
		while (it.hasNext()) {
			RelationshipWeightedEdge e = it.next();
			timeWithoutChangeOfLines += graph.getEdgeWeight(e);
		}

		return timeWithoutChangeOfLines + t*changeofLines(p).size();
	}*/
	
	// Menor Trajeto considerando Tempo Total Estimado
	public GraphPath <DefaultVertex, RelationshipWeightedEdge> shortestEstimatedPath 
				(String source, String sink, double t, int maxAttempts) {
		DefaultVertex v1 = VertexEdgeUtil.getVertexfromLabel(V, source);
    DefaultVertex v2 = VertexEdgeUtil.getVertexfromLabel(V, sink);
    
    if (!V.contains(v1) || !V.contains(v2)) {
      return emptyPath;
    }  
    YenKShortestPath <DefaultVertex, RelationshipWeightedEdge> yenk = new YenKShortestPath <>(graph);
    
    List<GraphPath<DefaultVertex,RelationshipWeightedEdge>> shortestPaths = yenk.getPaths(v1,v2,maxAttempts);

    //int lenPaths = yenk.getPaths(v1,v2,maxAttempts).size();

    int lenPaths = shortestPaths.size();

    if (lenPaths == 0) {
      return emptyPath;
    }

    double shortestTime = estimatedTime(shortestPaths.get(0),t);
    
    int shortestTimePosition = 0;
    for (int i = 1; i < lenPaths; i++) {
      if (estimatedTime(shortestPaths.get(i),t) < shortestTime) {
        shortestTime = estimatedTime(shortestPaths.get(i),t);
        shortestTimePosition = i;
      }
    }

    return shortestPaths.get(shortestTimePosition);       
	}

  
	// Menor rota de uma Estação para Atrações Turísticas
	public GraphPath <DefaultVertex, RelationshipWeightedEdge> bestRoute (String originStation, List<String> atts) {
	    
    for (String a: atts) {
      if (!attractions.containsKey(a)) {
        return emptyPath;
      }
    }

		GraphWalk<DefaultVertex, RelationshipWeightedEdge> walk = (GraphWalk<DefaultVertex, RelationshipWeightedEdge>) emptyPath;

	  List <RelationshipWeightedEdge> edgesList = new ArrayList<>();
	    
	  double totalPathWeight = 0;
	    
	  String currentStationLabel = originStation;
	    
	  DefaultVertex originStationVertex = VertexEdgeUtil.getVertexfromLabel(V, originStation);

	  if (originStation != null) {
	    for (int i = 0; i < atts.size(); i++) {
	    	String nextStationLabel = getStation(atts.get(i)).getLabel();
	    	
	      GraphPath<DefaultVertex, RelationshipWeightedEdge> currentPath = shortestPath(currentStationLabel, nextStationLabel);
	    	edgesList.addAll(currentPath.getEdgeList());
	    	totalPathWeight += currentPath.getWeight();
	    	currentStationLabel = nextStationLabel;
	    }
      
	    GraphPath<DefaultVertex, RelationshipWeightedEdge> finalPath = shortestPath(currentStationLabel, originStation);
	    edgesList.addAll(finalPath.getEdgeList());
	    totalPathWeight += finalPath.getWeight();
	    walk = new GraphWalk<DefaultVertex, RelationshipWeightedEdge>(graph, originStationVertex, originStationVertex, edgesList, totalPathWeight);
	    }
	    
	    return walk;
	}
  
	
	// Trechos em Destaque
	public List <RelationshipWeightedEdge> findSections (List <String> stations) {

		Set<DefaultVertex> X = new HashSet<>();
		
	  for (String a : stations) {
			X.add(VertexEdgeUtil.getVertexfromLabel(V, a));
		}

		Set<DefaultVertex> Y = new HashSet<>(V);
			Y.removeAll(X);

		List<RelationshipWeightedEdge> edgeCut = new ArrayList<RelationshipWeightedEdge>();
		for (RelationshipWeightedEdge e: E) {
			if ((X.contains(e.getV1()) && Y.contains(e.getV2())) ||  (X.contains(e.getV2()) && Y.contains(e.getV1()))) {
				edgeCut.add(e);
			}
		}

		return edgeCut;
	}
	
	public boolean serviceDisruption (List <String> stations) {

    graphTest = new WeightedMultigraph<>(VertexEdgeUtil.createDefaultVertexSupplier(),
				VertexEdgeUtil.createRelationshipWeightedEdgeSupplier());
	
		//ImportUtil.importGraphMultipleCSV(graphTest, graphpathname + "london.stations.csv", "id", "name",
				//graphpathname + "london.connections.csv", "station1", "station2", "time", false, true);
    
    //V2 = graphTest.vertexSet();

    Graphs.addAllVertices(graphTest, V);
		Graphs.addAllEdges(graphTest, graph, E);

    for (String a: stations) {
			graphTest.removeVertex(VertexEdgeUtil.getVertexfromLabel(V, a));
		}
		
		return !GraphTests.isConnected(graphTest);
	}
}
