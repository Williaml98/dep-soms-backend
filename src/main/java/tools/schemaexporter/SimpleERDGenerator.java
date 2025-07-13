//package tools.schemaexporter;
//
//import com.google.common.base.Function;
//import edu.uci.ics.jung.algorithms.layout.CircleLayout;
//import edu.uci.ics.jung.algorithms.layout.Layout;
//import edu.uci.ics.jung.graph.DirectedSparseGraph;
//import edu.uci.ics.jung.graph.Graph;
//import edu.uci.ics.jung.visualization.BasicVisualizationServer;
//import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
//import edu.uci.ics.jung.visualization.renderers.Renderer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.util.HashMap;
//import java.util.Map;
//import javax.imageio.ImageIO;
//import org.apache.commons.collections4.Transformer;
//
//public class SimpleERDGenerator {
//    public static void main(String[] args) {
//        try {
//            // Database connection details
//            String url = "jdbc:postgresql://localhost:5432/soms";
//            String user = "postgres";
//            String password = "1974";
//
//            // Output file for the ERD image
//            String outputFile = "C:/Users/William/Downloads/backup/erd_diagram.png";
//
//            // Connect to the database
//            Connection conn = DriverManager.getConnection(url, user, password);
//            DatabaseMetaData metadata = conn.getMetaData();
//
//            // Create a graph
//            Graph<String, String> graph = new DirectedSparseGraph<String, String>();
//
//            // Get all tables
//            ResultSet tables = metadata.getTables(null, "public", "%", new String[]{"TABLE"});
//
//            // Add tables as vertices
//            Map<String, String> tableMap = new HashMap<String, String>();
//            while (tables.next()) {
//                String tableName = tables.getString("TABLE_NAME");
//                if (!tableName.startsWith("flyway_") && !tableName.equals("databasechangelog") && !tableName.equals("databasechangeloglock")) {
//                    graph.addVertex(tableName);
//                    tableMap.put(tableName.toLowerCase(), tableName);
//                }
//            }
//
//            // Get foreign keys and add as edges
//            int edgeCount = 0;
//            for (String tableName : tableMap.values()) {
//                ResultSet foreignKeys = metadata.getImportedKeys(null, "public", tableName);
//                while (foreignKeys.next()) {
//                    String pkTable = foreignKeys.getString("PKTABLE_NAME");
//                    String fkTable = foreignKeys.getString("FKTABLE_NAME");
//                    String fkName = foreignKeys.getString("FK_NAME");
//
//                    if (tableMap.containsKey(pkTable.toLowerCase()) && tableMap.containsKey(fkTable.toLowerCase())) {
//                        String edgeName = "FK_" + (++edgeCount);
//                        if (fkName != null && !fkName.isEmpty()) {
//                            edgeName = fkName;
//                        }
//                        graph.addEdge(edgeName, fkTable, pkTable);
//                    }
//                }
//                foreignKeys.close();
//            }
//
//            // Create a layout for the graph
//            Layout<String, String> layout = new CircleLayout<String, String>(graph);
//            layout.setSize(new Dimension(1200, 900));
//
//            // Create a visualization server for the graph
//            BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<String, String>(layout);
//            vv.setPreferredSize(new Dimension(1200, 900));
//
//            // Setup vertex labels
//            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
//            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
//
//            // Setup edge labels
//            vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());
//
//            // Setup colors
//            Transformer<String, Paint> vertexPaint = new Transformer<String, Paint>() {
//                @Override
//                public Paint transform(String s) {
//                    return new Color(173, 216, 230); // Light blue for tables
//                }
//            };
//
//            Transformer<String, Paint> edgePaint = new Transformer<String, Paint>() {
//                @Override
//                public Paint transform(String s) {
//                    return new Color(0, 100, 0); // Dark green for relationships
//                }
//            };
//
//            vv.getRenderContext().setVertexFillPaintTransformer((Function<? super String, Paint>) vertexPaint);
//            vv.getRenderContext().setEdgeDrawPaintTransformer((Function<? super String, Paint>) edgePaint);
//
//            // Create a frame to display the graph
//            JFrame frame = new JFrame("Database ERD");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.getContentPane().add(vv);
//            frame.pack();
//
//            // Create a buffered image to save the graph
//            BufferedImage image = new BufferedImage(vv.getWidth(), vv.getHeight(), BufferedImage.TYPE_INT_RGB);
//            Graphics2D g2d = image.createGraphics();
//            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//            g2d.setColor(Color.WHITE);
//            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
//            vv.paint(g2d);
//            g2d.dispose();
//
//            // Save the image
//            File outputImageFile = new File(outputFile);
//            ImageIO.write(image, "png", outputImageFile);
//
//            System.out.println("ERD diagram generated successfully at: " + outputFile);
//
//            // Display the graph (optional)
//            frame.setVisible(true);
//
//            // Close the connection
//            conn.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

