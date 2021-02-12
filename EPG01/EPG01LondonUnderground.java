// Teoria dos Grafos - UFCG
// JGraphT EX05


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import util.PrintUtil;

public class EPG01LondonUnderground {
	
	Graph<DefaultVertex, RelationshipWeightedEdge> graph;
	GraphPath <DefaultVertex, RelationshipWeightedEdge> emptyPath;
  GraphPath <DefaultVertex, RelationshipWeightedEdge> path;
  Set<DefaultVertex> vertices;
	
	// Constructor
	public EPG01LondonUnderground() {
		graph = new WeightedMultigraph <> (VertexEdgeUtil.createDefaultVertexSupplier(), 
	            VertexEdgeUtil.createRelationshipWeightedEdgeSupplier());
		ImportUtil.importGraphMultipleCSV(graph, 
				"graphs/london.stations.csv","id","name",
				"graphs/london.connections.csv","station1", "station2", "time", false, true);
    emptyPath = new GraphWalk <> (graph,new ArrayList <DefaultVertex> (), 0.0);

    vertices = graph.vertexSet();
    // Complemente este construtor se necessário
	}
 
	// get methods
	public Graph<DefaultVertex, RelationshipWeightedEdge> getGraph() {
		return graph;
	}

	// Estações Centrais
	public List <DefaultVertex> centralKStations(int k) {
    ArrayList<DefaultVertex> centralStations = new ArrayList(graph.vertexSet());
    ArrayList<DefaultVertex> stations = new ArrayList();

      if (k > 0) {
			Collections.sort(centralStations, new Comparator<DefaultVertex>()
			{
				public int compare( DefaultVertex v1, DefaultVertex v2 ) {
					return (Integer.valueOf(graph.degreeOf(v2))).compareTo(Integer.valueOf(graph.degreeOf(v1)));
				}

			} );
          for (int i = 0; i < k; i++){
            stations.add(centralStations.get(i));
          }
          return stations;

        }
        else{
            return new ArrayList <>();
        } 
  }
	
	// Menor Trajeto de Trem entre duas Estações
	public GraphPath <DefaultVertex, RelationshipWeightedEdge> shortestPath(String source, String sink) {

    DefaultVertex v1 = VertexEdgeUtil.getVertexfromLabel(vertices, source);
    DefaultVertex v2 = VertexEdgeUtil.getVertexfromLabel(vertices, sink);

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
	
	// Menor Trajeto entre duas Estações sem usar Trens de uma Linha
	public GraphPath <DefaultVertex, RelationshipWeightedEdge> shortestPathDropLine 
			(String line, String source, String sink, int maxsteps) {
    YenKShortestPath <DefaultVertex, RelationshipWeightedEdge> yenk = new YenKShortestPath <>(graph);

    DefaultVertex v1 = VertexEdgeUtil.getVertexfromLabel(vertices, source);
    DefaultVertex v2 = VertexEdgeUtil.getVertexfromLabel(vertices, sink);

    YenShortestPathIterator <DefaultVertex, RelationshipWeightedEdge> yenI = new YenShortestPathIterator <> (graph,v1,v2);

    int count = 0;
    while ((yenI.hasNext()) && (count < maxsteps)) {

    GraphPath <DefaultVertex, RelationshipWeightedEdge> yenIpath = yenI.next();
      for (int a = 0; a < yenIpath.getEdgeList().size(); a++) {
        if (line.equals(yenIpath.getEdgeList().get(a).getAtt("line").toString())) {
          break;
        }
        else if (a == yenIpath.getEdgeList().size()-1 && !line.equals(yenIpath.getEdgeList().get(a).getAtt("line").toString())) {
          return yenIpath;
        }      
      }

      count++;

    }
		
    return emptyPath;
      
	}
}

