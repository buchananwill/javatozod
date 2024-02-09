import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class DtoParser {

    private static final Logger dtoParserLogger = LoggerFactory.getLogger(DtoParser.class);

    private static final Path FRONT_END_DTO_DIRECTORY = Paths.get("D:\\Coding\\frontend\\app\\api\\dtos");
    public static final String IMPORT_Z_FROM_ZOD = "import { z } from 'zod';\n";
    public static final String IMPORT_FROM_ZOD_MODS = "import { %s } from '../zod-mods';\n";

    public static final String DATE_ONLY = "zDateOnly";
    public static final String TIME_ONLY = "zTimeOnly";
    public static final String SCHEMA_DECLARATION_TERMINAL = "});\n";
    public static final String SCHEMA_DECLARTION_START = "export const %s = z.object({\n";
    public static final String Z_DAY_OF_WEEK = "zDayOfWeek";
    public static final String IMPORT_SCHEMA_FROM_CURRENT_DIR = "import { %s } from './%s';\n";
    public static final String EXPORT_INFERRED_TYPE = "export type %s = z.infer<typeof %s>;";


    public static void main(String[] args) {
        try {
            Files.createDirectories(FRONT_END_DTO_DIRECTORY);
            parseDtoRecords();
        } catch (IOException e) {
            dtoParserLogger.error("Error during Dto parsing", e);
            throw new RuntimeException(e);
        }
    }

    public static void parseDtoRecords() throws IOException {
        // Pseudocode for generating a Zod schema from a Java DTO
        for (Class<?> dtoClass : findAnnotationsIn("com.futuredyme.ServiceLayers")) {
            StringBuilder tsCode = new StringBuilder();
            Set<String> importStatements = new HashSet<>();
            String dtoName = dtoClass.getSimpleName();
            String schemaName = String.format("%sSchema", dtoName);
            tsCode.append(IMPORT_Z_FROM_ZOD);
            tsCode.append(String.format(SCHEMA_DECLARTION_START, schemaName));
            for (Field field : dtoClass.getDeclaredFields()) {
                try {
                    // extraImport is passed as a reference to allow the import statement to be set at the end of the loop.
                    StringBuilder extraImport = new StringBuilder();
                    StringBuilder builder = new StringBuilder();
                    builder.append(String.format("  %s: ", field.getName()));
                    String fieldType = constructFieldType(field, importStatements);
                    builder.append(fieldType);
                    builder.append(",\n");
                    tsCode.append(builder);

                    if (!tsCode.toString().contains(extraImport)) tsCode.insert(0, extraImport);
                } catch (IllegalArgumentException e) {
                    dtoParserLogger.error(
                            String.format(
                                    "Could not parse field '%s', with generic type '%s' in annotated class: '%s', with error: %s",
                                    field.getName(),
                                    field.getGenericType().getTypeName(),
                                    dtoName,
                                    e.getMessage()
                            )
                    );
                }
            }
            tsCode.append(SCHEMA_DECLARATION_TERMINAL);
            tsCode.append(String.format(EXPORT_INFERRED_TYPE, dtoName, schemaName));
            for (String importStatement : importStatements) {
                tsCode.insert(0, importStatement);
            }

            try {
                writeFile(String.format("%s.ts", schemaName), tsCode.toString());
            } catch (IOException e) {
                dtoParserLogger.error(String.format("Error while attempting to write output file: %s", e.getMessage()));
                throw new RuntimeException(e);
            }
        }

    }

    private static String constructFieldType(Field field, Set<String> extraImport) {
        String fieldType = field.getType().getSimpleName();
        if (fieldType.equalsIgnoreCase("list")) {
            return getListSchemaShape(field, extraImport);
        }
        if (fieldType.toLowerCase().contains("date") || fieldType.toLowerCase().contains("time")) {
            return getDateTimeField(fieldType, extraImport);

        } else return getNonCollectionFieldType(extraImport, fieldType);
    }

    private static String getNonCollectionFieldType(Set<String> extraImport, String fieldType) {
        String lowerCaseFieldType = fieldType.toLowerCase();
        switch (lowerCaseFieldType) {
            case "long", "integer", "double", "float", "string", "boolean", "int" -> {
                return (getSimpleTypeField(lowerCaseFieldType));
            }
            case "uuid" -> {
                return getUuidField();
            }
            case "dayofweek" -> {
                return getDayOfWeekField(extraImport);
            }
            default -> {
                return getSchemaField(fieldType, extraImport);
            }
        }
    }

    private static String getDayOfWeekField(Set<String> extraImports) {
        extraImports.add(String.format(IMPORT_FROM_ZOD_MODS, Z_DAY_OF_WEEK));
        return String.format("%s", Z_DAY_OF_WEEK);
    }

    private static String getUuidField() {
        return "z.string().uuid()";
    }

    private static String getDateTimeField(String fieldType, Set<String> extraImport) {
        switch (fieldType.toLowerCase()) {
            case "localdate" -> {
                extraImport.add(String.format(IMPORT_FROM_ZOD_MODS, DATE_ONLY));
                return DATE_ONLY;
            }
            case "localtime" -> {
                extraImport.add(String.format(IMPORT_FROM_ZOD_MODS, TIME_ONLY));
                return TIME_ONLY;
            }
            default -> {
                return "z.date()";
            }

        }
    }

    public static void writeFile(String filename, String content) throws IOException {
        // Define the path where the file should be written
        // This example writes to the current working directory. Adjust the path as needed.
        Path filePath = FRONT_END_DTO_DIRECTORY.resolve(filename);

        // Write the content to the file, creating the file if it doesn't exist
        Files.writeString(filePath, content);
    }


    private static String getListSchemaShape(Field field, Set<String> extraImport) {
        String schemaName = extractGenericFieldTypeName(field, extraImport);
        extraImport.add(getSchemaImportStatement(schemaName));
        return String.format("z.array(%s)", schemaName);
    }

    public static String extractGenericFieldTypeName(Field field, Set<String> extraImport) {
        String genericTypeName = field.getGenericType().getTypeName();
        int lastDotIndex = genericTypeName.lastIndexOf(".");
        int lastAngleBracketIndex = genericTypeName.lastIndexOf(">");

        if (lastDotIndex != -1 && lastAngleBracketIndex != -1 && lastDotIndex < lastAngleBracketIndex) {
            String fieldType = genericTypeName.substring(lastDotIndex + 1, lastAngleBracketIndex);
            if (fieldType.toLowerCase().contains("list")) {
                throw new IllegalArgumentException(String.format("Recursive Lists not currently supported: %s", fieldType));
            } else {
                return getNonCollectionFieldType(extraImport, fieldType);
            }
        }
        throw new IllegalArgumentException(String.format("Could not find simple type name: %s", genericTypeName));
    }

    private static String getSchemaField(String fieldType, Set<String> extraImport) {
        String schemaName = getSchemaName(fieldType);
        extraImport.add(getSchemaImportStatement(schemaName));
        return schemaName;
    }

    private static String getSchemaImportStatement(String schemaName) {
        return String.format(IMPORT_SCHEMA_FROM_CURRENT_DIR, schemaName, schemaName);
    }


    private static String getSchemaName(String simpleName) {
        return simpleName + "Schema";
    }


    public static Set<Class<?>> findAnnotationsIn(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(GenerateZodSchema.class);
    }

    public static String getSimpleTypeField(String simpleName) {
        return simpleName.equals("string") ? "z.string()"
                : simpleName.equals("boolean") ? "z.boolean()"
                : "z.number()";
    }


}