package tools.schemaexporter;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class SimpleERDGenerator {
    public static void main(String[] args) {
        try {
            // Database connection details
            String url = "jdbc:postgresql://localhost:5432/soms";
            String user = "postgres";
//            String password = "1974";
            String password = "K100921Z";

            // Output file for the ERD image
            String outputFile = "C:/Users/William/Downloads/backup/erd_diagram.png";

            // Connect to the database
            Connection conn = DriverManager.getConnection(url, user, password);
            DatabaseMetaData metadata = conn.getMetaData();

            // Create a graph
            Graph<String, String> graph = new DirectedSparseGraph<String, String>();

            // Get all tables
            ResultSet tables = metadata.getTables(null, "public", "%", new String[]{"TABLE"});

            // Add tables as vertices
            Map<String, String> tableMap = new HashMap<String, String>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (!tableName.startsWith("flyway_") && !tableName.equals("databasechangelog") && !tableName.equals("databasechangeloglock")) {
                    graph.addVertex(tableName);
                    tableMap.put(tableName.toLowerCase(), tableName);
                }
            }

            // Get foreign keys and add as edges
            int edgeCount = 0;
            for (String tableName : tableMap.values()) {
                ResultSet foreignKeys = metadata.getImportedKeys(null, "public", tableName);
                while (foreignKeys.next()) {
                    String pkTable = foreignKeys.getString("PKTABLE_NAME");
                    String fkTable = foreignKeys.getString("FKTABLE_NAME");
                    String fkName = foreignKeys.getString("FK_NAME");

                    if (tableMap.containsKey(pkTable.toLowerCase()) && tableMap.containsKey(fkTable.toLowerCase())) {
                        String edgeName = "FK_" + (++edgeCount);
                        if (fkName != null && !fkName.isEmpty()) {
                            edgeName = fkName;
                        }
                        graph.addEdge(edgeName, fkTable, pkTable);
                    }
                }
                foreignKeys.close();
            }

            // Create a layout for the graph
            Layout<String, String> layout = new CircleLayout<String, String>(graph);
            layout.setSize(new Dimension(1200, 900));

            // Create a visualization server for the graph
            BasicVisualizationServer<String, String> vv = new BasicVisualizationServer<String, String>(layout);
            vv.setPreferredSize(new Dimension(1200, 900));

            // Setup vertex labels
            vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller());
            vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);

            // Setup edge labels
            vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller());

            // Setup colors using Guava's Function interface
            Function<String, Paint> vertexPaint = new Function<String, Paint>() {
                @Override
                public Paint apply(String s) {
                    return new Color(173, 216, 230); // Light blue for tables
                }
            };

            Function<String, Paint> edgePaint = new Function<String, Paint>() {
                @Override
                public Paint apply(String s) {
                    return new Color(0, 100, 0); // Dark green for relationships
                }
            };

            vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
            vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);

            // Create a frame to display the graph
            JFrame frame = new JFrame("Database ERD");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(vv);
            frame.pack();

            // Create a buffered image to save the graph
            BufferedImage image = new BufferedImage(vv.getWidth(), vv.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
            vv.paint(g2d);
            g2d.dispose();

            // Save the image
            File outputImageFile = new File(outputFile);
            ImageIO.write(image, "png", outputImageFile);

            System.out.println("ERD diagram generated successfully at: " + outputFile);

            // Display the graph (optional)
            frame.setVisible(true);

            // Close the connection
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
