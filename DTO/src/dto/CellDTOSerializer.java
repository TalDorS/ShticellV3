//package dto;
//
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonSerializationContext;
//import com.google.gson.JsonSerializer;
//
//import java.lang.reflect.Type;
//import java.util.Map;
//
//public class CellDTOSerializer implements JsonSerializer<CellDTO> {
//
//    @Override
//    public JsonElement serialize(CellDTO cellDTO, Type type, JsonSerializationContext context) {
//        JsonObject jsonObject = new JsonObject();
//
//        // Add regular fields
//        jsonObject.addProperty("id", cellDTO.getCellId());
//        jsonObject.addProperty("value", cellDTO.getValue());
//        // Add any other fields you want to serialize
//
//        // Serialize dependsOnThem as a list of IDs instead of full objects to avoid recursion
//        JsonObject dependsOnThemJson = new JsonObject();
//        for (Map.Entry<String, CellDTO> entry : cellDTO.getDependsOnThem().entrySet()) {
//            dependsOnThemJson.addProperty(entry.getKey(), entry.getValue().getId());  // Avoid recursion
//        }
//        jsonObject.add("dependsOnThem", dependsOnThemJson);
//
//        // Serialize dependsOnMe in a similar way
//        JsonObject dependsOnMeJson = new JsonObject();
//        for (Map.Entry<String, CellDTO> entry : cellDTO.getDependsOnMe().entrySet()) {
//            dependsOnMeJson.addProperty(entry.getKey(), entry.getValue().getId());  // Avoid recursion
//        }
//        jsonObject.add("dependsOnMe", dependsOnMeJson);
//
//        return jsonObject;
//    }
//}
//
