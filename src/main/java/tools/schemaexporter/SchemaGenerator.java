//package tools.schemaexporter;
//
//import org.hibernate.boot.Metadata;
//import org.hibernate.boot.MetadataSources;
//import org.hibernate.boot.registry.StandardServiceRegistry;
//import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
//import org.hibernate.tool.hbm2ddl.SchemaExport;
//import org.hibernate.tool.schema.TargetType;
//import org.reflections.Reflections;
//
//import javax.persistence.Entity;
//import java.util.EnumSet;
//import java.util.Set;
//
//public class SchemaGenerator {
//    public static void main(String[] args) {
//        // Setup Hibernate programmatically
//        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
//                .applySetting("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
//                .applySetting("hibernate.connection.driver_class", "org.postgresql.Driver")
//                .applySetting("hibernate.connection.url", "jdbc:postgresql://localhost:5432/soms")
//                .applySetting("hibernate.connection.username", "postgres")
//                .applySetting("hibernate.connection.password", "1974")
//                .build();
//
//        MetadataSources sources = new MetadataSources(registry);
//
//        // Automatically scan and add all @Entity classes in your package
//        Reflections reflections = new Reflections("com.dep.soms.model"); // Change to your base entity package
//        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
//
//        for (Class<?> entityClass : entityClasses) {
//            sources.addAnnotatedClass(entityClass);
//            System.out.println("Added: " + entityClass.getName());
//        }
//
//        Metadata metadata = sources.buildMetadata();
//
//        SchemaExport export = new SchemaExport();
//        export.setOutputFile("C:/Users/William/Downloads/backup/schema.sql");
//        export.setFormat(true);
//        export.setDelimiter(";");
//
//        export.create(EnumSet.of(TargetType.SCRIPT), metadata);
//    }
//}

package tools.schemaexporter;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;

import jakarta.persistence.Entity;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaGenerator {
    public static void main(String[] args) {
        // Create configuration
        Map<String, Object> settings = new HashMap<>();
        settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        settings.put(Environment.FORMAT_SQL, "true");
        settings.put(Environment.HBM2DDL_AUTO, "none");

        // Setup Hibernate registry
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            MetadataSources sources = new MetadataSources(registry);

            // Automatically scan and add all @Entity classes in your package
            Reflections reflections = new Reflections("com.dep.soms.model"); // Change to your base entity package
            Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);

            for (Class<?> entityClass : entityClasses) {
                sources.addAnnotatedClass(entityClass);
                System.out.println("Added: " + entityClass.getName());
            }

            Metadata metadata = sources.buildMetadata();

            // Create output file path
            String outputFilePath = "C:/Users/William/Downloads/backup/schema.sql";
            File outputFile = new File(outputFilePath);
            outputFile.getParentFile().mkdirs();

            // Export schema
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setOutputFile(outputFilePath);
            schemaExport.setDelimiter(";");
            schemaExport.setFormat(true);
            schemaExport.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, metadata);

            System.out.println("Schema exported to: " + outputFile.getAbsolutePath());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
