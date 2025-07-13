package tools.schemaexporter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CompleteERDGenerator {
    public static void main(String[] args) {
        try {
            // Output directory for the ERD and documentation
//            String outputDir = "C:/Users/William/Downloads/backup/erd";
            String outputDir = "C:\\Users\\kzibi\\william-personal-projects";
            // Database connection details
            String dbHost = "localhost";
            String dbPort = "5432";
            String dbName = "soms";
            String dbUser = "postgres";
//            String dbPassword = "1974";
            String dbPassword = "K100921Z";

            // Create output directory if it doesn't exist
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }

            // First, check if there are any tables in the database
            boolean hasTables = checkForTables(dbHost, dbPort, dbName, dbUser, dbPassword);

            if (!hasTables) {
                System.out.println("\n==============================================");
                System.out.println("ERROR: No tables found in the database schema.");
                System.out.println("Please make sure your database has tables and the user has permissions to access them.");
                System.out.println("==============================================\n");
                return;
            }

            // Install Graphviz if not already installed
            boolean graphvizInstalled = installGraphvizIfNeeded();

            if (graphvizInstalled) {
                // Try to generate ERD with SchemaSpy
                boolean schemaspySuccess = generateSchemaSpyERD(dbHost, dbPort, dbName, dbUser, dbPassword, outputDir);

                if (!schemaspySuccess) {
                    System.out.println("SchemaSpy ERD generation failed. Falling back to text schema...");
                }
            } else {
                System.out.println("Graphviz installation failed. Falling back to text schema...");
            }

            // Always generate the text schema as a backup
            generateTextSchema(dbHost, dbPort, dbName, dbUser, dbPassword, outputDir);

            // Generate a simple HTML visualization
            generateSimpleHtmlERD(dbHost, dbPort, dbName, dbUser, dbPassword, outputDir);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean installGraphvizIfNeeded() {
        try {
            // Check if Graphviz is already installed
            ProcessBuilder checkProcess = new ProcessBuilder("dot", "-V");
            checkProcess.redirectErrorStream(true);
            Process process = checkProcess.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Graphviz is already installed.");
                return true;
            }

            System.out.println("Graphviz not found. Attempting to install...");

            // Install Graphviz based on the operating system
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                // Download the Graphviz installer
                String graphvizUrl = "https://gitlab.com/api/v4/projects/4207231/packages/generic/graphviz-releases/8.0.5/windows_10_cmake_Release_graphviz-install-8.0.5-win64.exe";
                String installerPath = "graphviz-installer.exe";

                downloadFile(graphvizUrl, installerPath);

                // Run the installer silently
                ProcessBuilder installProcess = new ProcessBuilder(
                        installerPath,
                        "/S", // Silent install
                        "/D=C:\\Program Files\\Graphviz"
                );
                installProcess.redirectErrorStream(true);
                Process installProc = installProcess.start();

                // Print the output
                java.io.InputStream is = installProc.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int installExitCode = installProc.waitFor();

                if (installExitCode == 0) {
                    System.out.println("Graphviz installed successfully.");

                    // Add Graphviz to PATH for this session
                    String path = System.getenv("PATH") + ";C:\\Program Files\\Graphviz\\bin";
                    System.setProperty("java.library.path", System.getProperty("java.library.path") + ";C:\\Program Files\\Graphviz\\bin");

                    return true;
                } else {
                    System.out.println("Graphviz installation failed with exit code: " + installExitCode);
                    return false;
                }
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                // For macOS, use Homebrew
                ProcessBuilder brewProcess = new ProcessBuilder("brew", "install", "graphviz");
                brewProcess.redirectErrorStream(true);
                Process brewProc = brewProcess.start();

                // Print the output
                java.io.InputStream is = brewProc.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int brewExitCode = brewProc.waitFor();
                return brewExitCode == 0;
            } else {
                // For Linux, use apt-get
                ProcessBuilder aptProcess = new ProcessBuilder("sudo", "apt-get", "install", "-y", "graphviz");
                aptProcess.redirectErrorStream(true);
                Process aptProc = aptProcess.start();

                // Print the output
                java.io.InputStream is = aptProc.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int aptExitCode = aptProc.waitFor();
                return aptExitCode == 0;
            }
        } catch (Exception e) {
            System.out.println("Error installing Graphviz: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static boolean generateSchemaSpyERD(String host, String port, String dbName, String user, String password, String outputDir) {
        try {
            // Download required JAR files if they don't exist
            String schemaspyJar = downloadFileIfNotExists(
                    "https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar",
                    "schemaspy-6.1.0.jar"
            );

            String postgresJar = downloadFileIfNotExists(
                    "https://jdbc.postgresql.org/download/postgresql-42.6.0.jar",
                    "postgresql-42.6.0.jar"
            );

            // Build the command to run SchemaSpy
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(schemaspyJar);
            command.add("-t");
            command.add("pgsql"); // PostgreSQL
            command.add("-db");
            command.add(dbName);
            command.add("-host");
            command.add(host);
            command.add("-port");
            command.add(port);
            command.add("-u");
            command.add(user);
            command.add("-p");
            command.add(password);
            command.add("-o");
            command.add(outputDir);
            command.add("-dp");
            command.add(postgresJar);
            command.add("-s");
            command.add("public"); // Schema name
            command.add("-hq"); // High quality diagrams
            command.add("-norows"); // Don't include row counts

            // Execute the command
            System.out.println("Executing command: " + String.join(" ", command));
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Print the output
            java.io.InputStream is = process.getInputStream();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("\n==============================================");
                System.out.println("ERD diagram generated successfully at: " + outputDir);
                System.out.println("Open " + outputDir + "/index.html in a browser to view the documentation");
                System.out.println("The main diagram is at: " + outputDir + "/diagrams/summary/relationships.real.large.png");
                System.out.println("==============================================\n");

                // Open the main diagram automatically
                File mainDiagram = new File(outputDir + "/diagrams/summary/relationships.real.large.png");
                if (mainDiagram.exists()) {
                    openFile(mainDiagram);
                }

                // Open the HTML documentation automatically
                File indexHtml = new File(outputDir + "/index.html");
                if (indexHtml.exists()) {
                    openFile(indexHtml);
                }

                return true;
            } else {
                System.out.println("ERD generation failed with exit code: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error generating SchemaSpy ERD: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static void generateSimpleHtmlERD(String host, String port, String dbName, String user, String password, String outputDir) {
        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            Connection conn = DriverManager.getConnection(url, user, password);

            // Get all tables and their relationships
            Statement stmt = conn.createStatement();
            ResultSet tables = stmt.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
                            "ORDER BY table_name"
            );

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("  <title>Database ERD: ").append(dbName).append("</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append("    .table { border: 1px solid #ccc; margin: 10px; padding: 10px; border-radius: 5px; display: inline-block; vertical-align: top; background-color: #f9f9f9; }\n");
            html.append("    .table-name { font-weight: bold; background-color: #e0e0e0; padding: 5px; margin-bottom: 10px; border-radius: 3px; }\n");
            html.append("    .column { margin: 2px 0; }\n");
            html.append("    .pk { color: #d9534f; }\n");
            html.append("    .fk { color: #5bc0de; }\n");
            html.append("    .relationship { margin: 5px 0; }\n");
            html.append("    #diagram { margin-top: 30px; }\n");
            html.append("    .container { display: flex; flex-wrap: wrap; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            html.append("  <h1>Database ERD: ").append(dbName).append("</h1>\n");

            // Add a container for tables
            html.append("  <div class=\"container\">\n");

            // List of all tables for the relationships section
            List<String> tableNames = new ArrayList<>();

            // Process each table
            while (tables.next()) {
                String tableName = tables.getString("table_name");
                tableNames.add(tableName);

                html.append("    <div class=\"table\" id=\"").append(tableName).append("\">\n");
                html.append("      <div class=\"table-name\">").append(tableName).append("</div>\n");

                // Get columns for this table
                Statement colStmt = conn.createStatement();
                ResultSet columns = colStmt.executeQuery(
                        "SELECT column_name, data_type, is_nullable " +
                                "FROM information_schema.columns " +
                                "WHERE table_schema = 'public' AND table_name = '" + tableName + "' " +
                                "ORDER BY ordinal_position"
                );

                // Get primary keys
                Statement pkStmt = conn.createStatement();
                ResultSet pks = pkStmt.executeQuery(
                        "SELECT c.column_name " +
                                "FROM information_schema.table_constraints tc " +
                                "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) " +
                                "JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema " +
                                "AND tc.table_name = c.table_name AND ccu.column_name = c.column_name " +
                                "WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_name = '" + tableName + "'"
                );

                List<String> pkColumns = new ArrayList<>();
                while (pks.next()) {
                    pkColumns.add(pks.getString("column_name"));
                }

                // Get foreign keys
                Statement fkStmt = conn.createStatement();
                ResultSet fks = fkStmt.executeQuery(
                        "SELECT kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name " +
                                "FROM information_schema.table_constraints AS tc " +
                                "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                                "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = '" + tableName + "'"
                );

                List<String> fkColumns = new ArrayList<>();
                while (fks.next()) {
                    fkColumns.add(fks.getString("column_name"));
                }

                // Display columns
                while (columns.next()) {
                    String colName = columns.getString("column_name");
                    String dataType = columns.getString("data_type");
                    String nullable = columns.getString("is_nullable").equals("YES") ? "NULL" : "NOT NULL";

                    html.append("      <div class=\"column");

                    if (pkColumns.contains(colName)) {
                        html.append(" pk");
                    } else if (fkColumns.contains(colName)) {
                        html.append(" fk");
                    }

                    html.append("\">");

                    if (pkColumns.contains(colName)) {
                        html.append("🔑 ");
                    } else if (fkColumns.contains(colName)) {
                        html.append("🔗 ");
                    }

                    html.append(colName).append(" (").append(dataType).append(", ").append(nullable).append(")");
                    html.append("</div>\n");
                }

                html.append("    </div>\n");
            }

            html.append("  </div>\n"); // Close container

            // Add relationships section
            html.append("  <h2>Relationships</h2>\n");
            html.append("  <div id=\"relationships\">\n");

            for (String tableName : tableNames) {
                Statement fkStmt = conn.createStatement();
                ResultSet fks = fkStmt.executeQuery(
                        "SELECT kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name " +
                                "FROM information_schema.table_constraints AS tc " +
                                "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                                "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = '" + tableName + "'"
                );

                while (fks.next()) {
                    String columnName = fks.getString("column_name");
                    String foreignTable = fks.getString("foreign_table_name");
                    String foreignColumn = fks.getString("foreign_column_name");

                    html.append("    <div class=\"relationship\">");
                    html.append(tableName).append(".").append(columnName);
                    html.append(" → ");
                    html.append(foreignTable).append(".").append(foreignColumn);
                    html.append("</div>\n");
                }
            }

            html.append("  </div>\n");

            // Add a simple JavaScript-based diagram
            html.append("  <h2>Visual Diagram</h2>\n");
            html.append("  <div id=\"diagram\">\n");
            html.append("    <canvas id=\"erdCanvas\" width=\"1200\" height=\"800\" style=\"border:1px solid #d3d3d3;\"></canvas>\n");
            html.append("  </div>\n");

            // Add JavaScript to draw the diagram
            html.append("  <script>\n");
            html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
            html.append("      const canvas = document.getElementById('erdCanvas');\n");
            html.append("      const ctx = canvas.getContext('2d');\n");
            html.append("      const tables = {};\n");
            html.append("      const relationships = [];\n");

            // Define table positions
            html.append("      // Define tables\n");
            int x = 50;
            int y = 50;
            int maxHeight = 0;

            for (String tableName : tableNames) {
                html.append("      tables['").append(tableName).append("'] = {x: ").append(x).append(", y: ").append(y).append(", width: 200, height: 150};\n");

                x += 250;
                if (x > 1000) {
                    x = 50;
                    y += maxHeight + 50;
                    maxHeight = 0;
                }
                maxHeight = Math.max(maxHeight, 150);
            }

            // Define relationships
            html.append("      // Define relationships\n");
            for (String tableName : tableNames) {
                Statement fkStmt = conn.createStatement();
                ResultSet fks = fkStmt.executeQuery(
                        "SELECT kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name " +
                                "FROM information_schema.table_constraints AS tc " +
                                "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                                "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = '" + tableName + "'"
                );

                while (fks.next()) {
                    String foreignTable = fks.getString("foreign_table_name");
                    html.append("      relationships.push({from: '").append(tableName).append("', to: '").append(foreignTable).append("'});\n");
                }
            }

            // Draw function
            html.append("      // Draw tables\n");
            html.append("      function drawTables() {\n");
            html.append("        ctx.font = '12px Arial';\n");
            html.append("        for (const tableName in tables) {\n");
            html.append("          const table = tables[tableName];\n");
            html.append("          // Draw table box\n");
            html.append("          ctx.fillStyle = '#f9f9f9';\n");
            html.append("          ctx.strokeStyle = '#333';\n");
            html.append("          ctx.lineWidth = 2;\n");
            html.append("          ctx.fillRect(table.x, table.y, table.width, table.height);\n");
            html.append("          ctx.strokeRect(table.x, table.y, table.width, table.height);\n");
            html.append("          \n");
            html.append("          // Draw table header\n");
            html.append("          ctx.fillStyle = '#e0e0e0';\n");
            html.append("          ctx.fillRect(table.x, table.y, table.width, 30);\n");
            html.append("          ctx.strokeRect(table.x, table.y, table.width, 30);\n");
            html.append("          \n");
            html.append("          // Draw table name\n");
            html.append("          ctx.fillStyle = '#000';\n");
            html.append("          ctx.textAlign = 'center';\n");
            html.append("          ctx.fillText(tableName, table.x + table.width/2, table.y + 20);\n");
            html.append("        }\n");
            html.append("      }\n");

            // Draw relationships
            html.append("      // Draw relationships\n");
            html.append("      function drawRelationships() {\n");
            html.append("        ctx.strokeStyle = '#5bc0de';\n");
            html.append("        ctx.lineWidth = 1;\n");
            html.append("        \n");
            html.append("        for (const rel of relationships) {\n");
            html.append("          const fromTable = tables[rel.from];\n");
            html.append("          const toTable = tables[rel.to];\n");
            html.append("          \n");
            html.append("          if (!fromTable || !toTable) continue;\n");
            html.append("          \n");
            html.append("          // Calculate connection points\n");
            html.append("          const fromX = fromTable.x + fromTable.width/2;\n");
            html.append("          const fromY = fromTable.y + fromTable.height/2;\n");
            html.append("          const toX = toTable.x + toTable.width/2;\n");
            html.append("          const toY = toTable.y + toTable.height/2;\n");
            html.append("          \n");
            html.append("          // Draw line\n");
            html.append("          ctx.beginPath();\n");
            html.append("          ctx.moveTo(fromX, fromY);\n");
            html.append("          ctx.lineTo(toX, toY);\n");
            html.append("          ctx.stroke();\n");
            html.append("          \n");
            html.append("          // Draw arrow\n");
            html.append("          const angle = Math.atan2(toY - fromY, toX - fromX);\n");
            html.append("          const arrowSize = 10;\n");
            html.append("          \n");
            html.append("          ctx.beginPath();\n");
            html.append("          ctx.moveTo(toX, toY);\n");
            html.append("          ctx.lineTo(toX - arrowSize * Math.cos(angle - Math.PI/6), toY - arrowSize * Math.sin(angle - Math.PI/6));\n");
            html.append("          ctx.lineTo(toX - arrowSize * Math.cos(angle + Math.PI/6), toY - arrowSize * Math.sin(angle + Math.PI/6));\n");
            html.append("          ctx.closePath();\n");
            html.append("          ctx.fillStyle = '#5bc0de';\n");
            html.append("          ctx.fill();\n");
            html.append("        }\n");
            html.append("      }\n");

            // Call drawing functions
            html.append("      drawTables();\n");
            html.append("      drawRelationships();\n");
            html.append("    });\n");
            html.append("  </script>\n");

            html.append("</body>\n");
            html.append("</html>\n");

            // Write the HTML to a file
            File htmlFile = new File(outputDir + "/simple-erd.html");
            java.io.FileWriter writer = new java.io.FileWriter(htmlFile);
            writer.write(html.toString());
            writer.close();

            System.out.println("\n==============================================");
            System.out.println("Generated a simple HTML ERD at: " + htmlFile.getAbsolutePath());
            System.out.println("==============================================\n");

            // Open the HTML file
            openFile(htmlFile);

        } catch (Exception e) {
            System.out.println("Error generating simple HTML ERD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkForTables(String host, String port, String dbName, String user, String password) {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'"
            );
            if (rs.next()) {
                int tableCount = rs.getInt(1);
                System.out.println("Found " + tableCount + " tables in the database schema.");
                return tableCount > 0;
            }
        } catch (Exception e) {
            System.out.println("Error checking for tables: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private static void generateTextSchema(String host, String port, String dbName, String user, String password, String outputDir) {
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            Statement stmt = conn.createStatement();

            // Get all tables
            ResultSet tables = stmt.executeQuery(
                    "SELECT table_name FROM information_schema.tables " +
                            "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
                            "ORDER BY table_name"
            );

            StringBuilder schema = new StringBuilder();
            schema.append("# Database Schema: ").append(dbName).append("\n\n");

            while (tables.next()) {
                String tableName = tables.getString("table_name");
                schema.append("## Table: ").append(tableName).append("\n\n");

                // Get columns for this table
                Statement colStmt = conn.createStatement();
                ResultSet columns = colStmt.executeQuery(
                        "SELECT column_name, data_type, is_nullable, column_default " +
                                "FROM information_schema.columns " +
                                "WHERE table_schema = 'public' AND table_name = '" + tableName + "' " +
                                "ORDER BY ordinal_position"
                );

                schema.append("| Column | Type | Nullable | Default |\n");
                schema.append("|--------|------|----------|--------|\n");

                while (columns.next()) {
                    String colName = columns.getString("column_name");
                    String dataType = columns.getString("data_type");
                    String nullable = columns.getString("is_nullable");
                    String defaultVal = columns.getString("column_default");

                    schema.append("| ").append(colName).append(" | ")
                            .append(dataType).append(" | ")
                            .append(nullable).append(" | ")
                            .append(defaultVal == null ? "" : defaultVal).append(" |\n");
                }

                schema.append("\n");

                // Get primary keys
                Statement pkStmt = conn.createStatement();
                ResultSet pks = pkStmt.executeQuery(
                        "SELECT c.column_name " +
                                "FROM information_schema.table_constraints tc " +
                                "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) " +
                                "JOIN information_schema.columns AS c ON c.table_schema = tc.constraint_schema " +
                                "AND tc.table_name = c.table_name AND ccu.column_name = c.column_name " +
                                "WHERE tc.constraint_type = 'PRIMARY KEY' AND tc.table_name = '" + tableName + "'"
                );

                List<String> pkColumns = new ArrayList<>();
                while (pks.next()) {
                    pkColumns.add(pks.getString("column_name"));
                }

                if (!pkColumns.isEmpty()) {
                    schema.append("Primary Key: ").append(String.join(", ", pkColumns)).append("\n\n");
                }

                // Get foreign keys
                Statement fkStmt = conn.createStatement();
                ResultSet fks = fkStmt.executeQuery(
                        "SELECT kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name " +
                                "FROM information_schema.table_constraints AS tc " +
                                "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                                "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                                "WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name = '" + tableName + "'"
                );

                List<String> foreignKeys = new ArrayList<>();
                while (fks.next()) {
                    String columnName = fks.getString("column_name");
                    String foreignTable = fks.getString("foreign_table_name");
                    String foreignColumn = fks.getString("foreign_column_name");

                    foreignKeys.add(columnName + " → " + foreignTable + "." + foreignColumn);
                }

                if (!foreignKeys.isEmpty()) {
                    schema.append("Foreign Keys:\n");
                    for (String fk : foreignKeys) {
                        schema.append("- ").append(fk).append("\n");
                    }
                    schema.append("\n");
                }

                // Get indexes
                Statement idxStmt = conn.createStatement();
                ResultSet indexes = idxStmt.executeQuery(
                        "SELECT indexname, indexdef " +
                                "FROM pg_indexes " +
                                "WHERE schemaname = 'public' AND tablename = '" + tableName + "'"
                );

                List<String> indexList = new ArrayList<>();
                while (indexes.next()) {
                    String indexName = indexes.getString("indexname");
                    String indexDef = indexes.getString("indexdef");

                    // Skip primary key indexes as they're already covered
                    if (!indexName.endsWith("_pkey")) {
                        indexList.add(indexDef);
                    }
                }

                if (!indexList.isEmpty()) {
                    schema.append("Indexes:\n");
                    for (String idx : indexList) {
                        schema.append("- ").append(idx).append("\n");
                    }
                    schema.append("\n");
                }
            }

            // Add a section for all relationships
            schema.append("# Relationships\n\n");

            Statement relStmt = conn.createStatement();
            ResultSet relationships = relStmt.executeQuery(
                    "SELECT tc.table_name, kcu.column_name, ccu.table_name AS foreign_table_name, ccu.column_name AS foreign_column_name " +
                            "FROM information_schema.table_constraints AS tc " +
                            "JOIN information_schema.key_column_usage AS kcu ON tc.constraint_name = kcu.constraint_name " +
                            "JOIN information_schema.constraint_column_usage AS ccu ON ccu.constraint_name = tc.constraint_name " +
                            "WHERE tc.constraint_type = 'FOREIGN KEY' " +
                            "ORDER BY tc.table_name, kcu.column_name"
            );

            while (relationships.next()) {
                String tableName = relationships.getString("table_name");
                String columnName = relationships.getString("column_name");
                String foreignTable = relationships.getString("foreign_table_name");
                String foreignColumn = relationships.getString("foreign_column_name");

                schema.append("- ").append(tableName).append(".").append(columnName)
                        .append(" → ").append(foreignTable).append(".").append(foreignColumn).append("\n");
            }

            // Write the schema to a file
            File schemaFile = new File(outputDir + "/schema.md");
            java.io.FileWriter writer = new java.io.FileWriter(schemaFile);
            writer.write(schema.toString());
            writer.close();

            System.out.println("\n==============================================");
            System.out.println("Generated a text-based schema at: " + schemaFile.getAbsolutePath());
            System.out.println("==============================================\n");

            // Open the schema file
            openFile(schemaFile);

        } catch (Exception e) {
            System.out.println("Error generating text schema: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String downloadFileIfNotExists(String url, String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            downloadFile(url, fileName);
        }
        return file.getAbsolutePath();
    }

    private static void downloadFile(String url, String fileName) throws IOException {
        System.out.println("Downloading " + fileName + " from " + url);

        java.net.URL website = new java.net.URL(url);
        java.nio.channels.ReadableByteChannel rbc = java.nio.channels.Channels.newChannel(website.openStream());
        java.io.FileOutputStream fos = new java.io.FileOutputStream(fileName);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();

        System.out.println("Download complete: " + fileName);
    }

    private static void openFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                System.out.println("Desktop not supported, cannot open file automatically");
            }
        } catch (Exception e) {
            System.out.println("Error opening file: " + e.getMessage());
        }
    }
}



