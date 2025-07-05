//package tools.schemaexporter;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.ArrayList;
//import java.util.List;
//
//public class ProfessionalERDGenerator {
//    public static void main(String[] args) {
//        try {
//            // Output directory for the ERD and documentation
//            String outputDir = "C:/Users/William/Downloads/backup/erd";
//
//            // Database connection details
//            String dbHost = "localhost";
//            String dbPort = "5432";
//            String dbName = "soms";
//            String dbUser = "postgres";
//            String dbPassword = "1974";
//
//            // Create output directory if it doesn't exist
//            File outputDirectory = new File(outputDir);
//            if (!outputDirectory.exists()) {
//                outputDirectory.mkdirs();
//            }
//
//            // Download required JAR files if they don't exist
//            String schemaspyJar = downloadFileIfNotExists(
//                    "https://github.com/schemaspy/schemaspy/releases/download/v6.2.4/schemaspy-6.2.4.jar",
//                    "schemaspy-6.2.4.jar"
//            );
//
//            String postgresJar = downloadFileIfNotExists(
//                    "https://jdbc.postgresql.org/download/postgresql-42.6.2.jar",
//                    "postgresql-42.6.2.jar"
//            );
//
//            // Build the command to run SchemaSpy
//            List<String> command = new ArrayList<>();
//            command.add("java");
//            command.add("-jar");
//            command.add(schemaspyJar);
//            command.add("-t");
//            command.add("pgsql"); // PostgreSQL
//            command.add("-db");
//            command.add(dbName);
//            command.add("-host");
//            command.add(dbHost);
//            command.add("-port");
//            command.add(dbPort);
//            command.add("-u");
//            command.add(dbUser);
//            command.add("-p");
//            command.add(dbPassword);
//            command.add("-o");
//            command.add(outputDir);
//            command.add("-dp");
//            command.add(postgresJar);
//            command.add("-s");
//            command.add("public"); // Schema name
//            command.add("-hq"); // High quality diagrams
//            command.add("-norows"); // Don't include row counts
//            command.add("-i"); // Include views
//            command.add("-degree"); // Show degree of separation
//            command.add("2"); // 2 degrees of separation
//            command.add("-renderer"); // Use specific renderer
//            command.add(":graphviz"); // Graphviz renderer
//            command.add("-vizjs"); // Use viz.js for interactive diagrams
//
//            // Execute the command
//            System.out.println("Executing command: " + String.join(" ", command));
//            ProcessBuilder processBuilder = new ProcessBuilder(command);
//            processBuilder.redirectErrorStream(true);
//            Process process = processBuilder.start();
//
//            // Print the output
//            java.io.InputStream is = process.getInputStream();
//            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                System.out.println(line);
//            }
//
//            // Wait for the process to complete
//            int exitCode = process.waitFor();
//
//            if (exitCode == 0) {
//                System.out.println("\n==============================================");
//                System.out.println("ERD diagram generated successfully at: " + outputDir);
//                System.out.println("Open " + outputDir + "/index.html in a browser to view the documentation");
//                System.out.println("The main diagram is at: " + outputDir + "/diagrams/summary/relationships.real.large.png");
//                System.out.println("==============================================\n");
//
//                // Open the main diagram automatically
//                File mainDiagram = new File(outputDir + "/diagrams/summary/relationships.real.large.png");
//                if (mainDiagram.exists()) {
//                    openFile(mainDiagram);
//                }
//
//                // Open the HTML documentation automatically
//                File indexHtml = new File(outputDir + "/index.html");
//                if (indexHtml.exists()) {
//                    openFile(indexHtml);
//                }
//            } else {
//                System.out.println("ERD generation failed with exit code: " + exitCode);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String downloadFileIfNotExists(String url, String fileName) {
//        File file = new File(fileName);
//        if (!file.exists()) {
//            System.out.println("Downloading " + fileName + " from " + url);
//            try {
//                ProcessBuilder processBuilder = new ProcessBuilder("curl", "-L", "-o", fileName, url);
//                processBuilder.redirectErrorStream(true);
//                Process process = processBuilder.start();
//
//                java.io.InputStream is = process.getInputStream();
//                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    System.out.println(line);
//                }
//
//                int exitCode = process.waitFor();
//                if (exitCode != 0) {
//                    System.out.println("Failed to download " + fileName);
//                    System.exit(1);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                System.exit(1);
//            }
//        }
//        return file.getAbsolutePath();
//    }
//
//    private static void openFile(File file) {
//        try {
//            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
//                new ProcessBuilder("cmd", "/c", file.getAbsolutePath()).start();
//            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
//                new ProcessBuilder("open", file.getAbsolutePath()).start();
//            } else {
//                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

package tools.schemaexporter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProfessionalERDGenerator {
    public static void main(String[] args) {
        try {
            // Output directory for the ERD and documentation
            String outputDir = "C:/Users/William/Downloads/backup/erd";

            // Database connection details
            String dbHost = "localhost";
            String dbPort = "5432";
            String dbName = "soms";
            String dbUser = "postgres";
            String dbPassword = "1974";

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
            command.add(dbHost);
            command.add("-port");
            command.add(dbPort);
            command.add("-u");
            command.add(dbUser);
            command.add("-p");
            command.add(dbPassword);
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
            } else {
                System.out.println("ERD generation failed with exit code: " + exitCode);

                // Generate a simple text-based schema as a fallback
                generateTextSchema(dbHost, dbPort, dbName, dbUser, dbPassword, outputDir);
            }

        } catch (Exception e) {
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

                if (fks.isBeforeFirst()) {
                    schema.append("Foreign Keys:\n");
                    while (fks.next()) {
                        String columnName = fks.getString("column_name");
                        String foreignTable = fks.getString("foreign_table_name");
                        String foreignColumn = fks.getString("foreign_column_name");

                        schema.append("- ").append(columnName)
                                .append(" -> ").append(foreignTable)
                                .append(".").append(foreignColumn).append("\n");
                    }
                    schema.append("\n");
                }
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

    private static String downloadFileIfNotExists(String url, String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("Downloading " + fileName + " from " + url);
            try {
                ProcessBuilder processBuilder = new ProcessBuilder("curl", "-L", "-o", fileName, url);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                java.io.InputStream is = process.getInputStream();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.out.println("Failed to download " + fileName);
                    System.exit(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return file.getAbsolutePath();
    }

    private static void openFile(File file) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                new ProcessBuilder("cmd", "/c", file.getAbsolutePath()).start();
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
